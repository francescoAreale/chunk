package com.chunk.ereafra.chunk;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chunk.ereafra.chunk.Model.Entity.Chunk;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowChunksFromMarker extends AppCompatActivity {

    private ImageView imageToDisplay;
    private CircleImageView chunkImageView;
    private TextView titleChunk;
    public static String LIST_OF_CHUNK = "list_of_chunks_to_use";
    ArrayList<Chunk> listOfChunk;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_chunks_from_marker);
        imageToDisplay = findViewById(R.id.imageChunk);
        titleChunk = findViewById(R.id.title_chunk);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Bundle extras = getIntent().getExtras();
        listOfChunk =  extras.getParcelableArrayList(LIST_OF_CHUNK);

        Glide.with(this)
                .load(listOfChunk.get(0).getImage())
                .into(imageToDisplay);
        titleChunk.setText(listOfChunk.get(0).getChunkName());

        imageToDisplay.setOnClickListener(new View.OnClickListener() {
            int count = 1;
            @Override
            public void onClick(View v) {
                if(listOfChunk.size() == count)
                    count =0;
                Glide.with(v)
                        .load(listOfChunk.get(count).getImage())
                        .into(imageToDisplay);
                titleChunk.setText(listOfChunk.get(count).getChunkName());
                count ++;
            }

        });

    }
}
