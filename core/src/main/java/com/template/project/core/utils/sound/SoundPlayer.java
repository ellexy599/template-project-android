package com.template.project.core.utils.sound;

import android.content.Context;
import android.media.MediaPlayer;

import java.io.File;
import java.util.ArrayList;

import com.template.project.core.utils.LogMe;

/** Utility class for playing background music. Playing a sound file require permission in manifest.
 * <pre>
 * &lt;uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/&gt;
 * &lt;uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/&gt;
 * </pre>
 * to be able read sound file in app cache internal or external directory.
 */
public class SoundPlayer {

    private final String TAG = SoundPlayer.class.getSimpleName();

    private Context ctx;

    private ArrayList<SoundPlayerFile> arrSpFiles;

    public SoundPlayer(Context ctx) {
        this.ctx = ctx;
        arrSpFiles = new ArrayList<>();
    }

    // Mapping of instance of MediaPlayer and associated sound file.
    private class SoundPlayerFile {
        public SoundPlayerFile(MediaPlayer mpObj, int soundFileResId) {
            this.mp = mpObj;
            this.soundResId = soundFileResId;
        }

        public SoundPlayerFile(MediaPlayer mpObj, File soundFile) {
            this.mp = mpObj;
            this.soundFile = soundFile;
        }

        public MediaPlayer mp;
        public int soundResId;// sound file stored in app resources
        public File soundFile;// sound file stored in app cache
    }

    /**
     * Player specific sound. If the soundResId is already created in list of existing MediaPlayer
     * instance, then it will use the existing MediaPlayer of that resource file. Note that when
     * playing the resource file, the resource sound file should have the format extension in
     * its file name. i.e "sample.mp3"
     * @param soundResId Sound resource id in raw folder.
     * @param isLooping Set wether the sound to played will be loop
     * @param rightVolume Set the volume of right mono/speaker
     * @param leftVolume Set the volume of the left mono/speaker
     */
    public void playSound(int soundResId, final boolean isLooping, float rightVolume, float leftVolume) {
        try {
            MediaPlayer mp = null;
            if(arrSpFiles != null && arrSpFiles.size() > 0) {
                for(SoundPlayerFile spPlayerFile : arrSpFiles) {
                    if(spPlayerFile.soundResId == soundResId) {
                        mp = spPlayerFile.mp;
                    }
                }
            }
            if(mp == null) {
                mp = MediaPlayer.create(ctx, soundResId);
                mp.setLooping(isLooping);
                mp.setVolume(rightVolume, leftVolume);
                arrSpFiles.add(new SoundPlayerFile(mp, soundResId));
                setMediaPlayerListener(mp, true);
            }
            if(mp.isPlaying()) {
                mp.stop();
                mp.start();
            } else {
                mp.start();
            }
        } catch (Exception e) {
            LogMe.e(TAG, "playSound ERROR " + e.toString());
        }
    }

    /**
     * Play sound file. If sound file is missing/empty/corrupted then MediaPlayer will not play it.
     *@param soundFile Sound file stored in app cache.
     * @param isLooping Set wether the sound to played will be loop
     * @param rightVolume Set the volume of right mono/speaker
     * @param leftVolume Set the volume of the left mono/speaker
     */
    public void playSound(File soundFile, final boolean isLooping, float rightVolume, float leftVolume) {
        try {
            if(soundFile != null && soundFile.exists() && soundFile.length() > 0) {
                MediaPlayer mp = null;
                if(arrSpFiles != null && arrSpFiles.size() > 0) {
                    for(SoundPlayerFile spPlayerFile : arrSpFiles) {
                        if(spPlayerFile.soundFile !=null
                                && spPlayerFile.soundFile.getName().equals(soundFile.getName())) {
                            mp = spPlayerFile.mp;
                        }
                    }
                }
                if(mp == null) {
                    LogMe.d(TAG, "playSound path: " + soundFile.getAbsolutePath() + " isReadable: " + soundFile.canRead());
                    mp = new MediaPlayer();
                    mp.setLooping(isLooping);
                    mp.setVolume(rightVolume, leftVolume);
                    mp.setDataSource(soundFile.getAbsolutePath());
                    setMediaPlayerListener(mp, false);
                    arrSpFiles.add(new SoundPlayerFile(mp, soundFile));
                    mp.prepare();
                } else {
                    LogMe.d(TAG, "playSound mp not null");
                    if(mp.isPlaying()) {
                        mp.stop();
                        mp.start();
                    } else {
                        mp.start();
                    }
                }
            } else {
                LogMe.e(TAG, "playSound soundFile is null or empty");
            }
        } catch (Exception e) {
            LogMe.e(TAG, "playSound ERROR " + e.toString());
        }
    }

    /**
     * Release instance of all created MediaPlayer. Stop playing background sound of media player 
     * associated with playing and release the MediaPlayer instance. Should be called right onDestroy of Activity.
     */
    public void releaseMediaPlayers() {
        if(arrSpFiles != null && arrSpFiles.size() > 0) {
            for(SoundPlayerFile spPlayerFile : arrSpFiles) {
                if(spPlayerFile.mp != null) {
                    spPlayerFile.mp.release();
                }
            }
        }
    }

    /**
     * Set MediaPlayer callback OnErrorListener, OnInfoListener, and OnCompletionListener.
     * @param mp The MediaPlayer instance to set callbacks into.
     * @param isResourceFile If file to be played by MediaPlayer is a resource file or app cache file.
     *                       Setting it to true will not set OnPreparedListener of mp.
     */
    private void setMediaPlayerListener(MediaPlayer mp, boolean isResourceFile) {
        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
                LogMe.e(TAG, "setOnErrorListener arg1: " + i + " arg2: " + i2);
                switch (i) {
                    case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                        LogMe.w(TAG, "setOnInfoListener arg1: MEDIA_ERROR_UNKNOWN");
                        break;
                    case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                        LogMe.w(TAG, "setOnInfoListener arg1: MEDIA_ERROR_SERVER_DIED");
                        break;
                }
                switch (i2) {
                    case MediaPlayer.MEDIA_ERROR_IO:
                        LogMe.w(TAG, "setOnInfoListener arg2: MEDIA_ERROR_IO");
                        break;
                    case MediaPlayer.MEDIA_ERROR_MALFORMED:
                        LogMe.w(TAG, "setOnInfoListener arg2: MEDIA_ERROR_MALFORMED");
                        break;
                    case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                        LogMe.w(TAG, "setOnInfoListener arg2: MEDIA_ERROR_UNSUPPORTED");
                        break;
                    case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                        LogMe.w(TAG, "setOnInfoListener arg2: MEDIA_ERROR_TIMED_OUT");
                        break;
                }
                return true;
            }
        });

        mp.setOnInfoListener(new MediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(MediaPlayer mediaPlayer, int i, int i2) {
                LogMe.w(TAG, "setOnInfoListener arg1: " + i + " arg2: " + i2);
                return true;
            }
        });

        if(!isResourceFile) {
            // start playing after prepare() has finished
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mediaPlayer) {
                    LogMe.d(TAG, "onPrepared() called");
                    if(mediaPlayer.isPlaying()) {
                        mediaPlayer.stop();
                        mediaPlayer.start();
                    } else {
                        mediaPlayer.start();
                    }
                }
            });
        }
    }

}
