package org.gnu.dougcl.android;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class Module extends ImageView {
	
	private Drawable img;
	private int hp; //number of horizontal spaces the module requires
	private double ar; //aspect ratio (w/h) of the module
	private double unitAr; //aspect ratio (w/h) of a single HP.
	private int position = 0; //The horizontal position of the module.
	private int height;
	private boolean isHighlighted = false;

	public Module(Context context, Drawable img, int hp) {
		super(context);
		this.img = img;
		this.hp = hp;
		this.ar = 1.0 * img.getIntrinsicWidth()/img.getIntrinsicHeight();
		this.unitAr = this.ar/hp; 
		//width spec here is ignored and overridden in OnMeasure below.
		RelativeLayout.LayoutParams layoutParams = new LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT); 
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        this.setLayoutParams(layoutParams);
        this.setImageDrawable(img);
        this.setScaleType(ScaleType.FIT_XY);
        this.bringToFront();
	}
	
	public Module getCopy(){
		Module m = new Module(this.getContext(),img,hp);
		return m;	
	}
	
	public int getScaledHeight(){
		return this.height;
	}
	
	public int getScaledWidth(){
		return (int)(this.ar * this.height);
	}
	
	public int getHP(){
		return this.hp;
	}
	
	public int getLeftMargin(){
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)this.getLayoutParams();
		return params.leftMargin;
	}
	
	public int getTopMargin(){
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)this.getLayoutParams();
		return params.topMargin;	
	}

	public void setPosition(int pos){
		//When the module is mounted in a subrack, this is the HP position.
		this.position = pos;
	}	
	
	public void setHighlighted(boolean isHighlighted){
		this.isHighlighted = isHighlighted;
	}
	
	public void moveBy(int dx, int dy){
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)this.getLayoutParams();
		params.leftMargin = params.leftMargin + dx;
		params.topMargin = params.topMargin + dy;
		this.requestLayout();
	}
	
	@Override 
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int width = 0;
		//This should not be necessary, but explicit w/h in layoutParams is sometimes ignored.
		if (this.getLayoutParams().height > 0){
			//height is specifically declared. Module is probably being dragged. 
			this.height = this.getLayoutParams().height;
		} else {
			//Height is not explicitly specified. Probably MATCH_PARENT, and module is mounted in subrack.
			this.height = MeasureSpec.getSize(heightMeasureSpec); //from parent
			RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)this.getLayoutParams();
			//Assume module is in a subrack. This means the left margin is based on the position.
			params.leftMargin = (int)(this.height * this.unitAr * (this.position - 1));	
			this.requestLayout();
		} 
		width = (int)(this.ar * this.height);
		//System.out.println("p=" + Integer.toString(position) + " w="+ Integer.toString(width)+ " h="+ Integer.toString(height)+ " wms="+ Integer.toString(widthMeasureSpec)+ " hms="+ Integer.toString(heightMeasureSpec)+ " wmode="+ Integer.toString(MeasureSpec.getMode(widthMeasureSpec))+ " hmode="+ Integer.toString(MeasureSpec.getMode(heightMeasureSpec))+ " lph="+ Integer.toString(this.getLayoutParams().height));
		super.onMeasure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY));
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		//highlight selected module
		if (this.isHighlighted) {
			Paint paint = new Paint();
			int width = (int)(this.getHeight() * ar);
			paint.setColor(Color.YELLOW);
			paint.setStrokeWidth(3);
	        canvas.drawLine(this.getWidth() - 2, 0, this.getWidth() - 2, this.getHeight() - 2, paint);     
	        canvas.drawLine(this.getWidth() - width + 2, 0, this.getWidth() - width + 2, this.getHeight() - 2, paint);     
		}	
	}
	
}
