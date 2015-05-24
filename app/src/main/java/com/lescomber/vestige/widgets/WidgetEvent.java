package com.lescomber.vestige.widgets;

public class WidgetEvent {
	private final Object source;
	private final String command;
	private final double value;

	public WidgetEvent(Object source, String command, double value) {
		this.source = source;
		this.command = command;
		this.value = value;
	}

	public WidgetEvent(Object source, String command) {
		this(source, command, 0);
	}

	public Object getSource() {
		return source;
	}

	public String getCommand() {
		return command;
	}

	public double getValue() {
		return value;
	}
}