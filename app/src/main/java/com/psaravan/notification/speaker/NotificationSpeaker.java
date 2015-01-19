package com.psaravan.notification.speaker;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Locale;
import java.util.UUID;

/**
 * Sets up the Text to Speech engine and allows
 *
 * @author Saravan Pantham
 */
public class NotificationSpeaker implements TextToSpeech.OnInitListener {

    private Context mContext;
    private TextToSpeech mTTS;
    private AudioManager mAudioManager;

    private String mText1 = null;
    private String mText2 = null;
    private String mText3 = null;

    public NotificationSpeaker(Context context) {
        mContext = context;
        mTTS = new TextToSpeech(mContext, this);
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        // Fire off an intent to check if a TTS engine is installed
        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        checkIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(checkIntent);

    }

    /**
     * Initiates the speaking process by requesting AudioFocus. If focus is received,
     * the internal speak() method is called (see below). If focus is not received, the
     * attempt to speak notifications is abandoned.
     */
    public void speak(final String text1, final String text2, final String text3) {
        mText1 = text1;
        mText2 = text2;
        mText3 = text3;

        mAudioManager.requestAudioFocus(audioFocusListener,
                                        AudioManager.STREAM_MUSIC,
                                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK);
    }

    /**
     * Speaks the strings that are passed in as arguments (with pauses in between).
     * This is a private, internal method that is only called if AudioFocus has been
     * received.
     */
    private void speak(String text1, String text2, String text3, boolean dummy) {
        mText1 = text1;
        mText2 = text2;
        mText3 = text3;

        if (Build.VERSION.SDK_INT==Build.VERSION_CODES.LOLLIPOP) {
            mTTS.playSilentUtterance(1500, TextToSpeech.QUEUE_ADD, UUID.randomUUID().toString());
            mTTS.speak(text1, TextToSpeech.QUEUE_ADD, null, UUID.randomUUID().toString());
            mTTS.playSilentUtterance(700, TextToSpeech.QUEUE_ADD, UUID.randomUUID().toString());

            if (text2!=null) {
                mTTS.speak(text2, TextToSpeech.QUEUE_ADD, null, UUID.randomUUID().toString());
                mTTS.playSilentUtterance(700, TextToSpeech.QUEUE_ADD, UUID.randomUUID().toString());
            }

            if (text3!=null) {
                mTTS.speak(text3, TextToSpeech.QUEUE_ADD, null, UUID.randomUUID().toString());
                mTTS.playSilentUtterance(700, TextToSpeech.QUEUE_ADD, UUID.randomUUID().toString());
            }

        } else {
            HashMap<String, String> hash = new HashMap<>();
            hash.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_NOTIFICATION));
            mTTS.playSilence(1500, TextToSpeech.QUEUE_ADD, hash);
            mTTS.speak(text1, TextToSpeech.QUEUE_ADD, hash);
            mTTS.playSilence(700, TextToSpeech.QUEUE_ADD, hash);

            if (text2!=null) {
                mTTS.speak(text2, TextToSpeech.QUEUE_ADD, hash);
                mTTS.playSilence(700, TextToSpeech.QUEUE_ADD, hash);
            }

            if (text3!=null) {
                mTTS.speak(text3, TextToSpeech.QUEUE_ADD, hash);
                mTTS.playSilence(700, TextToSpeech.QUEUE_ADD, hash);
            }
        }

        mAudioManager.abandonAudioFocus(audioFocusListener);
    }

    private AudioManager.OnAudioFocusChangeListener audioFocusListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange==AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                Log.e("DEBUG", ">>>>gained!");
                speak(mText1, mText2, mText3, true);
            } else {
                Log.e("DEBUG", ">>>" + focusChange);
            }
        }
    };

    @Override
    public void onInit(int status) {
        if (status==TextToSpeech.SUCCESS) {
            mTTS.setLanguage(Locale.UK);

            if (mText1!=null)
                speak(mText1, mText2, mText3);

        } else {
            mTTS = null;
            Toast.makeText(mContext, "Could not initialize Text-to-Speech engine.", Toast.LENGTH_SHORT).show();
        }
    }
}
