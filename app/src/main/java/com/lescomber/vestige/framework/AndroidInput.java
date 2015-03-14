package com.lescomber.vestige.framework;

import android.view.View;

import java.util.List;

public class AndroidInput implements Input
{
	TouchHandler touchHandler;
	
	public AndroidInput(View view, float scaleX, float scaleY)
	{
		if (android.os.Build.VERSION.SDK_INT < 5)
			touchHandler = new SingleTouchHandler(view, scaleX, scaleY);
		else
			touchHandler = new MultiTouchHandler(view, scaleX, scaleY);
	}

	@Override public boolean isTouchDown(int pointer) { return touchHandler.isTouchDown(pointer); }
	@Override public float getTouchX(int pointer) { return touchHandler.getTouchX(pointer);	}
	@Override public float getTouchY(int pointer) { return touchHandler.getTouchY(pointer);	}
	@Override public List<TouchEvent> getTouchEvents() { return touchHandler.getTouchEvents(); }
}