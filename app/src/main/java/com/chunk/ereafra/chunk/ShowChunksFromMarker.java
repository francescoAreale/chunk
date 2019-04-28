package com.chunk.ereafra.chunk;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chunk.ereafra.chunk.Model.ChatModel.Chat;
import com.chunk.ereafra.chunk.Model.Entity.Chunk;
import com.chunk.ereafra.chunk.Model.Entity.User;
import com.chunk.ereafra.chunk.Model.Interface.GetChatFromIDInterface;
import com.chunk.ereafra.chunk.Utils.FirebaseUtils;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ShowChunksFromMarker extends AppCompatActivity implements GetChatFromIDInterface {

    private ImageView imageToDisplay;

    private ImageView imageMore;
    private CircleImageView chunkImageView;
    private TextView titleChunk;
    public static String LIST_OF_CHUNK = "list_of_chunks_to_use";
    ArrayList<Chunk> listOfChunk;
    Chat chat;
    Button enter_now;
    int count = 0;

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
        enter_now = findViewById(R.id.enter_now);
        imageMore = findViewById(R.id.more_chunk);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        Bundle extras = getIntent().getExtras();
        listOfChunk =  extras.getParcelableArrayList(LIST_OF_CHUNK);
        if(listOfChunk.size()>1)
            imageMore.setVisibility(View.VISIBLE);
        FirebaseUtils.getChatFromId(listOfChunk.get(count).getChatOfChunkID(), this);
        Glide.with(this)
                .load(listOfChunk.get(0).getImage())
                .into(imageToDisplay);
        titleChunk.setText(listOfChunk.get(0).getChunkName());

        imageToDisplay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listOfChunk.size()>1){
                    if(count +1 < listOfChunk.size())
                    {
                        count++;

                    }else{
                        count = 0;
                    }
                    Glide.with(v)
                            .load(listOfChunk.get(count).getImage())
                            .into(imageToDisplay);
                    titleChunk.setText(listOfChunk.get(count).getChunkName());
                    FirebaseUtils.getChatFromId(listOfChunk.get(count).getChatOfChunkID(), (GetChatFromIDInterface)v.getContext());
                }

            }

        });

        enter_now.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ChunkChatActivity.class);
                Bundle b = new Bundle();
                b.putParcelable(ChunkChatActivity.ID_OF_CHAT, chat); //Your id
                intent.putExtras(b); //Put your id to your next Intent
                v.getContext().startActivity(intent);
                FirebaseUtils.addChatToUser(User.getInstance().getmFirebaseUser().getUid(),
                        listOfChunk.get(count).getChatOfChunkID());
                FirebaseUtils.registerTopic(listOfChunk.get(count).getChatOfChunkID());
            }
        });

    }

    @Override
    public void onChatReceived(Object chat) {
        this.chat = (Chat)chat;
    }
}
