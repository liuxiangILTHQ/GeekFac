package com.geekid.geekfactest.utils;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Service;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Vibrator;

import com.geekid.geekfactest.AppContext;

public class PlayMusic {
	private Context context;
	private MediaPlayer player;
	private Vibrator vib;
	
	public PlayMusic(Context base) {
		context = base;
	}

	public void play(int id) {
		int rsId;
		if (player != null) {
			player.release();
			player = null;
		}
		
		rsId=AppContext.musicId[id];
		
		player = MediaPlayer.create(context, rsId);
		player.start();
	}

	public void Playthreeseconds(int id) {
		play(id);

		Timer mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				if(player!=null)
				player.release();
			}
		}, 3000);
	}

	public void Playfiveseconds(int id) {
		play(id);

		Timer mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {

				if(player!=null)
				player.release();
			}
		}, 5000);
	}

	public void Playtenseconds(int id) {
		play(id);

		Timer mTimer = new Timer();
		mTimer.schedule(new TimerTask() {

			@Override
			public void run() {
				if(player!=null)
				player.release();
			}
		}, 10000);
	}

	

	public void release() {
		if (player != null) {
			player.release();
			player = null;
		}
	}

	public void Vibrate(Context context, long milliseconds) {
		vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
		vib.vibrate(milliseconds);
	}

	public void Vibrate(long milliseconds) {
		vib = (Vibrator) context.getSystemService(Service.VIBRATOR_SERVICE);
		vib.vibrate(milliseconds);
	}

	public void cancelVibrate() {
		if (vib != null) {
			vib.cancel();
		}
	}

}