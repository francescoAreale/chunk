package com.chunk.ereafra.chunk;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.chunk.ereafra.chunk.Utils.FirebaseUtils;
import com.chunk.ereafra.chunk.Utils.GPSutils;
import com.chunk.ereafra.chunk.Utils.LoginUtils;
import com.chunk.ereafra.chunk.Utils.NetworkUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;

public class NavigateChunk extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, MapSearchChunk.OnFragmentInteractionListener {

    private static String TAG = "NavigateChunk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate_chunk);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);
        // LoginUtils.performSignOut(this);
        LoginUtils.performLoginWithGoogle(this, this, this);
        //
        GPSutils.checkLocationPermission(this);
        GPSutils.checkGpsStatus(this);
        GPSutils.checkStoragePermission(this);
        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }
            // Create a new Fragment to be placed in the activity layout
            MapSearchChunk firstFragment = new MapSearchChunk();
            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            firstFragment.setArguments(getIntent().getExtras());

            // Add the fragment to the 'fragment_container' FrameLayout
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fragment_container, firstFragment).commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.addChunk) {
            // do something here
            startActivity(new Intent(NavigateChunk.this, NewChunk.class));
        }
        if (id == R.id.your_chunk)
            startActivity(new Intent(NavigateChunk.this, PersonalChunks.class));
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, NetworkUtils.ERROR_CONNECTION_GOOGLE_PLAY, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
