package com.psaravan.notification.speaker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.telephony.SmsMessage;
import android.util.Log;

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

        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {

            // Get the text message data that was received.
            Bundle bundle = intent.getExtras();

            // Grab the messages from the bundle.
            SmsMessage[] messages;
            String sender;

            // Extract the messages and the sender from the raw message data.
            if (bundle != null) {
                Object[] pdus = (Object[]) bundle.get("pdus");
                messages = new SmsMessage[pdus.length];
                for (int i = 0; i < messages.length; i++) {
                    messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                    sender = messages[i].getOriginatingAddress();
                    String messageBody = messages[i].getMessageBody();
                    speakMessage(sender, messageBody);
                }
            }
        }
    }

    public void speakMessage(String sender, String messageBody) {

        if (mAudioManager.isWiredHeadsetOn()==false)
            return;

        String text1 = mContext.getString(R.string.new_message_from) + " " + getContactName(sender) + ".";
        String text2 = messageBody + ".";

        if (LocalApp.getForegroundService()!=null)
            LocalApp.getForegroundService().getNotificationSpeaker().speak(text1, text2, null);
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
