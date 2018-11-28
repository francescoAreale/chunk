package com.chunk.ereafra.chunk.Model.PlaceModel;

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
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

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
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import de.hdodenhof.circleimageview.CircleImageView;

public class MapChunk implements VisualizeChunkInterface<Chunk> {

    MapView map = null;
    IMapController mapController = null;
    MyLocationNewOverlay mLocationOverlay = null;
    LocationManager manager = null;
    Context context;

    public MapChunk(Context context) {
        this.context = context;
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

    public MapView getMap() {
        return map;
    }

    public void setMap(MapView map) {
        this.map = map;
    }

    public void initializeOSM(int Idmap, View view) {

        map = (MapView) (view.findViewById(Idmap));
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        map.setZoomRounding(true);
        if (mLocationOverlay == null) {
            mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(context), map);
            map.getOverlays().add(mLocationOverlay);
            mapController = map.getController();
            mLocationOverlay.enableMyLocation();
            mLocationOverlay.enableFollowLocation();
            mapController.setZoom(20);
            mapController.animateTo(mLocationOverlay.getMyLocation());
            mapController.setCenter(mLocationOverlay.getMyLocation());
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
        final CircleImageView imageOfCHunk = new CircleImageView(context);
        Ion.with(context).load(chunk.getImage()).withBitmap().asBitmap()
                .setCallback(new FutureCallback<Bitmap>() {
                    @Override
                    public void onCompleted(Exception e, Bitmap result) {
                        startMarker.setIcon(new BitmapDrawable(context.getResources(), createUserBitmap(chunk, result)));
                        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
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

    public void loadCurrentChunkOnActualPosition() {
        if (!GPSutils.checkLocationPermission((AppCompatActivity) context))
            return;
        GPSutils.checkGpsStatus((AppCompatActivity) context);
        mapController.animateTo(mLocationOverlay.getMyLocation());
        mapController.setCenter(mLocationOverlay.getMyLocation());
        mapController.setZoom(20);
        if (mLocationOverlay.getMyLocation() != null)
            FirebaseUtils.getChunkAroundLocation(mLocationOverlay.getMyLocation().getLatitude(),
                    mLocationOverlay.getMyLocation().getLongitude(), 1.0, this);
    }

    @Override
    public void showChunk(Chunk objectToShow) {
        if (objectToShow.getImage() != null)
            createMarkerChunk(objectToShow);
    }
}
