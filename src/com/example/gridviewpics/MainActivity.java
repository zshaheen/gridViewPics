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
    
    
    public class ImageAdapter extends BaseAdapter {
        private Context mContext;
        int size = (int) getResources().getDimension(R.dimen.image_size);

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
            
            //Load in the direcroty and display the images
            /*String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/SnapDrawShare/";
            File path = new File(dir);

            if(path.isDirectory()) {
            	//Log.e("File path", "Path is a directory");
            	String[] fileNames = path.list();
            	//Log.e("fileName.length", Integer.toString(fileNames.length));
            	*/
            	//for(int i=0; i<fileNames.length; i++) {
            	//	Log.e("fileName[]", fileNames[i]);
            	//}
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 13;
            //options.inJustDecodeBounds = true;
            	Bitmap mBitmap = BitmapFactory.decodeFile(path.getPath()+"/"+ fileNames[position],options);
            	imageView.setImageBitmap(ThumbnailUtils.extractThumbnail(mBitmap, size, size));
           // }
            
            
            //imageView.setImageResource(mThumbIds[position]);
            return imageView;
        }
    }   
    
}




