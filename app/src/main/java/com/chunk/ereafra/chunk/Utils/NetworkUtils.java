package com.chunk.ereafra.chunk.Utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {

    public static final String ERROR_CONNECTION_GOOGLE_PLAY = "Google Play Services error...try again with connection";
    private static final int SUCCESFULL_REQUEST = 200;
    private static final String BASE_URL_TRANSLATE_NOMINATIM = "https://nominatim.openstreetmap.org";

    public static URL buildUrlForAddressTranslation(double latitude, double longitude) {
        URL url = null;
        try {
            url = new URL(BASE_URL_TRANSLATE_NOMINATIM + "/reverse?format=jsonv2&" + "lat=" + latitude
                    + "&lon=" + longitude);

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        return url;
    }

    public static URL buildUrlForAddressTranslationToLatandLong(String address) {
        URL url = null;
        try {
            url = new URL(BASE_URL_TRANSLATE_NOMINATIM + "/search/" + address.replace("//s","%20") + "?format=json&addressdetails=1");

        } catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        }
        return url;
    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connMgr != null) {
            NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
            return
                    (netInfo != null && netInfo.isConnected());
        } else
            return false;
    }

}
