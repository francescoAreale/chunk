package com.chunk.ereafra.chunk;

import android.app.Activity;
import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chunk.ereafra.chunk.Model.PlaceModel.AutoCompleteAdapter;
import com.chunk.ereafra.chunk.Model.PlaceModel.MapChunk;
import com.chunk.ereafra.chunk.Model.PlaceModel.Place;
import com.chunk.ereafra.chunk.Utils.GPSutils;
import com.chunk.ereafra.chunk.Utils.NetworkUtils;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

public class ChunkPosition extends AppCompatActivity {

    MapView map = null;
    IMapController mapController = null;
    MyLocationNewOverlay mLocationOverlay = null;
    LocationManager manager = null;
    TextView textPosition = null;
    LinearLayout sendLayoutButton = null;
    Double lastLatitude = null;
    Double lastLongitude = null;
    MapChunk mapChunk ;
    public static final String LATITUDE_RESULT = "latitude";
    public static final String LONGITUDE_RESULT = "longitude";
    // ARROTONDAMENTO PER DIFETTO
    public static double arrotondaPerDifetto(double value, int numCifreDecimali) {
        double temp = Math.pow(10, numCifreDecimali);
        return Math.floor(value * temp) / temp;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chunk_position);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        textPosition = (TextView) findViewById(R.id.positionLatLong);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!NetworkUtils.isOnline(ChunkPosition.this))
                {
                    Toast.makeText(ChunkPosition.this, R.string.impossible_connection, Toast.LENGTH_SHORT).show();
                }
                Toast.makeText(ChunkPosition.this, R.string.loading_chunk, Toast.LENGTH_SHORT).show();
                mapChunk.setMapToCenter();
               // mapChunk.loadCurrentChunkOnActualPosition();
            }
        });
        sendLayoutButton = (LinearLayout) findViewById(R.id.sendPosition);
        sendLayoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (lastLatitude == 0.0 && lastLongitude == 0.0)
                    return;
                Intent data = new Intent();
                data.putExtra(LATITUDE_RESULT, arrotondaPerDifetto(lastLatitude, 8));
                data.putExtra(LONGITUDE_RESULT, arrotondaPerDifetto(lastLongitude, 8));
                setResult(RESULT_OK, data);
                finish();
            }
        });
        lastLatitude = 0.0;
        lastLongitude = 0.0;
        mapChunk = new MapChunk(this,R.id.map);
        mapChunk.getMap().addMapListener(new MapListener() {
            @Override
            public boolean onScroll(ScrollEvent event) {
                lastLatitude = mapChunk.getMap().getMapCenter().getLatitude();
                lastLongitude = mapChunk.getMap().getMapCenter().getLongitude();
                textPosition.setText("(" + arrotondaPerDifetto(mapChunk.getMap().getMapCenter().getLongitude(), 6) + ","
                        + arrotondaPerDifetto(mapChunk.getMap().getMapCenter().getLatitude(), 6) + ")");
                return true;
            }

            @Override
            public boolean onZoom(ZoomEvent event) {
                return false;
            }
        });

        mapChunk.getmLocationOverlay().runOnFirstFix(new Runnable() {
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        mapChunk.setMapToCenter();
                        //mapChunk.loadCurrentChunkOnActualPosition();
                    }
                });

            }
        });

        mapChunk.parseCoordinatesReceived();

        final AutoCompleteTextView countrySearch = (AutoCompleteTextView) findViewById(R.id.search);
        final AutoCompleteAdapter adapter = new AutoCompleteAdapter(this,android.R.layout.simple_dropdown_item_1line);
        countrySearch.setAdapter(adapter);

        //when autocomplete is clicked
        countrySearch.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Place place = adapter.getItem(position);
                String countryName = place.getDisplayName();
                countrySearch.setText(countryName.substring(0,10) + getString(R.string.etc));
                mapChunk.setCenterOnTheMap(Double.parseDouble(place.getLat()),Double.parseDouble(place.getLon()));
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });
    }



}
