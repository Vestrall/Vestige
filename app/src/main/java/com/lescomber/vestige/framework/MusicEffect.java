package com.lescomber.vestige.framework;

import com.lescomber.vestige.Options;

public class MusicEffect {
	private Music music;

	private final String filename;
	private final float baseVolume;

	private float volume;

	public MusicEffect(String filename, float baseVolume) {
		this.filename = filename;
		this.baseVolume = baseVolume;
		updateVolume();
	}

	public void play() {
		if (music == null) {
			music = AudioManager.createMusic(filename);
			music.setVolume(volume);
		} else if (music.isPlaying())
			return;

		music.play();
	}

	public void stop() {
		if (music != null) {
			music.dispose();
			music = null;
		}
	}

	public void pause() {
		if (music != null)
			music.pause();
	}

	public void resume() {
		if (music != null)
			music.resume();
	}

	public void updateVolume() {
		volume = Options.getSfxVolume() * baseVolume;
		if (music != null)
			music.setVolume(volume);
	}

	public void dispose() {
		if (music != null)
			music.dispose();
	}
}