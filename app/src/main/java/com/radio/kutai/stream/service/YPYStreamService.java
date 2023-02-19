/*
 * Copyright (c) 2018. YPY Global - All Rights Reserved.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at.
 *
 *         http://ypyglobal.com/sourcecode/policy
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.radio.kutai.stream.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;

import com.radio.kutai.R;
import com.radio.kutai.XMultiRadioMainActivity;
import com.radio.kutai.XSingleRadioMainActivity;
import com.radio.kutai.dataMng.TotalDataManager;
import com.radio.kutai.dataMng.XRadioNetUtils;
import com.radio.kutai.model.RadioModel;
import com.radio.kutai.setting.XRadioSettingManager;
import com.radio.kutai.stream.audiofocus.AudioFocusHelper;
import com.radio.kutai.stream.audiofocus.IStreamFocusableListener;
import com.radio.kutai.stream.constant.IYPYStreamConstants;
import com.radio.kutai.stream.manager.YPYStreamManager;
import com.radio.kutai.stream.mediaplayer.YPYMediaPlayer;
import com.radio.kutai.ypylibs.executor.YPYExecutorSupplier;
import com.radio.kutai.ypylibs.utils.ApplicationUtils;
import com.radio.kutai.ypylibs.utils.IOUtils;

import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.view.ViewCompat;


import static com.radio.kutai.constants.IXRadioConstants.AUTO_NEXT_WHEN_COMPLETE;
import static com.radio.kutai.constants.IXRadioConstants.IS_MUSIC_PLAYER;


/**
 * @author:YPY Global
 * @Email: baodt@hanet.com
 * @Website: http://hanet.com/
 * Created by YPY Global on 5/21/17.
 */

public class YPYStreamService extends Service implements IYPYStreamConstants, IStreamFocusableListener {

    public static final String TAG = "DCM";
    public static final String ANDROID8_CHANNEL_ONE_NAME = "XRadioChannel";
    public static final int STATE_PREPARING = 1;
    public static final int STATE_PLAYING = 2;
    public static final int STATE_PAUSE = 3;
    public static final int STATE_STOP = 4;
    public static final int STATE_ERROR = 5;
    public static final int STATE_COMPLETE = 6;
    public static final int STATE_CONNECTION_LOST = 7;

    private int mCurrentState = STATE_STOP;

    private static final float MAX_VOLUME = 1f;
    public static final float DUCK_VOLUME = 0.1f;

    private AudioFocusHelper mAudioFocusHelper;
    private RadioModel mCurrentTrack;
    private boolean isStartLoading;

    private final Handler mHandlerSleep = new Handler();
    private int mMinuteCount;
    private boolean isPauseFromUser;

    private enum AudioFocus {
        NO_FOCUS_NO_DUCK, // we don't have audio focus, and can't duck
        NO_FOCUS_CAN_DUCK, // we don't have focus, but can play at a low volume
        FOCUSED // we have full audio focus
    }

    private AudioFocus mAudioFocus = AudioFocus.NO_FOCUS_NO_DUCK;

