package com.chunk.ereafra.chunk.Model.Entity;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseUser;

public class User {

    // static variable single_instance of type Singleton
    private static User single_instance = null;

    // variable of type String
    private String UserName;
    private String photoUser;
    private Boolean logged;
    private GoogleSignInClient mGoogleApiClient;
    private FirebaseUser mFirebaseUser;

    // private constructor restricted to this class itself
    private User() {
        logged = false;
    }

    // static method to create instance of the User
    public static User getInstance() {
        if (single_instance == null)
            single_instance = new User();

        return single_instance;
    }

    public FirebaseUser getmFirebaseUser() {
        return mFirebaseUser;
    }

    public void setmFirebaseUser(FirebaseUser mFirebaseUser) {
        this.mFirebaseUser = mFirebaseUser;
    }

    public GoogleSignInClient getmGoogleApiClient() {
        return mGoogleApiClient;
    }

    public void setmGoogleApiClient(GoogleSignInClient mGoogleApiClient) {
        this.mGoogleApiClient = mGoogleApiClient;
    }

    public String getUserName() {
        return UserName;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public String getPhotoUser() {
        return photoUser;
    }

    public void setPhotoUser(String photoUser) {
        this.photoUser = photoUser;
    }

    public Boolean isLogged() {
        return logged;
    }

    public void setLogged(Boolean logged) {
        this.logged = logged;
    }
}
