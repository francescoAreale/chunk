package com.chunk.ereafra.chunk.Utils;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.chunk.ereafra.chunk.Model.ChatModel.Chat;
import com.chunk.ereafra.chunk.Model.Chunk;
import com.chunk.ereafra.chunk.Model.User;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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
