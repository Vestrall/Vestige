package com.lescomber.vestige.framework;

import java.io.IOException;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioManager;
import android.media.SoundPool;


public class AndroidAudio
{
	private static final String audioDir = "Audio/";
	
	AssetManager assets;
	SoundPool soundPool;
	
	public AndroidAudio(Activity activity)
	{
		activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		assets = activity.getAssets();
		soundPool = new SoundPool(20, AudioManager.STREAM_MUSIC, 0);
	}
	
	public AndroidMusic createMusic(String filename)
	{
		try {
			final AssetFileDescriptor assetDescriptor = assets.openFd(audioDir + filename);
			return new AndroidMusic(assetDescriptor);
		}
		catch (final IOException e) {
			throw new RuntimeException("Couldn't load music '" + filename + "'");
		}
	}
	
	public AndroidSound createSound(String filename)
	{
		try {
			final AssetFileDescriptor assetDescriptor = assets.openFd(audioDir + filename);
			final int soundId = soundPool.load(assetDescriptor,  0);
			return new AndroidSound(soundPool, soundId);
		}
		catch (final IOException e) {
			throw new RuntimeException("Couldn't load sound '" + filename + "'");
		}
	}
}