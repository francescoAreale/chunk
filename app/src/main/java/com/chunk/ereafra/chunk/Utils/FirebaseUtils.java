package com.chunk.ereafra.chunk.Utils;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.chunk.ereafra.chunk.Model.ChatModel.Chat;
import com.chunk.ereafra.chunk.Model.Entity.Chunk;
import com.chunk.ereafra.chunk.Model.Entity.User;
import com.chunk.ereafra.chunk.Model.Interface.GetChatFromIDInterface;
import com.chunk.ereafra.chunk.Model.Interface.VisualizeChunkInterface;
import com.chunk.ereafra.chunk.R;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class FirebaseUtils {

    public static String TAG = "Firebase Util Static CLASS";
    private static String CHUNK_POSITION = "chunk_position";
    private static String CHUNK_TITLE = "chunk";
    public static final String CHAT_USERS = "users_chat";
    public static String CHAT_TITLE = "chat";
    public static void insertFirebaseLocation(GeoLocation geo, String IDChunk) {

        GeoFire geoFire = new GeoFire(FirebaseDatabase.getInstance().getReference(CHUNK_POSITION));
        geoFire.setLocation(IDChunk, geo, new GeoFire.CompletionListener() {
            @Override
            public void onComplete(String key, DatabaseError error) {
                if (error != null) {
                    System.err.println("There was an error saving the location to GeoFire: " + error);
                } else {
                    System.out.println("Location saved on server successfully!");
                }
            }
        });
    }

    public static void registerUserToTopic(String idUser)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference().child("users_chat").child(idUser);
        myRef.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                String id = (String)dataSnapshot.getValue();
                FirebaseUtils.registerTopic(id);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
       /* myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });*/
    }

    public static void addChatToUser(final String idUser, final String idChunk) {
        FirebaseDatabase.getInstance().getReference().child(CHAT_USERS).
                child(idUser).
                addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        boolean exists = false;
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            if (((String) child.getValue()).equals(idChunk)) {
                                exists = true;
                                break;
                            }
                        }
                        if (!exists)
                            FirebaseDatabase.getInstance().getReference().child(CHAT_USERS).child(idUser)
                                    .push().setValue(idChunk);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        // Getting Post failed, log a message
                        Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
                        // ...
                    }
                });

    }

    public static void registerTopic(String message){
        FirebaseMessaging.getInstance().subscribeToTopic(message);
    }

    public static void getChatFromId(final String Id, final GetChatFromIDInterface<Chat> chatInterface){
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference().child("chat").child(Id);
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Chat chat = dataSnapshot.getValue(Chat.class);
                chat.setId(Id);
                chatInterface.onChatReceived(chat);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public static void getChunkFromID(String ID, final VisualizeChunkInterface<Chunk> objectVisualizer) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference().child(CHUNK_TITLE).child(ID);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Chunk chunk = dataSnapshot.getValue(Chunk.class);
                objectVisualizer.showChunk(chunk);
                Log.d(TAG, "Value is: " + chunk.getChunkName());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }


    public static void getChunkAroundLocation(double latitude, double longitude,
                                              double radiuskm, final VisualizeChunkInterface<Chunk> objectVisualizer) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(CHUNK_POSITION);
        GeoFire geoFire = new GeoFire(ref);
        final GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latitude, longitude), radiuskm);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                getChunkFromID(key, objectVisualizer);
            }

            @Override
            public void onKeyExited(String key) {
                System.out.println(String.format("Key %s is no longer in the search area", key));
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
            }

            @Override
            public void onGeoQueryReady() {
                System.out.println("All initial data has been loaded and events have been fired!");

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                System.err.println("There was an error with this query: " + error);
            }
        });

    }

    public static void insertChunkOnDB(final Chunk newChunk, final Uri imagePicURI) {

        FirebaseDatabase.getInstance().getReference().child(CHUNK_TITLE).push()
                .setValue(newChunk, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError,
                                           DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            String key = databaseReference.getKey();
                            StorageReference storageReference =
                                    FirebaseStorage.getInstance()
                                            .getReference(User.getInstance().getmFirebaseUser().getUid())
                                            .child(key)
                                            .child(imagePicURI.getLastPathSegment());
                            insertFirebaseLocation(new GeoLocation(newChunk.getLatitude(), newChunk.getLongitude()), key);
                            putImageInStorage(storageReference, imagePicURI, key, newChunk);

                        } else {
                            Log.w(TAG, "Unable to write message to database.",
                                    databaseError.toException());
                        }
                    }
                });
    }

    public static void updateChatOnFirebase(Chat chat, String Message) {
        chat.setLastMessage(Message);
        FirebaseDatabase.getInstance().getReference().child(CHAT_TITLE).child(chat.getId())
                .setValue(chat);
    }

    public static void createChatOnFirebase(final String idChunk, final Chunk newChunk) {
        Chat newChat = new Chat(null, "No Message Until Now", (System.currentTimeMillis() / 1000),
                newChunk.getChunkName(), newChunk.getImage());
        FirebaseDatabase.getInstance().getReference().child(CHAT_TITLE).push()
                .setValue(newChat, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(DatabaseError databaseError,
                                           DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            String key = databaseReference.getKey();
                            newChunk.setChatOfChunkID(key);
                            FirebaseDatabase.getInstance().getReference().child(CHUNK_TITLE).child(idChunk)
                                    .setValue(newChunk);
                            FirebaseUtils.addChatToUser(User.getInstance().getmFirebaseUser().getUid(), key);
                            FirebaseUtils.registerTopic(key);
                        } else {
                            Log.w(TAG, "Unable to write message to database.",
                                    databaseError.toException());
                        }
                    }
                });
    }

    public static void putImageInStorage(final StorageReference storageReference, Uri uri, final String key, final Chunk newChunk) {
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
                    newChunk.setImage(downUri.toString());
                    FirebaseDatabase.getInstance().getReference().child(CHUNK_TITLE).child(key)
                            .setValue(newChunk);
                    createChatOnFirebase(key, newChunk);
                    Log.d(TAG, "the image CHUNK has been loaded in the database");
                }
            }
        });
    }


}
