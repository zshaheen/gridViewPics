package com.example.gridviewpics;

import java.io.File;
import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class MainActivity extends Activity {
	String[] fileNames;
	File path;
	String prefix;
	private LruCache<String, Bitmap> mMemoryCache;

	
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/SnapDrawShare/";
        path = new File(dir);

        if(path.isDirectory()) {
        	//Log.e("File path", "Path is a directory");
        	fileNames = path.list();
        	//for(int i =0; i<fileNames.length; i++)
            //	Log.e("paths",path.getPath()+"/"+ fileNames[i]);
        }
        prefix = path.getPath()+"/";
        
        //Caching the bitmaps
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 2;//maxMemory / 8;
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

        
      //Create a gird view in activity_main.xml with id of 'gridview'
        GridView gridView = (GridView) findViewById(R.id.gridview);
        gridView.setAdapter(new ImageAdapter(this));
        

        
    }
    
    
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }
    
    public Bitmap decodeSampledBitmapFromFile(String filePath, int reqWidth, int reqHeight) {
		
    	final BitmapFactory.Options options = new BitmapFactory.Options();
    	options.inJustDecodeBounds = true;
    	BitmapFactory.decodeFile(filePath,options);
    	
        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        Log.e("calculateInSampleSize", Integer.toString(options.inSampleSize));
        //options.inSampleSize = 6;
    	//options.inSampleSize = 128;
    	
        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath,options);

    	
    }
    
    public int calculateInSampleSize(
	            BitmapFactory.Options options, int reqWidth, int reqHeight) {
	    // Raw height and width of image

    	final int height = options.outHeight;
        final int width = options.outWidth;

        int stretch_width = Math.round((float)width / (float)reqWidth);
        int stretch_height = Math.round((float)height / (float)reqHeight);

        if (stretch_width <= stretch_height) 
            return stretch_height;
        else 
            return stretch_width;
	}
    
    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        private int size = (int) getResources().getDimension(R.dimen.image_size);
        //Bitmap bitmap = null;
        private Bitmap loadingBitmap = null;

        public ImageAdapter(Context c) {
            mContext = c;
            //Set loadingBitmap
            loadingBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.transparent);
        }

        public int getCount() {
            //return mThumbIds.length;
        	return fileNames.length;
        }

        public Object getItem(int position) {
            return fileNames[position];
        }

        public long getItemId(int position) {
            return 0;
        }
        
        
        // create a new ImageView for each item referenced by the Adapter
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView imageView;
            if (convertView == null) {  // if it's not recycled, initialize some attributes
                imageView = new ImageView(mContext);
                imageView.setLayoutParams(new GridView.LayoutParams(size, size));
                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                imageView.setPadding(8, 8, 8, 8);
            } else {
                imageView = (ImageView) convertView;
            }
            /*
            BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inSampleSize = 16;
            //options.inJustDecodeBounds = true;
			Bitmap mBitmap = BitmapFactory.decodeFile(path.getPath()+"/"+ fileNames[position],options);
			imageView.setImageBitmap(mBitmap);
            */
            /////////////////////Log.e("position", Integer.toString(position));
            
            
            //attemps to load a bitmap with key of the filename
            /*bitmap = getBitmapFromMemCache(fileNames[position]);
            if (bitmap != null) {
            	//image was found in cache
                imageView.setImageBitmap(bitmap);
            } else {
            	//load in the image and save to cache
            	bitmap = decodeSampledBitmapFromFile(path.getPath()+"/"+ fileNames[position],size,size);
            	addBitmapToMemoryCache(fileNames[position], bitmap );
            	
            	imageView.setImageBitmap(bitmap);
            }*/
            
            //imageView.setImageBitmap();
            //imageView.setImageResource(R.drawable.ic_launcher );
            loadBitmap(fileNames[position], imageView, loadingBitmap, position );
            //imageView.setImageBitmap(decodeSampledBitmapFromFile(path.getPath()+"/"+ fileNames[position],size,size));

            return imageView;
        }
    }   
    
    
    public static boolean cancelPotentialWork(String fileName, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);
        
        if (bitmapWorkerTask != null) {
            final String bitmapString = bitmapWorkerTask.filename;
            // If bitmapData is not yet set or it differs from the new data
            if ( bitmapString != fileName) {
                // Cancel previous task
                bitmapWorkerTask.cancel(true);
            } else {
                // The same work is already in progress
                return false;
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }
    
    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
    	   if (imageView != null) {
    		   
    		   //Drawable is null!!!!
    	       final Drawable drawable = imageView.getDrawable();
    	       
    	       //if(drawable == null)
    	    	  // Log.e("error", "drwawable is null");
    	       
    	       if (drawable instanceof AsyncDrawable) {
    	    	  // Log.e("Shit","Drawable is of type AsyncDrawable");
    	           final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
    	           return asyncDrawable.getBitmapWorkerTask();
    	       }
    	    }
    	   //Log.e("error", "getBitmapWorkerTask returns null");
    	    return null;
    	}
    
    public void loadBitmap(String filename, ImageView imageView, Bitmap loadingImage, int position) {
    	//Log.e("loadBitmap filename", filename);
    	//Log.e("loadBitmap position", Integer.toString(position));
    	//Log.e("loadBitmap position", Integer.toString(position));
    	final Bitmap bitmap = getBitmapFromMemCache(filename);
    	if (bitmap != null) {
    		Log.e("cache",  filename+" FROM cache");
    		imageView.setImageBitmap(bitmap);
    	} else {
    		Log.e("cache",  filename+" FROM dissssskkkkkkkk");
    		if (cancelPotentialWork(filename, imageView)) {
	    		BitmapWorkerTask task = new BitmapWorkerTask(imageView);
	    		final AsyncDrawable asyncDrawable =
	                    new AsyncDrawable(getResources(), loadingImage, task);
	    		imageView.setImageDrawable(asyncDrawable);
	            //task.execute(prefix+filename);
	    		task.execute(filename, position);
    		}
    		
    	}
    	
    	/*if (cancelPotentialWork(filename, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(imageView);
            final AsyncDrawable asyncDrawable =
                    new AsyncDrawable(getResources(), loadingImage, task);
            imageView.setImageDrawable(asyncDrawable);
            task.execute(prefix+filename);
    	}*/
    }
    
    public class BitmapWorkerTask extends AsyncTask<Object, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String filename;
        private int position;
        int size = (int) getResources().getDimension(R.dimen.image_size);
        
        
        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(Object... params) {
        	filename = (String)params[0];
        	position = (Integer)params[1];
        	final Bitmap bitmap = decodeSampledBitmapFromFile(prefix+filename, size, size);
        	///////////////Log.e("doInBackground filename", filename+" from disk");
        	//addBitmapToMemoryCache(filename, bitmap);
        	addBitmapToMemoryCache(filename, bitmap);
        	return bitmap;
        }

        // Once complete, see if ImageView is still around and set bitmap.
        @Override
        protected void onPostExecute(Bitmap bitmap) {
        	if (isCancelled()) {
                bitmap = null;
            }

            if (imageViewReference != null && bitmap != null) {
                final ImageView imageView = imageViewReference.get();
                //This is not being loaded correctly
                final BitmapWorkerTask bitmapWorkerTask =
                        getBitmapWorkerTask(imageView);
                
                //Log.e("bitmapWorkerTask", bitmapWorkerTask.filename);
                //Log.e("bitmapWorkerTask", Integer.toString(bitmapWorkerTask.size));
                //imageView.setImageResource(R.drawable.ic_launcher );
                	//Log.e("Stuff", "this == bitmapWorkerTask");
                //Log.e("Stuff", "this == bitmapWorkerTask");
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
            
        }
    }
    
    
    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;
        
        //Something is wrong below
        public AsyncDrawable(Resources res, Bitmap bitmap,
                BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference =
                new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }
    
}




