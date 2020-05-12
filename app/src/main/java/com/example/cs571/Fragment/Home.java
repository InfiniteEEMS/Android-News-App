package com.example.cs571.Fragment;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.cs571.DetailActivity;
import com.example.cs571.MyApplication;
import com.example.cs571.NewsItem;
import com.example.cs571.Adapter.NewsListAdapter;
import com.example.cs571.R;
import com.example.cs571.Utilities;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


/**
 * A simple {@link Fragment} subclass.
 */
public class Home extends Fragment  {


    //public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    LocationManager locationManager;
    String provider;

    private View frag_home;
    private Context mContext;

    private ArrayList<NewsItem> news;
    private RecyclerView newsRecycler;
    private NewsListAdapter newsAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private LocationListener locationListener;

    private RequestQueue q;
    private LinearLayout spinner;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private final int REQUEST_LOCATION_PERMISSION = 1;


    public Home() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        frag_home = inflater.inflate(R.layout.fragment_home, container, false);
        mContext = getContext();


        /* recycler & adapter*/
        news = new ArrayList<>();
        newsRecycler = frag_home.findViewById(R.id.c_news_list);
        mLayoutManager = new LinearLayoutManager(getContext());
        newsRecycler.setLayoutManager(mLayoutManager);
        newsAdapter = new NewsListAdapter(news, newsRecycler);
        newsRecycler.setAdapter(newsAdapter);

        newsAdapter.setOnItemClickListener(new NewsListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                switch (view.getId()) {
                    case R.id.c_news_bookmark:
                        changeBookmark(news.get(position));
                        break;
                    default:
                        Intent mIntent = new Intent(view.getContext(), DetailActivity.class);
                        mIntent.putExtra("id", news.get(position).getId());
                        startActivity(mIntent);
                        break;
                }
            }

