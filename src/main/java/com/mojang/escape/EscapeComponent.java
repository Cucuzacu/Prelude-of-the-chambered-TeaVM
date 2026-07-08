package com.mojang.escape;

import org.teavm.jso.JSBody;
import org.teavm.jso.browser.Window;
import org.teavm.jso.browser.AnimationFrameCallback;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.canvas.ImageData;
import org.teavm.jso.typedarrays.Uint8ClampedArray;
import com.mojang.escape.gui.Screen;
import com.mojang.escape.level.Level;

public class EscapeComponent {
	private static final int WIDTH = 160;
	private static final int HEIGHT = 120;
	private static final int SCALE = 4;

	private boolean running;
	private Game game;
	private Screen screen;
	private InputHandler inputHandler;
	
	private HTMLCanvasElement canvas;
	private CanvasRenderingContext2D ctx;
	private ImageData imageData;

	@JSBody(script = "return performance.now() / 1000.0;")
	private static native double getTimeInSeconds();

	public EscapeComponent() {
		HTMLDocument document = Window.current().getDocument();
		canvas = (HTMLCanvasElement) document.createElement("canvas");
		
		canvas.setWidth(WIDTH);
		canvas.setHeight(HEIGHT);
		
		canvas.getStyle().setProperty("width", (WIDTH * SCALE) + "px");
		canvas.getStyle().setProperty("height", (HEIGHT * SCALE) + "px");
		canvas.getStyle().setProperty("image-rendering", "pixelated");
		canvas.getStyle().setProperty("image-rendering", "crisp-edges");
		document.getBody().appendChild(canvas);

		ctx = (CanvasRenderingContext2D) canvas.getContext("2d");
		imageData = ctx.createImageData(WIDTH, HEIGHT);

		game = new Game();
		screen = new Screen(WIDTH, HEIGHT);
		inputHandler = new InputHandler();
	}

	public void start() {
		if (running) return;
		running = true;
		
		Window.requestAnimationFrame(new AnimationFrameCallback() {
			private double unprocessedSeconds = 0;
			private double lastTime = getTimeInSeconds();
			private double secondsPerTick = 1.0 / 60.0;
			private int frames = 0;
			private double lastFpsTime = lastTime;

			@Override
			public void onAnimationFrame(double timestamp) {
				double now = getTimeInSeconds();
				double passedTime = now - lastTime;
				lastTime = now;

				if (passedTime < 0) passedTime = 0;
				if (passedTime > 0.1) passedTime = 0.1;

				unprocessedSeconds += passedTime;

				boolean ticked = false;
				while (unprocessedSeconds > secondsPerTick) {
					tick();
					unprocessedSeconds -= secondsPerTick;
					ticked = true;
				}

				if (ticked) {
					render();
					frames++;
				}

				if (now - lastFpsTime >= 1.0) {
					System.out.println(frames + " fps");
					frames = 0;
					lastFpsTime = now;
				}

				if (running) {
					Window.requestAnimationFrame(this);
				}
			}
		});
	}

	public void stop() {
		running = false;
	}

	private void tick() {
		game.tick(inputHandler.keys);
	}

	private void render() {
		screen.render(game, true);

		Uint8ClampedArray data = imageData.getData();
		for (int i = 0; i < WIDTH * HEIGHT; i++) {
			int col = screen.pixels[i];
			int r = (col >> 16) & 0xff;
			int g = (col >> 8) & 0xff;
			int b = col & 0xff;

			int index = i * 4;
			data.set(index, r);
			data.set(index + 1, g);
			data.set(index + 2, b);
			data.set(index + 3, 255); 
		}

		ctx.putImageData(imageData, 0, 0);
	}

	public static void main(String[] args) {
		Level.initLevels();
		
		String[] levels = {"start", "crypt", "overworld", "temple", "ice", "dungeon"};
		
		preloadLevels(levels, 0, () -> {
			Art.loadAll(() -> {
				Sound.loadAll();
				EscapeComponent gameComponent = new EscapeComponent();
				gameComponent.start();
			});
		});
	}

	private static void preloadLevels(String[] levels, int index, Runnable onComplete) {
		if (index >= levels.length) {
			onComplete.run();
			return;
		}
		Level.preloadLevelData(levels[index], () -> {
			preloadLevels(levels, index + 1, onComplete);
		});
	}
}