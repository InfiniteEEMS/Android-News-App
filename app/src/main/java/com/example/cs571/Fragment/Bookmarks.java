package com.example.cs571.Fragment;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.toolbox.Volley;
import com.example.cs571.Adapter.FavoriteNewsListAdapter;
import com.example.cs571.Adapter.NewsListAdapter;
import com.example.cs571.DetailActivity;
import com.example.cs571.MyApplication;
import com.example.cs571.NewsItem;
import com.example.cs571.R;
import com.example.cs571.Utilities;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class Bookmarks extends Fragment {

    private View frag_bookmarks;

    private ArrayList<NewsItem> news;
    private RecyclerView newsRecycler;
    private FavoriteNewsListAdapter newsAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private TextView noBookmarkPrompt;

    public Bookmarks() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        frag_bookmarks = inflater.inflate(R.layout.fragment_bookmarks, container, false);


        noBookmarkPrompt = frag_bookmarks.findViewById(R.id.c_bookmarks_no_bookmarks_prompt);


        /* recycler & adapter*/
        news = new ArrayList<>();
        newsRecycler = frag_bookmarks.findViewById(R.id.c_bookmarks_news_list);
        mLayoutManager = new GridLayoutManager(getContext(), 2, GridLayoutManager.VERTICAL, false);
        newsRecycler.setLayoutManager(mLayoutManager);
        newsAdapter = new FavoriteNewsListAdapter(news, newsRecycler);
        newsRecycler.setAdapter(newsAdapter);
        newsRecycler.addItemDecoration(new DividerItemDecoration(newsRecycler.getContext(), DividerItemDecoration.VERTICAL));

        newsAdapter.setOnItemClickListener(new FavoriteNewsListAdapter.OnItemClickListener() {
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


        /* spinner & refresher*/



        /*Go Doing*/
        update_data_fromPreference();


        return frag_bookmarks;
    }

    @Override
    public void onResume() {
        update_data_fromPreference();
        super.onResume();
    }

    private void update_data_fromPreference(){
        news.clear();
        SharedPreferences pref = MyApplication.myGetAppContext().getSharedPreferences("favorite", 0);
        try {
            JSONObject ids = new JSONObject(pref.getString("ids", null));
            JSONArray ids_arr = ids.getJSONArray("ids_arr");
            for(int i = 0; i< ids_arr.length(); i++){
                JSONObject n = ids_arr.getJSONObject(i);
                NewsItem cur = new NewsItem();
                cur.setId(n.getString("id"));
                cur.setDate(n.getString("date"));
                cur.setSection(n.getString("section"));
                cur.setImage(n.getString("image"));
                cur.setTitle(n.getString("title"));
                cur.setShare_url(n.getString("share_url"));
                cur.setMarked(true);
                news.add(cur);
            }
            Log.d("BOOKMARK", news.toString());
            newsAdapter.notifyDataSetChanged();
            if(news.size()==0){
                noBookmarkPrompt.setVisibility(View.VISIBLE);
                newsRecycler.setVisibility(View.GONE);
            }else{
                newsRecycler.setVisibility(View.VISIBLE);
                noBookmarkPrompt.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        update_data_fromPreference();
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
