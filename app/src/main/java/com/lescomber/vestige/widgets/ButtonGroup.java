package com.lescomber.vestige.widgets;

public class ButtonGroup {
	private boolean busy;

	public ButtonGroup() {

	}

	public boolean requestLock() {
		if (!busy) {
			busy = true;
			return true;
		} else
			return false;
	}

	public void releaseLock() {
		busy = false;
	}
}