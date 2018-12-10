package com.chunk.ereafra.chunk.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import com.chunk.ereafra.chunk.Model.Entity.User;
import com.chunk.ereafra.chunk.SignInActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginUtils {

    // this object return a UserFirebase and Run the LoginActivity if the User is not logged
    public static void performLoginWithGoogle(Context context,
                                              GoogleApiClient.OnConnectionFailedListener onFailedListner,
                                              FragmentActivity fragmentActivity) {
        // Initialize FirebaseUtils Auth
        GoogleApiClient mGoogleApiClient = new GoogleApiClient.Builder(fragmentActivity.getApplicationContext())
                .enableAutoManage(fragmentActivity /* FragmentActivity */, onFailedListner /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
        User tmpUser = null;

        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            Intent ciao = new Intent(context, SignInActivity.class);
            context.startActivity(ciao);
            // fragmentActivity.finish();
        }
            tmpUser = User.getInstance();
            tmpUser.setLogged(true);
            if (mFirebaseUser.getPhotoUrl() != null) {
                tmpUser.setPhotoUser(mFirebaseUser.getPhotoUrl().toString());
            }
            tmpUser.setUserName(mFirebaseUser.getDisplayName());
            tmpUser.setmGoogleApiClient(mGoogleApiClient);
            tmpUser.setmFirebaseUser(mFirebaseUser);

    }


    public static void performSignOut(Context context) {

        FirebaseAuth.getInstance().signOut();
        Auth.GoogleSignInApi.signOut(User.getInstance().getmGoogleApiClient());
        User.getInstance().setLogged(false);
        context.startActivity(new Intent(context, SignInActivity.class));
    }
}
