package com.chunk.ereafra.chunk.Model.PlaceModel;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.Volley;
import com.chunk.ereafra.chunk.R;
import com.chunk.ereafra.chunk.Utils.NetworkUtils;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class AutoCompleteAdapter extends ArrayAdapter implements Filterable {
    private ArrayList<Place> mCountry;
    RequestQueue queue = null;

    public AutoCompleteAdapter(Context context, int resource) {
        super(context, resource);
        mCountry = new ArrayList<Place>();
        queue = Volley.newRequestQueue(context);
    }

    @Override
    public int getCount() {
        return mCountry.size();
    }

    @Override
    public Place getItem(int position) {
        return (Place) mCountry.get(position);
    }

    @Override
    public Filter getFilter() {
        Filter myFilter = new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults filterResults = new FilterResults();
                if(constraint != null){
                    try{
                        //get data from the web
                        String term = constraint.toString();
                       // mCountry = parseNominatiumAddress(term);
                        mCountry = (ArrayList<Place>) new DownloadCountry().execute(term).get();
                    }catch (Exception e){
                        Log.d("HUS","EXCEPTION "+e);
                    }
                    filterResults.values = mCountry;
                    filterResults.count = mCountry.size();
                }
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if(results != null && results.count > 0){
                    notifyDataSetChanged();
                }else{
                    notifyDataSetInvalidated();
                }
            }
        };

        return myFilter;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.auto_complete,parent,false);

        //get Country
        Place contry = mCountry.get(position);

        TextView countryName = (TextView) view.findViewById(R.id.countryName);

        countryName.setText(contry.getDisplayName());

        return view;
    }

   /* public ArrayList<Place> parseNominatiumAddress(String addres)
    {
        JSONArray response = null;
        ArrayList<Place> places = null;
        RequestFuture<JSONArray> future = RequestFuture.newFuture();
        String URL = NetworkUtils.buildUrlForAddressTranslationToLatandLong(addres).toString();
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET,
                URL,
                new JSONArray(),
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        return;
                    }
                },
                new Response.ErrorListener(){
                    @Override
                    public void onErrorResponse(VolleyError error){
                        // Do something when error occurred

                    }
                })
        {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("User-Agent", "Mozilla/5.0 (Linux; Android 6.0.1; CPH1607 Build/MMB29M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/63.0.3239.111 Mobile Safari/537.36");
                return params;
            }
        };

        future.setRequest(request);
        queue.add(request);

        try {
             response = future.get(); // this will block
            Type type = new TypeToken<ArrayList<Place>>() {
            }.getType();
            places = new Gson().fromJson(response.toString(), type);

        } catch (InterruptedException e) {
            // exception handling
        } catch (ExecutionException e) {
            // exception handling
            Log.d("Nominatium","failedQuery "+e.toString());
        }
            return places;
    }*/




    //download mCountry list
    private class DownloadCountry extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] params)   {
            try {
                //Create a new COUNTRY SEARCH url Ex "search.php?term=india"
                URL url = NetworkUtils.buildUrlForAddressTranslationToLatandLong((String)params[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 6.0.1; CPH1607 Build/MMB29M; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/63.0.3239.111 Mobile Safari/537.36");
                InputStream in = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                StringBuilder sb = new StringBuilder();
                while ((line = reader.readLine()) != null){
                    sb.append(line).append("\n");
                }

                //parse JSON and store it in the list
                String jsonString =  sb.toString();
                ArrayList<Place> countryList = new ArrayList<Place>();

                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jo = jsonArray.getJSONObject(i);
                    //store the country name
                    Type type = new TypeToken<Place>() {
                    }.getType();
                    Place place = new Gson().fromJson(jo.toString(), type);
                    countryList.add(place);
                }

                //return the countryList
                return countryList;

            } catch (Exception e) {
                Log.d("HUS", "EXCEPTION " + e);
                return null;
            }
        }

    }
}