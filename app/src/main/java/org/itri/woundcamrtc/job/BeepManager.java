package org.itri.woundcamrtc.job;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.preference.PreferenceManager;
import android.util.Log;


import org.itri.woundcamrtc.AppResultReceiver;

import org.itri.woundcamrtc.R;


public final class BeepManager {

    private static final String TAG = BeepManager.class.getSimpleName();

    public static final boolean DEFAULT_TOGGLE_BEEP = true;
    private static final float BEEP_VOLUME = 0.10f;

    private final Activity activity;

    private boolean playBeep;

    private MediaPlayer mediaPlayer;

    private SoundPool soundPool01;
    private int Sound01;

    private SoundPool soundPool02;
    private int Sound02;

    private SoundPool soundPool03;
    private int Sound03;


    public BeepManager(Activity activity) {
        this.activity = activity;
        this.mediaPlayer = null;
        this.soundPool01 = null;
        this.soundPool02 = null;
        this.soundPool03 = null;
        updatePrefs();
    }

    public void updatePrefs() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        playBeep = shouldBeep(prefs, activity);
        if (playBeep && mediaPlayer == null) {
            // The volume on STREAM_SYSTEM is not adjustable, and users found it too loud,
            // so we now play on the music stream.
            activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
            mediaPlayer = buildMediaPlayer(activity);
            soundPool01 = buildSoundPool01(activity);
            soundPool02 = buildSoundPool02(activity);
            soundPool03 = buildSoundPool03(activity);
        }
    }

    public synchronized void playBeepSoundAndVibrate() {
//    if (playBeep && mediaPlayer != null) {
//      mediaPlayer.start();
//    }
        if (playBeep && soundPool01 != null) {
            soundPool01.play(this.Sound01, 1, 1, 0, 0, 1);
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public synchronized void playBeepSoundAndVibrate(int soundId) {
//    if (playBeep && mediaPlayer != null) {
//      mediaPlayer.start();
//    }
        switch (soundId) {
            case 1:
                if (playBeep && soundPool01 != null) {
                    soundPool01.play(this.Sound01, 1, 1, 0, 0, 1);
                }
                break;
            case 2:
                if (playBeep && soundPool02 != null) {
                    soundPool02.play(this.Sound02, 1, 1, 0, 0, 1);
                }
                break;
            case 3:
                if (playBeep && soundPool03 != null) {
                    soundPool03.play(this.Sound03, 1, 1, 0, 0, 1);
                }
                break;
        }
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    private static boolean shouldBeep(SharedPreferences prefs, Context activity) {
        boolean shouldPlayBeep = prefs.getBoolean(AppResultReceiver.KEY_PLAY_BEEP, DEFAULT_TOGGLE_BEEP);
        if (shouldPlayBeep) {
            // See if sound settings overrides this
            AudioManager audioService = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
            if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                shouldPlayBeep = false;
            }
        }
        return shouldPlayBeep;
    }

    private static MediaPlayer buildMediaPlayer(Context activity) {
        MediaPlayer mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        // When the beep has finished playing, rewind to queue up another one.
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            public void onCompletion(MediaPlayer player) {
                player.seekTo(0);
            }
        });

        AssetFileDescriptor file = activity.getResources().openRawResourceFd(R.raw.camera_shot);
        try {
            mediaPlayer.setDataSource(file.getFileDescriptor(), file.getStartOffset(), file.getLength());
            mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
            mediaPlayer.prepareAsync();
            file.close();
        } catch (Exception ioe) {
            Log.w(TAG, ioe);
            mediaPlayer = null;
        }
        return mediaPlayer;
    }


    private SoundPool buildSoundPool01(Context activity) {
        SoundPool player = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);

        AssetFileDescriptor file = activity.getResources().openRawResourceFd(R.raw.camera_shot);
        try {
            this.Sound01 = player.load(file, 1);
            file.close();
        } catch (Exception ioe) {
            Log.w(TAG, ioe);
            player = null;
        }
        return player;
    }

    private SoundPool buildSoundPool02(Context activity) {
        SoundPool player = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);

        AssetFileDescriptor file = activity.getResources().openRawResourceFd(R.raw.camera_focus);
        try {
            this.Sound02 = player.load(file, 1);
            file.close();
        } catch (Exception ioe) {
            Log.w(TAG, ioe);
            player = null;
        }
        return player;
    }

    private SoundPool buildSoundPool03(Context activity) {
        SoundPool player = new SoundPool(10, AudioManager.STREAM_MUSIC, 5);

        AssetFileDescriptor file = activity.getResources().openRawResourceFd(R.raw.camera_focusing);
        try {
            this.Sound03 = player.load(file, 1);
            file.close();
        } catch (Exception ioe) {
            Log.w(TAG, ioe);
            player = null;
        }
        return player;
    }

}
