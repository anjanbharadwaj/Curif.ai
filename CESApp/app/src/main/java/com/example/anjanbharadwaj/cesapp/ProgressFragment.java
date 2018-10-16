package com.example.anjanbharadwaj.cesapp;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
        ref.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString()).child("Pictures").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<Double> percentages = new ArrayList<Double>();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String key = snapshot.getKey().toString();

                    Log.v("Progress", "In the loop");

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

                    Log.v("Progress", ""+percent_disease*100);


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




    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
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
