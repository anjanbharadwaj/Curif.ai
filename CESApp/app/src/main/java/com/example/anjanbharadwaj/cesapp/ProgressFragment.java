package com.example.anjanbharadwaj.cesapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.wajahatkarim3.easyflipview.EasyFlipView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;


public class ProgressFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener{

    //    SwipeRefreshLayout swipeRefreshLayout;
    static String mode = "view";
    RecyclerView listView;
    TextView share;
    static Context context;

    ArrayList<GraphCardInformation> listData = new ArrayList<>();

    ArrayAdapter<GraphCardInformation> graphCardArrayAdapter;
    //ArrayList<String> listData1 = new ArrayList<>();
    RecyclerViewClickListener listener;
    public DatabaseReference database;
    public static String name = "Progress";

    private SwipeRefreshLayout mSwipeRefreshLayout;

    int numCopies;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_progress, container, false);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // do your variables initialisations here except Views!!!

        context = getActivity().getApplicationContext();

        listener = new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                GraphCardInformation gci = listData.get(position);

                GraphCardAdapter.GraphViewHolder holder = (GraphCardAdapter.GraphViewHolder)listView.findViewHolderForAdapterPosition(position);

                holder.easyFlipView.flipTheView(true);

            }
        };
    }

    public void loadData() {
        //Clear our arraylists that hold the old data
        listData.clear();

        //Instantiate the two array adapters that connect the arraylists to listviews
        graphCardArrayAdapter = new GraphCardArrayAdapter(context, 0, listData);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString()).child("Pictures").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                int numChildren = (int) dataSnapshot.getChildrenCount();


                for (DataSnapshot d : dataSnapshot.getChildren()) {


                    String body_part = d.getKey().toString();

                    // count the majority diagnosis

                    Map<String, Integer> diag_counts = new HashMap<String, Integer>();

                    for (DataSnapshot dd : d.getChildren()) {

                        String diagnosis = dd.child("Diagnosis").getValue().toString();

                        if (!diag_counts.containsKey(diagnosis)) {
                            diag_counts.put(diagnosis, 1);
                        } else {
                            diag_counts.put(diagnosis, diag_counts.get(diagnosis) + 1);
                        }
                    }

                    String max_key = "";
                    int max = -1;

                    for (String key : diag_counts.keySet()) {
                        if (diag_counts.get(key) > max) {
                            max_key = key;
                            max = diag_counts.get(key);
                        }
                    }

                    Log.v("max_key", max_key);

                    ArrayList<Double> percentages = new ArrayList<>();
                    ArrayList<Double> feelings = new ArrayList<>();
                    ArrayList<Entry> data_graph_percentages = new ArrayList<>();
                    ArrayList<Entry> data_feelings = new ArrayList<>();

                    for (DataSnapshot snapshot : d.getChildren()) {
                        Log.v("EachPic","Prints once");
                        String key = snapshot.getKey().toString();

                        Log.v("hi", key);

                        double percent_to_add = 0;
                        if(max_key.equals("1")) {
                            percent_to_add = Double.parseDouble(snapshot.child("FullPredictions").child("Diagnosis 1").getValue().toString());
                        } else if (max_key.equals("2")) {
                            percent_to_add = Double.parseDouble(snapshot.child("FullPredictions").child("Diagnosis 2").getValue().toString());
                        } else if (max_key.equals("3")) {
                            percent_to_add = Double.parseDouble(snapshot.child("FullPredictions").child("Diagnosis 3").getValue().toString());
                        } else if(max_key.equals("4")) {
                            percent_to_add = Double.parseDouble(snapshot.child("FullPredictions").child("Diagnosis 4").getValue().toString());
                        }

                        percentages.add(percent_to_add);

                        feelings.add(Double.parseDouble(snapshot.child("Feeling").getValue().toString()));



                    }

                    for (int i = 0; i < percentages.size(); i++) {
                        data_graph_percentages.add(new Entry(i, percentages.get(i).floatValue()));
                    }

                    for (int j = 0; j < feelings.size(); j++) {
                        data_feelings.add(new Entry(j, feelings.get(j).floatValue()));
                    }

                    Log.v("help", body_part + data_graph_percentages.size());
                    GraphCardInformation gci = new GraphCardInformation("" + max_key, data_graph_percentages, data_feelings,  max_key, body_part);
                    listData.add(gci);


                }
                showCards();

                Log.v("LISTData", listData.toString());

                mSwipeRefreshLayout.setRefreshing(false);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private void showCards() {
        GraphCardAdapter graphCardAdapter = new GraphCardAdapter(listData, listener);
        listView.setAdapter(graphCardAdapter);


    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialize our views

        listView = (RecyclerView) view.findViewById(R.id.progress_recycler_view);
        listView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(llm);

        listView.setVisibility(View.VISIBLE);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.progress_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mSwipeRefreshLayout.post(new Runnable() {

            @Override
            public void run() {

                if(mSwipeRefreshLayout != null) {
                    mSwipeRefreshLayout.setRefreshing(true);
                }
                loadData();
            }
        });



        loadData();
    }

    public static void setListViewHeight(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) return;

        int totalHeight = 0;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            View listItem = listAdapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }

    @Override
    public void onRefresh() {
        loadData();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);

    }

    public static String getName() {
        return name;
    }
}






//adapter which manages the data in the profile fragment list view.
class GraphCardArrayAdapter extends ArrayAdapter<GraphCardInformation> {

