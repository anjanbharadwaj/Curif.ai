package com.example.anjanbharadwaj.cesapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.net.URL;
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
    String mode = "view";
    RecyclerView listView;
    TextView share;
    static Context context;

    ArrayList<DataPointProfile> listData = new ArrayList<>();

    ArrayAdapter<DataPointProfile> dataPointProfileArrayAdapter;
    //ArrayList<String> listData1 = new ArrayList<>();
    RecyclerViewClickListener listener;
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

        listener = new RecyclerViewClickListener() {
            @Override
            public void onClick(View view, int position) {
                DataPointProfileAdapter.DataPointViewHolder holder = (DataPointProfileAdapter.DataPointViewHolder)listView.findViewHolderForAdapterPosition(position);
                if(mode.equals("select")) {
                    Log.e("COLOR",""+holder.cardView.getCardBackgroundColor());
                    if(holder.cardView.getCardBackgroundColor().getDefaultColor()==Color.YELLOW){
                        holder.cardView.setCardBackgroundColor(Color.WHITE);

                    } else {
                        holder.cardView.setCardBackgroundColor(Color.YELLOW);
                    }
                }
            }
        };
    }

    public void loadData() {
        if(HomePage.noReload){
            Toast.makeText(this.context, "Analyzing picture - hold on!", Toast.LENGTH_LONG).show();
            swipeRefreshLayout.setRefreshing(false);
        }
        else {
            //Clear our arraylists that hold the old data
            listData.clear();
            listView.setVisibility(View.INVISIBLE);

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
                    while (i.hasNext()) {
                        String key = ((DataSnapshot) (i.next())).getKey().toString();
                        String diagnosis = dataSnapshot.child(key).child("Diagnosis").getValue().toString();
                        String url = dataSnapshot.child(key).child("URL").getValue().toString();
                        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
                        String dateString = formatter.format(new Date(Long.valueOf(key)));
                        DataPointProfile point = new DataPointProfile(url, "Diagnosis " + diagnosis, dateString);
                        listData.add(point);

                    }
                    //dataPointProfileArrayAdapter.notifyDataSetChanged();

                    //listView.setAdapter(dataPointProfileArrayAdapter);

                    //setListViewHeight(listView);
                    showCards();
                    swipeRefreshLayout.setRefreshing(false);
                    listView.setVisibility(View.VISIBLE);

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }


            });
        }
    }
    private void showCards() {
        DataPointProfileAdapter dataPointProfileAdapter = new DataPointProfileAdapter(listData, listener);
        listView.setAdapter(dataPointProfileAdapter);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // initialize our views

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.profileSwipeRefresh);

        listView = (RecyclerView) view.findViewById(R.id.profileListView);
        listView.setHasFixedSize(true);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        listView.setLayoutManager(llm);

//        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            @Override
//            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                if(mode.equals("select")){
//                    Toast.makeText(getContext(),"Click",Toast.LENGTH_SHORT).show();
//                    view.setSelected(true);
//                    //parent.getItemAtPosition(position);
//                    //((CardView)(view.getItemAtPosition(position))).setCardBackgroundColor(Color.YELLOW);
//                }
//
//            }
//        });
        share = (TextView) view.findViewById(R.id.select);


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
                if(mode.equals("select")){
                    mode = "view";
                    share.setTextColor(Color.BLACK);
                } else {
                    mode = "select";
                    share.setTextColor(Color.BLUE);
                }
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

class DataPointProfileAdapter extends RecyclerView.Adapter<DataPointProfileAdapter.DataPointViewHolder> {
    private ArrayList<DataPointProfile> datapoints;
    private RecyclerViewClickListener mListener;
    //Default constructor
    DataPointProfileAdapter(ArrayList<DataPointProfile> datapoints, RecyclerViewClickListener listener) {
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
    public void onBindViewHolder(DataPointViewHolder pointViewHolder, int i) {
        //Set each field to its corresponding attribute
        DataPointProfile point = datapoints.get(i);
        pointViewHolder.diagnosis.setText(point.diagnosis);
        pointViewHolder.date.setText(point.date);
        //Load the proper image into the imageView using the Glide framework
        Glide.with(ProfileFragment.context)
                .load(point.url)
                .into(pointViewHolder.image);
    }

    @Override
    public DataPointViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        //Inflate the view using the proper xml layout
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.profile_photo_taken, viewGroup, false);

        return new DataPointViewHolder(itemView, mListener);
    }

    static class DataPointViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public CardView cardView;
        public TextView diagnosis;
        public TextView date;
        public ImageView image;

        private RecyclerViewClickListener mListener;

        DataPointViewHolder(View v, RecyclerViewClickListener mListener) {
            super(v);
            cardView = v.findViewById(R.id.profileCardView);
            diagnosis = v.findViewById(R.id.diagnosis);
            date = v.findViewById(R.id.date);
            image = v.findViewById(R.id.picture);
            //instantiation of views
//            cardView = (CardView)       v.findViewById(R.id.cardView);
//            title =  (TextView)         v.findViewById(R.id.bookTitle);
//            author = (TextView)         v.findViewById(R.id.bookAuthor);
//            description = (TextView)    v.findViewById(R.id.bookDescription);
//            ratingBar = (RatingBar)     v.findViewById(R.id.ratingBar);
//            bookImage = (ImageView)     v.findViewById(R.id.bookImageView);

            this.mListener = mListener;
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            mListener.onClick(v, getAdapterPosition());
        }
    }
}