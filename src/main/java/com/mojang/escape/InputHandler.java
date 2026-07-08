package com.mojang.escape;

import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.events.KeyboardEvent;
import org.teavm.jso.browser.Window;

public class InputHandler {
	public boolean[] keys = new boolean[65536];

	public InputHandler() {
		Window.current().addEventListener("keydown", new EventListener<KeyboardEvent>() {
			@Override
			public void handleEvent(KeyboardEvent e) {
				int code = e.getKeyCode();
				if (code > 0 && code < keys.length) {
					keys[code] = true;
				}

				if (code == 32 || (code >= 37 && code <= 40)) {
					e.preventDefault();
				}
			}
		});

		Window.current().addEventListener("keyup", new EventListener<KeyboardEvent>() {
			@Override
			public void handleEvent(KeyboardEvent e) {
				int code = e.getKeyCode();
				if (code > 0 && code < keys.length) {
					keys[code] = false;
				}
			}
		});

		Window.current().addEventListener("blur", new EventListener<Event>() {
			@Override
			public void handleEvent(Event e) {
				for (int i = 0; i < keys.length; i++) {
					keys[i] = false;
				}
			}
		});
	}
}