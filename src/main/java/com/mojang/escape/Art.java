package com.mojang.escape;

import org.teavm.jso.browser.Window;
import org.teavm.jso.dom.html.HTMLCanvasElement;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLImageElement;
import org.teavm.jso.canvas.CanvasRenderingContext2D;
import org.teavm.jso.canvas.ImageData;
import org.teavm.jso.typedarrays.Uint8ClampedArray;
import com.mojang.escape.gui.Bitmap;

public class Art {
	public static Bitmap walls;
	public static Bitmap floors;
	public static Bitmap sprites;
	public static Bitmap font;
	public static Bitmap panel;
	public static Bitmap items;
	public static Bitmap sky;
	public static Bitmap logo;

	private static int remaining = 8;
	private static Runnable onCompleteCallback;

	public static void loadAll(Runnable onComplete) {
		onCompleteCallback = onComplete;
		loadBitmap("tex/walls.png", b -> { walls = b; checkComplete(); });
		loadBitmap("tex/floors.png", b -> { floors = b; checkComplete(); });
		loadBitmap("tex/sprites.png", b -> { sprites = b; checkComplete(); });
		loadBitmap("tex/font.png", b -> { font = b; checkComplete(); });
		loadBitmap("tex/gamepanel.png", b -> { panel = b; checkComplete(); });
		loadBitmap("tex/items.png", b -> { items = b; checkComplete(); });
		loadBitmap("tex/sky.png", b -> { sky = b; checkComplete(); });
		loadBitmap("gui/logo.png", b -> { logo = b; checkComplete(); });
	}

	private static void checkComplete() {
		remaining--;
		if (remaining == 0 && onCompleteCallback != null) {
			onCompleteCallback.run();
		}
	}

	public interface BitmapConsumer {
		void accept(Bitmap bitmap);
	}

	public static void loadBitmap(String fileName, BitmapConsumer callback) {
		HTMLDocument doc = Window.current().getDocument();
		HTMLImageElement img = (HTMLImageElement) doc.createElement("img");
		img.setSrc(fileName);
		
		img.addEventListener("load", e -> {
			int w = img.getNaturalWidth();
			int h = img.getNaturalHeight();

			HTMLCanvasElement canvas = (HTMLCanvasElement) doc.createElement("canvas");
			canvas.setWidth(w);
			canvas.setHeight(h);
			CanvasRenderingContext2D ctx = (CanvasRenderingContext2D) canvas.getContext("2d");
			ctx.drawImage(img, 0, 0);

			ImageData data = ctx.getImageData(0, 0, w, h);
			Uint8ClampedArray bytes = data.getData();

			Bitmap result = new Bitmap(w, h);
			for (int i = 0; i < w * h; i++) {
				int r = bytes.get(i * 4) & 0xff;
				int g = bytes.get(i * 4 + 1) & 0xff;
				int b = bytes.get(i * 4 + 2) & 0xff;
				int a = bytes.get(i * 4 + 3) & 0xff;

				int in = (a << 24) | (r << 16) | (g << 8) | b;
				int col = (in & 0xf) >> 2;
				if (in == 0xffff00ff) col = -1;
				result.pixels[i] = col;
			}
			callback.accept(result);
		});
	}

	public static int getCol(int c) {
		int r = (c >> 16) & 0xff;
		int g = (c >> 8) & 0xff;
		int b = (c) & 0xff;

		r = r * 0x55 / 0xff;
		g = g * 0x55 / 0xff;
		b = b * 0x55 / 0xff;

		return r << 16 | g << 8 | b;
	}
}