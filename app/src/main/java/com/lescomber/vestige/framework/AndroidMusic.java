package com.lescomber.vestige.framework;

import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;

import java.io.IOException;


public class AndroidMusic implements OnCompletionListener, OnSeekCompleteListener, OnPreparedListener, OnVideoSizeChangedListener
{
	MediaPlayer mediaPlayer;
	boolean isPrepared = false;

	public AndroidMusic(AssetFileDescriptor assetDescriptor)
	{
		mediaPlayer = new MediaPlayer();
		try
		{
			mediaPlayer.setDataSource(assetDescriptor.getFileDescriptor(), assetDescriptor.getStartOffset(), assetDescriptor.getLength());
			mediaPlayer.prepare();
			isPrepared = true;
			mediaPlayer.setOnCompletionListener(this);
			mediaPlayer.setOnSeekCompleteListener(this);
			mediaPlayer.setOnPreparedListener(this);
			mediaPlayer.setOnVideoSizeChangedListener(this);
		} catch (final Exception e)
		{
			throw new RuntimeException("Couldn't load music");
		}
	}

	public void dispose()
	{
		if (mediaPlayer.isPlaying())
			mediaPlayer.stop();

		mediaPlayer.release();
	}

	public boolean isLooping()
	{
		return mediaPlayer.isLooping();
	}

	public boolean isPlaying()
	{
		return mediaPlayer.isPlaying();
	}

	public boolean isStopped()
	{
		return !isPrepared;
	}

	public void pause()
	{
		if (mediaPlayer.isPlaying())
			mediaPlayer.pause();
	}

	public void play()
	{
		if (mediaPlayer.isPlaying())
			return;

		try
		{
			synchronized (this)
			{
				if (!isPrepared)
					mediaPlayer.prepare();
				mediaPlayer.start();
			}
		} catch (final IllegalStateException e)
		{
			e.printStackTrace();
		} catch (final IOException e)
		{
			e.printStackTrace();
		}
	}

	public void setLooping(boolean isLooping)
	{
		mediaPlayer.setLooping(isLooping);
	}

	public void setVolume(float volume)
	{
		mediaPlayer.setVolume(volume, volume);
	}

	public void stop()
	{
		if (mediaPlayer.isPlaying() == true)
		{
			mediaPlayer.stop();

			synchronized (this)
			{
				isPrepared = false;
			}
		}
	}

	@Override
	public void onCompletion(MediaPlayer player)
	{
		synchronized (this)
		{
			isPrepared = false;
		}
	}

	public void seekBegin()
	{
		mediaPlayer.seekTo(0);
	}

	@Override
	public void onPrepared(MediaPlayer player)
	{
		synchronized (this)
		{
			isPrepared = true;
		}
	}

	@Override
	public void onSeekComplete(MediaPlayer player)
	{
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer player, int width, int height)
	{
	}
}