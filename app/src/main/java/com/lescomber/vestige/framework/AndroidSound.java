package com.lescomber.vestige.framework;

import android.media.SoundPool;


public class AndroidSound
{
	private final int soundId;
	private final SoundPool soundPool;

	public AndroidSound(SoundPool soundPool, int soundId)
	{
		this.soundId = soundId;
		this.soundPool = soundPool;
	}

	public void play(float volume)
	{
		soundPool.play(soundId, volume, volume, 0, 0, 1);
	}

	public void dispose()
	{
		soundPool.unload(soundId);
	}
}