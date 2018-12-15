package com.chunk.ereafra.chunk.Utils;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;

import com.chunk.ereafra.chunk.Model.Entity.User;
import com.chunk.ereafra.chunk.R;
import com.chunk.ereafra.chunk.SignInActivity;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginUtils {

    // this object return a UserFirebase and Run the LoginActivity if the User is not logged
    public static void performLoginWithGoogle(Context context,
                                              GoogleApiClient.OnConnectionFailedListener onFailedListner,
                                              FragmentActivity fragmentActivity) {
        // Initialize FirebaseUtils Auth


        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser mFirebaseUser = mFirebaseAuth.getCurrentUser();
        User tmpUser = null;


        if (mFirebaseUser == null ) {
            // Not signed in, launch the Sign In activity
            Intent ciao = new Intent(context, SignInActivity.class);
            context.startActivity(ciao);
            ((AppCompatActivity)context).finish();
        }else {
            if(!User.getInstance().isLogged()) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.default_web_client_id))
                        .requestEmail()
                        .build();

                GoogleSignInClient mGoogleSignInClient = GoogleSignIn.getClient(context, gso);
                tmpUser = User.getInstance();
                tmpUser.setLogged(true);
                if (mFirebaseUser.getPhotoUrl() != null) {
                    tmpUser.setPhotoUser(mFirebaseUser.getPhotoUrl().toString());
                }
                tmpUser.setUserName(mFirebaseUser.getDisplayName());
                tmpUser.setmGoogleApiClient(mGoogleSignInClient);
                tmpUser.setmFirebaseUser(mFirebaseUser);
            }
        }
    }


    public static void performSignOut(Context context) {
        if (User.getInstance().isLogged())
        {
            if (FirebaseAuth.getInstance().getCurrentUser() != null)
                FirebaseAuth.getInstance().signOut();
            User.getInstance().getmGoogleApiClient().signOut();
            User.getInstance().setLogged(false);
            context.startActivity(new Intent(context, SignInActivity.class));
            ((AppCompatActivity) context).finish();
        }
    }
}
