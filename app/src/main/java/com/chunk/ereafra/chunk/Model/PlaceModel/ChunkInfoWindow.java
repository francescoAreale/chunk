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
import com.chunk.ereafra.chunk.Model.Entity.Chunk;
import com.chunk.ereafra.chunk.Model.Entity.User;
import com.chunk.ereafra.chunk.NavigateChunk;
import com.chunk.ereafra.chunk.NewChunk;
import com.chunk.ereafra.chunk.R;
import com.chunk.ereafra.chunk.Utils.FirebaseUtils;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;

import java.util.ArrayList;

public class ChunkInfoWindow extends InfoWindow {
    Chunk chunk;
    MapView mapView;
    /**
     * @param mapView
     */
    private Bitmap image;

    public ChunkInfoWindow(MapView mapView, Bitmap image, Chunk chunk) {
        super(R.layout.bubble_black, mapView);
        this.image = image;
        this.chunk = chunk;
        this.mapView = mapView;
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
                Intent intent = new Intent(mView.getContext(), ChunkChatActivity.class);
                Bundle b = new Bundle();
                b.putParcelable(ChunkChatActivity.ID_OF_CHAT, chunk); //Your id
                intent.putExtras(b); //Put your id to your next Intent
                mView.getContext().startActivity(intent);
                FirebaseUtils.addChatToUser(User.getInstance().getmFirebaseUser().getUid(),
                        chunk.getChatOfChunkID());
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
}
