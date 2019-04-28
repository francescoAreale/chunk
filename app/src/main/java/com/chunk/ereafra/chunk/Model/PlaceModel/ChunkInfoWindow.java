package com.chunk.ereafra.chunk.Model.PlaceModel;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.chunk.ereafra.chunk.ChunkChatActivity;
import com.chunk.ereafra.chunk.Model.ChatModel.Chat;
import com.chunk.ereafra.chunk.Model.Entity.Chunk;
import com.chunk.ereafra.chunk.Model.Entity.User;
import com.chunk.ereafra.chunk.Model.Interface.GetChatFromIDInterface;
import com.chunk.ereafra.chunk.NavigateChunk;
import com.chunk.ereafra.chunk.NewChunk;
import com.chunk.ereafra.chunk.R;
import com.chunk.ereafra.chunk.Utils.FirebaseUtils;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import java.util.ArrayList;

public class ChunkInfoWindow extends InfoWindow implements GetChatFromIDInterface {
    Chunk chunk;
    MapView mapView;
    /**
     * @param mapView
     */
    private Bitmap image;
    private Chat chat;

    public ChunkInfoWindow(MapView mapView, Bitmap image, Chunk chunk) {
        super(R.layout.bubble_black, mapView);
        this.image = image;
        this.chunk = chunk;
        this.mapView = mapView;

        FirebaseUtils.getChatFromId(chunk.getChatOfChunkID(), this);
    }

    /**
     * close all InfoWindows currently opened on this MapView
     */
    static public void closeAllInfoWindowsOn(MapView mapView) {
        ArrayList<InfoWindow> opened = getOpenedInfoWindowsOn(mapView);
        for (InfoWindow infoWindow : opened) {
            infoWindow.close();
        }
    }

    /**
     * return all InfoWindows currently opened on this MapView
     */
    static public ArrayList<InfoWindow> getOpenedInfoWindowsOn(MapView mapView) {
        int count = mapView.getChildCount();
        ArrayList<InfoWindow> opened = new ArrayList<InfoWindow>(count);
        for (int i = 0; i < count; i++) {
            final View child = mapView.getChildAt(i);
            Object tag = child.getTag();
            if (tag != null && tag instanceof InfoWindow) {
                InfoWindow infoWindow = (InfoWindow) tag;
                opened.add(infoWindow);
            }
        }
        return opened;
    }

    @Override
    public void onOpen(Object item) {
        //super.onOpen(item);
        closeAllInfoWindowsOn(mapView);
        close(); //if it was already opened
        Button bt = (Button) mView.findViewById(R.id.bubble_moreinfo);
        bt.setVisibility(View.VISIBLE);
        bt.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });

        ImageView img = (ImageView) mView.findViewById(R.id.bubble_image);
        img.setVisibility(View.VISIBLE);
        img.setImageBitmap(image);

        TextView title_chunk = (TextView) mView.findViewById(R.id.bubble_title);
        title_chunk.setVisibility(View.VISIBLE);
        title_chunk.setText(chunk.getChunkName());

        mIsVisible = true;
    }

    @Override
    public void onClose() {
        if (mIsVisible) {
            mIsVisible = false;
            ((ViewGroup) mView.getParent()).removeView(mView);
            //onClose();
        }
    }

    @Override
    public void onChatReceived(Object chat) {
        this.chat = (Chat)chat;
    }
}
