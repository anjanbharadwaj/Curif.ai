package com.example.anjanbharadwaj.cesapp;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.firebase.FirebaseApiNotAvailableException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ramotion.foldingcell.FoldingCell;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProgressFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProgressFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProgressFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<GraphCardInformation> graphListData = new ArrayList<>();

    private RecyclerViewClickListener listener;

    public ProgressFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProgressFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProgressFragment newInstance(String param1, String param2) {
        ProgressFragment fragment = new ProgressFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final FirebaseDatabase database = FirebaseDatabase.getInstance();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
        ref.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString()).child("Pictures").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Double> percentages = new ArrayList<Double>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey().toString();

                    int diagnosis = Integer.parseInt(snapshot.child("Diagnosis").getValue().toString());

                    double class_one_percent = Double.parseDouble(snapshot.child("FullPredictions").child("Diagnosis 1").getValue().toString());
                    double class_two_percent = Double.parseDouble(snapshot.child("FullPredictions").child("Diagnosis 2").getValue().toString());
                    double class_three_percent = Double.parseDouble(snapshot.child("FullPredictions").child("Diagnosis 3").getValue().toString());
                    double class_four_percent = Double.parseDouble(snapshot.child("FullPredictions").child("Diagnosis 4").getValue().toString());

                    double percent_disease = 0;

                    if(diagnosis == 1){
                        percent_disease = class_one_percent;
                    } else if(diagnosis == 2) {
                        percent_disease = class_two_percent;
                    } else if(diagnosis == 3) {
                        percent_disease = class_three_percent;
                    } else if(diagnosis == 4) {
                        percent_disease = class_four_percent;
                    }
                    percentages.add(percent_disease);
                }

                // in this example, a LineChart is initialized from xml
                LineChart chart = (LineChart) getView().findViewById(R.id.chart);
                Log.v("Progress", chart.toString());
                List<Entry> entries = new ArrayList<Entry>();

                for(int i = 0; i < percentages.size(); i++) {
                    entries.add(new Entry(i, percentages.get(i).floatValue()));
                }

                LineDataSet dataSet = new LineDataSet(entries, "Recovery Over Time"); // add entries to dataset
                dataSet.setColor(Color.WHITE);
                dataSet.setValueTextColor(Color.WHITE); // styling, ...
                LineData lineData = new LineData(dataSet);
                chart.setData(lineData);
                chart.invalidate(); // refresh
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        mRecyclerView = (RecyclerView) getView().findViewById(R.id.progress_recycler_view);

        mRecyclerView.setAdapter(mAdapter);

        mLayoutManager = new LinearLayoutManager(getActivity());

        mRecyclerView.setLayoutManager(mLayoutManager);

        load_data();

    }

    public void load_data() {

        graphListData.clear();


        ArrayList<Double> diseasePercentages = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            diseasePercentages.add(new Double(i));
        }

        graphListData.add(new GraphCardInformation("Hello", diseasePercentages, "title"));

        mAdapter = new GraphCardAdapter(graphListData, new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                System.out.println("Clicked!");

            }
        });

        showCards();

    }

    private void showCards() {
        GraphCardAdapter dataPointProfileAdapter = new GraphCardAdapter(graphListData, listener);
        mRecyclerView.setAdapter(dataPointProfileAdapter);
    }

    //adapter which manages the data in the profile fragment list view.
    class ProgressArrayAdapter extends ArrayAdapter<GraphCardInformation> {

        private Context context;
        private List<GraphCardInformation> dataList;

        public ProgressArrayAdapter(Context context, int resource, List<GraphCardInformation> dataList) {
            super(context, resource, dataList);


            this.context = context;
            this.dataList = dataList;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            //inflates a card and populates/adds the proper information
            GraphCardInformation graphCard = dataList.get(position);


            GraphCardInformation dataPoint = dataList.get(position);
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.blank_graph_slate, null);

            //initializes the views on the card.
            LineChart chart = (LineChart) view.findViewById(R.id.graph);

            List<Entry> entries = new ArrayList<Entry>();

            for(int i = 0; i < graphCard.getPercentages().size(); i++) {
                entries.add(new Entry(i, graphCard.getPercentages().get(i).floatValue()));
            }

            LineDataSet dataSet = new LineDataSet(entries, dataPoint.getTitle().toString()); // add entries to dataset
            dataSet.setColor(Color.WHITE);
            dataSet.setValueTextColor(Color.WHITE); // styling, ...
            LineData lineData = new LineData(dataSet);
            chart.setData(lineData);
            chart.invalidate(); // refresh

            return view;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        listener = new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {

                GraphCardAdapter.GraphCardViewHolder holder = (GraphCardAdapter.GraphCardViewHolder) mRecyclerView.findViewHolderForAdapterPosition(position);

            }
        };


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_progress, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}

class GraphCardAdapter extends RecyclerView.Adapter<GraphCardAdapter.GraphCardViewHolder> {
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
    public void onBindViewHolder(GraphCardViewHolder pointViewHolder, int i) {
        //Set each field to its corresponding attribute
        GraphCardInformation point = datapoints.get(i);
    }

    @Override
    public GraphCardViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        //Inflate the view using the proper xml layout
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.blank_graph_slate, viewGroup, false);

        return new GraphCardViewHolder(itemView, mListener);
    }

    static class GraphCardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public CardView cardView;
        public LineChart graph;

        private RecyclerViewClickListener mListener;

        GraphCardViewHolder(View v, RecyclerViewClickListener mListener) {
            super(v);
            graph = v.findViewById(R.id.graph);

            //modify the graph

            Log.v("PLEASE", "HERE!");


            this.mListener = mListener;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onClick(v, getAdapterPosition());
        }
    }
}
