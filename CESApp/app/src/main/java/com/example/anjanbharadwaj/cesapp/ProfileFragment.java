package com.example.anjanbharadwaj.cesapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;


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
        StorageReference profileReference = FirebaseStorage.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString()).child("Profile");
        load_current_profile_image_from_firebase(profileReference);

        final TextView profile_name = view.findViewById(R.id.nameTag);

        mReference = FirebaseDatabase.getInstance().getReference();

        profile_image = (ImageView) view.findViewById(R.id.profile_picture);


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
                boolean is_profile_searchable = Boolean.valueOf(dataSnapshot.child("is_profile_searchable").getValue().toString());

                checkBox.setChecked(is_profile_searchable);

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

    private void load_current_profile_image_from_firebase(StorageReference profileRef1) {

        // Create a storage reference from our app
        //StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        // Create a reference to "mountains.jpg"

        //String UID = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

        //StorageReference profileRef = storageRef.child("Users/" + UID + "/Profile/pic.jpg");


        // Reference to an image file in Cloud Storage
        //StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        StorageReference profileRef = profileRef1.child("profilepic.jpg");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Log.v("OnSucess", ""+uri);
                ImageView imageView = getActivity().findViewById(R.id.profile_picture);

                Glide.with(getActivity().getApplicationContext() /* context */)
                        .asBitmap()
                        .load(uri.toString())
                        .into(imageView);

            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == getActivity().RESULT_OK) {
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

            upload_bitmap_to_firebase(resizedBitmap);
        }
    }

    private void upload_bitmap_to_firebase(Bitmap resizedBitmap) {

        // Create a storage reference from our app
        StorageReference storageRef = FirebaseStorage.getInstance().getReference();

        // Create a reference to "mountains.jpg"

        String UID = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

        StorageReference profileRef = storageRef.child("Users/" + UID + "/Profile/profilepic.jpg");

        // Get the data from an ImageView as bytes
        profile_image.setDrawingCacheEnabled(true);
        profile_image.buildDrawingCache();
        Bitmap bitmap = ((BitmapDrawable) profile_image.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        UploadTask uploadTask = profileRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
            }
        });


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
