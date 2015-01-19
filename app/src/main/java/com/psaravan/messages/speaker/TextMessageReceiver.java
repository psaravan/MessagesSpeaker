package com.psaravan.messages.speaker;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;

/**
 * BroadcastReceiver for text messages.
 *
 * @author Saravan Pantham
 */
public class TextMessageReceiver extends BroadcastReceiver {

    private Context mContext;
    AudioManager mAudioManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        mAudioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        // Check if the user only wants messages to be spoken when a headset is plugged in.
        boolean headsetOnly = LocalApp.getSharedPreferences().getBoolean(LocalApp.SPEAK_ONLY_ON_HEADSET, true);
        if (headsetOnly && !mAudioManager.isWiredHeadsetOn()) {
            return;
        }

        // Check if the user only wants messages to be spoken when the device is locked.
        KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
        boolean isLocked = km.inKeyguardRestrictedInputMode();

        if (!isLocked && LocalApp.getSharedPreferences().getBoolean(LocalApp.SPEAK_ONLY_WHEN_LOCKED, true)) {
            return;
        }

        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {

            // Get the text message data that was received.
            Bundle bundle = intent.getExtras();

            // Grab the messages from the bundle.
            SmsMessage[] messages;
            String sender = null;

            // Extract the messages and the sender from the raw message data.
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                messages = new SmsMessage[pdus.length];
                String previousSender = sender;
                for (int i = 0; i < messages.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    previousSender = sender;
                    sender = messages[i].getOriginatingAddress();
                    String messageBody = messages[i].getMessageBody();

                    if (messages.length==1)
                        speakMessage(sender, messageBody, true);
                    else
                        if (i > 0 && !previousSender.equals(sender))
                            speakMessage(sender, messageBody, true);
                        else if (i > 0 && previousSender.equals(sender))
                            speakMessage(sender, messageBody, false);
                        else
                            speakMessage(sender, messageBody, true);
                }
            }
        }
    }

    public void speakMessage(String sender, String messageBody, boolean newMessage) {
        String text1 = "";
        String text2 = "";
        if (newMessage) {
            text1 += mContext.getString(R.string.new_message_from) + " " + getContactName(sender);
            text2 += messageBody + ".";
        } else {
            text1 += messageBody + ".";
            text2 = null;
        }

        LocalApp.getForegroundService().getSpeaker().speak(text1, text2, null);
    }

    private String getContactName(String phone){
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        String projection[] = new String[]{ContactsContract.Data.DISPLAY_NAME};

        Cursor cursor = mContext.getContentResolver().query(uri, projection, null, null, null);
        if(cursor.moveToFirst()){
            return cursor.getString(0);
        } else {
            return "Unknown sender";
        }
    }
}
