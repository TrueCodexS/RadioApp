package com.radio.kutai.firebasemsg;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import com.firebase.jobdispatcher.FirebaseJobDispatcher;
import com.firebase.jobdispatcher.GooglePlayDriver;
import com.firebase.jobdispatcher.Job;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.radio.kutai.R;
import com.radio.kutai.XRadioSplashActivity;
import com.radio.kutai.ypylibs.utils.YPYLog;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;


/**
 * @author:YPY Global
 * @Email: bl911vn@gmail.com
 * @Website: http://radio.com
 * Created by YPY Global on 6/9/18.
 */
public class XRadioFBMessagingService extends FirebaseMessagingService {
    private static final String TAG = "XRadioFBMessagingService";

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        try{
            // TODO(developer): Handle FCM messages here.
            YPYLog.d(TAG, "From: " + remoteMessage.getFrom());

            // Check if message contains a data payload.
            if (remoteMessage.getData().size() > 0) {
                YPYLog.d(TAG, "Message data payload: " + remoteMessage.getData());
                scheduleJob();
            }
            // Check if message contains a notification payload.
            if (remoteMessage.getNotification() != null) {
                YPYLog.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
                sendNotification(remoteMessage.getNotification().getBody());
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Schedule a job using FirebaseJobDispatcher.
     */

    private void scheduleJob() {
        // [START dispatch_job]
        FirebaseJobDispatcher dispatcher = new FirebaseJobDispatcher(new GooglePlayDriver(this));
        Job myJob = dispatcher.newJobBuilder()
                .setService(XRadioJobService.class)
                .setTag(getPackageName()+TAG)
                .build();
        dispatcher.schedule(myJob);
    }


    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void sendNotification(String messageBody) {
        try{
            if(TextUtils.isEmpty(messageBody)) return;

            Intent intent = new Intent(this, XRadioSplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

            String channelId = getPackageName()+".N3";
            Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder =
                    new NotificationCompat.Builder(this, channelId)
                            .setSmallIcon(R.drawable.ic_notification_24dp)
                            .setContentTitle(getString(R.string.app_name))
                            .setContentText(messageBody)
                            .setAutoCancel(true)
                            .setSound(defaultSoundUri)
                            .setContentIntent(pendingIntent);

            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        getPackageName()+"_CHANNEL1",
                        NotificationManager.IMPORTANCE_HIGH);
                if (notificationManager != null) {
                    notificationManager.createNotificationChannel(channel);
                }
            }
            Notification mNotification = notificationBuilder.build();
            if (notificationManager != null) {
                notificationManager.notify(0 , mNotification);
            }
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }
}
