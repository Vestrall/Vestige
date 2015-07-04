package com.lescomber.vestige.framework;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import java.io.IOException;

public class Music {
	private final MediaPlayer mediaPlayer;
	private boolean isPrepared;
	private final String filename;

	public Music(AssetFileDescriptor assetDescriptor, String filename) {
		this.filename = filename;
		mediaPlayer = new MediaPlayer();
		try {
			mediaPlayer.setDataSource(assetDescriptor.getFileDescriptor(), assetDescriptor.getStartOffset(), assetDescriptor.getLength());
			mediaPlayer.prepare();
			isPrepared = true;
			mediaPlayer.setLooping(true);
		} catch (final Exception e) {
			throw new RuntimeException("Couldn't load music");
		}
	}

	public void dispose() {
		if (mediaPlayer.isPlaying())
			mediaPlayer.stop();

		mediaPlayer.release();
	}

	public boolean isPlaying() {
		return mediaPlayer.isPlaying();
	}

	public boolean isPlaying(String filename) {
		return (this.filename.equals(filename) && isPlaying());
	}

	public void pause() {
		if (mediaPlayer.isPlaying())
			mediaPlayer.pause();
	}

	public void play() {
		if (mediaPlayer.isPlaying())
			return;

		try {
			synchronized (this) {
				if (!isPrepared) {
					mediaPlayer.prepare();
					isPrepared = true;
				}
			}
			mediaPlayer.start();
		} catch (final IllegalStateException | IOException e) {
			e.printStackTrace();
		}
	}

	public void resume() {
		if (isPrepared && !mediaPlayer.isPlaying())
			mediaPlayer.start();
	}

	public void setVolume(float volume) {
		mediaPlayer.setVolume(volume, volume);
	}

	public void stop() {
		if (mediaPlayer.isPlaying()) {
			synchronized (this) {
				mediaPlayer.stop();
				isPrepared = false;
			}
		}
	}
}