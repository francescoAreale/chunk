package com.chunk.ereafra.chunk.Model.PlaceModel;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.chunk.ereafra.chunk.ChunkChatActivity;
import com.chunk.ereafra.chunk.ChunkPosition;
import com.chunk.ereafra.chunk.Model.Entity.Chunk;
import com.chunk.ereafra.chunk.Model.Interface.VisualizeChunkInterface;
import com.chunk.ereafra.chunk.R;
import com.chunk.ereafra.chunk.Utils.FirebaseUtils;
import com.chunk.ereafra.chunk.Utils.GPSutils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapChunk implements VisualizeChunkInterface<Chunk> {

    MapView map = null;
    IMapController mapController = null;
    MyLocationNewOverlay mLocationOverlay = null;
    LocationManager manager = null;
    Context context;
    RequestQueue queue = null;

    /** Base path for osmdroid files. Zip files are in this folder. */
    public static File OSMDROID_PATH = new File(Environment.getExternalStorageDirectory(),
            "osmdroid");

    /** Base path for tiles. */
    public static File TILE_PATH_BASE = new File(OSMDROID_PATH, "tiles");

    /** 600 Mb */
    public static long TILE_MAX_CACHE_SIZE_BYTES = 600L * 1024 * 1024;

    /** 500 Mb */
    public static long TILE_TRIM_CACHE_SIZE_BYTES = 500L * 1024 * 1024;

    public static void setCachePath(String newFullPath){
        OSMDROID_PATH = new File(newFullPath);
        TILE_PATH_BASE = new File(OSMDROID_PATH, "tiles");
    }

    /** Change the osmdroid tiles cache sizes
     * @param maxCacheSize in Mb. Default is 600 Mb.
     * @param trimCacheSize When the cache size exceeds maxCacheSize, tiles will be automatically removed to reach this target. In Mb. Default is 500 Mb.
     */
    public static void setCacheSizes(long maxCacheSize, long trimCacheSize){
        TILE_MAX_CACHE_SIZE_BYTES = maxCacheSize * 1024 * 1024;
        TILE_TRIM_CACHE_SIZE_BYTES = trimCacheSize * 1024 * 1024;
    }


    public MapChunk(Context context, int id_map) {
        this.context = context;
        Configuration.getInstance().setExpirationOverrideDuration(365L * 24L * 3600L * 1000L);
        Configuration.getInstance().setTileFileSystemCacheMaxBytes(1024L * 1024L * 1024L * 10L);
        Configuration.getInstance().setTileFileSystemCacheTrimBytes(1024L * 1024L * 1024L * 9L);
        setCachePath("chunk/data/com.chunk.ereafra.chunk");
        Configuration.getInstance().setOsmdroidBasePath(OSMDROID_PATH);
        Configuration.getInstance().setOsmdroidTileCache(OSMDROID_PATH);
        //LinearLayout contentLayout = (LinearLayout)((Activity)contfiext).findViewById(R.id.layout_map);
        //this.map  = new MapView(context);
        this.map = ((Activity)context).findViewById(id_map);
       /* this.map.setTileSource(TileSourceFactory.MAPNIK);
        org.osmdroid.views.MapView.LayoutParams mapParams = new org.osmdroid.views.MapView.LayoutParams(
                org.osmdroid.views.MapView.LayoutParams.MATCH_PARENT,
                org.osmdroid.views.MapView.LayoutParams.MATCH_PARENT,
                null, 0, 0, 0);*/
        initializeOSM();
        queue = Volley.newRequestQueue(context);
       // contentLayout.addView( this.map, mapParams);
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(input);
            return myBitmap;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    public void parseCoordinatesReceived() {

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                "https://ipapi.co/json/",
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Type type = new TypeToken<AddressFromNetwork>() {
                        }.getType();

                        AddressFromNetwork addr = new Gson().fromJson(response, type);
                        if(getmLocationOverlay().getMyLocation()==null) {
                            setCenterOnTheMap(addr.getLatitude(), addr.getLongitude());
                            loadCurrentChunkOnCenterPosition();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "error on getting position from the network", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("User-Agent", "Mozilla/5.0 (Linux; Android 6.0.1; CPH1607 Build/MMB29M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/63.0.3239.111 Mobile Safari/537.36");
                return params;
            }
        };

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public MapView getMap() {
        return map;
    }

    public void setMap(MapView map) {
        this.map = map;
    }

    public void initializeOSM() {

        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setMultiTouchControls(true);
        map.setZoomRounding(true);

        if (mLocationOverlay == null) {
            mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), map);
            map.getOverlays().add(mLocationOverlay);
            mapController = map.getController();
            //mLocationOverlay.enableFollowLocation();
            mLocationOverlay.enableMyLocation();
            mapController.setZoom(15);
            mapController.animateTo(mLocationOverlay.getMyLocation());
            mapController.setCenter(mLocationOverlay.getMyLocation());
            map.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver() {

                @Override
                public boolean singleTapConfirmedHelper(GeoPoint p) {
                    ChunkInfoWindow.closeAllInfoWindowsOn(map);
                    return true;
                }

                @Override
                public boolean longPressHelper(GeoPoint p) {
                    return false;
                }
            }));
        }
    }

    public IMapController getMapController() {
        return mapController;
    }

    public void setMapController(IMapController mapController) {
        this.mapController = mapController;
    }

    public MyLocationNewOverlay getmLocationOverlay() {
        return mLocationOverlay;
    }

    public void setmLocationOverlay(MyLocationNewOverlay mLocationOverlay) {
        this.mLocationOverlay = mLocationOverlay;
    }

    public LocationManager getManager() {
        return manager;
    }

    public void setManager(LocationManager manager) {
        this.manager = manager;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public Marker createMarkerChunk(final Chunk chunk) {
        GeoPoint locationOnMap = new GeoPoint(chunk.getLatitude(), chunk.getLongitude());
        final Marker startMarker = new Marker(map);
        startMarker.setPosition(locationOnMap);
        Ion.with(context).load(chunk.getImage()).withBitmap().asBitmap()
                .setCallback(new FutureCallback<Bitmap>() {
                    @Override
                    public void onCompleted(Exception e, Bitmap result) {
                        startMarker.setIcon(new BitmapDrawable(context.getResources(), createUserBitmap(chunk, result)));
                        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_TOP);
                        startMarker.setInfoWindow(new ChunkInfoWindow(map, result, chunk));
                        map.getOverlays().add(startMarker);
                    }
                });

        return startMarker;
    }

    public int dp(float value) {
        if (value == 0) {
            return 0;
        }
        return (int) Math.ceil(context.getResources().getDisplayMetrics().density * value);
    }

    private Bitmap createUserBitmap(Chunk chunk, Bitmap bitmap) {
        Bitmap result = null;
        try {
            result = Bitmap.createBitmap(dp(100), dp(100), Bitmap.Config.ARGB_8888);
            result.eraseColor(Color.TRANSPARENT);
            Canvas canvas = new Canvas(result);
            Drawable drawable = context.getDrawable(R.drawable.livepin);
            drawable.setBounds(0, 0, dp(90), dp(90));
            drawable.draw(canvas);

            Paint roundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            RectF bitmapRect = new RectF();
            canvas.save();

            if (bitmap != null) {
                BitmapShader shader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
                Matrix matrix = new Matrix();
                float scale = dp(70) / (float) bitmap.getWidth();
                matrix.postTranslate(dp(5), dp(5));
                matrix.postScale(scale, scale);
                roundPaint.setShader(shader);
                shader.setLocalMatrix(matrix);
                bitmapRect.set(dp(5), dp(5), dp(50), dp(50));
                bitmapRect.offset(dp(18), dp(4));
                canvas.drawRoundRect(bitmapRect, dp(60), dp(60), roundPaint);

            }
            canvas.restore();
            try {
                canvas.setBitmap(null);
            } catch (Exception e) {
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return result;
    }

    public void setCenterOnTheMap(double lat, double longitude){
        IMapController mapController = map.getController();
        GeoPoint locationOnMap = new GeoPoint(lat, longitude);
        mapController.animateTo(locationOnMap);
        mapController.setCenter(locationOnMap);
        map.setVisibility(View.VISIBLE);
        Marker startMarker = new Marker(map);
        startMarker.setPosition(locationOnMap);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        map.getOverlays().clear();
        map.getOverlays().add(startMarker);
        map.getOverlays().add(new MapEventsOverlay(new MapEventsReceiver() {

            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                ChunkInfoWindow.closeAllInfoWindowsOn(map);
                return true;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                return false;
            }
        }));
    }

    public void setMapToCenter(){
        if(mLocationOverlay.getMyLocation()!=null)
        {
            this.setCenterOnTheMap(mLocationOverlay.getMyLocation().getLatitude(), mLocationOverlay.getMyLocation().getLongitude());
        }
    }

    public void loadCurrentChunkOnCenterPosition() {

            FirebaseUtils.getChunkAroundLocation(map.getMapCenter().getLatitude(),
                    map.getMapCenter().getLongitude(), 2.0, this);
    }

    public void loadCurrentChunkOnActualPosition() {
        GPSutils.asksForAllPermission((AppCompatActivity) context);
        GPSutils.checkGpsStatus((AppCompatActivity) context);
        //mapController.animateTo(mLocationOverlay.getMyLocation());
        //mapController.setCenter(mLocationOverlay.getMyLocation());
        //mapController.setZoom(20);
        if (mLocationOverlay.getMyLocation() != null)
            FirebaseUtils.getChunkAroundLocation(mLocationOverlay.getMyLocation().getLatitude(),
                    mLocationOverlay.getMyLocation().getLongitude(), 2.0, this);
    }

    @Override
    public void showChunk(Chunk objectToShow) {
        if (objectToShow.getImage() != null)
            createMarkerChunk(objectToShow);
    }
}
