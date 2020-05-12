package com.example.cs571.Adapter;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.cs571.Fragment.Bookmarks;
import com.example.cs571.Fragment.Headlines;
import com.example.cs571.Fragment.Home;
import com.example.cs571.Fragment.Trending;

public class OuterPagerAdapter extends FragmentStatePagerAdapter {
    public OuterPagerAdapter(@NonNull FragmentManager fm) {
        super(fm);
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new Home();
            case 1:
                return new Headlines();
            case 2:
                return new Trending();
            case 3:
                return new Bookmarks();
            default:
                return new Home();
        }
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        return super.instantiateItem(container, position);
    }

    @Override
    public int getCount() {
        return 4;
    }

    @Override
    public int getItemPosition(@NonNull Object object) {
        return POSITION_NONE;
    }
}


