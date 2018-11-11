package com.example.anjanbharadwaj.cesapp;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private DatabaseReference mReference;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private ImageView profile_image;

    public ProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ProfileFragment newInstance(String param1, String param2) {
        ProfileFragment fragment = new ProfileFragment();
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final TextView profile_name = view.findViewById(R.id.nameTag);

        mReference = FirebaseDatabase.getInstance().getReference();

        profile_image = (ImageView) view.findViewById(R.id.imageView);


        mReference.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString()).child("Name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = dataSnapshot.getValue().toString();
                profile_name.setText(name);
                profile_name.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);


            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        final RadioGroup group = (RadioGroup) view.findViewById(R.id.radioGroup);


        final CheckBox checkBox = (CheckBox) view.findViewById(R.id.public_checkbox);


        //update the UI buttons to match the options stored in the database.
        final DatabaseReference data_control_ref = mReference.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString())
                .child("DataControlSettings");

        data_control_ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                boolean can_share_data = Boolean.valueOf(dataSnapshot.child("can_share_with_researchers").getValue().toString());
                boolean is_profile_searchable = Boolean.valueOf(dataSnapshot.child("is_profile_searchable").getValue().toString());

                checkBox.setChecked(is_profile_searchable);

                if(can_share_data) {
                    group.check(R.id.radio_yes);
                } else {
                    group.check(R.id.radio_no);

                }

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        

        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                RadioButton button_checked = radioGroup.findViewById(id);

                boolean can_we_share_data_with_researchers = false;

                if (id == R.id.radio_no) {
                    if (button_checked.isChecked()) {
                        can_we_share_data_with_researchers = false;
                    }
                } else if (id == R.id.radio_yes) {
                    if(button_checked.isChecked()) {
                        can_we_share_data_with_researchers = true;
                    }
                }

                DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());

                database.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());

                database.child("DataControlSettings").child("can_share_with_researchers").setValue(can_we_share_data_with_researchers);

//                Toast.makeText(getActivity().getApplicationContext(), "Updating Data Settings", Toast.LENGTH_SHORT).show();
            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());
                database.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());

                if (compoundButton.isChecked()) {
                    database.child("DataControlSettings").child("is_profile_searchable").setValue(true);
                } else {
                    database.child("DataControlSettings").child("is_profile_searchable").setValue(false);
                }

//                Toast.makeText(getActivity().getApplicationContext(), "Updating Data Settings", Toast.LENGTH_SHORT).show();

            }
        });

        ImageView change_profile_picture = (ImageView) view.findViewById(R.id.change_profile_picture);
        change_profile_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.v("Camera", "clicked");



                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);


            }
        });
    }
    private Uri picUri;
    final int PIC_CROP = 2;
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {

            picUri = data.getData();
            performCrop();
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            DisplayMetrics metrics = new DisplayMetrics();
            this.getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
            int displayWidth = metrics.widthPixels;

            double current_width = profile_image.getWidth();
            double current_height = profile_image.getHeight();

            Bitmap current_bitmap = ((BitmapDrawable)profile_image.getDrawable()).getBitmap();

            double width_scaling_factor = current_bitmap.getWidth()/current_width;
            double height_scaling_factor = current_bitmap.getHeight()/current_height;


            Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                    imageBitmap, (int)(width_scaling_factor * current_width), (int) (height_scaling_factor * current_height), false);


            
            profile_image.setImageBitmap(resizedBitmap);
        }
        else if(requestCode == PIC_CROP){
            Bundle extras = data.getExtras();
//get the cropped bitmap
            Bitmap thePic = extras.getParcelable("data");
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
    private void performCrop(){
        try {
            Intent cropIntent = new Intent("com.android.camera.action.CROP");
            //indicate image type and Uri
            cropIntent.setDataAndType(picUri, "image/*");
            //set crop properties
            cropIntent.putExtra("crop", "true");
            //indicate aspect of desired crop
            cropIntent.putExtra("aspectX", 1);
            cropIntent.putExtra("aspectY", 1);
            //indicate output X and Y
            cropIntent.putExtra("outputX", 256);
            cropIntent.putExtra("outputY", 256);
            //retrieve data on return
            cropIntent.putExtra("return-data", true);
            //start the activity - we handle returning in onActivityResult
            startActivityForResult(cropIntent, PIC_CROP);
        }
        catch(ActivityNotFoundException anfe){
            //display an error message
            String errorMessage = "Whoops - your device doesn't support the crop action!";
            Toast toast = Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
