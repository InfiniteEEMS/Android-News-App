package com.example.cs571.Adapter;

import android.content.Context;
import android.widget.ArrayAdapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class AutoSuggestionAdapter extends ArrayAdapter {

    private List<String> mListData;

    public AutoSuggestionAdapter(@NonNull Context context, int resource) {
        super(context, resource);
        mListData = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return mListData.size();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return mListData.get(position);
    }

    public void setData(List<String> list) {
        mListData.clear();
        mListData.addAll(list);
    }

}
