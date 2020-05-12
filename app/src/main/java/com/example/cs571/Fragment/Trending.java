package com.example.cs571.Fragment;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.cs571.NewsItem;
import com.example.cs571.R;
import com.example.cs571.Utilities;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IFillFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.LineDataProvider;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class Trending extends Fragment {

    private LineChart chart;
    //private final int fillColor = Color.argb(150, 51, 181, 229);
    private View frag_trending;
    private EditText editText;
    private RequestQueue q;

    private String keyword;

    public Trending() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        frag_trending = inflater.inflate(R.layout.fragment_trending, container, false);


        q = Volley.newRequestQueue(getContext());
        keyword = "CoronaVirus";

        chart = frag_trending.findViewById(R.id.chart1);
        editText = frag_trending.findViewById(R.id.c_trend_search_text);


//        chart.setBackgroundColor(Color.WHITE);
//        chart.setGridBackgroundColor(fillColor);
//        chart.setDrawGridBackground(true);

//        chart.setDrawBorders(true);
//
//        // no description text
//        chart.getDescription().setEnabled(false);
//
//        // if disabled, scaling can be done on x- and y-axis separately
//        chart.setPinchZoom(false);

        Legend l = chart.getLegend();
        l.setEnabled(true);
        l.setTextSize(20f);
        //l.setTextColor(Color.rgb(98, 0, 238));

        editText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND || event.getAction() == KeyEvent.ACTION_DOWN ) {
                    keyword = editText.getText().toString();
                    update_chart_and_legend();
                    //Toast.makeText(getContext(), editText.getText(), Toast.LENGTH_SHORT).show();
                    return true;
                }
                return false;
            }
        });
//
//        XAxis xAxis = chart.getXAxis();
//        xAxis.setEnabled(false);
//
//        YAxis leftAxis = chart.getAxisLeft();
//        leftAxis.setAxisMaximum(900f);
//        leftAxis.setAxisMinimum(-250f);
//        leftAxis.setDrawAxisLine(false);
//        leftAxis.setDrawZeroLine(false);
//        leftAxis.setDrawGridLines(false);
//
//        chart.getAxisRight().setEnabled(false);

        // add data
        update_chart_and_legend();

        return frag_trending;



    }


    @Override
    public void onResume() {
        editText.setText("");
        Log.d("CHART", "setting text to null");
        super.onResume();
    }

    private void setData(int count, float range) {

        ArrayList<Entry> values1 = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            float val = (float) (Math.random() * range) + 50;
            values1.add(new Entry(i, val));
        }


        LineDataSet set1;

//        if (chart.getData() != null &&
//                chart.getData().getDataSetCount() > 0) {
//            set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
//            set1.setValues(values1);
//            chart.getData().notifyDataChanged();
//            chart.notifyDataSetChanged();
//        } else {
//            // create a dataset and give it a type
            set1 = new LineDataSet(values1, "DataSet 1");
//
            set1.setAxisDependency(YAxis.AxisDependency.LEFT);
//            set1.setColor(Color.rgb(255, 241, 46));
//            set1.setDrawCircles(false);
//            set1.setLineWidth(2f);
//            set1.setCircleRadius(3f);
//            set1.setFillAlpha(255);
//            set1.setDrawFilled(true);
//            set1.setFillColor(Color.WHITE);
//            set1.setHighLightColor(Color.rgb(244, 117, 117));
//            set1.setDrawCircleHole(false);
//            set1.setFillFormatter(new IFillFormatter() {
//                @Override
//                public float getFillLinePosition(ILineDataSet dataSet, LineDataProvider dataProvider) {
//                    // change the return value here to better understand the effect
//                    // return 0;
//                    return chart.getAxisLeft().getAxisMinimum();
//                }
//            });


            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
            dataSets.add(set1); // add the data sets

            // create a data object with the data sets
            LineData data = new LineData(dataSets);
//            data.setDrawValues(false);

            // set data
            chart.setData(data);
        }

    private void update_chart_and_legend(){
        String url = Utilities.trend_url +  "?keyword=" + keyword;
        Log.d("URL", "querying... " + url);

        final ArrayList<Entry> values = new ArrayList<>();

        JsonObjectRequest request = new JsonObjectRequest(url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONObject defaul = response.getJSONObject("default");
                            JSONArray arr = defaul.getJSONArray("timelineData");
                            for (int i = 0; i < arr.length(); i++) {

                                JSONObject n = arr.getJSONObject(i);
                                JSONArray value_arr = n.getJSONArray("value");
                                int val = value_arr.getInt(0);
                                values.add(new Entry(i, (float)val));
                            }

                            LineDataSet set1 = new LineDataSet(values, "Trending Chart for "+keyword);
                            set1.setAxisDependency(YAxis.AxisDependency.LEFT);
                            set1.setValueTextSize(10);
                            set1.setDrawCircleHole(false);
                            set1.setCircleColor(Color.rgb(98, 0, 238));
                            set1.setValueTextColor(Color.rgb(98, 0, 238));
                            set1.setColor(Color.rgb(98, 0, 238));

                            ArrayList<ILineDataSet> dataSets = new ArrayList<>();
                            dataSets.add(set1); // add the data sets

                            LineData data = new LineData(dataSets);

                            chart.setData(data);
                            chart.invalidate();

                            chart.getData().notifyDataChanged();
                            chart.notifyDataSetChanged();



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
}
