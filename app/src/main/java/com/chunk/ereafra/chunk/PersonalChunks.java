package com.chunk.ereafra.chunk;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.chunk.ereafra.chunk.Model.ChatModel.Chat;
import com.chunk.ereafra.chunk.Model.ChatModel.MessageChat;
import com.chunk.ereafra.chunk.Model.Entity.Chunk;
import com.chunk.ereafra.chunk.Model.Entity.User;
import com.chunk.ereafra.chunk.Utils.FirebaseUtils;
import com.chunk.ereafra.chunk.Utils.LoginUtils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonalChunks extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    RecyclerView mMessageRecyclerView;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<String, PersonalChunks.ChunkViewHolder> mFirebaseAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private int mAppWidgetId ;
    private TextView noMessageText;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_chunks);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        noMessageText = (TextView) findViewById(R.id.no_message);

        mMessageRecyclerView = (RecyclerView) findViewById(R.id.personaChunks);
        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        //mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            ImageView imageTitle = findViewById(R.id.toolbar_title);
            imageTitle.setImageDrawable(getDrawable(R.drawable.select_a_chunk));
        }else{
            mAppWidgetId = 0;
        }
        startFirebase();
    }

    @Override
    public void onPause() {
        mFirebaseAdapter.stopListening();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAdapter.startListening();
    }

    private void startFirebase() {

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        // create a parser between the json model in the database and a class


            /*SnapshotParser<MessageChat> parser = new SnapshotParser<MessageChat>() {
                @Override
                public MessageChat parseSnapshot(DataSnapshot dataSnapshot) {
                    MessageChat chatMessage = dataSnapshot.getValue(MessageChat.class);
                    if (chatMessage != null) {
                        chatMessage.setId(dataSnapshot.getKey());
                        Log.d(TAG, "initializeFirebase() " + dataSnapshot.getKey());
                        // i guess that the ID is automatically added and set in the object
                    }
                    return chatMessage;
                }
            };*/

        DatabaseReference messagesRef = mFirebaseDatabaseReference.child(FirebaseUtils.CHAT_USERS).
                child(User.getInstance().getmFirebaseUser().getUid());
        FirebaseRecyclerOptions<String> options =
                new FirebaseRecyclerOptions.Builder<String>()
                        .setQuery(messagesRef, String.class)
                        .build();

        mFirebaseAdapter = new FirebaseRecyclerAdapter<String, PersonalChunks.ChunkViewHolder>(options) {
            @NonNull
            @Override
            public PersonalChunks.ChunkViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

                return new PersonalChunks.ChunkViewHolder(inflater.inflate(R.layout.item_personal_chat, viewGroup,
                        false),PersonalChunks.this);
            }

            @Override
            protected void onBindViewHolder(@NonNull final PersonalChunks.ChunkViewHolder holder, int position, @NonNull final String idChat) {
                noMessageText.setVisibility(View.GONE);

                FirebaseDatabase.getInstance().getReference().child(FirebaseUtils.CHAT_TITLE).
                        child(idChat).
                        addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Chat chat = (Chat) dataSnapshot.getValue(Chat.class);
                                chat.setId(idChat);
                                holder.TitleChunk.setText(chat.getTitleChat());
                                holder.urlImage = chat.getUrlImage();
                                Glide.with(getApplicationContext())
                                        .load(chat.getUrlImage())
                                        .into(holder.chunkImgView);
                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                String done = prefs.getString(chat.getId(),"");
                                String chatID = chat.getId();
                                SharedPreferences prefs2 = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                                Boolean enable = prefs2.getBoolean("Notification"+chat.getId(),true);
                                if(enable)
                                    holder.no_sound.setVisibility(View.GONE);
                                else
                                    holder.no_sound.setVisibility(View.VISIBLE);

                                if(!done.equals(chat.getLastMessage()))
                                {
                                    holder.new_message.setVisibility(View.VISIBLE);
                                    holder.lastMessage.setTypeface(null, Typeface.BOLD_ITALIC);

                                }else{
                                    holder.new_message.setVisibility(View.GONE);
                                    holder.lastMessage.setTypeface(null, Typeface.NORMAL);
                                }
                                holder.lastMessage.setText(chat.getLastMessage());
                                holder.setChat(chat);
                                if(mAppWidgetId!=0)
                                    holder.setIsForWidget(mAppWidgetId);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Getting Post failed, log a message
                                // ...
                            }
                        });
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
            }
        });

        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_personal_chunk, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                LoginUtils.performSignOut(this);
                finish();
                return true;
            case R.id.addChunk:
                startActivity(new Intent(PersonalChunks.this, NewChunk.class));

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        LoginUtils.performLoginWithGoogle(this, this, this);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }


    public static class ChunkViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView lastMessage;
        TextView TitleChunk;
        CircleImageView chunkImgView;
        Chat chat;
        String urlImage;
        int mAppWidgetId = 0;
        Context contex;
        TextView new_message;
        ImageView no_sound;

        public void saveHashMap(String key , Object obj) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(contex);
            SharedPreferences.Editor editor = prefs.edit();
            Gson gson = new Gson();
            String json = gson.toJson(obj);
            editor.putString(key,json);
            editor.apply();     // This line is IMPORTANT !!!
        }



        public ChunkViewHolder(View v, Context contex) {
            super(v);
            this.contex = contex;
            TitleChunk = (TextView) itemView.findViewById(R.id.TitleChat);
            lastMessage = (TextView) itemView.findViewById(R.id.messageTextView);
            new_message = (TextView)itemView.findViewById(R.id.new_message);
            chunkImgView =  itemView.findViewById(R.id.imageSingleChunk);
            no_sound = itemView.findViewById(R.id.no_sound_icon);
            chunkImgView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ShowImageFull.class);
                    Bundle extras = new Bundle();
                    extras.putString(ShowImageFull.IMAGE_TO_DISPLAY, urlImage);
                    //extras.putString(ShowImageFull.IMAGE_USER, chunk.getImage());
                    extras.putString(ShowImageFull.TITLE_CHUNK, chat.getTitleChat());
                    intent.putExtras(extras);
                    v.getContext().startActivity(intent);

                }
            });
            v.setOnClickListener(this);
        }

        public void setChat(Chat chat) {
            this.chat = chat;
        }

        public void setIsForWidget(int isForWidget){
            this.mAppWidgetId = isForWidget;
        }

        @Override
        public void onClick(View v) {
            if(mAppWidgetId != 0)
            {
                saveHashMap(String.valueOf(mAppWidgetId),chat);
                Intent intent = new Intent();
                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
// Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
// since it seems the onUpdate() is only fired on that:
                int[] ids = AppWidgetManager.
                        getInstance(contex.getApplicationContext()).
                        getAppWidgetIds(new ComponentName(contex.getApplicationContext(), Chunk_widget.class));

                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                contex.sendBroadcast(intent);
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                ((Activity)contex).setResult(RESULT_OK, resultValue);
                ((Activity)contex).finish();
            }
             else if (this.chat!= null) {
                Intent intent = new Intent(v.getContext(), ChunkChatActivity.class);
                Bundle b = new Bundle();
                b.putParcelable(ChunkChatActivity.ID_OF_CHAT, this.chat); //Your id
                intent.putExtras(b); //Put your id to your next Intent
                v.getContext().startActivity(intent);
            }
        }
    }
}
