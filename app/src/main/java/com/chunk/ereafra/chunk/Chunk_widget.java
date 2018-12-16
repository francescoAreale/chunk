package com.chunk.ereafra.chunk;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.chunk.ereafra.chunk.Model.Entity.Chunk;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.HashMap;

/**
 * Implementation of App Widget functionality.
 */
public class Chunk_widget extends AppWidgetProvider {

    Chunk chunk;
    AppWidgetTarget appWidgetTarget;

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId, Chunk chunk) {

    }
    public static Chunk getHashMap(String key, Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = prefs.getString(key,"");
        java.lang.reflect.Type type = new TypeToken<Chunk>(){}.getType();
        Chunk obj = gson.fromJson(json, type);
        return obj;
    }
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        // Construct the RemoteViews object
        for( int appWidgetId: appWidgetIds) {
            Chunk retrived_chunk_shared = getHashMap(String.valueOf(appWidgetId), context);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.chunk_widget);
            try {
                int [] arrayId = {appWidgetId};
                AppWidgetTarget appWidgetTarget = new AppWidgetTarget(context, R.id.widget_image, views, arrayId);
                Glide
                        .with(context.getApplicationContext()) // safer!
                        .asBitmap()
                        .apply(new RequestOptions().override(500, 500))
                        .load(retrived_chunk_shared.getImage())
                        .into(appWidgetTarget);

                Intent intent = new Intent(context.getApplicationContext(), ChunkChatActivity.class);
                Bundle b = new Bundle();
                b.putParcelable(ChunkChatActivity.ID_OF_CHAT, retrived_chunk_shared); //Your id
                intent.putExtras(b); //Put your id to your next Intent
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
                views.setOnClickPendingIntent(R.id.widget_image, pendingIntent);
            } catch (Exception e) {
                Log.e("Chunk_widget", e.getMessage());
            }
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

