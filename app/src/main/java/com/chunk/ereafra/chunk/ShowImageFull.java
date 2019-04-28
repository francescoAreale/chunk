package com.chunk.ereafra.chunk;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowImageFull extends AppCompatActivity {

    private ImageView imageToDisplay;
    private CircleImageView chunkImageView;
    private TextView titleChunk;
    public static String IMAGE_TO_DISPLAY = "image_to_display";
    public static String IMAGE_USER = "image_user";
    public static String TITLE_CHUNK = "title_user";

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image_full);
        imageToDisplay = findViewById(R.id.imageChunk);
        titleChunk = findViewById(R.id.title_chunk);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Bundle extras = getIntent().getExtras();
        String imageDisplayed = (String) extras.getString(IMAGE_TO_DISPLAY);
        String imageUser = (String) extras.getString(IMAGE_USER);
        String titleChunkToShow = (String) extras.getString(TITLE_CHUNK);

        Glide.with(this)
                .load(imageDisplayed)
                .into(imageToDisplay);

        titleChunk.setText(titleChunkToShow);
       // imageToDisplay.setImageBitmap(bmp );
    }
}
