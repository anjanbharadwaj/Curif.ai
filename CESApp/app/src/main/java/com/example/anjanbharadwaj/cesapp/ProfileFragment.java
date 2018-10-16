package com.example.anjanbharadwaj.cesapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/*
    This is the StudentTeacherProfile Fragment. It is the rightmost screen in the home page, and has information about the
    current user, include their holds/checked out books and more.
 */
public class ProfileFragment extends Fragment {

    SwipeRefreshLayout swipeRefreshLayout;

    ListView listView;
    ImageView share;
    Context context;

    ArrayList<DataPointProfile> listData = new ArrayList<>();

    ArrayAdapter<DataPointProfile> dataPointProfileArrayAdapter;
    //ArrayList<String> listData1 = new ArrayList<>();

    public DatabaseReference database;
    public static String name = "Profile";

    int numCopies;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // do your variables initialisations here except Views!!!

        context = getActivity().getApplicationContext();
    }

    public void loadData() {


        //Clear our arraylists that hold the old data
        listData.clear();

        //Instantiate the two array adapters that connect the arraylists to listviews
        dataPointProfileArrayAdapter = new DataPointProfileArrayAdapter(context, 0, listData);
        database = FirebaseDatabase.getInstance().getReference();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        database.child("Users").child(uid).child("Pictures").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                listData.clear();
                name = "Profile";//dataSnapshot.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Name").getValue().toString();

                //Notify the adapters that the arraylists have changed, and that they have to update info
                Iterator i = dataSnapshot.getChildren().iterator();
                while(i.hasNext()){
                    String key = ((DataSnapshot) (i.next())).getKey().toString();
                    String diagnosis = dataSnapshot.child(key).child("Diagnosis").getValue().toString();
                    String url = "https://www.selfcare4rsi.com/images/upper-arm-lift-300x297.jpg";
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                    String dateString = formatter.format(new Date(Long.valueOf(key)));
                    DataPointProfile point = new DataPointProfile(url, diagnosis, dateString);
                    listData.add(point);

                }
                dataPointProfileArrayAdapter.notifyDataSetChanged();

                listView.setAdapter(dataPointProfileArrayAdapter);

                setListViewHeight(listView);

                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }


        });
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialize our views

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.profileSwipeRefresh);

        listView = (ListView) view.findViewById(R.id.profileListView);
        share = (ImageView) view.findViewById(R.id.share);

        //loading

        loadData();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
            }
        });

        // Holds share button pressed
        share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


            }
        });
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

//Wrapper class for book information.
class DataPointProfile {
    String url;
    String diagnosis;
    String date;

    public DataPointProfile(String url, String diagnosis, String date) {
        this.url = url;
        this.diagnosis = diagnosis;
        this.date = date;
    }
}

//adapter which manages the data in the profile fragment list view.
class DataPointProfileArrayAdapter extends ArrayAdapter<DataPointProfile> {

    private Context context;
    private List<DataPointProfile> dataList;

    public DataPointProfileArrayAdapter(Context context, int resource, List<DataPointProfile> dataList) {
        super(context, resource, dataList);

        this.context = context;
        this.dataList = dataList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        //inflates a card and populates/adds the proper information

        DataPointProfile dataPoint = dataList.get(position);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.profile_photo_taken, null);

        //initializes the views on the card.
        ImageView picture = (ImageView) view.findViewById(R.id.picture);
        TextView diagnosis = (TextView) view.findViewById(R.id.diagnosis);
        TextView date = (TextView) view.findViewById(R.id.date);

        //loading book image async with Glide loading library.
        Glide.with(context).load(dataPoint.url).into(picture);

        //adding proper data to views.
        diagnosis.setText(dataPoint.diagnosis);
        date.setText(dataPoint.date);

        return view;
    }
}
