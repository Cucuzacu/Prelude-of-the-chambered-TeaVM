package com.mojang.escape;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;

public class Sound {
	public static Sound altar;
	public static Sound bosskill;
	public static Sound click1;
	public static Sound click2;
	public static Sound hit;
	public static Sound hurt;
	public static Sound hurt2;
	public static Sound kill;
	public static Sound death;
	public static Sound splash;
	public static Sound key;
	public static Sound pickup;
	public static Sound roll;
	public static Sound shoot;
	public static Sound treasure;
	public static Sound crumble;
	public static Sound slide;
	public static Sound cut;
	public static Sound thud;
	public static Sound ladder;
	public static Sound potion;

	private Object nativeBuffer;

	@JSBody(script = "if (!window._audioCtx) { window._audioCtx = new (window.AudioContext || window.webkitAudioContext)(); } return window._audioCtx;")
	private static native Object getAudioContext();

	@JSBody(params = { "ctx", "url", "callback" }, script =
		"fetch(url)" +
		".then(function(res) { return res.arrayBuffer(); })" +
		".then(function(data) { return ctx.decodeAudioData(data); })" +
		".then(function(buf) { callback(buf); })" +
		".catch(function(err) { console.error('Failed to load sound:', url, err); });")
	private static native void nativeLoadSound(Object ctx, String url, SoundCallback callback);

	@JSBody(params = { "ctx", "buffer" }, script =
		"if (!buffer) return;" +
		"if (ctx.state === 'suspended') { ctx.resume(); }" +
		"var source = ctx.createBufferSource();" +
		"source.buffer = buffer;" +
		"source.connect(ctx.destination);" +
		"source.start(0);")
	private static native void nativePlaySound(Object ctx, Object buffer);

	@JSFunctor
	public interface SoundCallback extends JSObject {
  	        void onLoaded(Object buffer);
	}

	public static void loadAll() {
		Object ctx = getAudioContext();
		altar = loadSound(ctx, "snd/altar.wav");
		bosskill = loadSound(ctx, "snd/bosskill.wav");
		click1 = loadSound(ctx, "snd/click.wav");
		click2 = loadSound(ctx, "snd/click2.wav");
		hit = loadSound(ctx, "snd/hit.wav");
		hurt = loadSound(ctx, "snd/hurt.wav");
		hurt2 = loadSound(ctx, "snd/hurt2.wav");
		kill = loadSound(ctx, "snd/kill.wav");
		death = loadSound(ctx, "snd/death.wav");
		splash = loadSound(ctx, "snd/splash.wav");
		key = loadSound(ctx, "snd/key.wav");
		pickup = loadSound(ctx, "snd/pickup.wav");
		roll = loadSound(ctx, "snd/roll.wav");
		shoot = loadSound(ctx, "snd/shoot.wav");
		treasure = loadSound(ctx, "snd/treasure.wav");
		crumble = loadSound(ctx, "snd/crumble.wav");
		slide = loadSound(ctx, "snd/slide.wav");
		cut = loadSound(ctx, "snd/cut.wav");
		thud = loadSound(ctx, "snd/thud.wav");
		ladder = loadSound(ctx, "snd/ladder.wav");
		potion = loadSound(ctx, "snd/potion.wav");
	}

	public static Sound loadSound(Object ctx, String fileName) {
    		Sound sound = new Sound();
   		nativeLoadSound(ctx, fileName, new SoundCallback() {
        		@Override
        		public void onLoaded(Object buffer) {
            			sound.nativeBuffer = buffer;
        		}
    		});
    		return sound;
	}

	public void play() {
		nativePlaySound(getAudioContext(), this.nativeBuffer);
	}
}