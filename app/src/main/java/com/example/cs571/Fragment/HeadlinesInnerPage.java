package com.example.cs571.Fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

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

import java.util.ArrayList;



/*
 *    Headlines 界面中Tab下方的
 *
 * */
public class HeadlinesInnerPage extends Fragment {

    private View frag_innerNews;
    private String section_name;

    private ArrayList<NewsItem> news;
    private RecyclerView newsRecycler;
    private NewsListAdapter newsAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private RequestQueue q;
    private LinearLayout spinner;
    private SwipeRefreshLayout mSwipeRefreshLayout;



    public static HeadlinesInnerPage newInstance(String section_name) {
        HeadlinesInnerPage fragment = new HeadlinesInnerPage();
        Bundle bundle = new Bundle();
        bundle.putString("section_name", section_name);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        // Inflate the layout for this fragment
        frag_innerNews = inflater.inflate(R.layout.fragment_headlines_inner_news, container, false);

        Bundle bundle = this.getArguments();
        assert bundle != null;
        section_name = bundle.getString("section_name", "world");





        /* recycler & adapter*/
        news = new ArrayList<>();
        newsRecycler = frag_innerNews.findViewById(R.id.c_headlines_news_list);
        mLayoutManager = new LinearLayoutManager(getContext());
        newsRecycler.setLayoutManager(mLayoutManager);
        newsAdapter = new NewsListAdapter(news, newsRecycler);
        newsRecycler.setAdapter(newsAdapter);

        ///// QIULONG
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
                showDialog(news.get(position), view.getContext());
            }
        });
        ///// QIULONG




        /* spinner & refresher*/
        spinner = frag_innerNews.findViewById(R.id.c_progress_bar);
        q = Volley.newRequestQueue(getContext());
        mSwipeRefreshLayout = frag_innerNews.findViewById(R.id.c_swipe_refresh);
        // set listener to swipe
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //spinner.setVisibility(View.VISIBLE);
                //newsRecycler.setVisibility(View.GONE);
                update_news();
            }
        });



        /* Doing */
        spinner.setVisibility(View.VISIBLE);
        newsRecycler.setVisibility(View.GONE);
        update_news();


        return frag_innerNews;
    }


    @Override
    public void onResume() {
        Utilities.updateNewsListFromPreference(news);
        newsAdapter.notifyDataSetChanged();
        super.onResume();
    }


    // fetch url and store them as obj in arrayList news. notifyDataChange -> onBindViewHolder
    private void update_news() {

        String url = Utilities.other_url + "?category=" + section_name;
        Log.d("URL", "querying... " + url);

        JsonObjectRequest request = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

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
                                if (Utilities.checkNewsInPreference(cur.getId())){
                                    cur.setMarked(true);
                                }else{
                                    cur.setMarked(false);
                                }
                                news.add(cur);
                            }
                            spinner.setVisibility(View.GONE);
                            if(mSwipeRefreshLayout.isRefreshing()){
                                mSwipeRefreshLayout.setRefreshing(false);
                            }else{
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


    private void changeBookmark(NewsItem cur) {
        Log.d("DBG", "bookmark clicked");
        SharedPreferences pref = MyApplication.myGetAppContext().getSharedPreferences("favorite", 0);
        SharedPreferences.Editor editor = pref.edit();
        String text = '"' + cur.getTitle() + '"';
        if (cur.isMarked()) {
            Toast.makeText(getContext(), text+" was removed from Bookmarks", Toast.LENGTH_SHORT).show();
            unMark(cur, pref, editor);
        } else {
            Toast.makeText(getContext(), text+" was added to Bookmarks", Toast.LENGTH_SHORT).show();
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
            newsAdapter.notifyDataSetChanged();

        } catch (JSONException e) {
            Log.e("JSON", "Error in transform string to json in sharePreference ");
            e.printStackTrace();
        }
    }

    private void showDialog(final NewsItem cur, Context context){
        //////////
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
                newsAdapter.notifyDataSetChanged();
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