            @Override
            public void onItemLongClick(View view, int position) {
                //Toast.makeText(getContext(), "点长击" + position, Toast.LENGTH_SHORT).show();
                showDialog(news.get(position), view.getContext());
            }
        });



        /* spinner & refresher*/
        spinner = frag_home.findViewById(R.id.c_progress_bar);
        mSwipeRefreshLayout = frag_home.findViewById(R.id.c_swipe_refresh);
        q = Volley.newRequestQueue(getContext());
        // set listener to swipe
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //spinner.setVisibility(View.VISIBLE);
                //newsRecycler.setVisibility(View.GONE);
                update_news();
            }
        });



        /*Go Doing*/
        spinner.setVisibility(View.VISIBLE);
        newsRecycler.setVisibility(View.GONE);
        update_news();


        // request premission
        locationManager = (LocationManager) getContext().getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                List<Address> addresses = null;
                try {
                    addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    Log.d("LOCATION", addresses.toString());

                    String cityName = addresses.get(0).getLocality();
                    String stateName = addresses.get(0).getAdminArea();
                    MyApplication.city = cityName;
                    MyApplication.state = stateName;
                    Log.d("LOCATION", "setting..." + MyApplication.city + " || " + MyApplication.state);

                } catch (IOException e) {

                    MyApplication.city = "Los Angeles";
                    MyApplication.state = "California";
                    Log.d("LOCATION", "EXCEPTION!!! \n setting..." + MyApplication.city + " || " + MyApplication.state);

                    e.printStackTrace();
                }


                update_weather();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        }else if(MyApplication.city == null){
            requestLocationPermission();
        }else{
            update_weather();
        }


        return frag_home;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        // Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @AfterPermissionGranted(REQUEST_LOCATION_PERMISSION)
    public void requestLocationPermission() {
        String[] perms = {Manifest.permission.ACCESS_FINE_LOCATION};
        if (EasyPermissions.hasPermissions(getContext(), perms)) {
            //Toast.makeText(getContext(), "Permission already granted", Toast.LENGTH_SHORT).show();
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "PLEASE GRANT PERMISSION!!!", Toast.LENGTH_SHORT ).show();
                return;
            }
            Log.d("LOCATION", "PREPARE TO CALL");
            Log.d("LOCATION", "provider:" + provider);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 1, locationListener);
        }
        else {
            EasyPermissions.requestPermissions(this, "Please grant the location permission", REQUEST_LOCATION_PERMISSION, perms);
        }
    }





    @Override
    public void onResume() {
        Utilities.updateNewsListFromPreference(news);
        newsAdapter.notifyDataSetChanged();
        super.onResume();
    }

    // fetch url and store them as obj in arrayList news. notifyDataChange -> onBindViewHolder
    private void update_news() {

        String url = Utilities.homepage_url;
        Log.d("URL", "querying... " + url);

        JsonObjectRequest request = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        news.clear();
                        try {
                            JSONArray arr = response.getJSONArray("data");
                            for (int i = 0; i < arr.length(); i++) {
                                JSONObject n = arr.getJSONObject(i);
                                NewsItem cur = new NewsItem();
                                cur.setId(n.getString("id"));
                                cur.setDate(n.getString("date"));
                                cur.setSection(n.getString("section"));
                                cur.setImage(n.getString("image"));
                                cur.setTitle(n.getString("title"));
                                cur.setShare_url(n.getString("share_url_detail"));
                                if (Utilities.checkNewsInPreference(cur.getId())) {
                                    cur.setMarked(true);
                                } else {
                                    cur.setMarked(false);
                                }
                                news.add(cur);
                            }
                            spinner.setVisibility(View.GONE);
                            if (mSwipeRefreshLayout.isRefreshing()) {
                                mSwipeRefreshLayout.setRefreshing(false);
                            } else {
                                newsRecycler.setVisibility(View.VISIBLE);
                            }
                            newsAdapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        q.add(request);
    }

    // fetch weather data. Set data and image in c_weather_card
    private void update_weather() {
        String city = MyApplication.city;
        String url = "https://api.openweathermap.org/data/2.5/weather?q=" + city + "&units=metric&appid=e627a2f61748e2cce8dd176abba50dd9";
        Log.d("URL", "querying...." + url);

        JsonObjectRequest request = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String w = response.getJSONArray("weather").getJSONObject(0).getString("main");
                            Integer t = response.getJSONObject("main").getInt("temp");
                            TextView weather = frag_home.findViewById(R.id.c_weather_weather);
                            TextView temp = frag_home.findViewById(R.id.c_weather_temperature);
                            TextView city = frag_home.findViewById(R.id.c_weather_city);
                            TextView state = frag_home.findViewById(R.id.c_weather_state);
                            city.setText(MyApplication.city);
                            state.setText(MyApplication.state);

                            ImageView weather_card = frag_home.findViewById(R.id.c_weather_image);
                            if (w.toLowerCase().equals("clear")) {
                                weather_card.setBackgroundResource(R.drawable.weather_clear);
                            } else if (w.toLowerCase().equals("clouds")) {
                                weather_card.setBackgroundResource(R.drawable.weather_cloudy);
                            } else if (w.toLowerCase().equals("snow")) {
                                weather_card.setBackgroundResource(R.drawable.weather_snowy);
                            } else if (w.toLowerCase().equals("rain") || w.toLowerCase().equals("drizzle")) {
                                weather_card.setBackgroundResource(R.drawable.weather_rainy);
                            } else if (w.toLowerCase().equals("thunderstorm")) {
                                weather_card.setBackgroundResource(R.drawable.weather_thunder);
                            } else {
                                weather_card.setBackgroundResource(R.drawable.weather_sunny);
                            }
                            weather.setText(w);
                            String tmp = t.toString() + " ℃";
                            temp.setText(tmp);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                    }
                });
        q.add(request);
    }


    private void changeBookmark(NewsItem cur) {
        Log.d("DBG", "bookmark clicked");
        SharedPreferences pref = MyApplication.myGetAppContext().getSharedPreferences("favorite", 0);
        SharedPreferences.Editor editor = pref.edit();
        String text = '"' + cur.getTitle() + '"';
        if (cur.isMarked()) {
            Toast.makeText(getContext(), text + " was removed from Bookmarks", Toast.LENGTH_SHORT).show();
            unMark(cur, pref, editor);
        } else {
            Toast.makeText(getContext(), text + " was added to Bookmarks", Toast.LENGTH_SHORT).show();
            doMark(cur, pref, editor);
        }
    }

    private void doMark(NewsItem cur, SharedPreferences pref, SharedPreferences.Editor editor) {
        Log.d("DBG", "enter domark");
        try {
            // create JSON obj
            JSONObject ids = new JSONObject(pref.getString("ids", null));
            JSONArray ids_arr = ids.getJSONArray("ids_arr");
            ids_arr.put(cur.myToJSONObj());
            JSONObject res = new JSONObject();
            res.put("ids_arr", ids_arr);
            // editor write
            editor.putString("ids", res.toString());
            editor.commit();
            Utilities.printCurrentPreference();
            // set new bookmark image and notify data changed.
            cur.setMarked(true);
            newsAdapter.notifyDataSetChanged();
        } catch (JSONException e) {
            Log.e("JSON", "Error in transform string to json in sharePreference ");
            e.printStackTrace();
        }
    }

    private void unMark(NewsItem cur, SharedPreferences pref, SharedPreferences.Editor editor) {
        Log.d("DBG", "enter unmark");
        try {
            // find target obj and remove
            JSONObject ids = new JSONObject(pref.getString("ids", null));
            JSONArray ids_arr = ids.getJSONArray("ids_arr");
            for (int i = 0; i < ids_arr.length(); i++) {
                JSONObject n = ids_arr.getJSONObject(i);
                if (n.getString("id").equals(cur.getId())) {
                    ids_arr.remove(i);
                }
            }
            // construct new obj
            JSONObject res = new JSONObject();
            res.put("ids_arr", ids_arr);
            // editor write
            editor.putString("ids", res.toString());
            editor.commit();
            Utilities.printCurrentPreference();

            // set new bookmark image and notify data changed.
            cur.setMarked(false);
            newsAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            Log.e("JSON", "Error in transform string to json in sharePreference ");
            e.printStackTrace();
        }
    }

    private void showDialog(final NewsItem cur, Context context) {
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog);
        final ImageView dialog_image = dialog.findViewById(R.id.c_dialog_image);
        TextView dialog_title = dialog.findViewById(R.id.c_dialog_title);
        dialog_title.setText(cur.getTitle());
        final ImageView dialog_bookmark = dialog.findViewById(R.id.c_dialog_bookmark);
        final ImageView dialog_twitter = dialog.findViewById(R.id.c_dialog_twitter);
        dialog_twitter.setImageResource(R.drawable.bluetwitter);
        if (cur.isMarked()) {
            dialog_bookmark.setImageResource(R.drawable.bookmark);
        } else {
            dialog_bookmark.setImageResource(R.drawable.bookmark_not);
        }

        dialog_bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBookmark(cur);
                if (cur.isMarked()) {
                    dialog_bookmark.setImageResource(R.drawable.bookmark);
                } else {
                    dialog_bookmark.setImageResource(R.drawable.bookmark_not);
                }
                newsAdapter.notifyDataSetChanged();
            }
        });

        dialog_twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://twitter.com/intent/tweet?url=" + cur.getShare_url() + "&text=" + "Check out this Link：\n";
                url = url + "&hashtags=CSCI571NewsSearch";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });
        Picasso.with(context).load(cur.getImage()).resize(300, 200).into(dialog_image);
        dialog.show();
        ////////
    }





