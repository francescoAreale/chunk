package com.chunk.ereafra.chunk.Utils;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;

import com.chunk.ereafra.chunk.Model.ChatModel.Chat;
import com.chunk.ereafra.chunk.Model.ChatModel.MessageChat;
import com.chunk.ereafra.chunk.Model.Chunk;
import com.chunk.ereafra.chunk.Model.User;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.auth.api.Auth;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.net.URI;

public class FirebaseUtils {

    public static String TAG = "Firebase Util Static CLASS";
    private static String CHUNK_POSITION = "chunk_position";
    private static String CHUNK_TITLE = "chunk";
    private static String CHAT_TITLE = "chat";

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


    public static void getChunkFromID(String ID) {

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference().child(CHUNK_TITLE).child(ID);
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                Chunk chunk = dataSnapshot.getValue(Chunk.class);
                Log.d(TAG, "Value is: " + chunk.getChunkName());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });


    }

    public static void getChunkAroundLocation(double latitude, double longitude, double radiuskm, final MapView map) {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference(CHUNK_POSITION);
        GeoFire geoFire = new GeoFire(ref);
        final GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latitude, longitude), radiuskm);
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                GeoPoint locationOnMap = new GeoPoint(location.latitude, location.longitude);
                map.setVisibility(View.VISIBLE);
                Marker startMarker = new Marker(map);
                startMarker.setPosition(locationOnMap);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                map.getOverlays().add(startMarker);
                System.out.println(String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));
                getChunkFromID(key);
            }

            @Override
            public void onKeyExited(String key) {
                System.out.println(String.format("Key %s is no longer in the search area", key));
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                System.out.println(String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
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
                            createChatOnFirebase(key, newChunk);
                        } else {
                            Log.w(TAG, "Unable to write message to database.",
                                    databaseError.toException());
                        }
                    }
                });
    }

    public static void createChatOnFirebase(final String idChunk, final Chunk newChunk) {
        Chat newChat = new Chat(null, "No Message Until Now", (System.currentTimeMillis() / 1000));
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
                    Log.d(TAG, "the image CHUNK has been loaded in the database");
                }
            }
        });
    }


}
