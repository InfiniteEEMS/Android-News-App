package com.example.cs571;

import android.app.Application;
import android.content.Context;

public class MyApplication extends Application {

    private static Context context;
    public static String city;
    public static String state;


    public void onCreate() {
        super.onCreate();
        MyApplication.context = getApplicationContext();
    }

    public static Context myGetAppContext() {
        return MyApplication.context;
    }
}