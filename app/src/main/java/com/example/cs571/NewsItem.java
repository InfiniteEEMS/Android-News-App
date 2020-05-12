package com.example.cs571;


import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import org.json.JSONException;
import org.json.JSONObject;

// News
public class NewsItem {
    private String id;
    private String title;
    private String image;
    private String section;
    private String date;
    private boolean marked;
    private String share_url;

    public NewsItem() {
    }

    public NewsItem(String id, String title, String image, String section, String date, String description, boolean marked) {
        this.id = id;
        this.title = title;
        this.image = image;
        this.section = section;
        this.date = date;
        this.marked = marked;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setMarked(boolean marked) {
        this.marked = marked;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getImage() {
        return image;
    }

    public String getSection() {
        return section;
    }

    public String getDate() {
        return date;
    }

    public boolean isMarked() {
        return marked;
    }

    public String getShare_url() {
        return share_url;
    }

    public void setShare_url(String share_url) {
        this.share_url = share_url;
    }

    public JSONObject myToJSONObj(){
        JSONObject res = new JSONObject();
        try {
            res.put("id", this.id);
            res.put("title", this.title);
            res.put("image", this.image);
            res.put("date", this.date);
            res.put("section", this.section);
            res.put("share_url", this.share_url);
        } catch (JSONException e) {
            Log.e("JSON", "Error in creat ingjson obj");
            e.printStackTrace();
        }
        return res;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public String get_time_and_section(){
        return Utilities.timeConvertToDiff(this.date) + " | " + this.getSection();
    };
}
