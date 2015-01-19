package com.psaravan.notification.speaker;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

/**
 * Application context class for singleton objects/shared methods.
 *
 * @author Saravan Pantham
 */
public class LocalApp extends Application {

    private static Context mContext;
    private static NotificationSpeakerService mNotificationSpeakerService;
    private static SharedPreferences mSharedPreferences;

    // SharedPreferences constants/keys.
    public static final String SPEAK_ONLY_ON_HEADSET = "SPEAK_ONLY_ON_HEADSET";

    public void onCreate(){
        super.onCreate();
        mContext = getApplicationContext();
        mSharedPreferences = getSharedPreferences(this.getPackageName(), Context.MODE_PRIVATE);
    }

    public static Context getContext() {
        return mContext;
    }

    public static NotificationSpeakerService getForegroundService() {
        return mNotificationSpeakerService;
    }

    public static void startForegroundService() {
        Intent intent = new Intent(getContext(), NotificationSpeakerService.class);
        getContext().startService(intent);
    }

    public static void setForegroundService(NotificationSpeakerService service) {
        mNotificationSpeakerService = service;
    }

    public static SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

}
