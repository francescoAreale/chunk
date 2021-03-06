package com.chunk.ereafra.chunk;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.chunk.ereafra.chunk.Model.Entity.User;
import com.chunk.ereafra.chunk.Utils.FirebaseUtils;
import com.chunk.ereafra.chunk.Utils.GPSutils;
import com.chunk.ereafra.chunk.Utils.LoginUtils;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;


public class SignInActivity extends AppCompatActivity implements
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    private static final String TAG = "SignInActivity";
    private static final int RC_SIGN_IN = 9001;

    private SignInButton mSignInButton;
    private GoogleApiClient mGoogleApiClient;
    // FirebaseUtils instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth mAuth;
// FirebaseUtils instance variables
    private GoogleSignInClient mGoogleSignInClient;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String done = prefs.getString(IntroActivity.PRESENTATION_DONE,"");
        if(!done.equals(IntroActivity.PRESENTATION_DONE)) {
            startActivity(new Intent(this, IntroActivity.class));
            finish();
        }else {
            LoginUtils.performLoginWithGoogle(this, this, this);
            if (User.getInstance().isLogged()) {
                startActivity(new Intent(SignInActivity.this, NavigateChunk.class));
                finish();
            }
            GPSutils.asksForAllPermission(this);


            progress = (ProgressBar) findViewById(R.id.progressBar);
            progress.setVisibility(View.GONE);
            // GPSutils.asksForAllPermission(this);
            // Assign fields
            mSignInButton = (SignInButton) findViewById(R.id.sign_in_button);

            // Set click listeners
            mSignInButton.setOnClickListener(this);

            // Configure Google Sign In
            GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build();

            mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
            mAuth = FirebaseAuth.getInstance();
            // Initialize FirebaseAuth
            mFirebaseAuth = FirebaseAuth.getInstance();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                progress.setVisibility(View.VISIBLE);
                mSignInButton.setVisibility(View.GONE);
                signIn();
                break;
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        progress.setVisibility(View.GONE);
        mSignInButton.setVisibility(View.VISIBLE);
        Toast.makeText(this, getString(R.string.google_play_error),Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                progress.setVisibility(View.GONE);
                mSignInButton.setVisibility(View.VISIBLE);
                Toast.makeText(this, R.string.impossible_connection, Toast.LENGTH_SHORT).show();
            }catch(Exception ex){
                progress.setVisibility(View.GONE);
                mSignInButton.setVisibility(View.VISIBLE);
                Toast.makeText(this, R.string.impossible_connection, Toast.LENGTH_LONG).show();

            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            FirebaseUser user = mAuth.getCurrentUser();
                            User tmpUser = User.getInstance();
                            tmpUser.setLogged(true);
                            if (user.getPhotoUrl() != null) {
                                tmpUser.setPhotoUser(user.getPhotoUrl().toString());
                            }
                            tmpUser.setUserName(user.getDisplayName());
                            tmpUser.setmGoogleApiClient(mGoogleSignInClient);
                            tmpUser.setmFirebaseUser(user);
                            startActivity(new Intent(SignInActivity.this, NavigateChunk.class));
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            progress.setVisibility(View.GONE);
                            mSignInButton.setVisibility(View.VISIBLE);
                            Snackbar.make(findViewById(R.id.sign_in_layout), getString(R.string.authentication_error), Snackbar.LENGTH_SHORT).show();
                        }

                        // ...
                    }
                });
    }

    }

