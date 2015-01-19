package com.psaravan.messages.speaker;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

/**
 * Service that provides the TTS service.
 *
 * @author Saravan Pantham
 */
public class SpeakerService extends Service {

    private static final int NOTIFICATION_ID = 24324; //Some random int for the id.
    private TextMessageReceiver mTextMessageReceiver;
    private Speaker mSpeaker;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Set the service as a foreground service.
        LocalApp.setForegroundService(this);
        startForeground();

        //Initialize the TTS Engine.
        mSpeaker = new Speaker(this);

        // Register the text messages receiver.
        mTextMessageReceiver = new TextMessageReceiver();
        IntentFilter textMessagesFilter = new IntentFilter();
        textMessagesFilter.addAction("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(mTextMessageReceiver, textMessagesFilter);

        return START_STICKY;
    }

    private void startForeground() {
        Intent showTaskIntent = new Intent(getApplicationContext(), MainActivity.class);
        showTaskIntent.setAction(Intent.ACTION_MAIN);
        showTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        showTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(getApplicationContext(),
                                                                0,
                                                                showTaskIntent,
                                                                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setContentTitle(getString(R.string.fg_notif_title))
               .setContentText(getString(R.string.fg_notif_content))
               .setSmallIcon(R.drawable.ic_launcher)
               .setWhen(System.currentTimeMillis())
               .setPriority(NotificationCompat.PRIORITY_MIN)
               .setContentIntent(contentIntent);

        startForeground(NOTIFICATION_ID, builder.build());
    }

    public Speaker getSpeaker() {
        return mSpeaker;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, R.string.service_destroyed, Toast.LENGTH_LONG).show();
        LocalApp.setForegroundService(null);
        unregisterReceiver(mTextMessageReceiver);
    }
}
