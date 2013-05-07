package org.gnu.dougcl.android;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class Subrack extends ImageView implements Scalable {
	private int intrinsicWidth;
	private int intrinsicHeight;
	public Subrack(Context context, Drawable drawable) {
		super(context);
		this.setImageDrawable(drawable);
		this.intrinsicWidth = drawable.getIntrinsicWidth();
		this.intrinsicHeight = drawable.getIntrinsicHeight();
		RelativeLayout.LayoutParams params = 
			new RelativeLayout.LayoutParams(intrinsicWidth,intrinsicHeight);
		params.topMargin = 0;
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		this.setLayoutParams(params);
		this.setScaleType(ScaleType.FIT_XY);
	}

	public void setScale(double scale) {
		this.getLayoutParams().width = (int)(scale * this.intrinsicWidth);
		this.getLayoutParams().height = (int)(scale * this.intrinsicHeight);
	}

}