    private Context context;
    private List<GraphCardInformation> dataList;

    public GraphCardArrayAdapter(Context context, int resource, List<GraphCardInformation> dataList) {
        super(context, resource, dataList);

        this.context = context;
        this.dataList = dataList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //inflates a card and populates/adds the proper information
        GraphCardInformation dataPoint = dataList.get(position);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.blank_graph_slate, null);
        return view;
    }
}

class GraphCardAdapter extends RecyclerView.Adapter<GraphCardAdapter.GraphViewHolder> {
    private ArrayList<GraphCardInformation> datapoints;
    private RecyclerViewClickListener mListener;
    //Default constructor
    GraphCardAdapter(ArrayList<GraphCardInformation> datapoints, RecyclerViewClickListener listener) {
        this.datapoints = datapoints;
        mListener = listener;
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    @Override
    public int getItemCount() {
        return datapoints.size();
    }

    @Override
    public void onBindViewHolder(GraphViewHolder pointViewHolder, int i) {
        //Set each field to its corresponding attribute

        if(datapoints.size() == 0) {
            return;
        }

        GraphCardInformation point = datapoints.get(i);

        LineChart graph = pointViewHolder.graph_data;

        LineChart graph2 = pointViewHolder.graph_feeling;


        TextView title = pointViewHolder.title_data;

        LineDataSet dataset2 = new LineDataSet(point.feelings, point.getTitle().toString());
        //dataset2.setColor(Color.WHITE);
        //dataset2.setValueTextColor(Color.WHITE); // styling, ...
        LineData lineData2 = new LineData(dataset2);
        pointViewHolder.graph_feeling.setData(lineData2);


        dataset2.setColor(R.color.colorPrimary);
        dataset2.setFillColor(R.color.colorPrimary);
        dataset2.setDrawFilled(true);


        XAxis xAxis2 = graph2.getXAxis();
        xAxis2.setTextSize(10f);
        xAxis2.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis2.setTextColor(Color.BLACK);
        xAxis2.setDrawAxisLine(true);
        xAxis2.setDrawGridLines(false);


        graph2.getAxisRight().setEnabled(false);
        YAxis yAxis2 = graph2.getAxisLeft();
        yAxis2.setTextSize(10f); // set the text size
        yAxis2.setTextColor(Color.BLACK);
        yAxis2.setGranularity(0.01f); // interval 1
        yAxis2.setDrawGridLines(false);


        graph2.getDescription().setEnabled(false);

        graph2.setTouchEnabled(false);


        LineDataSet dataSet = new LineDataSet(point.percentages, point.getTitle().toString()); // add entries to dataset
        dataSet.setColor(R.color.colorPrimary);
        dataSet.setDrawFilled(true);
        dataSet.setFillColor(R.color.colorPrimary);
        dataSet.setColor(Color.WHITE);
        dataSet.setValueTextColor(Color.WHITE); // styling, ...
        LineData lineData = new LineData(dataSet);
        pointViewHolder.graph_data.setData(lineData);

        dataSet.setColor(R.color.colorPrimary);
        dataSet.setFillColor(R.color.colorPrimary);
        dataSet.setDrawFilled(true);


        XAxis xAxis = graph.getXAxis();
        xAxis.setTextSize(10f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);


        graph.getAxisRight().setEnabled(false);
        YAxis yAxis = graph.getAxisLeft();
        yAxis.setTextSize(10f); // set the text size
        yAxis.setTextColor(Color.BLACK);
        yAxis.setGranularity(0.01f); // interval 1
        yAxis.setDrawGridLines(false);


        graph.getDescription().setEnabled(false);

        graph.setTouchEnabled(false);

        graph.setData(lineData);

        Legend legend = graph.getLegend();
        legend.setEnabled(false);
        Legend legend2 = graph2.getLegend();
        legend2.setEnabled(false);



        graph.invalidate(); // refresh
        graph2.invalidate(); // refresh


        pointViewHolder.title_data.setText("Treatment Of " + point.title);
        pointViewHolder.title_feeling.setText("Happiness Associated with " + point.title);
        pointViewHolder.location_data.setText("Site of Picture: " + point.location);
        pointViewHolder.location_feeling.setText("Site of Picture: " + point.location);
        Log.v("InBindHolder",point.percentages.toString());
    }

    @Override
    public GraphViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        //Inflate the view using the proper xml layout
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.blank_graph_slate, viewGroup, false);

        return new GraphViewHolder(itemView, mListener);
    }

    static class GraphViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public EasyFlipView easyFlipView;
        public TextView title_data;
        public LineChart graph_data;
        public TextView location_data;

        public TextView title_feeling;
        public LineChart graph_feeling;
        public TextView location_feeling;


        private RecyclerViewClickListener mListener;

        GraphViewHolder(View v, RecyclerViewClickListener mListener) {
            super(v);
            easyFlipView = v.findViewById(R.id.flipviewprogress);
            title_data = v.findViewById(R.id.title);
            graph_data = v.findViewById(R.id.graphData);
            location_data = v.findViewById(R.id.location_title);

            title_feeling = v.findViewById(R.id.title_feeling);
            graph_feeling = v.findViewById(R.id.graphFeeling);
            location_feeling = v.findViewById(R.id.location_title_feeling);
            //instantiation of views
            easyFlipView.setFlipOnTouch(false);
            easyFlipView.setFlipTypeFromFront();
            this.mListener = mListener;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onClick(v, getAdapterPosition());
        }
    }
}