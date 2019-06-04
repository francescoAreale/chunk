package com.chunk.ereafra.chunk;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.chunk.ereafra.chunk.Model.ChatModel.Chat;
import com.chunk.ereafra.chunk.Model.Interface.GetChatFromIDInterface;
import com.chunk.ereafra.chunk.Utils.FirebaseUtils;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService implements GetChatFromIDInterface<Chat> {
    private static final String TAG = "MyFirebaseIIDService";
    private NotificationManager notificationManager = null;
    private static final String NOTIFICATION_ID_EXTRA = "notificationId";
    private static final String IMAGE_URL_EXTRA = "imageUrl";
    private static final String ADMIN_CHANNEL_ID ="admin_channel";
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        //sendRegistrationToServer(token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            FirebaseUtils.getChatFromId(remoteMessage.getData().get("chunk_id"), this);
            Log.d(TAG, "Message data payload: " + remoteMessage.getData().get("chunk_id"));
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    public int makeHash(String stringToHash)
    {
        int hash = 0;
        for (int i = 0; i < stringToHash.length(); i++) {
            hash = (hash << 5) - hash + stringToHash.charAt(i);
        }
        return hash;
    }

    @Override
    public void onChatReceived(final Chat chat) {
        String chatID = chat.getId();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        Boolean enable = prefs.getBoolean("Notification"+chat.getId(),true);
        if(!enable)
            return;
        Log.d(TAG, "chat title: " + ((Chat)chat).getId());
        if(!ChunkChatActivity.lastIdChat.equals(chat.getId())) {
            Intent notificationIntent = new Intent(this, ChunkChatActivity.class);
            Bundle b = new Bundle();
            b.putParcelable(ChunkChatActivity.ID_OF_CHAT, chat); //Your id
            notificationIntent.putExtras(b); //Put your id to your next Intent

            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            final PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    0 /* Request code */, notificationIntent,
                    PendingIntent.FLAG_ONE_SHOT);

            //You should use an actual ID instead
            final int notificationId = new Random().nextInt(60000);


            final Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                setupChannels();
            }

            Glide.with(this)
                    .asBitmap()
                    .load(chat.getUrlImage())
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            NotificationCompat.Builder notificationBuilder =
                                    new NotificationCompat.Builder(getApplicationContext(), ADMIN_CHANNEL_ID)
                                            .setLargeIcon(getCircleBitmap(resource.copy(resource.getConfig(), true)))
                                            .setSmallIcon(R.mipmap.ic_launcher_foreground)
                                            .setContentTitle(chat.getTitleChat())
                                            .setContentText(chat.getLastMessage())
                                            .setAutoCancel(true)
                                            .setContentIntent(pendingIntent)
                                            .setVibrate(new long[] { 0, 100, 200, 300 })
                                            .setSound(alarmSound)
                                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                                            /*.setStyle(new NotificationCompat.BigPictureStyle()
                                                    .setSummaryText(chat.getLastMessage())
                                                    .bigPicture(resource.copy(resource.getConfig(), true)))/*Notification with Image;*/

                                            .setStyle(new NotificationCompat.MessagingStyle("Me")
                                                        .setConversationTitle(chat.getTitleChat())
                                                        .addMessage(chat.getLastMessage(), chat.getTimestamp(), chat.getTitleChat()) );// Pass in null for user.

                            notificationManager.notify(makeHash(chat.getId()), notificationBuilder.build());
                        }
                    });

        }
    }

    private Bitmap getCircleBitmap(Bitmap bitmap) {
        final Bitmap output = Bitmap.createBitmap(bitmap.getWidth()+250,
                bitmap.getHeight()+250, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(output);

        final int color = Color.RED;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth()+250, bitmap.getHeight()+250);
        final RectF rectF = new RectF(rect);

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawOval(rectF, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        bitmap.recycle();

        return output;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(){
        CharSequence adminChannelName = getString(R.string.notifications_admin_channel_name);
        String adminChannelDescription = getString(R.string.notifications_admin_channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_HIGH);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableLights(true);
        adminChannel.setLightColor(Color.RED);
        adminChannel.enableVibration(true);
        adminChannel.setImportance(importance);
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(adminChannel);

    }
}