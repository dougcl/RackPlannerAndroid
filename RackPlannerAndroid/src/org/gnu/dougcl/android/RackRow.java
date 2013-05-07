package org.gnu.dougcl.android;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


public class RackRow extends RelativeLayout implements Scalable {
	
	private int intrinsicWidth;
	private int intrinsicHeight;
	private int hp;
	private int cols;
	
	private double unitAr;


	public RackRow(Context context, Drawable bg, int hp, int cols) {
		super(context);
		this.hp = hp;
		this.cols = cols;
		this.intrinsicWidth = bg.getIntrinsicWidth() * cols;
		this.intrinsicHeight = bg.getIntrinsicHeight();
		this.unitAr = 1.0 * bg.getIntrinsicWidth()/bg.getIntrinsicHeight()/hp;
		LinearLayout.LayoutParams params = 
			new LinearLayout.LayoutParams(this.intrinsicWidth,this.intrinsicHeight);
		this.setLayoutParams(params);
		for(int i = 0;i < cols;i++){
			this.addView(createSubrack(context,bg));
		}
	}
	
	public ImageView createSubrack(Context context, Drawable drawable){
		Subrack subrack = new Subrack(context,drawable);
		int childCount = this.getChildCount();
		subrack.setId(childCount + 1);
		RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)subrack.getLayoutParams();
		if (childCount == 0){
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		} else {
			params.addRule(RelativeLayout.RIGHT_OF, childCount);
		}
		return subrack;
	}
	
	public double getUnitAr(){
		return this.unitAr;
	}
	
	public void addModule(Module module,int position){
		this.addView(module);
		//make sure module position is valid.
		position = Math.max(position, 1);
		position = Math.min(position,this.hp * this.cols - module.getHP() + 1);
		module.setPosition(position);
	}
	
	
	public void setScale(double scale){
		this.getLayoutParams().width = (int)(scale * this.intrinsicWidth);
		this.getLayoutParams().height = (int)(scale * this.intrinsicHeight);
		this.requestLayout();
		Scalable child;
		for(int i = 0;i < this.getChildCount();i++){
			if (this.getChildAt(i) instanceof Scalable){
				child = (Scalable)this.getChildAt(i);
				child.setScale(scale);
			}
		}
	}
		
}
