package com.example.cs571;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuItemCompat;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.cs571.Adapter.AutoSuggestionAdapter;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DetailActivity extends AppCompatActivity {

    private String id;

    private LinearLayout spinner;
    private RequestQueue q;
    private View detail_card;
    private ImageView detail_image;
    private Context mContext;
    private TextView detail_title;
    private TextView detail_description;
    private TextView detail_section;
    private TextView detail_date;
    private TextView detail_viewfull;
    private MenuItem detail_share;
    private MenuItem detail_bookmark;

    private NewsItem newsItem;

    private Toolbar myToolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mContext = this;
        newsItem = new NewsItem();

        myToolbar = (Toolbar) findViewById(R.id.c_toolBar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setHomeButtonEnabled(true);

        id = getIntent().getStringExtra("id");
        Log.d("DETAIL", "Entering Detail with id: " +id);



        /* News */
        q= Volley.newRequestQueue(this);
        detail_card = this.findViewById(R.id.c_detail_card);
        detail_image = this.findViewById(R.id.c_detail_image);
        detail_title = this.findViewById(R.id.c_detail_title);
        detail_date = this.findViewById(R.id.c_detail_date);
        detail_section = this.findViewById(R.id.c_detail_section);
        detail_description = this.findViewById(R.id.c_detail_description);
        detail_viewfull = this.findViewById(R.id.c_detail_viewfull);
        detail_viewfull.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = newsItem.getShare_url();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
            }
        });


        /* spinner */
        q= Volley.newRequestQueue(this);
        spinner = this.findViewById(R.id.c_progress_bar);



        /* Doing */
        spinner.setVisibility(View.VISIBLE);
        detail_card.setVisibility(View.GONE);
        update_news();

    }





    // 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.detail_activity_toolbar, menu);
        detail_share = menu.findItem(R.id.c_detail_toolbar_share);
        detail_bookmark = menu.findItem(R.id.c_detail_toolbar_bookmark);
        return super.onCreateOptionsMenu(menu);
    }

    // 
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // app icon in action bar clicked; goto parent activity.
                this.finish();
                return true;

            case R.id.c_detail_toolbar_bookmark:
                if(newsItem.isMarked()){
                    changeBookmark(newsItem);
                    detail_bookmark.setIcon(R.drawable.bookmark_not_48dp);
                }else{
                    changeBookmark(newsItem);
                    detail_bookmark.setIcon(R.drawable.bookmark_48dp);
                }
                return true;

            case R.id.c_detail_toolbar_share:
                String url = "https://twitter.com/intent/tweet?url=" + newsItem.getShare_url() + "&text="+ "Check out this Link：\n";
                url = url + "&hashtags=CSCI571NewsSearch";
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }




    // fetch url and update the card
    private void update_news(){

        String url = Utilities.other_url + "?id=" + id;
        Log.d("URL", "querying... " + url);

        JsonObjectRequest request = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @RequiresApi(api = Build.VERSION_CODES.O)
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray arr = response.getJSONArray("data");
                            for(int i=0; i<arr.length(); i++){
                                JSONObject n = arr.getJSONObject(i);

                                newsItem.setImage(n.getString("image"));
                                newsItem.setTitle(n.getString("title"));
                                newsItem.setDate(n.getString("date"));
                                newsItem.setSection(n.getString("section"));
                                newsItem.setId(n.getString("id"));
                                newsItem.setShare_url(n.getString("share_url_detail"));

                                Picasso.with(mContext).load(n.getString("image")).resize(400, 250).into(detail_image);
                                detail_title.setText(n.getString("title"));
                                detail_date.setText(Utilities.timeConvertToDDMMYYYY(n.getString("date")));
                                detail_section.setText(n.getString("section"));
                                detail_description.setText(Html.fromHtml(n.getString("description")));
                                detail_viewfull.setText(Html.fromHtml("<u>"+"View All Article"+"</u>"));

                                myToolbar.setTitle(n.getString("title"));
                                if (Utilities.checkNewsInPreference(n.getString("id"))){
                                    newsItem.setMarked(true);
                                    detail_bookmark.setIcon(R.drawable.bookmark_48dp);
                                }else{
                                    newsItem.setMarked(false);
                                    detail_bookmark.setIcon(R.drawable.bookmark_not_48dp);
                                }
                            }
                            spinner.setVisibility(View.GONE);
                            detail_card.setVisibility(View.VISIBLE);
                        } catch (JSONException e) {e.printStackTrace(); }
                    }
                },
                new Response.ErrorListener(){
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
            Toast.makeText(this, text+" was removed from Bookmarks", Toast.LENGTH_SHORT).show();
            unMark(cur, pref, editor);
        } else {
            Toast.makeText(this, text+" was added to Bookmarks", Toast.LENGTH_SHORT).show();
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
            //newsAdapter.notifyDataSetChanged();
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
            for(int i = 0; i< ids_arr.length(); i++){
                JSONObject n = ids_arr.getJSONObject(i);
                if(n.getString("id").equals(cur.getId())){
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
            //newsAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            Log.e("JSON", "Error in transform string to json in sharePreference ");
            e.printStackTrace();
        }
    }

    private void showDialog(final NewsItem cur, Context context){
        //////////
        // QIULONG:可以在这里直接用adapter
        final Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog);
        final ImageView dialog_image = dialog.findViewById(R.id.c_dialog_image);
        TextView dialog_title = dialog.findViewById(R.id.c_dialog_title);
        dialog_title.setText(cur.getTitle());
        final ImageView dialog_bookmark = dialog.findViewById(R.id.c_dialog_bookmark);
        final ImageView dialog_twitter = dialog.findViewById(R.id.c_dialog_twitter);
        dialog_twitter.setImageResource(R.drawable.bluetwitter);
        if(cur.isMarked()){
            dialog_bookmark.setImageResource(R.drawable.bookmark);
        }else{
            dialog_bookmark.setImageResource(R.drawable.bookmark_not);
        }

        dialog_bookmark.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeBookmark(cur);
                if(cur.isMarked()){
                    dialog_bookmark.setImageResource(R.drawable.bookmark);
                }else{
                    dialog_bookmark.setImageResource(R.drawable.bookmark_not);
                }
                //newsAdapter.notifyDataSetChanged();
            }
        });

        dialog_twitter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://twitter.com/intent/tweet?url=" + cur.getShare_url() + "&text="+ "Check out this Link：\n";
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

}
