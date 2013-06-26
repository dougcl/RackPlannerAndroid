package org.gnu.dougcl.android;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


public class Rack extends LinearLayout implements Scalable {
	
	private Context context;

	private int rows;
	private int cols;
	private int hp;
	
	private int intrinsicHeight;
	private int intrinsicWidth;
	
	private double scale;

	public Rack(Context context) {
		super(context);
		this.context = context;
		RelativeLayout.LayoutParams params = 
			new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		params.leftMargin = 0;
		params.topMargin = 0;
		this.setOrientation(LinearLayout.VERTICAL);
		this.setLayoutParams(params);
		this.intrinsicWidth = 0;
		this.intrinsicHeight = 0;
	}
	
	public void load(String filename){
		//Default is a 6U eurorack with an MX-4S in it.
		rows = 2;
		cols = 1;
		double scale = 0.8;
		int hp = 84;
		this.hp = hp * cols;
		Drawable bg = this.getResources().getDrawable(R.drawable.euro_84hp);
		
		//read rack file from disk. If it's not there, create it.
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			//Memory Card is present and mounted with read/write access
	        File dir = new File(Environment.getExternalStorageDirectory() + "/RackPlanner");
	        if(dir.exists() && dir.isDirectory()) 
	        {
	           //Folder exists. Check for rack file.
	        	
	        } else {
	        	//Create default folders and default module zip from resources
	            String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
	            File newFolder = new File(extStorageDirectory + "/RackPlanner");
	            newFolder.mkdir();
	            newFolder = new File(extStorageDirectory + "/RackPlanner/images");
	            newFolder.mkdir();
	            newFolder = new File(extStorageDirectory + "/RackPlanner/euro_modules");
	            newFolder.mkdir();     
	            try {
	            	//Write out example background
		            Bitmap bm = BitmapFactory.decodeResource( this.context.getResources(), R.drawable.euro_84hp);
		            File imgFile = new File(extStorageDirectory + "/RackPlanner/images/euro_84hp.jpg");
					FileOutputStream outStream = new FileOutputStream(imgFile);
					bm.compress(Bitmap.CompressFormat.JPEG,100, outStream);
				    outStream.flush();
				    outStream.close();
				    //Write out example module.
				    InputStream is = this.context.getResources().openRawResource(R.raw.cwejman_mx_4s);
				    File moduleZip = new File(extStorageDirectory + "/RackPlanner/euro_modules/Cwejman_E_MX-4S.zip");
				    outStream = new FileOutputStream(moduleZip);
				    int bufferSize = 1024;
				    byte[] buffer = new byte[bufferSize];
				    int len = 0;
				    while ((len = is.read(buffer)) != -1) {
				        outStream.write(buffer, 0, len);
				    }
				    outStream.flush();
				    outStream.close();
				    //Write out example rack.xml
				    is = this.context.getResources().openRawResource(R.raw.rack);
				    File rackFile = new File(extStorageDirectory + "/RackPlanner/rack.xml");
				    outStream = new FileOutputStream(rackFile);
				    buffer = new byte[bufferSize];
				    len = 0;
				    while ((len = is.read(buffer)) != -1) {
				        outStream.write(buffer, 0, len);
				    }
				    outStream.flush();
				    outStream.close();
	            } catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	        }
       } else {
    	   //No SD card. Create default rack from resources
       }
		
	

