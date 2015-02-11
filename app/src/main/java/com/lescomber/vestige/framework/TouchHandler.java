package com.lescomber.vestige.framework;

import java.util.List;

import android.view.View.OnTouchListener;

import com.lescomber.vestige.framework.Input.TouchEvent;

public interface TouchHandler extends OnTouchListener
{
	public boolean isTouchDown(int pointer);
	
	public float getTouchX(int pointer);
	
	public float getTouchY(int pointer);
	
	public List<TouchEvent> getTouchEvents();
}