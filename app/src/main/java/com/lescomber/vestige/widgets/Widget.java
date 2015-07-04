package com.lescomber.vestige.widgets;

import com.lescomber.vestige.framework.TouchHandler.TouchEvent;

import java.util.ArrayList;

public abstract class Widget {
	private final ArrayList<WidgetListener> listeners;

	public Widget() {
		listeners = new ArrayList<>(2);
	}

	public void addWidgetListener(WidgetListener listener) {
		listeners.add(listener);
	}

	void notifyListeners(WidgetEvent we) {
		for (final WidgetListener wl : listeners)
			wl.widgetEvent(we);
	}

	public abstract void handleEvent(TouchEvent e);

	public abstract void setVisible(boolean isVisible);

	public void close() {
		setVisible(false);
	}
}