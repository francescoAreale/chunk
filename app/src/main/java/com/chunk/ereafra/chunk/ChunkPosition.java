package com.chunk.ereafra.chunk;

import android.content.Intent;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
                if (!GPSutils.checkLocationPermission(ChunkPosition.this))
                    return;
                GPSutils.checkGpsStatus(ChunkPosition.this);
                mapController.animateTo(mLocationOverlay.getMyLocation());
                mapController.setCenter(mLocationOverlay.getMyLocation());
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
        initializeOSM();
    }

    public void initializeOSM() {
        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        lastLatitude = 0.0;
        lastLongitude = 0.0;
        map.setZoomRounding(false);
        if (mLocationOverlay == null) {
            mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(ChunkPosition.this), map);
            map.getOverlays().add(mLocationOverlay);
            mapController = map.getController();
            mLocationOverlay.enableMyLocation();
            mLocationOverlay.enableFollowLocation();
            mapController.setZoom(20);
            mapController.animateTo(mLocationOverlay.getMyLocation());
            mapController.setCenter(mLocationOverlay.getMyLocation());
            map.addMapListener(new MapListener() {
                @Override
                public boolean onScroll(ScrollEvent event) {
                    lastLatitude = map.getMapCenter().getLatitude();
                    lastLongitude = map.getMapCenter().getLongitude();
                    textPosition.setText("(" + arrotondaPerDifetto(map.getMapCenter().getLongitude(), 6) + ","
                            + arrotondaPerDifetto(map.getMapCenter().getLatitude(), 6) + ")");
                    Log.d("scroll", "lat: " + map.getMapCenter().getLatitude());
                    return true;
                }

                @Override
                public boolean onZoom(ZoomEvent event) {
                    return false;
                }
            });
        }
    }

}
