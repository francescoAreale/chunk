package com.chunk.ereafra.chunk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.chunk.ereafra.chunk.Model.ChatModel.Chat;
import com.chunk.ereafra.chunk.Model.ChatModel.MessageChat;
import com.chunk.ereafra.chunk.Model.Entity.Chunk;
import com.chunk.ereafra.chunk.Model.Entity.User;
import com.chunk.ereafra.chunk.Utils.FirebaseUtils;
import com.chunk.ereafra.chunk.Utils.LoginUtils;
import com.chunk.ereafra.chunk.Utils.NetworkUtils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.firebase.ui.database.SnapshotParser;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChunkChatActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final String ANONYMOUS = "anonymous";
    private static final String TAG = "ChunkChatActivity";
    public static final String FRIENDLY_MSG_LENGTH = "friendly_msg_length";
    public static final String MESSAGES_CHILD = "messages";
    public static final String CHAT_CHILD = "chat";
    public static final int DEFAULT_MSG_LENGTH_LIMIT = 1000;
    private static final String LOADING_IMAGE_URL = "https://loading.io/spinners/typing/lg.-text-entering-comment-loader.gif";
    private static final int REQUEST_IMAGE = 2;

    // google
    private GoogleApiClient mGoogleApiClient;

    // FirebaseUtils instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;
    private DatabaseReference mFirebaseDatabaseReference;
    private FirebaseRecyclerAdapter<MessageChat, MessageViewHolder> mFirebaseAdapter;

    // attriute of the user
    private String mUsername;
    private String mPhotoUrl;

    // attribute of the main activity
    private ImageButton mSendButton;
    private RecyclerView mMessageRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;
    private EditText mMessageEditText;
    private ImageView mAddMessageImageView;
    public static final String ID_OF_CHAT = "id_of_chunk_chat";
    private Chat chat;
    private TextView noMessageText;
    private CircleImageView ImageViewTitleChunk;
    // shared preference
    private TextView titleInToolbar;
    private SharedPreferences mSharedPreferences;
    public static String lastIdChat = "";
    public static Boolean isVisible ;
    @Override
    protected void onStart() {
        super.onStart();
        isVisible = true;
        LoginUtils.performLoginWithGoogle(this, this, this);
    }

    public void initializeFirebase() {

        mFirebaseDatabaseReference = FirebaseDatabase.getInstance().getReference();
        // create a parser between the json model in the database and a class


        SnapshotParser<MessageChat> parser = new SnapshotParser<MessageChat>() {
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
        };

        DatabaseReference messagesRef = mFirebaseDatabaseReference.child(MESSAGES_CHILD).child(chat.getId());
        FirebaseRecyclerOptions<MessageChat> options =
                new FirebaseRecyclerOptions.Builder<MessageChat>()
                        .setQuery(messagesRef, parser)
                        .build();

        mFirebaseAdapter = new FirebaseRecyclerAdapter<MessageChat, MessageViewHolder>(options) {

            @Override
            public int getItemViewType(int position) {
                //Implement your logic here
                return position;
            }

            @NonNull
            @Override
            public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
                MessageChat chat = this.getItem(i);
                if (chat.getName().equals(User.getInstance().getUserName()))
                    return new MessageViewHolder(inflater.inflate(R.layout.my_message, viewGroup, false), true);
                else
                    return new MessageViewHolder(inflater.inflate(R.layout.message_item, viewGroup, false), false);
            }


            public void bindOtherViewHolder(final MessageViewHolder holder, MessageChat message) {
                if (message.getText() != null) {
                    holder.messageTextView.setText(message.getText());
                    holder.messageTextView.setVisibility(TextView.VISIBLE);
                    holder.messageImageView.setVisibility(ImageView.GONE);
                } else if (message.getImageUrl() != null) {
                    String imageUrl = message.getImageUrl();
                    if (imageUrl.startsWith("gs://")) {
                        StorageReference storageReference = FirebaseStorage.getInstance()
                                .getReferenceFromUrl(imageUrl);
                        storageReference.getDownloadUrl().addOnCompleteListener(
                                new OnCompleteListener<Uri>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Uri> task) {
                                        if (task.isSuccessful()) {
                                            String downloadUrl = task.getResult().toString();
                                            Glide.with(holder.messageImageView.getContext())
                                                    .load(downloadUrl)
                                                    .into(holder.messageImageView);
                                        } else {
                                            Log.w(TAG, "Getting download url was not successful.",
                                                    task.getException());
                                        }
                                    }
                                });
                    } else {
                        holder.downloadUri = message.getImageUrl();
                        holder.chat = chat;
                        Glide.with(holder.messageImageView.getContext())
                                .load(message.getImageUrl())
                                .into(holder.messageImageView);
                    }
                    holder.messageImageView.setVisibility(ImageView.VISIBLE);
                    holder.messageTextView.setVisibility(TextView.GONE);
                }

                if (!message.getName().equals(User.getInstance().getUserName())) {
                    holder.messengerTextView.setText(message.getName());
                    if (message.getPhotoUrl() == null) {
                        holder.messengerImageView.setImageDrawable(ContextCompat.getDrawable(ChunkChatActivity.this,
                                R.drawable.ic_account_circle_black_36dp));
                    } else {
                        Glide.with(ChunkChatActivity.this)
                                .load(message.getPhotoUrl())
                                .into(holder.messengerImageView);
                    }
                }
            }

            @Override
            protected void onBindViewHolder(@NonNull MessageViewHolder holder, int position, @NonNull MessageChat message) {
                mProgressBar.setVisibility(ProgressBar.INVISIBLE);
                noMessageText.setVisibility(View.INVISIBLE);
                bindOtherViewHolder(holder, message);
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int friendlyMessageCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added message.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (friendlyMessageCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mMessageRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mMessageRecyclerView.setAdapter(mFirebaseAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.sign_out_menu:
                LoginUtils.performSignOut(this);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /* This code initially adds all existing messages
    and then listens for new child entries under
     the messages path in the FirebaseUtils Realtime Database*/

    public void initializeMainActivity() {
        // Initialize ProgressBar and RecyclerView.
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //getSupportActionBar().setTitle(chunk_release.getChunkName());
        ImageViewTitleChunk = findViewById(R.id.image_chunk);
        Glide.with(getApplicationContext())
                .load(chat.getUrlImage())
                .into(ImageViewTitleChunk);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        titleInToolbar = findViewById(R.id.title_chunk);
        titleInToolbar.setText(chat.getTitleChat());
        //getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mMessageRecyclerView = (RecyclerView) findViewById(R.id.messageRecyclerView);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setStackFromEnd(true);
        mMessageRecyclerView.setLayoutManager(mLinearLayoutManager);
        mProgressBar.setVisibility(ProgressBar.INVISIBLE);
        noMessageText = (TextView) findViewById(R.id.no_message);
        // mProgressBar.setVisibility(ProgressBar.INVISIBLE);
        initializeFirebase();

        mMessageEditText = (EditText) findViewById(R.id.messageEditText);
        mMessageEditText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(mSharedPreferences
                .getInt(FRIENDLY_MSG_LENGTH, DEFAULT_MSG_LENGTH_LIMIT))});
        mMessageEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0) {
                    mSendButton.setEnabled(true);
                } else {
                    mSendButton.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mSendButton = (ImageButton) findViewById(R.id.sendButton);
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MessageChat friendlyMessage = new
                        MessageChat(mMessageEditText.getText().toString(),
                        User.getInstance().getUserName(),
                        User.getInstance().getPhotoUser(),
                        null /* no image */);
                mFirebaseDatabaseReference.child(MESSAGES_CHILD).child(chat.getId())
                        .push().setValue(friendlyMessage);
                FirebaseUtils.updateChatOnFirebase(chat, mMessageEditText.getText().toString());
                mMessageEditText.setText("");
            }
        });

        mAddMessageImageView = (ImageView) findViewById(R.id.addMessageImageView);
        mAddMessageImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onPause() {
        mFirebaseAdapter.stopListening();
        super.onPause();
        isVisible=false;
        lastIdChat = "";
    }

    @Override
    public void onResume() {
        super.onResume();
        mFirebaseAdapter.startListening();
        isVisible = true;
        if(chat != null)
            lastIdChat = chat.getId();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);


        LoginUtils.performLoginWithGoogle(this, this, this);

        Bundle b = getIntent().getExtras();
        if (b != null)
            chat = b.getParcelable(ID_OF_CHAT);
        lastIdChat = chat.getId();
        //initialize the main activity with
        initializeMainActivity();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    final Uri uri = data.getData();
                    Log.d(TAG, "Uri: " + uri.toString());

                    MessageChat tempMessage = new MessageChat(null, User.getInstance().getUserName(), User.getInstance().getPhotoUser(),
                            LOADING_IMAGE_URL);
                    mFirebaseDatabaseReference.child(MESSAGES_CHILD).child(chat.getId()).push()
                            .setValue(tempMessage, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(DatabaseError databaseError,
                                                       DatabaseReference databaseReference) {
                                    if (databaseError == null) {
                                        String key = databaseReference.getKey();
                                        StorageReference storageReference =
                                                FirebaseStorage.getInstance()
                                                        .getReference(User.getInstance().getmFirebaseUser().getUid())
                                                        .child(key)
                                                        .child(uri.getLastPathSegment());
                                        putImageInStorage(storageReference, uri, key);
                                    } else {
                                        Log.w(TAG, "Unable to write message to database.",
                                                databaseError.toException());
                                    }
                                }
                            });
                }
            }
        }
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        ImageView messageImageView;

        TextView messengerTextView;
        CircleImageView messengerImageView;
        String downloadUri;
        Chat chat;

        public MessageViewHolder(View v, Boolean isMineMessage) {
            super(v);
            messageTextView = (TextView) itemView.findViewById(R.id.messageTextView);
            messageImageView = (ImageView) itemView.findViewById(R.id.messageImageView);
            messageImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ShowImageFull.class);
                    Bundle extras = new Bundle();
                    extras.putString(ShowImageFull.IMAGE_TO_DISPLAY, downloadUri);
                    extras.putString(ShowImageFull.IMAGE_USER, chat.getUrlImage());
                    extras.putString(ShowImageFull.TITLE_CHUNK, chat.getTitleChat());
                    intent.putExtras(extras);
                    v.getContext().startActivity(intent);
                }
            });
            if (!isMineMessage) {
                messengerTextView = (TextView) itemView.findViewById(R.id.messengerTextView);
                messengerImageView = (CircleImageView) itemView.findViewById(R.id.messengerImageView);
            }
        }
    }

    private void putImageInStorage(final StorageReference storageReference, Uri uri, final String key) {
        storageReference.putFile(uri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
            @Override
            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                if (!task.isSuccessful()) {
                    throw task.getException();
                }
                return storageReference.getDownloadUrl();
            }
        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
            @Override
            public void onComplete(@NonNull Task<Uri> task) {
                if (task.isSuccessful()) {
                    Uri downUri = task.getResult();
                    MessageChat friendlyMessage =
                            new MessageChat(null, User.getInstance().getUserName(), User.getInstance().getPhotoUser(),
                                    downUri.toString());
                    mFirebaseDatabaseReference.child(MESSAGES_CHILD).child(chat.getId()).child(key)
                            .setValue(friendlyMessage);
                    FirebaseUtils.updateChatOnFirebase(chat, "Photo");
                }
            }
        });
    }

    /*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }*/


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, NetworkUtils.ERROR_CONNECTION_GOOGLE_PLAY, Toast.LENGTH_SHORT).show();
    }
}
