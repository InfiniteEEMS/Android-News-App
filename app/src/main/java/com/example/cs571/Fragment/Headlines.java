package com.example.cs571.Fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.cs571.Adapter.HeadlinesInnerPagerAdapter;
import com.example.cs571.R;
import com.google.android.material.tabs.TabLayout;


/**
 * A simple {@link Fragment} subclass.
 */
public class Headlines extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private TabLayout tabLayout;
    private ViewPager innerPager;
    private View frag_headline;
    private PagerAdapter innerPagerAdapter;


    public Headlines() {
        // Required empty public constructor
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        frag_headline = inflater.inflate(R.layout.fragment_headlines, container, false);


        innerPager = frag_headline.findViewById(R.id.c_headlines_viewpager);
        innerPagerAdapter = new HeadlinesInnerPagerAdapter(getChildFragmentManager());
        innerPager.setAdapter(innerPagerAdapter);
        innerPager.setOffscreenPageLimit(1);

        Log.w("DESTROY CREATE", "CREATE");

        tabLayout = frag_headline.findViewById(R.id.c_tab_layout);
        tabLayout.setOnTabSelectedListener( new TabLayout.OnTabSelectedListener(){
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                innerPager.setCurrentItem(tab.getPosition());
                //innerPagerAdapter.notifyDataSetChanged();
                Log.v("TAB SELECT", "clicked--" + (String) tab.getText());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        innerPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout){
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                innerPagerAdapter.notifyDataSetChanged();
            }
        });

        selectTabIndex(0);


        return frag_headline;
    }


//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        Log.d("DESTROY CREATE", "OCCURED -- DESTROY");
//        innerPagerAdapter.destroyItem();
//        innerPager=null;
//    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tabLayout.setScrollPosition(0, 0, true);
        innerPager.setCurrentItem(0);
        Log.v("TAB SELECT", "automatic select to -- WORLD");
    }

    private void selectTabIndex(final int index){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                tabLayout.setScrollPosition(index, 0, true);
                innerPager.setCurrentItem(index);
                innerPagerAdapter.notifyDataSetChanged();
                // or
                // tabLayout.getTabAt(index).select();
                Log.v("TAB SELECT", "automatic select to -- WORLD");
            }
        },100);
    }

}
