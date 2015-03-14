package com.lescomber.vestige.framework;

import android.view.MotionEvent;
import android.view.View;

import com.lescomber.vestige.framework.Input.TouchEvent;
import com.lescomber.vestige.framework.Pool.PoolObjectFactory;

import java.util.ArrayList;
import java.util.List;


public class SingleTouchHandler implements TouchHandler
{
	boolean isTouched;
	float touchX;
	float touchY;
	Pool<TouchEvent> touchEventPool;
	List<TouchEvent> touchEvents = new ArrayList<TouchEvent>();
	List<TouchEvent> touchEventsBuffer = new ArrayList<TouchEvent>();
	float scaleX;
	float scaleY;
	
	public SingleTouchHandler(View view, float scaleX, float scaleY)
	{
		final PoolObjectFactory<TouchEvent> factory = new PoolObjectFactory<TouchEvent>() {
			@Override
			public TouchEvent createObject() {
				return new TouchEvent();
			}
		};
		
		touchEventPool = new Pool<TouchEvent>(factory, 100);
		view.setOnTouchListener(this);
		
		this.scaleX = scaleX;
		this.scaleY = scaleY;
	}
	
	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		synchronized (this)
		{
			final TouchEvent touchEvent = touchEventPool.newObject();
			switch (event.getAction())
			{
			case MotionEvent.ACTION_DOWN:
				touchEvent.type = TouchEvent.TOUCH_DOWN;
				isTouched = true;
				break;
			case MotionEvent.ACTION_MOVE:
				touchEvent.type = TouchEvent.TOUCH_DRAGGED;
				isTouched = true;
				break;
			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				touchEvent.type = TouchEvent.TOUCH_UP;
				isTouched = false;
				break;
			}
			
			touchEvent.x = touchX = event.getX() * scaleX;
			touchEvent.y = touchY = event.getY() * scaleY;
			touchEventsBuffer.add(touchEvent);
			
			return true;
		}
	}
	
	@Override
	public boolean isTouchDown(int pointer)
	{
		synchronized (this)
		{
			if (pointer == 0)
				return isTouched;
			else
				return false;
		}
	}
	
	@Override
	public float getTouchX(int pointer)
	{
		synchronized (this)
		{
			return touchX;
		}
	}
	
	@Override
	public float getTouchY(int pointer)
	{
		synchronized (this)
		{
			return touchY;
		}
	}
	
	@Override
	public List<TouchEvent> getTouchEvents()
	{
		synchronized (this)
		{
			final int len = touchEvents.size();
			for(int i=0; i<len; i++)
				touchEventPool.free(touchEvents.get(i));
			
			touchEvents.clear();
			touchEvents.addAll(touchEventsBuffer);
			touchEventsBuffer.clear();
			return touchEvents;
		}
	}
}