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
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import jp.co.recruit_lifestyle.android.widget.WaveSwipeRefreshLayout;


public class ProgressFragment extends Fragment {

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
                Toast.makeText(context,"Clicked",Toast.LENGTH_LONG).show();
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
                ArrayList<ArrayList<Double>> percentages = new ArrayList<>();
                percentages.add(new ArrayList<Double>());
                percentages.add(new ArrayList<Double>());
                percentages.add(new ArrayList<Double>());
                percentages.add(new ArrayList<Double>());

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey().toString();

                    int diagnosis = Integer.parseInt(snapshot.child("Diagnosis").getValue().toString());
                    Log.v("DiagnosisPred",""+diagnosis);
                    double class_one_percent = Double.parseDouble(snapshot.child("FullPredictions").child("Diagnosis 1").getValue().toString());
                    double class_two_percent = Double.parseDouble(snapshot.child("FullPredictions").child("Diagnosis 2").getValue().toString());
                    double class_three_percent = Double.parseDouble(snapshot.child("FullPredictions").child("Diagnosis 3").getValue().toString());
                    double class_four_percent = Double.parseDouble(snapshot.child("FullPredictions").child("Diagnosis 4").getValue().toString());
                    percentages.get(0).add(class_one_percent);
                    percentages.get(1).add(class_two_percent);
                    percentages.get(2).add(class_three_percent);
                    percentages.get(3).add(class_four_percent);
                    Log.v("Diag3",percentages.get(2).toString());

//                    double percent_disease = 0;
//
//                    if(diagnosis == 1){
//                        percent_disease = class_one_percent;
//                    } else if(diagnosis == 2) {
//                        percent_disease = class_two_percent;
//                    } else if(diagnosis == 3) {
//                        percent_disease = class_three_percent;
//                    } else if(diagnosis == 4) {
//                        percent_disease = class_four_percent;
//                    }
//                    percentages.add(percent_disease);
                }

                ArrayList<ArrayList<Entry>> entries = new ArrayList<>();
                entries.add(new ArrayList<Entry>());
                entries.add(new ArrayList<Entry>());
                entries.add(new ArrayList<Entry>());
                entries.add(new ArrayList<Entry>());
                for(int j =0;j<entries.size(); j++) {
                    for (int i = 0; i < percentages.get(j).size(); i++) {
                        entries.get(j).add(new Entry(i, percentages.get(j).get(i).floatValue()));
                    }
                    GraphCardInformation gci = new GraphCardInformation(""+j,entries.get(j),"Disease "+j+" progression");
                    listData.add(gci);

                }
                Log.v("Entries3",entries.get(2).toString());

                Log.v("LISTData", listData.toString());

                showCards();

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


        //OLD

        loadData();


        // Holds share button pressed
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
        //initializes the views on the card.
        LineChart graph = (LineChart)view.findViewById(R.id.graph);

        LineDataSet dataSet = new LineDataSet(dataPoint.getPercentages(), dataPoint.getTitle().toString()); // add entries to dataset
        dataSet.setColor(Color.WHITE);
        dataSet.setValueTextColor(Color.WHITE); // styling, ...
        LineData lineData = new LineData(dataSet);
        graph.setData(lineData);
        graph.invalidate(); // refresh

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
        GraphCardInformation point = datapoints.get(i);

        LineChart graph = pointViewHolder.graph;
        TextView title = pointViewHolder.title;

        LineDataSet dataSet = new LineDataSet(point.percentages, point.getTitle().toString()); // add entries to dataset
        dataSet.setColor(Color.WHITE);
        dataSet.setValueTextColor(Color.WHITE); // styling, ...
        LineData lineData = new LineData(dataSet);
        pointViewHolder.graph.setData(lineData);
       // pointViewHolder.graph.invalidate(); // refresh

        pointViewHolder.title.setText(point.title);
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

        public CardView cardView;
        public TextView title;
        public LineChart graph;

        private RecyclerViewClickListener mListener;

        GraphViewHolder(View v, RecyclerViewClickListener mListener) {
            super(v);
            cardView = v.findViewById(R.id.profileCardView);
            title = v.findViewById(R.id.title);
            graph = v.findViewById(R.id.graph);
            //instantiation of views

            this.mListener = mListener;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onClick(v, getAdapterPosition());
        }
    }
}