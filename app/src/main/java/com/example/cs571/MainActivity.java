package com.example.cs571;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuItemCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.cs571.Adapter.AutoSuggestionAdapter;
import com.example.cs571.Adapter.OuterPagerAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 最外层activity。
 * Layout包含了SearchBar, BottomNavigation和中间的ViewPager
 */

public class MainActivity extends AppCompatActivity {



    private HashMap<String, Integer> id_to_index;
    private static final int TRIGGER_AUTO_COMPLETE = 100;
    private static final long AUTO_COMPLETE_DELAY = 300;
    private Handler handler;
    private AutoSuggestionAdapter autoSuggestAdapter;
    private RequestQueue q;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        q = Volley.newRequestQueue(this);
        mContext = this;

        Toolbar myToolbar = (Toolbar) findViewById(R.id.c_toolBar);
        setSupportActionBar(myToolbar);
//        actionBar.setDisplayShowHomeEnabled(true);
//        myToolbar.setDisplayUseLogoEnabled(true);
//        myToolbar.setLogo(R.drawable.bookmark);
//        myToolbar.setTitle("dev2qa.com - Search Example");

        final ViewPager viewPagerOuter = findViewById(R.id.c_view_pager_outer);
        viewPagerOuter.setOffscreenPageLimit(1);
        final PagerAdapter pagerAdapterOuter = new OuterPagerAdapter(getSupportFragmentManager());
        viewPagerOuter.setAdapter(pagerAdapterOuter);


        BottomNavigationView bottomNavigation = findViewById(R.id.c_bottom_navigator);
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    case R.id.c_home:
                        viewPagerOuter.setCurrentItem(0);
                        //pagerAdapterOuter.notifyDataSetChanged();
                        return true;
                    case R.id.c_headlines:
                        viewPagerOuter.setCurrentItem(1);
                        //pagerAdapterOuter.notifyDataSetChanged();
                        return true;
                    case R.id.c_trending:
                        viewPagerOuter.setCurrentItem(2);
                        //pagerAdapterOuter.notifyDataSetChanged();
                        return true;
                    case R.id.c_bookmarks:
                        viewPagerOuter.setCurrentItem(3);
                        //pagerAdapterOuter.notifyDataSetChanged();
                        return true;
                }
                return false;
            }
        });


        viewPagerOuter.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                // Tell the adapter dataset is changed
                pagerAdapterOuter.notifyDataSetChanged();
            }
            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        initSharedPreferenceData();


    }


    @SuppressLint("ApplySharedPref")
    private void initSharedPreferenceData(){
        SharedPreferences pref = MyApplication.myGetAppContext().getSharedPreferences("favorite", 0);
        SharedPreferences.Editor editor = pref.edit();

//        //TODO: TEMORARY TEST
//        editor.clear();
//        editor.commit();

        if(pref.getString("ids", null) == null){
            JSONObject ids = new JSONObject();
            JSONArray ids_arr = new JSONArray();

            try {
                ids.put("ids_arr", ids_arr);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            editor.putString("ids", ids.toString());
            editor.commit();
        };
        Utilities.printCurrentPreference();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_activity_search_bar, menu);

        MenuItem searchMenu = menu.findItem(R.id.app_bar_menu_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchMenu);

        final AutoCompleteTextView searchAutoCompleteView = searchView.findViewById(androidx.appcompat.R.id.search_src_text);


        autoSuggestAdapter = new AutoSuggestionAdapter(this, android.R.layout.simple_dropdown_item_1line);
        searchAutoCompleteView.setThreshold(1);
        searchAutoCompleteView.setAdapter(autoSuggestAdapter);


        searchAutoCompleteView.setOnItemClickListener(
                new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        searchView.setQuery(autoSuggestAdapter.getItem(position), false);
                        //searchView.clearFocus();
                        //Toast.makeText(view.getContext(), "in list select:" + autoSuggestAdapter.getItem(position), Toast.LENGTH_SHORT).show();
                    }
                });


        searchAutoCompleteView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int
                    count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                handler.removeMessages(TRIGGER_AUTO_COMPLETE);
                handler.sendEmptyMessageDelayed(TRIGGER_AUTO_COMPLETE,
                        AUTO_COMPLETE_DELAY);
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        handler = new Handler(new Handler.Callback() {

            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == TRIGGER_AUTO_COMPLETE) {
                    if (!Utilities.isEmptyString(searchAutoCompleteView.getText().toString())) {
                        get_suggestions(searchAutoCompleteView.getText().toString());
                    }
                }
                return false;
            }
        });


//        searchAutoComplete.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> adapterView, View view, int itemIndex, long id) {
//                String queryString=(String)adapterView.getItemAtPosition(itemIndex);
//                searchAutoComplete.setText("" + queryString);
//                Toast.makeText(MainActivity.this, "you clicked " + queryString, Toast.LENGTH_LONG).show();
//            }
//        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
//                AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
//                alertDialog.setMessage("Search keyword is " + query);
//                alertDialog.show();
                Intent mIntent = new Intent(mContext, SearchActivity.class);
                mIntent.putExtra("keyword", query);
                startActivity(mIntent);

                return false;
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                Toast.makeText(MainActivity.this, "you changed test into: " + newText, Toast.LENGTH_SHORT).show();
//                return false;
//            }
        });

        return super.onCreateOptionsMenu(menu);
    }


    private void get_suggestions(String s) {

        String url = "https://571hw4-autosuggest.cognitiveservices.azure.com/bing/v7.0/suggestions?q=" + s;

        final List<String> res = new ArrayList<String>();

        JsonObjectRequest request = new JsonObjectRequest(url, null,

                    // success
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                JSONArray suggestionGroups = response.getJSONArray("suggestionGroups");
                                JSONArray searchSuggestions = suggestionGroups.getJSONObject(0).getJSONArray("searchSuggestions");
                                for(int i = 0; i<Math.min(searchSuggestions.length(), 5); i++){
                                    res.add(searchSuggestions.getJSONObject(i).getString("displayText"));
                                }
                                Log.d("AUTOSUGGEST", res.toString());
                                autoSuggestAdapter.setData(res);

                                autoSuggestAdapter.notifyDataSetChanged();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    // error
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    })
            // set header
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Ocp-Apim-Subscription-Key", "4a418c42a1b445448120e05de9147fa3");
                    return headers;
                }
            };

            q.add(request);
        }










}
