package com.example.anjanbharadwaj.cesapp;

import android.content.Context;
import android.net.Network;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
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


    private OnFragmentInteractionListener mListener;

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
        List<NetworkUser> dummy_users = new ArrayList<>();
        dummy_users.add(new NetworkUser("Billy Joe"));
        dummy_users.add(new NetworkUser("John Morgan"));
        dummy_users.add(new NetworkUser("Sreehari Ram Mohan"));
        dummy_users.add(new NetworkUser("Johny English"));

        adapter = new NetworkUserAdapter(dummy_users, this.getContext());

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
                    Toasty.warning(getContext(), "Enable Public Searching", Toast.LENGTH_LONG, true).show();
                    recyclerView.setVisibility(View.INVISIBLE);
                } else {
                    all_perms_met.setVisibility(View.INVISIBLE);
                    error_image.setVisibility(View.INVISIBLE);
                    enable_public_search.setVisibility(View.INVISIBLE);
                    recyclerView.setVisibility(View.VISIBLE);
                    Toasty.success(getContext(), "Privacy Permissions Granted", Toast.LENGTH_LONG, true).show();
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

    public NetworkUser(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

class NetworkUserAdapter extends RecyclerView.Adapter<NetworkUserAdapter.ViewHolder>{

    private List<NetworkUser> listItems;
    private Context context;

    public NetworkUserAdapter(List<NetworkUser> listItems, Context context) {
        this.listItems = listItems;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.network_profile, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        NetworkUser user = listItems.get(position);
        String name = user.getName();

        //setting the textview to our data
        holder.name_textview.setText(name);
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }


    //this object represents the view item
    class ViewHolder extends RecyclerView.ViewHolder {

        public TextView name_textview;

        public ViewHolder(View itemView) {
            super(itemView);

            name_textview = (TextView) itemView.findViewById(R.id.name);
        }
    }

}
