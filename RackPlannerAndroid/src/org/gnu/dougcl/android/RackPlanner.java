package org.gnu.dougcl.android;


import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.CountDownTimer;
import android.view.Display;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.view.View.OnTouchListener;

public class RackPlanner extends RelativeLayout implements OnTouchListener {
	
	private Rack rack;
	//Used for touch events.
	static final int NONE = 0;
	static final int ZOOM = 1;
	static final int MOVE = 2;
	private int touch_mode = NONE;
	
	private float oldDist;
	private int mouseX; //location of press
	private int mouseY; //location of press
	private int rackX; //upper left corner of the rack
	private int rackY; //upper left corner of the rack
	private int screenWidth;
	private int screenHeight;
	
	private double scale = 0.8; 
	private double zoom = 1.0;
	
	private GestureDetector gestureScanner;
	
	private View touchedView;
	private float x0;
	private float x1;
	private float y0;
	private float y1;
	private Module selectedModule;
	
	public RackPlanner(Context context) {
		super(context);
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		screenWidth = display.getWidth();
		screenHeight = display.getHeight();
		
		Drawable img = this.getResources().getDrawable(R.drawable.background);
		this.setBackgroundDrawable(img);
		
		this.setOnTouchListener(this);
		LayoutParams params = 
			new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT);
		this.setLayoutParams(params);
		rack = new Rack(context);
		this.addView(rack);		
		
