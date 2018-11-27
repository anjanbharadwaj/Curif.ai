package com.example.anjanbharadwaj.cesapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Network;
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
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.wajahatkarim3.easyflipview.EasyFlipView;

import org.w3c.dom.Text;

import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import es.dmoral.toasty.Toasty;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link NetworkFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link NetworkFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NetworkFragment extends Fragment {


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private TextView enable_public_search;
    private ImageView error_image;
    private TextView all_perms_met;

    private RecyclerView recyclerView;
    private NetworkUserAdapter adapter;
    ArrayList<NetworkUser> listData = new ArrayList<>();
    RecyclerViewClickListener listener;


    private OnFragmentInteractionListener mListener;

    DatabaseReference database;

    public void loadData() {

            //Clear our arraylists that hold the old data
            listData.clear();
            recyclerView.setVisibility(View.INVISIBLE);

            //Instantiate the two array adapters that connect the arraylists to listviews
           // adapter = new NetworkUserAdapter(getActivity().getApplicationContext(), 0, listData);

            database = FirebaseDatabase.getInstance().getReference();
            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            database.child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(final DataSnapshot dataSnapshot) {
                    listData.clear();


                    Iterator diseasesI = dataSnapshot.child(uid).child("Conditions").getChildren().iterator();
                    ArrayList<String> diseases = new ArrayList<>();
                    while(diseasesI.hasNext()){
                        String disease = ((DataSnapshot)(diseasesI.next())).getValue().toString();
                        diseases.add(disease);
                    }

                    Iterator i = dataSnapshot.getChildren().iterator();
                    while (i.hasNext()) {
                        String uid1 = ((DataSnapshot) (i.next())).getKey().toString();
                        if(uid.equals(uid1)){
                            continue;
                        }
                        if(dataSnapshot.child(uid1).child("DataControlSettings").child("is_profile_searchable").getValue().toString().equals("false")){
                            continue;
                        }
                        ArrayList<String> conditions = new ArrayList<>();
                        Iterator conditionsI = dataSnapshot.child(uid1).child("Conditions").getChildren().iterator();
                        while(conditionsI.hasNext()){
                            conditions.add( ((DataSnapshot)(conditionsI.next())).getValue().toString());
                        }
                        ArrayList<String> common = common(conditions,diseases);
                        for(int jk = 0; jk<common.size(); jk++){

                            String diag = common.get(jk);
                            diag = diag.replace("Diagnosis ","");
                            common.set(jk, HomePage.conversionMap.get(new Integer(diag)).toString());

                        }
                        String name = dataSnapshot.child(uid1).child("Name").getValue().toString();

                        StorageReference ref = FirebaseStorage.getInstance().getReference().child("Users").child(uid1).child("Profile").child("profilepic.jpg");
                        ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String similarities = "";
                                if(common.size()>2){
                                    similarities = "You both have " + convertToDesc(common);
                                } else if(common.size()==2){
                                    similarities = "You both have " + common.get(0) + " and " + common.get(1);
                                }
                                else if(common.size()==1){
                                    similarities = "You both have " + common.get(0);
                                }
                                if(common.size()!=0) {
                                    NetworkUser nu = new NetworkUser(name, uri, similarities, uid1);
                                    listData.add(nu);
                                    Log.v("NetUser", nu.getName() + "," + nu.getDescription() + "," + nu.getUri());
                                }
                                showCards();

                            }
                        });

                    }
                    //dataPointProfileArrayAdapter.notifyDataSetChanged();

                    //listView.setAdapter(dataPointProfileArrayAdapter);

                    //setListViewHeight(listView);
                    //listView.setVisibility(View.VISIBLE);
                    Log.v("Showing cards", "now");


                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }


            });

    }

    private String convertToDesc(ArrayList<String> common) {
        String text = "";
        for(int i = 0; i<common.size(); i++){
            if(i==common.size()-1){
                text += "and " + common.get(i);
            } else{
                text += common.get(i) + ", ";
            }
        }
        return text;
    }

    private ArrayList<String> common(ArrayList<String> a, ArrayList<String> b){
        ArrayList<String> c = new ArrayList<>();
        Iterator i = a.iterator();
        while(i.hasNext()){
            String value = i.next().toString();
            if(b.contains(value)){
                c.add(value);
            }
        }
        return c;
    }
    private void showCards() {
        NetworkUserAdapter networkUserAdapter = new NetworkUserAdapter(listData, getContext(), listener);
        recyclerView.setAdapter(networkUserAdapter);
    }




    public NetworkFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment NetworkFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static NetworkFragment newInstance(String param1, String param2) {
        NetworkFragment fragment = new NetworkFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
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
                NetworkUserAdapter.NetworkUserViewHolder holder = (NetworkUserAdapter.NetworkUserViewHolder)recyclerView.findViewHolderForAdapterPosition(position);
                NetworkUser user = listData.get(position);

                holder.easyFlipView.flipTheView(true);

                holder.email.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                       emailIntent(user);
                    }
                });
                holder.phone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        phoneIntent(user);
                    }
                });
            }
        };

    }

    public void emailIntent(NetworkUser user){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(user.uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String email = dataSnapshot.child("Email").getValue().toString();
                Intent emailIntent = new Intent(Intent.ACTION_SEND);
                emailIntent.setType("text/plain");

                emailIntent.putExtra(Intent.EXTRA_EMAIL  , new String[]{email});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Support Network Email - " );
                String body1 = "Hi " + user.getName() + ", \n\n";
                emailIntent.putExtra(Intent.EXTRA_TEXT, body1.toString());

                startActivity(emailIntent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    public void phoneIntent(NetworkUser user){
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(user.uid);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String phone1 =  dataSnapshot.child("Phone").getValue().toString();
                String body1 = "Hi " + user.getName() + ", \n\n";
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_VIEW);
                sharingIntent.setType("vnd.android-dir/mms-sms");
                sharingIntent.setData(Uri.parse("sms:"+phone1));

                sharingIntent.putExtra("sms_body", body1);
                startActivity(Intent.createChooser(sharingIntent, "Share"));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_network, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        enable_public_search = (TextView) view.findViewById(R.id.enable_public_search);
        error_image = (ImageView) view.findViewById(R.id.error_image);
        all_perms_met = (TextView) view.findViewById(R.id.granted);


        //initialize recycler view
        recyclerView = (RecyclerView) view.findViewById(R.id.network_recycler_view);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext()));

        //dummy data
//        List<NetworkUser> dummy_users = new ArrayList<>();
//        dummy_users.add(new NetworkUser("Billy Joe"));
//        dummy_users.add(new NetworkUser("John Morgan"));
//        dummy_users.add(new NetworkUser("Sreehari Ram Mohan"));
//        dummy_users.add(new NetworkUser("Johny English"));
        loadData();
        adapter = new NetworkUserAdapter(listData, this.getContext(), listener);

        recyclerView.setAdapter(adapter);



        final DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());
        database.child("DataControlSettings").child("is_profile_searchable").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean can_share = Boolean.valueOf(dataSnapshot.getValue().toString());

                if(can_share == false) {
                    all_perms_met.setVisibility(View.INVISIBLE);
                    error_image.setVisibility(View.VISIBLE);
                    enable_public_search.setVisibility(View.VISIBLE);
                    //Toasty.warning(getContext(), "Enable Public Searching", Toast.LENGTH_LONG, true).show();
                    recyclerView.setVisibility(View.INVISIBLE);
                } else {
                    all_perms_met.setVisibility(View.INVISIBLE);
                    error_image.setVisibility(View.INVISIBLE);
                    enable_public_search.setVisibility(View.INVISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                    //Toasty.success(getContext(), "Privacy Permissions Granted", Toast.LENGTH_LONG, true).show();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


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

class NetworkUser {
    private String name;
    private Uri uri;
    private String description;
    public String uid;
    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public NetworkUser(String name, Uri uri, String description, String uid) {
        this.name = name;
        this.uri = uri;
        this.description = description;
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}

class NetworkUserAdapter extends RecyclerView.Adapter<NetworkUserAdapter.NetworkUserViewHolder>{

    private List<NetworkUser> listItems;
    private Context context;
    private RecyclerViewClickListener mListener;

    public NetworkUserAdapter(List<NetworkUser> listItems, Context context, RecyclerViewClickListener listener) {
        this.listItems = listItems;
        this.context = context;
        mListener = listener;
    }

    @NonNull
    @Override
    public NetworkUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.network_profile, parent, false);
        return new NetworkUserViewHolder(v, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull NetworkUserViewHolder holder, int position) {
        NetworkUser user = listItems.get(position);
        String name = user.getName();
        String description = user.getDescription();
        Uri uri = user.getUri();
        //setting the textview to our data
        holder.name_textview.setText(name);
        holder.description.setText(description);

        Glide.with(context).load(uri).into(holder.profilepic);

    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    static class NetworkUserViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView name_textview;
        public TextView description;
        public ImageView profilepic;
        public EasyFlipView easyFlipView;
        private RecyclerViewClickListener mListener;

        //-----------------------------------------------------
        public ImageView email;
        public ImageView phone;
        NetworkUserViewHolder(View v, RecyclerViewClickListener mListener) {
            super(v);
            name_textview = (TextView) v.findViewById(R.id.name_front);
            description = (TextView) v.findViewById(R.id.description_front);
            profilepic = (ImageView)v.findViewById(R.id.profile_image_front);
            easyFlipView = (EasyFlipView)v.findViewById(R.id.flipview);
            email = (ImageView)v.findViewById(R.id.emailButton);
            phone = (ImageView)v.findViewById(R.id.phoneButton);
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

