package com.example.cs571.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.cs571.Fragment.HeadlinesInnerPage;


public class HeadlinesInnerPagerAdapter extends FragmentStatePagerAdapter {

    public HeadlinesInnerPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        String[] m = new String[]{"world", "business", "politics", "sport", "technology", "science"};
        String section_name = m[position];
        return HeadlinesInnerPage.newInstance(section_name);
    }

    @Override
    public int getCount() {
        return 6;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }
}
