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

package com.radio.kutai.stream.mediaplayer;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ts.DefaultTsPayloadReaderFactory;
import com.google.android.exoplayer2.metadata.Metadata;
import com.google.android.exoplayer2.metadata.MetadataOutput;
import com.google.android.exoplayer2.metadata.icy.IcyHeaders;
import com.google.android.exoplayer2.metadata.icy.IcyInfo;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.DefaultHlsExtractorFactory;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.radio.kutai.ypylibs.executor.YPYExecutorSupplier;
import com.radio.kutai.ypylibs.utils.YPYLog;

import androidx.annotation.NonNull;


/**
 * @author:YPY Global
 * @Email: baodt@hanet.com
 * @Website: http://hanet.com/
 * @Project: cyberfm
 * Created by YPY Global on 5/21/17.
 */

public class YPYMediaPlayer {

    private final Context mContext;

    private OnStreamListener onStreamListener;
    private MetadataOutput mMetadataOutput;

    private boolean isPrepared;
    private SimpleExoPlayer mAudioPlayer;
    private final String userAgent;

    public YPYMediaPlayer(Context mContext, String mUserAgent) {
        this.mContext = mContext;
        this.userAgent = mUserAgent;
    }

    public void release() {
        isPrepared = false;
        if (mAudioPlayer != null) {
            if (mMetadataOutput != null) {
                mAudioPlayer.removeMetadataOutput(mMetadataOutput);
                mMetadataOutput = null;
            }
            mAudioPlayer.release();
            mAudioPlayer = null;
        }

    }

    public void setVolume(float volume) {
        try {
            if (mAudioPlayer != null) {
                mAudioPlayer.setVolume(volume);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOnStreamListener(OnStreamListener onStreamListener) {
        this.onStreamListener = onStreamListener;
    }


    public void setDataSource(String url) {
        if (!TextUtils.isEmpty(url)) {
            String mUrlStream = url;

            AdaptiveTrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory();
            DefaultTrackSelector trackSelector = new DefaultTrackSelector(mContext, trackSelectionFactory);
            SimpleExoPlayer.Builder mAudioBuilder = new SimpleExoPlayer.Builder(mContext);
            mAudioBuilder.setTrackSelector(trackSelector);
            mAudioPlayer = mAudioBuilder.build();

            mAudioPlayer.addListener(new Player.EventListener() {
                @Override
                public void onTracksChanged(@NonNull TrackGroupArray trackGroups, @NonNull TrackSelectionArray trackSelections) {

                }

                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    if (playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE) {
                        if (onStreamListener != null) {
                            onStreamListener.onComplete();
                        }
                    }
                    else if (playbackState == Player.STATE_READY) {
                        if (onStreamListener != null && !isPrepared) {
                            isPrepared = true;
                            onStreamListener.onPrepare();
                        }
                    }
                }

                @Override
                public void onRepeatModeChanged(int repeatMode) {

                }

                @Override
                public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

                }

                @Override
                public void onPlayerError(@NonNull ExoPlaybackException error) {
                    if (onStreamListener != null) {
                        onStreamListener.onError();
                    }
                }

                @Override
                public void onPositionDiscontinuity(int reason) {
                }

                @Override
                public void onPlaybackParametersChanged(@NonNull PlaybackParameters playbackParameters) {

                }
            });
            DataSource.Factory dataSourceFactory;
            MediaSource mediaSource;

            if (mUrlStream.endsWith("_Other")) {
                mUrlStream = mUrlStream.replace("_Other", "");
            }

            //allow support redirect
            DefaultHttpDataSource.Factory mHttpFact = new DefaultHttpDataSource.Factory();
            mHttpFact.setUserAgent(getUserAgent(mContext));
            mHttpFact.setAllowCrossProtocolRedirects(true);
            mHttpFact.setTransferListener(null);
            dataSourceFactory = new DefaultDataSourceFactory(mContext, mHttpFact);

            MediaItem mMediaItem = MediaItem.fromUri(Uri.parse(mUrlStream));
            YPYLog.e("DCM", "======>start stream url stream=" + mUrlStream);
            if (mUrlStream.endsWith(".m3u8") || mUrlStream.endsWith(".M3U8")) {
                mediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                        .setAllowChunklessPreparation(false)
                        .setExtractorFactory(
                                new DefaultHlsExtractorFactory(
                                        DefaultTsPayloadReaderFactory.FLAG_IGNORE_H264_STREAM, false))
                        .createMediaSource(mMediaItem);
            }
            else {
                mediaSource = new ProgressiveMediaSource.Factory(dataSourceFactory, new DefaultExtractorsFactory())
                        .createMediaSource(mMediaItem);
            }
            mAudioPlayer.setMediaSource(mediaSource);
            mAudioPlayer.prepare();

            mMetadataOutput = metadata -> {
                if (metadata.length() > 0) {
                    try {
                        int size = metadata.length();
                        for (int i = 0; i < size; i++) {
                            Metadata.Entry mEntry = metadata.get(i);
                            if (mEntry instanceof IcyInfo) {
                                processMetadata((((IcyInfo) mEntry).title));
                                break;
                            }
                            else if (mEntry instanceof IcyHeaders) {
                                processMetadata(((IcyHeaders) mEntry).name);
                                break;
                            }

                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };
            mAudioPlayer.addMetadataOutput(mMetadataOutput);
            start();
            return;

        }
        if (onStreamListener != null) {
            onStreamListener.onError();
        }
    }

    private void processMetadata(String title) {
        try {
            StreamInfo mStreamInfo = new StreamInfo();
            if (!TextUtils.isEmpty(title)) {
                String[] metadata = title.split(" - ");
                if (metadata.length > 0) {
                    if (metadata.length == 3) {
                        mStreamInfo.artist = metadata[1];
                        mStreamInfo.title = metadata[2];
                    }
                    else if (metadata.length == 2) {
                        mStreamInfo.artist = metadata[0];
                        mStreamInfo.title = metadata[1];
                    }
                    else {
                        mStreamInfo.title = metadata[0];
                    }
                }
            }
            YPYExecutorSupplier.getInstance().forMainThreadTasks().execute(() -> {
                if (onStreamListener != null) {
                    onStreamListener.onUpdateMetaData(mStreamInfo);
                }
            });
        }
        catch (Exception e) {
            e.printStackTrace();
        }


    }


    public void start() {
        try {
            if (mAudioPlayer != null) {
                mAudioPlayer.setPlayWhenReady(true);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void stop() {
        try {
            if (mAudioPlayer != null) {
                mAudioPlayer.stop();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void pause() {
        try {
            if (mAudioPlayer != null) {
                mAudioPlayer.setPlayWhenReady(false);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

    }

    public boolean isPlaying() {
        try {
            return mAudioPlayer != null && mAudioPlayer.getPlayWhenReady();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private String getUserAgent(Context mContext) {
        if (!TextUtils.isEmpty(userAgent)) {
            return userAgent;
        }
        return Util.getUserAgent(mContext, getClass().getSimpleName());
    }


    public interface OnStreamListener {
        void onPrepare();

        void onError();

        void onComplete();

        void onUpdateMetaData(StreamInfo info);
    }

    public static class StreamInfo {
        public String title;
        public String artist;
        public String imgUrl;
    }


}