		this.intrinsicWidth = bg.getIntrinsicWidth() * cols;
		this.intrinsicHeight = bg.getIntrinsicHeight() * rows;
		RackRow rr;
		for(int i = 1;i <= rows;i++){
			rr = new RackRow(this.getContext(), bg, hp, cols);
			this.addView(rr);
			rr.setId(this.getChildCount());
			Activity activity = (Activity)context;
			activity.registerForContextMenu(rr);
			rr.setOnTouchListener((OnTouchListener)this.getParent());
		}
		//For performance, call after adding rows, but before adding modules.
		this.setScale(scale);
		addModule("module_filename",1, 40);

	}
	
	
	public int getRows() {
		return this.rows;
	}
	
	public int getHP() {
		return this.hp;
	}
	
	public double getScale(){
		return this.scale;
	}

	public void setScale(double scale){
		this.scale = scale;
		Scalable child;
		for(int i = 0;i < this.getChildCount();i++){
			if (this.getChildAt(i) instanceof Scalable){
				child = (Scalable)this.getChildAt(i);
				child.setScale(this.scale);
			}
		}
	}
	
	public int getScaledHeight(){
		return (int)(this.intrinsicHeight * this.scale);
	}
	
	public int getScaledWidth(){
		return (int)(this.intrinsicWidth * this.scale);
	}
	
	public int getTopMargin(){
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)this.getLayoutParams();
		return params.topMargin;
	}
	
	public int getLeftMargin(){
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)this.getLayoutParams();
		return params.leftMargin;
	}
		
	public void addModule(String filename,int row, int col){
		int pos = col;
		RackRow rr = (RackRow)this.getChildAt(row - 1);
		Drawable img = this.getResources().getDrawable(R.drawable.module);
		int hp = 20;
		//Can't trust module image aspect ratio. Force width to the claimed HP.
		img = new BitmapDrawable(Bitmap.createScaledBitmap(
					((BitmapDrawable) img).getBitmap(), 
					(int) (img.getIntrinsicHeight() * rr.getUnitAr() * hp),
					img.getIntrinsicHeight(), 	 
					true));

		Module m = new Module(this.getContext(),img,hp);
		rr.addModule(m,pos);
		m.setOnTouchListener((OnTouchListener)this.getParent());
	}
	
	public void addModule(Module m, int row, int col){
		int pos = col;
		RackRow rr = (RackRow)this.getChildAt(row - 1);	
		rr.addModule(m,pos);
		m.setOnTouchListener((OnTouchListener)this.getParent());
	}
	
	public boolean dragModule(Module m){
		//Remove module from rack and add to parent in parent coordinates.
		if (m.getParent() instanceof RackRow){
			RackRow rr = (RackRow)m.getParent();
			int h = m.getScaledHeight();
			int w = m.getScaledWidth();
			rr.removeView(m);
			m.setPosition(1);
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)m.getLayoutParams();
			params.width = w;
			params.height = h;
			params.leftMargin = params.leftMargin + this.getLeftMargin();
			params.topMargin = this.getTopMargin() + rr.getTop();
			m.requestLayout();
			ViewGroup parent = (ViewGroup)this.getParent();
			parent.addView(m);
			m.bringToFront();
			return true;
		} else {
			return false;
		}
	}
	
    public void dropModule(Module m){
    	//add module to rack in rack coordinates
    	int x = m.getLeftMargin();
		int y = m.getTopMargin() + (int)(1.0* m.getScaledHeight()/2); //vertical positioning relative to module center.
		int w = this.getScaledWidth(); 
		int h = this.getScaledHeight(); 
    	int row = (int)(1.0*(y-this.getTopMargin())/h * this.getRows()) + 1;
		row = Math.max(row, 1);
		row = Math.min(row,this.getRows());
		int pos = (int)(1.0*(x-this.getLeftMargin())/w * this.getHP()) + 1;
    	RackRow rr = (RackRow)this.getChildAt(row - 1);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)m.getLayoutParams();
		params.width = LayoutParams.MATCH_PARENT; //ignored by onMeasure
		params.height = LayoutParams.MATCH_PARENT;
		params.topMargin = 0;
    	rr.addModule(m, pos);
    	m.bringToFront();  	
    }
	
	public void moveBy(int dx, int dy){	
		//keep rack from scrolling off screen.
		int maxScrollX = 0;
		int minScrollX = 0;
		int maxScrollY = 0;
		int minScrollY = 0;
		
		View parent = (View)this.getParent();
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)this.getLayoutParams();
		
		int rackLeft = params.leftMargin;
		int parentWidth = parent.getWidth();
		int rackWidth = this.getScaledWidth();
		int rackTop = params.topMargin;
		int parentHeight = parent.getHeight();
		int rackHeight = this.getScaledHeight();
			
		minScrollX = Math.min(-rackLeft, parentWidth - (rackWidth + rackLeft));
		maxScrollX = Math.max(-rackLeft, parentWidth - (rackWidth + rackLeft));		
		minScrollY = Math.min(-rackTop, parentHeight - (rackHeight + rackTop));
		maxScrollY = Math.max(-rackTop, parentHeight - (rackHeight + rackTop));
			
		dx = Math.max(dx, minScrollX);
		dx = Math.min(dx, maxScrollX);
		dy = Math.max(dy, minScrollY);
		dy = Math.min(dy, maxScrollY);
			
		params.leftMargin = params.leftMargin + dx;
		params.topMargin = params.topMargin + dy;
		this.requestLayout();
	}

}