    private YPYMediaPlayer mRadioMediaPlayer;
    private NotificationManager mNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mAudioFocusHelper = new AudioFocusHelper(this.getApplicationContext(), this);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (!TextUtils.isEmpty(action)) {
                String packageName = getPackageName();
                if (action.equalsIgnoreCase(packageName + ACTION_TOGGLE_PLAYBACK)) {
                    if (mCurrentState == STATE_COMPLETE || mCurrentState == STATE_CONNECTION_LOST) {
                        startSleepMode();
                        onActionPlay();
                    }
                    else {
                        isPauseFromUser = mCurrentState == STATE_PLAYING;
                        onActionTogglePlay();
                    }
                }
                else if (action.equalsIgnoreCase(packageName + ACTION_PLAY)) {
                    setUpNotification();
                    startSleepMode();
                    onActionPlay();
                }
                else if (action.equalsIgnoreCase(packageName + ACTION_NEXT)) {
                    setUpNotification();
                    onActionNext();
                }
                else if (action.equalsIgnoreCase(packageName + ACTION_PREVIOUS)) {
                    setUpNotification();
                    onActionPrevious();
                }
                else if (action.equalsIgnoreCase(packageName + ACTION_STOP)) {
                    setUpNotification();
                    onActionStop();
                }
                else if (action.equals(packageName + ACTION_UPDATE_SLEEP_MODE)) {
                    startSleepMode();
                }
                else if (action.equals(packageName + ACTION_CONNECTION_LOST)) {
                    mCurrentState = STATE_CONNECTION_LOST;
                    onActionComplete();
                    sendMusicBroadcast(ACTION_CONNECTION_LOST);
                }
            }
        }
        return START_NOT_STICKY;
    }

    private void startSleepMode() {
        try {
            int minute = XRadioSettingManager.getSleepMode(this);
            mHandlerSleep.removeCallbacksAndMessages(null);
            if (minute > 0) {
                this.mMinuteCount = minute * ONE_MINUTE;
                startCountSleep();
            }
            else {
                sendMusicBroadcast(ACTION_UPDATE_SLEEP_MODE, 0);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void startCountSleep() {
        try {
            if (mMinuteCount > 0) {
                mHandlerSleep.postDelayed(() -> {
                    mMinuteCount = mMinuteCount - 1000;
                    sendMusicBroadcast(ACTION_UPDATE_SLEEP_MODE, mMinuteCount);
                    if (mMinuteCount <= 0) {
                        onActionStop();
                    }
                    else {
                        startCountSleep();
                    }
                }, 1000);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void onActionStop() {
        isStartLoading = false;
        isPauseFromUser = false;
        boolean isError = mCurrentState == STATE_ERROR;
        try {
            releaseData();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (isError) {
            sendMusicBroadcast(ACTION_ERROR);
        }
        else {
            sendMusicBroadcast(ACTION_STOP);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void onActionPrevious() {
        try {
            isPauseFromUser = false;
            mCurrentTrack = YPYStreamManager.getInstance().prevPlay();
            if (mCurrentTrack != null) {
                startPlayNewSong();
            }
            else {
                mCurrentState = STATE_ERROR;
                onActionStop();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void onActionNext() {
        try {
            isPauseFromUser = false;
            mCurrentTrack = YPYStreamManager.getInstance().nextPlay();
            if (mCurrentTrack != null) {
                startPlayNewSong();
            }
            else {
                mCurrentState = STATE_ERROR;
                onActionStop();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void onActionComplete() {
        isStartLoading = false;
        try {
            if (mRadioMediaPlayer != null) {
                releaseMedia(false);
            }
            setUpNotification();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void onActionPlay() {
        isPauseFromUser = false;
        processPlayRequest(true);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void onActionTogglePlay() {
        try {
            if (mCurrentState == STATE_PAUSE || mCurrentState == STATE_STOP) {
                processPlayRequest(false);
            }
            else {
                processPauseRequest();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void processPauseRequest() {
        if (mCurrentTrack == null || mRadioMediaPlayer == null) {
            mCurrentState = STATE_ERROR;
            onActionStop();
            return;
        }
        try {
            if (mCurrentState == STATE_PLAYING) {
                if(IS_MUSIC_PLAYER){
                    mCurrentState = STATE_PAUSE;
                    mRadioMediaPlayer.pause();
                }
                else{
                    mCurrentState = STATE_COMPLETE;
                    mRadioMediaPlayer.stop();
                }
                setUpNotification();
                sendMusicBroadcast(ACTION_PAUSE);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            onActionNext();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void processPlayRequest(boolean isForces) {
        mCurrentTrack = YPYStreamManager.getInstance().getCurrentRadio();
        if (mCurrentTrack == null) {
            mCurrentState = STATE_ERROR;
            onActionStop();
            return;
        }
        if (mCurrentState == STATE_STOP || mCurrentState == STATE_PLAYING || isForces) {
            startPlayNewSong();
            sendMusicBroadcast(ACTION_NEXT);
        }
        else if (mCurrentState == STATE_PAUSE) {
            mCurrentState = STATE_PLAYING;
            tryToGetAudioFocus();
            configAndStartRadioMediaPlayer();
            setUpNotification();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private synchronized void startPlayNewSong() {
        tryToGetAudioFocus();
        if (!isStartLoading) {
            mCurrentState = STATE_STOP;
            isStartLoading = true;
            if (mCurrentTrack == null) {
                mCurrentState = STATE_ERROR;
                onActionStop();
                return;
            }
            if (mRadioMediaPlayer != null) {
                releaseMedia(true);
            }
            startStreamMusic();
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private synchronized void startStreamMusic() {
        if (mCurrentTrack != null) {
            releaseMedia(true);
            sendMusicBroadcast(ACTION_LOADING);
            setUpNotification();
            YPYStreamManager.getInstance().setLoading(true);
            YPYExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
                final String uriStream = mCurrentTrack.getLinkRadio(this);
                Log.e("DCM", "========>uriStream=" + uriStream);
                YPYExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                    setUpMediaForStream(uriStream);
                    isStartLoading = false;
                });

            });
        }
    }

    private void setUpMediaForStream(final String path) {
        createRadioMediaPlayer();
        try {
            if (mRadioMediaPlayer != null) {
                mCurrentState = STATE_PREPARING;
                mRadioMediaPlayer.setDataSource(path);

            }
        }
        catch (Exception ex) {
            Log.d(TAG, "IOException playing next song: " + ex.getMessage());
            ex.printStackTrace();
            onActionStop();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseData();
        onGiveUpAudioFocus();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onGrantAudioFocus() {
        try {
            tryToGetAudioFocus();
            if (mCurrentState == STATE_PAUSE && !isPauseFromUser) {
                configAndStartRadioMediaPlayer();
                setUpNotification();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onGiveUpAudioFocus() {
        try {
            if (mAudioFocusHelper != null) {
                mAudioFocusHelper.abandonFocus();
            }
            mAudioFocus = AudioFocus.NO_FOCUS_NO_DUCK;
            if (mRadioMediaPlayer != null && mRadioMediaPlayer.isPlaying()) {
                onActionTogglePlay();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onLostAudioFocus(boolean canDuck) {
        try {
            mAudioFocus = canDuck ? AudioFocus.NO_FOCUS_CAN_DUCK : AudioFocus.NO_FOCUS_NO_DUCK;
            if (mRadioMediaPlayer != null && mRadioMediaPlayer.isPlaying()) {
                configAndStartRadioMediaPlayer();
            }

        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void tryToGetAudioFocus() {
        try {
            if (mAudioFocus != null && mAudioFocus != AudioFocus.FOCUSED
                    && mAudioFocusHelper != null
                    && mAudioFocusHelper.requestFocus())
                mAudioFocus = AudioFocus.FOCUSED;
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void configAndStartRadioMediaPlayer() {
        try {
            if (mRadioMediaPlayer != null && (mCurrentState == STATE_PLAYING || mCurrentState == STATE_PAUSE)) {
                if (mAudioFocus == AudioFocus.NO_FOCUS_NO_DUCK) {
                    if (mRadioMediaPlayer.isPlaying()) {
                        onActionTogglePlay();
                    }
                    return;
                }
                else if (mAudioFocus == AudioFocus.NO_FOCUS_CAN_DUCK) {
                    mRadioMediaPlayer.setVolume(DUCK_VOLUME);
                }
                else {
                    mRadioMediaPlayer.setVolume(MAX_VOLUME);
                }
                if (!mRadioMediaPlayer.isPlaying()) {
                    mRadioMediaPlayer.start();
                }
                sendMusicBroadcast(ACTION_PLAY);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void sendMusicBroadcast(String action) {
        sendMusicBroadcast(action, -1);
    }

    private void sendMusicBroadcast(String action, long value) {
        try {
            Intent mIntent = new Intent(getPackageName() + ACTION_BROADCAST_PLAYER);
            mIntent.putExtra(KEY_ACTION, action);
            if (value != -1) {
                mIntent.putExtra(KEY_VALUE, value);
            }
            sendBroadcast(mIntent);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendMusicBroadcast(String action, String value) {
        try {
            Intent mIntent = new Intent(getPackageName() + ACTION_BROADCAST_PLAYER);
            mIntent.putExtra(KEY_ACTION, action);
            mIntent.putExtra(KEY_VALUE, value);
            sendBroadcast(mIntent);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isSupportRTL() {
        try {
            return TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == ViewCompat.LAYOUT_DIRECTION_RTL;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setUpNotification() {
        try {
            boolean isSingle = TotalDataManager.getInstance(getApplicationContext()).isSingleRadio();
            String packageName = getPackageName();
            PendingIntent pi;
            if (isSingle) {
                Intent mIntent = new Intent(this.getApplicationContext(), XSingleRadioMainActivity.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                pi = PendingIntent.getActivity(getApplicationContext(), NOTIFICATION_ID, mIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);

            }
            else {
                Intent mIntent = new Intent(this.getApplicationContext(), XMultiRadioMainActivity.class);
                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                pi = PendingIntent.getActivity(getApplicationContext(), NOTIFICATION_ID, mIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);
            }

            String CHANNEL_ONE_ID = getPackageName() + ".N2";
            String CHANNEL_ONE_NAME = getPackageName() + ANDROID8_CHANNEL_ONE_NAME;
            if (IOUtils.hasAndroid80()) {
                try {
                    NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID,
                            CHANNEL_ONE_NAME, NotificationManager.IMPORTANCE_LOW);
                    notificationChannel.enableLights(true);
                    notificationChannel.setLightColor(Color.RED);
                    notificationChannel.setShowBadge(true);
                    notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
                    mNotificationManager.createNotificationChannel(notificationChannel);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ONE_ID);
            mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
            mBuilder.setSmallIcon(R.drawable.ic_notification_24dp);
            mBuilder.setColor(getResources().getColor(R.color.color_noti_background));
            mBuilder.setShowWhen(false);

            Intent nextIntent = new Intent(this, YPYIntentReceiver.class);
            nextIntent.setAction(packageName + ACTION_NEXT);
            PendingIntent pendingNextIntent = PendingIntent.getBroadcast(this, 100, nextIntent,PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            Intent stopIntent = new Intent(this, YPYIntentReceiver.class);
            stopIntent.setAction(packageName + ACTION_STOP);
            PendingIntent stopPendingIntent = PendingIntent.getBroadcast(this, 100, stopIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            Intent toggleIntent = new Intent(this, YPYIntentReceiver.class);
            toggleIntent.setAction(packageName + ACTION_TOGGLE_PLAYBACK);
            PendingIntent pendingToggleIntent = PendingIntent.getBroadcast(this, 100, toggleIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            int resId = isSingle ? R.layout.item_single_notification_music : R.layout.item_multi_notification_music;

            RemoteViews notificationView = new RemoteViews(getPackageName(), resId);
            notificationView.setOnClickPendingIntent(R.id.btn_stop, stopPendingIntent);
            if (!isSingle) {
                notificationView.setOnClickPendingIntent(R.id.btn_next, pendingNextIntent);
            }
            notificationView.setOnClickPendingIntent(R.id.btn_play, pendingToggleIntent);
            String data = mCurrentTrack != null ? mCurrentTrack.getName() : getString(R.string.app_name);
            notificationView.setTextViewText(R.id.tv_radio_name, data);
            if (!isSingle && isSupportRTL()) {
                notificationView.setImageViewResource(R.id.btn_next, R.drawable.ic_skip_previous_white_36dp);
            }

            String info = mCurrentTrack != null ? mCurrentTrack.getTags() : getString(R.string.title_unknown);
            if (mCurrentTrack != null && !TextUtils.isEmpty(mCurrentTrack.getSong())) {
                info = mCurrentTrack.getMetaData();
            }
            if (TextUtils.isEmpty(info)) {
                info = getString(R.string.title_unknown);
            }
            notificationView.setTextViewText(R.id.tv_info, info);

            boolean isPlay = YPYStreamManager.getInstance().isPlaying();
            if (isPlay) {
                notificationView.setImageViewResource(R.id.btn_play, R.drawable.ic_pause_white_36dp);
            }
            else {
                notificationView.setImageViewResource(R.id.btn_play, R.drawable.ic_play_arrow_white_36dp);
            }
            mBuilder.setCustomContentView(notificationView);
            mBuilder.setPriority(NotificationCompat.PRIORITY_DEFAULT);

            Notification mNotification = mBuilder.build();
            mNotification.contentIntent = pi;
            mNotification.flags |= Notification.FLAG_NO_CLEAR;
            startForeground(NOTIFICATION_ID, mNotification);

        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }


    private void createRadioMediaPlayer() {
        try {
            String userAgent = mCurrentTrack != null ? mCurrentTrack.getUserAgentRadio() : null;
            mRadioMediaPlayer = new YPYMediaPlayer(this, userAgent);
            mRadioMediaPlayer.setOnStreamListener(new YPYMediaPlayer.OnStreamListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onPrepare() {
                    sendMusicBroadcast(ACTION_DIMINISH_LOADING);
                    mCurrentState = STATE_PLAYING;
                    YPYStreamManager.getInstance().setLoading(false);
                    configAndStartRadioMediaPlayer();
                    setUpNotification();
                }

                @Override
                public void onError() {
                    try {
                        YPYStreamManager.getInstance().setLoading(false);
                        sendMusicBroadcast(ACTION_DIMINISH_LOADING);
                        mCurrentState = STATE_ERROR;
                        onActionStop();
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onComplete() {
                    if (AUTO_NEXT_WHEN_COMPLETE) {
                        mCurrentState = STATE_STOP;
                        onActionNext();
                        sendMusicBroadcast(ACTION_NEXT);
                    }
                    else {
                        mCurrentState = STATE_COMPLETE;
                        onActionComplete();
                        sendMusicBroadcast(ACTION_COMPLETE);
                    }

                }

//                @Override
//                public void onBuffering(long percent) {
//                    mCurrentState = STATE_PREPARING;
//                    sendMusicBroadcast(ACTION_BUFFERING, percent);
//                }

                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onUpdateMetaData(YPYMediaPlayer.StreamInfo info) {
                    if (!IS_MUSIC_PLAYER) {
                        YPYStreamManager.getInstance().setStreamInfo(info);
                        if (info != null) {
                            String title = info.title;
                            String artist = info.artist;
                            if (mCurrentTrack != null) {
                                mCurrentTrack.setSong(title);
                                mCurrentTrack.setArtist(artist);
                            }
                            sendMusicBroadcast(ACTION_UPDATE_INFO);
                            setUpNotification();
                            startGetImageOfSong(title, artist, info);
                        }
                        else {
                            if (mCurrentTrack != null) {
                                mCurrentTrack.setSong(null);
                                mCurrentTrack.setArtist(null);
                            }
                            setUpNotification();
                            sendMusicBroadcast(ACTION_RESET_INFO);
                        }
                    }

                }
            });
            YPYStreamManager.getInstance().setRadioMediaPlayer(mRadioMediaPlayer);
        }
        catch (Exception e) {
            e.printStackTrace();
            mCurrentState = STATE_ERROR;
            onActionStop();
        }

    }


    private void releaseData() {
        mHandlerSleep.removeCallbacksAndMessages(null);
        releaseMedia(true);
        try {
            stopForeground(true);
            stopSelf();
            YPYStreamManager.getInstance().onDestroy();
        }
        catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void startGetImageOfSong(String title, String artist, YPYMediaPlayer.StreamInfo mStreamInfo) {
        if (ApplicationUtils.isOnline(this) && mStreamInfo != null && (!TextUtils.isEmpty(title) || !TextUtils.isEmpty(artist))) {
            YPYExecutorSupplier.getInstance().forBackgroundTasks().execute(() -> {
                String lastFmKey = TotalDataManager.getInstance(getApplicationContext()).getLastFmKey();
                String url = XRadioNetUtils.getImageOfSong(title, artist, lastFmKey);
                Log.e("DCM", "=====>startGetImageOfSong=" + url);
                mStreamInfo.imgUrl = !TextUtils.isEmpty(url) ? url : "";
                sendMusicBroadcast(ACTION_UPDATE_COVER_ART, url);
            });
        }

    }

    private void releaseMedia(boolean isNeedResetState) {
        try {
            if (mRadioMediaPlayer != null) {
                mRadioMediaPlayer.release();
                YPYStreamManager.getInstance().onResetMedia();
                mRadioMediaPlayer = null;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        if (isNeedResetState) {
            mCurrentState = STATE_STOP;
        }

    }


}
