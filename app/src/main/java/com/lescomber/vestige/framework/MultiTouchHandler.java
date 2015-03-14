package com.lescomber.vestige.framework;

import android.view.MotionEvent;
import android.view.View;

import com.lescomber.vestige.framework.Input.TouchEvent;
import com.lescomber.vestige.framework.Pool.PoolObjectFactory;

import java.util.ArrayList;
import java.util.List;

public class MultiTouchHandler implements TouchHandler
{
	private static final int MAX_TOUCHPOINTS = 10;
	
	boolean[] isTouched = new boolean[MAX_TOUCHPOINTS];
	float[] touchX = new float[MAX_TOUCHPOINTS];
	float[] touchY = new float[MAX_TOUCHPOINTS];
	int[] id = new int[MAX_TOUCHPOINTS];
	Pool<TouchEvent> touchEventPool;
	List<TouchEvent> touchEvents = new ArrayList<TouchEvent>();
	List<TouchEvent> touchEventsBuffer = new ArrayList<TouchEvent>();
	float scaleX;
	float scaleY;
	
	public MultiTouchHandler(View view, float scaleX, float scaleY)
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
			final int action = event.getActionMasked();
			final int pointerIndex = event.getActionIndex();
			final int pointerCount = event.getPointerCount();
			TouchEvent touchEvent;
			for (int i=0; i<MAX_TOUCHPOINTS; i++)
			{
				if (i >= pointerCount)
				{
					isTouched[i] = false;
					id[i] = -1;
					continue;
				}
				
				final int pointerId = event.getPointerId(i);
				
				if (event.getAction() != MotionEvent.ACTION_MOVE && i != pointerIndex)
				{
					// if it's an up/down/cancel/out event, mask the id to see if we should process it for this touch point
					continue;
				}
				
				switch (action)
				{
				case MotionEvent.ACTION_DOWN:
				case MotionEvent.ACTION_POINTER_DOWN:
					touchEvent = touchEventPool.newObject();
					touchEvent.type = TouchEvent.TOUCH_DOWN;
					touchEvent.pointer = pointerId;
					touchEvent.x = touchX[i] = event.getX(i) * scaleX;
					touchEvent.y = touchY[i] = event.getY(i) * scaleY;
					isTouched[i] = true;
					id[i] = pointerId;
					touchEventsBuffer.add(touchEvent);
					break;
				case MotionEvent.ACTION_UP:
				case MotionEvent.ACTION_POINTER_UP:
				case MotionEvent.ACTION_CANCEL:
					touchEvent = touchEventPool.newObject();
					touchEvent.type = TouchEvent.TOUCH_UP;
					touchEvent.pointer = pointerId;
					touchEvent.x = touchX[i] = event.getX(i) * scaleX;
					touchEvent.y = touchY[i] = event.getY(i) * scaleY;
					isTouched[i] = false;
					id[i] = -1;
					touchEventsBuffer.add(touchEvent);
					break;
				case MotionEvent.ACTION_MOVE:
					touchEvent = touchEventPool.newObject();
					touchEvent.type = TouchEvent.TOUCH_DRAGGED;
					touchEvent.pointer = pointerId;
					touchEvent.x = touchX[i] = event.getX(i) * scaleX;
					touchEvent.y = touchY[i] = event.getY(i) * scaleY;
					isTouched[i] = true;
					id[i] = pointerId;
					touchEventsBuffer.add(touchEvent);
					break;
				}
			}
			
			return true;
		}
	}
	
	@Override
	public boolean isTouchDown(int pointer)
	{
		synchronized (this)
		{
			final int index = getIndex(pointer);
			if (index < 0 || index >= MAX_TOUCHPOINTS)
				return false;
			else
				return isTouched[index];
		}
	}
	
	@Override
	public float getTouchX(int pointer)
	{
		synchronized (this)
		{
			final int index = getIndex(pointer);
			if (index < 0 || index >= MAX_TOUCHPOINTS)
				return 0;
			else
				return touchX[index];
		}
	}
	
	@Override
	public float getTouchY(int pointer)
	{
		synchronized (this)
		{
			final int index = getIndex(pointer);
			if (index < 0 || index >= MAX_TOUCHPOINTS)
				return 0;
			else
				return touchY[index];
		}
	}
	
	@Override
	public List<TouchEvent> getTouchEvents()
	{
		synchronized (this)
		{
			final int len = touchEvents.size();
			for (int i=0; i<len; i++)
				touchEventPool.free(touchEvents.get(i));
			
			touchEvents.clear();
			touchEvents.addAll(touchEventsBuffer);
			touchEventsBuffer.clear();
			
			return touchEvents;
		}
	}
	
	// returns the index for a given pointer id or returns -1 if no index
	private int getIndex(int pointerId)
	{
		for (int i=0; i<MAX_TOUCHPOINTS; i++)
		{
			if (id[i] == pointerId)
				return i;
		}
		
		return -1;
	}
}