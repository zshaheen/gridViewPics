package com.example.gridviewpics;

import java.io.File;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.os.Build;

public class MainActivity extends Activity {
	String[] fileNames;
	File path;
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
        
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
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
        
        
        /*String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/SnapDrawShare/";
        File path = new File(dir);

        if(path.isDirectory()) {
        	Log.e("File path", "Path is a directory");
        	String[] fileNames = path.list();
        	Log.e("fileName.length", Integer.toString(fileNames.length));
        	
        	for(int i=0; i<fileNames.length; i++) {
        		Log.e("fileName[]", fileNames[i]);
        	}
        }
        else {
        	//Do something, Toast an error, create a warning
        	Log.e("fileName[]", "Path DOESNT exist");
        }
        */
        
    }
    
    
    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(key);
    }
    
    
    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        int size = (int) getResources().getDimension(R.dimen.image_size);
        Bitmap bitmap = null;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            //return mThumbIds.length;
        	return fileNames.length;
        }

        public Object getItem(int position) {
            return fileNames[position];
        }

        public long getItemId(int position) {
            return position;
        }
        
        public int calculateInSampleSize(
                BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }
        
        public Bitmap decodeSampledBitmapFromFile(String filePath, int reqWidth, int reqHeight) {
			
        	final BitmapFactory.Options options = new BitmapFactory.Options();
        	options.inJustDecodeBounds = true;
        	BitmapFactory.decodeFile(filePath,options);
        	
            // Calculate inSampleSize
            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        	//options.inSampleSize = 128;
        	
            // Decode bitmap with inSampleSize set
            options.inJustDecodeBounds = false;
            return BitmapFactory.decodeFile(filePath,options);
	
        	
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
            
            
            //attemps to load a bitmap with key of the filename
            bitmap = getBitmapFromMemCache(fileNames[position]);
            if (bitmap != null) {
            	//image was found in cache
                imageView.setImageBitmap(bitmap);
            } else {
            	//load in the image and save to cache
            	bitmap = decodeSampledBitmapFromFile(path.getPath()+"/"+ fileNames[position],size,size);
            	addBitmapToMemoryCache(fileNames[position], bitmap );
            	
            	imageView.setImageBitmap(bitmap);
            }
            
            
			//imageView.setImageBitmap(decodeSampledBitmapFromFile(path.getPath()+"/"+ fileNames[position],size,size));

            return imageView;
        }
    }   
    
}




