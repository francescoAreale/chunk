package com.chunk.ereafra.chunk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.chunk.ereafra.chunk.Model.PlaceModel.AddressFromNetwork;
import com.chunk.ereafra.chunk.Model.PlaceModel.AutoCompleteAdapter;
import com.chunk.ereafra.chunk.Model.PlaceModel.MapChunk;
import com.chunk.ereafra.chunk.Model.PlaceModel.Place;
import com.chunk.ereafra.chunk.Utils.FirebaseUtils;
import com.chunk.ereafra.chunk.Utils.LoginUtils;
import com.chunk.ereafra.chunk.Utils.NetworkUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.osmdroid.api.IMapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class NavigateChunk extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, MapSearchChunk.OnFragmentInteractionListener {

    private static String TAG = "NavigateChunk";
    public MapChunk mapChunk= null;
    private FloatingActionButton fab ;
    private FloatingActionButton fab2 ;
    public  ProgressBar pbar = null;
    @Override
    protected void onStart() {
        super.onStart();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LoginUtils.performLoginWithGoogle(this, this, this);
        setContentView(R.layout.activity_navigate_chunk);
        mapChunk = new MapChunk(this,R.id.map_explore_chunk);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        pbar = (ProgressBar) findViewById(R.id.progressBar);
        pbar.setVisibility(View.INVISIBLE);
        //GPSutils.checkGpsStatus(this);
        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        Drawable myFabSrc = getDrawable(android.R.drawable.ic_menu_compass);
//copy it in a new one
        Drawable willBeWhite = myFabSrc.getConstantState().newDrawable();
//set the color filter, you can use also Mode.SRC_ATOP
        willBeWhite.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
//set it to your fab button initialized before
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setImageDrawable(willBeWhite);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!NetworkUtils.isOnline(NavigateChunk.this))
                {
                    Toast.makeText(NavigateChunk.this, R.string.impossible_connection, Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(NavigateChunk.this, R.string.loading_chunk, Toast.LENGTH_SHORT).show();
                mapChunk.setMapToCenter();
                mapChunk.loadCurrentChunkOnActualPosition();
            }
        });

        fab2 = (FloatingActionButton) findViewById(R.id.fab_load_center);
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                    mapChunk.loadCurrentChunkOnCenterPosition();
                    Toast.makeText(NavigateChunk.this,R.string.loading_chunk_actual_position,Toast.LENGTH_SHORT).show();
            }
        });



        //mapChunk.initializeOSM();
       mapChunk.getmLocationOverlay().runOnFirstFix(new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                                  public void run() {
                                      pbar.setVisibility(View.INVISIBLE);
                                      mapChunk.setMapToCenter();
                                      mapChunk.loadCurrentChunkOnActualPosition();
                                  }
                              });

            }
        });

       // if(mapChunk.getmLocationOverlay().getMyLocation()==null)
         mapChunk.parseCoordinatesReceived();
         mapChunk.loadCurrentChunkOnActualPosition();

        final AutoCompleteTextView countrySearch = (AutoCompleteTextView) findViewById(R.id.search);
        final AutoCompleteAdapter adapter = new AutoCompleteAdapter(this,android.R.layout.simple_dropdown_item_1line);
        countrySearch.setAdapter(adapter);

        //when autocomplete is clicked
        countrySearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Place place = adapter.getItem(position);
                String countryName = place.getDisplayName();
                String search_to_display = countryName.substring(0,10) + getString(R.string.etc);
                countrySearch.setText(search_to_display);
                mapChunk.setCenterOnTheMap(Double.parseDouble(place.getLat()),Double.parseDouble(place.getLon()));
                mapChunk.loadCurrentChunkOnCenterPosition();
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });


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
        if(id == R.id.sign_out_menu)
            LoginUtils.performSignOut(this);

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

    public void onResume(){
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        if(mapChunk!=null)
        {
            mapChunk.getMap().onResume();
            //mapChunk.loadCurrentChunkOnActualPosition();
        }
            //needed for compass, my location overlays, v6.0.0 and up
    }

    public void onPause(){
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);

        if(mapChunk!=null)
            mapChunk.getMap().onPause();  //needed for compass, my location overlays, v6.0.0 and up
            //baseUtils.stopGeoListening();
    }
}