//    @Override
//    public void onLocationChanged(Location location) {
//        Log.d("LOCATION", "FUCK");
//        Toast.makeText(getContext(), "Catch Update", Toast.LENGTH_SHORT ).show();
//    }
//
//    @Override
//    public void onStatusChanged(String provider, int status, Bundle extras) {
//    }
//
//    @Override
//    public void onProviderEnabled(String provider) {
//    }
//
//    @Override
//    public void onProviderDisabled(String provider) {
//    }
//
//
//

//
//    public boolean checkLocationPermission() {
//        if (ContextCompat.checkSelfPermission(getContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//
//            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)getContext(),
//                    Manifest.permission.ACCESS_FINE_LOCATION)) {
//
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//                new AlertDialog.Builder(getContext())
//                        .setTitle("HI")
//                        .setMessage("HI")
//                        .setPositiveButton("HI", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i) {
//                                //Prompt the user once explanation has been shown
//                                requestPermissions(
//                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                                        MY_PERMISSIONS_REQUEST_LOCATION);
//                            }
//                        })
//                        .create()
//                        .show();
//            } else {
//                // No explanation needed, we can request the permission.
//                requestPermissions(
//                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                        MY_PERMISSIONS_REQUEST_LOCATION);
//            }
//            return false;
//        } else {
//            return true;
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode,
//                                           String permissions[], int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case MY_PERMISSIONS_REQUEST_LOCATION: {
////
////                Log.d("LOCATION", permissions.toString());
////                Log.d("LOCATION", grantResults.toString());
////
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    // permission was granted, yay! Do the
//                    // location-related task you need to do.
//                    Log.d("LOCATION", "have > 0 length");
//                    if (ContextCompat.checkSelfPermission( getContext(),
//                            Manifest.permission.ACCESS_FINE_LOCATION)
//                            == PackageManager.PERMISSION_GRANTED) {
//                        //Request location updates:
//                        Log.d("LOCATION", "GRANTED");
//
//                        locationManager.requestLocationUpdates(provider, 400, 1, locationListener);
//                    }
//                } else {
//                    Log.d("LOCATION", "have 0 length");
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                }
//                return;
//            }
//
//        }
//    }

}
