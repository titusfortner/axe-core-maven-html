package com.deque.html.axecore.selenium;

public class AxeScript {
	private String script;
	private String message;
	private String type;

	public AxeScript(String script, String message, String type) {
		this.script = script;
		this.message = message;
		this.type = type;
	}

	public String getScript() {
		return script;
	}

	public String getMessage() {
		return message;
	}

	public String getType() {
		return type;
	}
}
