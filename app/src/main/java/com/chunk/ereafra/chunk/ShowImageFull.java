package com.chunk.ereafra.chunk;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image_full);
        imageToDisplay = findViewById(R.id.imageChunk);
        chunkImageView = findViewById(R.id.chunk_image);
        titleChunk = findViewById(R.id.title_chunk);
        Bundle extras = getIntent().getExtras();
        String imageDisplayed = (String) extras.getString(IMAGE_TO_DISPLAY);
        String imageUser = (String) extras.getString(IMAGE_USER);
        String titleChunkToShow = (String) extras.getString(TITLE_CHUNK);

        Glide.with(this)
                .load(imageDisplayed)
                .into(imageToDisplay);

        Glide.with(this)
                .load(imageUser)
                .into(chunkImageView);

        titleChunk.setText(titleChunkToShow);
       // imageToDisplay.setImageBitmap(bmp );
    }
}