		rack.load("rack.xml");
		gestureScanner = new GestureDetector(simpleOnGestureListener);
	}
	
	public Rack getRack(){
		return this.rack;
	}
	
	public boolean moduleSelected(){
		if (this.selectedModule == null) {
			return false;
		} else {
			return true;
		}
	}
	
	public void deleteSelectedModule(){
		if (selectedModule != null) {
			ViewGroup parent = (ViewGroup)selectedModule.getParent();
			parent.removeView(selectedModule); 
			selectedModule = null;
		}
	}
	
	public Module getSelectedModule(){
		return this.selectedModule;
	}
	
    public void setSelectedModule(Module m){
		if (selectedModule != null)
		{
		    selectedModule.setHighlighted(false);
		    selectedModule.invalidate();	    
		}	
		m.setHighlighted(true);
		m.bringToFront();
		m.invalidate();
		selectedModule = m;
    }
    
    private void clearSelectedModule(){
    	if (selectedModule != null){
    		selectedModule.setHighlighted(false);
    		selectedModule.invalidate();
    		selectedModule = null;
    	}
    }
    
	
	public boolean onTouch(View v, MotionEvent event) {
	   	touchedView = v;
	   	//getX and getY are in local view coordinates. Convert to raw for multi-touch.
		int[] loc = {0,0};
		touchedView.getLocationOnScreen(loc);
		x0 = loc[0] + event.getX(0);
		y0 = loc[1] + event.getY(0);
		if (event.getPointerCount() > 1){
			x1 = loc[0] + event.getX(1);
			y1 = loc[1] + event.getY(1);
		}
		
		
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		       
			case MotionEvent.ACTION_DOWN: {
				//Pivot point is in absolute screen coordinates, so get same for rack location.
				int[] location = {0,0};
				rack.getLocationOnScreen(location);
				rackX = location[0];
				rackY = location[1];
				mouseX = (int)event.getRawX();
				mouseY = (int)event.getRawY();
				break;
			}
			
			case MotionEvent.ACTION_UP: {
				if (touch_mode == ZOOM){
					this.scale = rack.getScale();
					this.zoom = 1.0;
				} else if (touch_mode == MOVE) {
					if (selectedModule != null){
						int[] location = {0,0};
						rack.getLocationOnScreen(location);
						this.removeView(selectedModule);
						if (
								(event.getRawX() > location[0]) &&
								(event.getRawX() < location[0] + rack.getScaledWidth()) &&
								(event.getRawY() > location[1]) && 
								(event.getRawY() < location[1] + rack.getScaledHeight()) 
							){
							//module has been dragged onto the rack
							rack.dropModule(selectedModule);
						} else {
							//module has been dragged off the rack. 
							clearSelectedModule();							
						}
					}
				}
				touch_mode = NONE;
				break;
			}
			
			case MotionEvent.ACTION_POINTER_DOWN: {	
				oldDist = spacing(x0,x1,y0,y1);
				if (touch_mode != MOVE) {
					//don't combine module move with a zoom.
					touch_mode = ZOOM;
				}
				break;
			}
			
			case MotionEvent.ACTION_MOVE: {	
				if (touch_mode == ZOOM) {
					double newDist = spacing(x0,x1,y0,y1);
					double k = 0.2; //smooth the pinch response. The smaller the smoother. k in [0,1].
					double new_zoom = Math.round(100*(k * newDist / oldDist + (1 - k) * zoom))/100.0;
					double new_scale = new_zoom * this.scale;
					
					//the zoom center
					int pivotX = (int)((x0 + x1)/2);
					int pivotY = (int)((y0 + y1)/2);

					if ((new_zoom != zoom) && 
							(new_scale > .3 ) &&
							(new_scale < 5)							
						) {

						int dx = (int)((rackX - pivotX) * (new_zoom - zoom));
						int dy = (int)((rackY - pivotY) * (new_zoom - zoom));
						rack.moveBy(dx,dy);
						rack.setScale(new_scale);
						zoom = new_zoom;						
					} 
				} else if (touch_mode == MOVE){
					int rawX = (int)event.getRawX();
					int rawY = (int)event.getRawY();
					int dx = rawX - mouseX;
					int dy = rawY - mouseY;
					mouseX = rawX;
					mouseY = rawY;
					selectedModule.moveBy(dx, dy);
				}
				break;
			}
		}
		if (touch_mode != ZOOM && touch_mode != MOVE) {
			gestureScanner.onTouchEvent(event);
		}
		return true;
	}
	
	SimpleOnGestureListener simpleOnGestureListener 
	= new SimpleOnGestureListener(){
	
		private double dx = 0;
		private double dy = 0;
		private int dt = 10;
		
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			double new_zoom;
			if (rack.getScale() < 0.6){
				new_zoom = 3.0;
			} else {
				new_zoom = 0.333;
			}
			double new_scale = new_zoom * rack.getScale();

			if ((new_scale > .3 ) &&
					(new_scale < 5)							
				) {
				rack.setScale(new_scale);
				scale = new_scale;
				//keep rack from moving while zooming
				int dx = (int)((rackX - mouseX) * (new_zoom - 1));
				int dy = (int)((rackY - mouseY) * (new_zoom - 1));
				rack.moveBy(dx,dy);
				//try to leave the zoom point centered on the screen.
				dx = (int)((screenWidth/2.0 - mouseX));
				dy = (int)((screenHeight/2.0 - mouseY));
				rack.moveBy(dx,dy);
			} 
			return super.onDoubleTap(e);
		}
	
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			
			int duration = 2000; //two seconds scroll duration
			dx = 5*(e2.getRawX() - e1.getRawX() + velocityX)* dt/duration;
			dy = 5*(e2.getRawY() - e1.getRawY() + velocityY)* dt/duration;
			
			new CountDownTimer(duration, dt) { 
		        public void onTick(long millisUntilFinished) { 
		            rack.moveBy((int)dx,(int)dy);
		            dx = dx - dx/dt; //slow down during motion
		            dy = dy - dy/dt;		            
		        } 
		        public void onFinish() { 
		        } 
		    }.start();
			return super.onFling(e1, e2, velocityX, velocityY);
		}
	
		@Override
		public void onLongPress(MotionEvent e) {
			if (touch_mode != ZOOM && touch_mode != MOVE) {
				if (touchedView instanceof Module){
					Module m = (Module)touchedView;
					setSelectedModule(m);
					if (rack.dragModule(m)) {
						touch_mode = MOVE;
					}		
				} else {
					//clearSelectedModule();
					touchedView.showContextMenu();
				}			
			}
			super.onLongPress(e);
		}
	
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			if ((touchedView instanceof Module)){
				setSelectedModule((Module)touchedView);
			} else {
				clearSelectedModule();
			}
			return super.onSingleTapConfirmed(e);
		}
		
	};
	
	private float spacing(float x0, float x1, float y0, float y1) {
		float x = x0 - x1;
		float y = y0 - y1;
		return x * x + y * y;
	}
	
}
