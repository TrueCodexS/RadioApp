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

package com.radio.kutai.stream.audiofocus;

import android.content.Context;
import android.media.AudioManager;

public class AudioFocusHelper implements AudioManager.OnAudioFocusChangeListener {

	private AudioManager mAM;
	private IStreamFocusableListener mFocusable;

	public AudioFocusHelper(Context ctx, IStreamFocusableListener focusable) {
		mAM = (AudioManager) ctx.getSystemService(Context.AUDIO_SERVICE);
		mFocusable = focusable;
	}

	/** Requests audio focus. Returns whether request was successful or not. */
	public boolean requestFocus() {
		final int result = mAM.requestAudioFocus(this,
				AudioManager.STREAM_MUSIC,
				AudioManager.AUDIOFOCUS_GAIN);
		return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
	}

	/** Abandons audio focus. Returns whether request was successful or not. */
	public void abandonFocus() {
		mAM.abandonAudioFocus(this);
	}

	/**
	 * Called by AudioManager on audio focus changes. We implement this by
	 * calling our MusicFocusable appropriately to relay the message.
	 */
	@Override
	public void onAudioFocusChange(int focusChange) {
		try{
			if (mFocusable == null){
				return;
			}
			switch (focusChange) {
				case AudioManager.AUDIOFOCUS_GAIN:
					mFocusable.onGrantAudioFocus();
					break;
				case AudioManager.AUDIOFOCUS_LOSS:
					mFocusable.onGiveUpAudioFocus();
					break;
				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
					mFocusable.onLostAudioFocus(false);
					break;
				case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
					mFocusable.onLostAudioFocus(true);
					break;
				default:
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}

	}
}
