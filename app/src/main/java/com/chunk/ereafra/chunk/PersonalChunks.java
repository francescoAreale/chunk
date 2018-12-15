package com.chunk.ereafra.chunk;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class PersonalChunks extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    RecyclerView mMessageRecyclerView;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<String, PersonalChunks.ChunkViewHolder> mFirebaseAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_chunks);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.personaChunks);
        mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        //mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
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
                        false));
            }

            @Override
            protected void onBindViewHolder(@NonNull final PersonalChunks.ChunkViewHolder holder, int position, @NonNull final String idChat) {

                FirebaseDatabase.getInstance().getReference().child(FirebaseUtils.CHAT_TITLE).
                        child(idChat).
                        addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Chat chat = (Chat) dataSnapshot.getValue(Chat.class);
                                holder.TitleChunk.setText(chat.getTitleChat());
                                Glide.with(getApplicationContext())
                                        .load(chat.getUrlImage())
                                        .into(holder.chunkImgView);
                                holder.lastMessage.setText(chat.getLastMessage());
                                holder.setChunk(new Chunk(null, chat.getTitleChat(),
                                        0, 0.0, 0.0, idChat, chat.getUrlImage()));
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
        Chunk chunk;

        public ChunkViewHolder(View v) {
            super(v);
            TitleChunk = (TextView) itemView.findViewById(R.id.TitleChat);
            lastMessage = (TextView) itemView.findViewById(R.id.messageTextView);
            chunkImgView = (CircleImageView) itemView.findViewById(R.id.imageSingleChunk);
            v.setOnClickListener(this);
        }

        public void setChunk(Chunk chunk) {
            this.chunk = chunk;
        }


        @Override
        public void onClick(View v) {
            if (this.chunk != null) {
                Intent intent = new Intent(v.getContext(), ChunkChatActivity.class);
                Bundle b = new Bundle();
                b.putParcelable(ChunkChatActivity.ID_OF_CHAT, this.chunk); //Your id
                intent.putExtras(b); //Put your id to your next Intent
                v.getContext().startActivity(intent);
            }
        }
    }
}
