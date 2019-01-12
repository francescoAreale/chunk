package com.chunk.ereafra.chunk;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.chunk.ereafra.chunk.Model.Entity.Chunk;
import com.chunk.ereafra.chunk.Model.PlaceModel.Place;
import com.chunk.ereafra.chunk.Utils.GPSutils;
import com.chunk.ereafra.chunk.Utils.NetworkUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import static com.chunk.ereafra.chunk.Utils.FirebaseUtils.insertChunkOnDB;
import static com.chunk.ereafra.chunk.Utils.GPSutils.MY_PERMISSIONS_REQUEST_LOCATION;


public class NewChunk extends AppCompatActivity {

    MapView map = null;
    private static final String TAG = "NewChunk";
    private static final int REQUEST_IMAGE = 2;
    private static final int REQUEST_POSITION = 100;
    IMapController mapController = null;

    Uri imagePicURI = null;
    RequestQueue queue = null;

    FloatingActionButton fabPhotoChunk = null;
    EditText titleChunk = null;
    TextView positionsTextView = null;
    ImageView imagePic;
    ImageButton commitButton = null;
    FloatingActionButton positionButton = null;

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {
                        GPSutils.checkGpsStatus(this);
                    }
                }
                return;
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_new_chunk);
        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        positionsTextView = (TextView) findViewById(R.id.positionsChunk);
        titleChunk = (EditText) findViewById(R.id.editChunkTitle);
        positionButton = (FloatingActionButton) findViewById(R.id.positionChunk);
        setSupportActionBar(toolbar);
        getSupportActionBar().hide();
        initializeOSM();
        initializeImageChunkAndButton();
        GPSutils.asksForAllPermission(this);
        GPSutils.checkGpsStatus(this);
        initializeCommitButton();

        positionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(NewChunk.this, ChunkPosition.class), REQUEST_POSITION);
            }
        });
        queue = Volley.newRequestQueue(this);
    }

    public void initializeImageChunkAndButton() {

        fabPhotoChunk = (FloatingActionButton) findViewById(R.id.fabPhotoChunk);
        Drawable myFabSrc = getDrawable(android.R.drawable.ic_menu_camera);
//copy it in a new one
        Drawable willBeWhite = myFabSrc.getConstantState().newDrawable();
//set the color filter, you can use also Mode.SRC_ATOP
        willBeWhite.mutate().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
        fabPhotoChunk.setImageDrawable(willBeWhite);
        imagePic = (ImageView) findViewById(R.id.imageChunk);
        fabPhotoChunk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_IMAGE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_IMAGE:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        // set the image selected from the user
                        final Uri uri = data.getData();
                        imagePicURI = uri;
                        Log.d(TAG, "Uri: " + uri.toString());
                        imagePic.setImageURI(imagePicURI);
                    }
                }
                break;
            case REQUEST_POSITION:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        Double latitude = data.getExtras().getDouble(ChunkPosition.LATITUDE_RESULT);
                        Double longitude = data.getExtras().getDouble(ChunkPosition.LONGITUDE_RESULT);
                        GeoPoint locationOnMap = new GeoPoint(latitude, longitude);
                        mapController.animateTo(locationOnMap);
                        mapController.setCenter(locationOnMap);
                        positionsTextView.setVisibility(View.VISIBLE);
                        map.setVisibility(View.VISIBLE);
                        Marker startMarker = new Marker(map);
                        startMarker.setPosition(locationOnMap);
                        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        map.getOverlays().clear();
                        map.getOverlays().add(startMarker);
                        parseCoordinatesReceived(latitude, longitude);
                    }
                }
            default:
                break;
        }
        ;
    }

    public void initializeOSM() {

        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(false);
        map.setMultiTouchControls(true);
        mapController = map.getController();
        mapController.setZoom(20);
    }

    private boolean commitOnFirebase() {

        if (titleChunk.getText().toString().equals("")) {
            Toast.makeText(this, "Insert a Title", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (imagePicURI == null) {
            Toast.makeText(this, "Select an image for your chunk", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (map.getMapCenter().getLatitude() == 0.0 || map.getMapCenter().getLongitude() == 0.0) {
            Toast.makeText(this, "Select a position for your chunk", Toast.LENGTH_SHORT).show();
            return false;
        }

        final Chunk newChunk = new Chunk(null,
                titleChunk.getText().toString(),
                (System.currentTimeMillis() / 1000),
                map.getMapCenter().getLatitude(),
                map.getMapCenter().getLongitude(),
                null, null);

        insertChunkOnDB(newChunk, imagePicURI);
        return true;
    }

    private void initializeCommitButton() {
        commitButton = (ImageButton) findViewById(R.id.sendChunk);
        Drawable myIcon = getDrawable(R.drawable.start_new_chunk_little);
        /*myIcon.setBounds(0, 0, (int)(myIcon.getIntrinsicWidth()*0.5),
                (int)(myIcon.getIntrinsicHeight()*0.5));
        ScaleDrawable sd = new ScaleDrawable(myIcon, 0, 10F, 10F);*/
        commitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (commitOnFirebase())
                    finish();
            }
        });
    }

    public void parseCoordinatesReceived(Double Latitude, Double Longitude) {
        String request = NetworkUtils.buildUrlForAddressTranslation(Latitude, Longitude).toString();
// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                request,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Type type = new TypeToken<Place>() {
                        }.getType();
                        Place place = new Gson().fromJson(response, type);
                        positionsTextView.setText(place.getDisplayName());
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                positionsTextView.setText(getString(R.string.error_on__request )+ error.getStackTrace());
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("User-Agent", getString(R.string.user_agent_per_nominatium));
                return params;
            }
        };

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public void onResume() {
        super.onResume();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up

    }

    public void onPause() {
        super.onPause();
        //this will refresh the osmdroid configuration on resuming.
        //if you make changes to the configuration, use
        //SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        //Configuration.getInstance().save(this, prefs);
        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up

    }

}
