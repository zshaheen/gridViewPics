package com.example.gridviewpics;

import java.io.File;
import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
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
        
        final String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/SnapDrawShare/";
        path = new File(dir);

        if(path.isDirectory()) {
        	fileNames = path.list();
        }
        
        prefix = path.getPath()+"/";
        
        //Caching the bitmaps
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        //0.25 of main memory is allocated to the cache
        final int cacheSize = maxMemory / 4;
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
        
        gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				
				Intent sendPic = new Intent(Intent.ACTION_SEND); 
            	sendPic.setType("image/*");
            	
            	Log.i("send this file",dir + fileNames[position]);
            	sendPic.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + dir + fileNames[position]));
            	
            	startActivity(Intent.createChooser(sendPic, "Send picture"));
				
			}
        });
        
        
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
        private Bitmap loadingBitmap = null;

        public ImageAdapter(Context c) {
            mContext = c;
            loadingBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.transparent);
        }

        public int getCount() {
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
            
            
            loadBitmap(fileNames[position], imageView, loadingBitmap );

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
    	       final Drawable drawable = imageView.getDrawable();
    	       
    	       if (drawable instanceof AsyncDrawable) {
    	           final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
    	           return asyncDrawable.getBitmapWorkerTask();
    	       }
    	    }
    	    return null;
    	}
    
    
    public void loadBitmap(String filename, ImageView imageView, Bitmap loadingImage) {
    	
    	final Bitmap bitmap = getBitmapFromMemCache(filename);
    	
    	if (bitmap != null) {
    		Log.i("CACHE",  filename+" FROM CACHE");
    		imageView.setImageBitmap(bitmap);
    	} else {
    		Log.i("DISK",  filename+" from DISK");
    		if (cancelPotentialWork(filename, imageView)) {
	    		BitmapWorkerTask task = new BitmapWorkerTask(imageView);
	    		final AsyncDrawable asyncDrawable =
	                    new AsyncDrawable(getResources(), loadingImage, task);
	    		imageView.setImageDrawable(asyncDrawable);
	    		task.execute(filename);
    		}
    		
    	}
    	
    }
    
    public class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<ImageView> imageViewReference;
        private String filename;
        int size = (int) getResources().getDimension(R.dimen.image_size);
        
        
        public BitmapWorkerTask(ImageView imageView) {
            // Use a WeakReference to ensure the ImageView can be garbage collected
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... params) {
        	filename = params[0];
        	final Bitmap bitmap = decodeSampledBitmapFromFile(prefix+filename, size, size);
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

                final BitmapWorkerTask bitmapWorkerTask =
                        getBitmapWorkerTask(imageView);
                
                if (this == bitmapWorkerTask && imageView != null) {
                    imageView.setImageBitmap(bitmap);
                }
            }
            
        }
    }
    
    
    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;
        
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




