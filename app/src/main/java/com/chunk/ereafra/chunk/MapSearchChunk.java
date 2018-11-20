package com.chunk.ereafra.chunk;

import android.content.Context;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chunk.ereafra.chunk.Utils.FirebaseUtils;
import com.chunk.ereafra.chunk.Utils.GPSutils;

import org.osmdroid.api.IMapController;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapSearchChunk.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * create an instance of this fragment.
 */
public class MapSearchChunk extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    MapView map = null;
    IMapController mapController = null;
    MyLocationNewOverlay mLocationOverlay = null;
    LocationManager manager = null;
    private OnFragmentInteractionListener mListener;

    public MapSearchChunk() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
   /* public static MapSearchChunk newInstance(String param1, String param2) {
        MapSearchChunk fragment = new MapSearchChunk();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }*/
    public void initializeOSM() {

        map = (MapView) (getView().findViewById(R.id.map));
        map.setTileSource(TileSourceFactory.MAPNIK);
        map.setBuiltInZoomControls(true);
        //  lastLatitude = 0.0;
        // lastLongitude = 0.0;
        map.setZoomRounding(false);
        if (mLocationOverlay == null) {
            mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getContext()), map);
            map.getOverlays().add(mLocationOverlay);
            mapController = map.getController();
            mLocationOverlay.enableMyLocation();
            mLocationOverlay.enableFollowLocation();
            mapController.setZoom(20);
            mapController.animateTo(mLocationOverlay.getMyLocation());
            mapController.setCenter(mLocationOverlay.getMyLocation());
            FirebaseUtils.getChunkAroundLocation(40.438293, -3.714093, 1.0, map);
            map.addMapListener(new MapListener() {
                @Override
                public boolean onScroll(ScrollEvent event) {
                   /* lastLatitude = map.getMapCenter().getLatitude();
                    lastLongitude = map.getMapCenter().getLongitude();
                    textPosition.setText("(" + arrotondaPerDifetto(map.getMapCenter().getLongitude(), 6) + ","
                            + arrotondaPerDifetto(map.getMapCenter().getLatitude(), 6) + ")");*/
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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        initializeOSM();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map_search_chunk, container, false);
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
