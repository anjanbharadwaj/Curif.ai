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
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import com.suke.widget.SwitchButton;

import java.io.ByteArrayOutputStream;

import br.com.sapereaude.maskedEditText.MaskedEditText;


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

    ImageView profile_image;
    MaskedEditText phoneNumber;
    SwitchButton switchButton;
    TextView profile_name;
    ImageView change_profile_picture;
    Button editProfile;
    TextInputEditText bio;
    MaskedEditText height;
    MaskedEditText weight;
    MaskedEditText age;
    EditText doctor_name;
    EditText doctor_email;
    MaskedEditText doctor_phone;
    boolean editEnabled = false;
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


        mReference = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());

        profile_name = view.findViewById(R.id.nameTag);
        profile_image = (ImageView) view.findViewById(R.id.imageView);
        phoneNumber = (MaskedEditText)view.findViewById(R.id.profile_phone_text);
        switchButton = (SwitchButton)view.findViewById(R.id.profile_data_switch);
        change_profile_picture = (ImageView) view.findViewById(R.id.change_profile_picture);
        editProfile = (Button)view.findViewById(R.id.editProfileButton);
        bio = (TextInputEditText) view.findViewById(R.id.profile_bio_text);
        height = (MaskedEditText)view.findViewById(R.id.profile_height_text);
        weight = (MaskedEditText)view.findViewById(R.id.profile_weight_text);
        age = (MaskedEditText)view.findViewById(R.id.profile_age_text);
        doctor_email = (EditText)view.findViewById(R.id.profile_doctor_text_email);
        doctor_name = (EditText)view.findViewById(R.id.profile_doctor_text_name);
        doctor_phone = (MaskedEditText)view.findViewById(R.id.profile_doctor_text_phone);
        profile_name.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        setAllEditTextsEditStatus(false);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(editEnabled){
                    editProfile.setText("                    Edit Profile                    ");
                    saveValuesFromEditText(mReference);
                    setAllEditTextsEditStatus(false);
                    editProfile.setBackground(getResources().getDrawable(R.drawable.roundshapebutton));


                }else {
                    editProfile.setText("                    Save Changes                    ");
                    editProfile.setBackground(getResources().getDrawable(R.drawable.roundshapebuttonv2));
                    setAllEditTextsEditStatus(true);

                }
                editEnabled=!editEnabled;
            }
        });
        mReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("Name").getValue().toString();
                profile_name.setText(name);


                boolean is_profile_searchable = Boolean.valueOf(dataSnapshot.child("DataControlSettings").child("is_profile_searchable").getValue().toString());
                Log.v("DATAPUBLIC",""+is_profile_searchable);
                switchButton.setChecked(is_profile_searchable);

                String phoneNumberText = dataSnapshot.child("Phone").getValue().toString();
                if(!phoneNumberText.isEmpty()) {
                    phoneNumber.setText(phoneNumberText);
                }else{
                    phoneNumber.setText("Add your phone number!");
                }


                try {
                    String bioText = dataSnapshot.child("Bio").getValue().toString();
                    if (!bioText.isEmpty()) {
                        bio.setText(bioText);
                    } else {
                    }
                }catch(Exception e){
                }

                try {
                    String ageText = dataSnapshot.child("Age").getValue().toString();
                    if (!ageText.isEmpty()) {
                        age.setText(ageText);
                    } else {
                    }
                }catch(Exception e){

                }
                try {
                    String heightText = dataSnapshot.child("Height").getValue().toString();
                    if (!heightText.isEmpty()) {
                        height.setText(heightText);
                    } else {
                    }
                }catch (Exception e){

                }

                try {
                    String weightText = dataSnapshot.child("Weight").getValue().toString();
                    if (!weightText.isEmpty()) {
                        weight.setText(weightText);
                    } else {
                    }
                }catch(Exception e){

                }

                try {
                    String doctorNameText = dataSnapshot.child("DoctorInfo").child("Name").getValue().toString();
                    if (!doctorNameText.isEmpty()) {
                        doctor_name.setText(doctorNameText);
                    } else {
                    }
                }catch(Exception e){

                }

                try {
                    String doctorEmailText = dataSnapshot.child("DoctorInfo").child("Email").getValue().toString();
                    if (!doctorEmailText.isEmpty()) {
                        doctor_email.setText(doctorEmailText);
                    } else {

                    }
                }catch(Exception e){

                }

                try {
                    String doctorPhoneText = dataSnapshot.child("DoctorInfo").child("Phone").getValue().toString();
                    if (doctorPhoneText.isEmpty()) {
                        doctor_phone.setText(doctorPhoneText);
                    } else {

                    }
                }catch(Exception e){

                }






            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


                DatabaseReference database = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());
                database.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());

        change_profile_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.v("Camera", "clicked");



                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);


            }
        });



    }

    private void saveValuesFromEditText(DatabaseReference ref) {
        ref.child("Phone").setValue(phoneNumber.getText().toString().trim());
        ref.child("Bio").setValue(bio.getText().toString());
        ref.child("Height").setValue(height.getRawText().toString());
        ref.child("Weight").setValue(weight.getRawText().toString());
        ref.child("Age").setValue(age.getRawText().toString());
        ref.child("DoctorInfo").child("Email").setValue(doctor_email.getText().toString().trim());
        ref.child("DoctorInfo").child("Phone").setValue(doctor_phone.getRawText().toString().trim());
        ref.child("DoctorInfo").child("Name").setValue(doctor_name.getText().toString().trim());
        ref.child("DataControlSettings").child("is_profile_searchable").setValue(switchButton.isChecked());
    }

    private void setAllEditTextsEditStatus(boolean b) {
        phoneNumber.setEnabled(b);
        switchButton.setEnabled(b);
        bio.setEnabled(b);
        height.setEnabled(b);
        weight.setEnabled(b);
        age.setEnabled(b);
        doctor_email.setEnabled(b);
        doctor_name.setEnabled(b);
        doctor_phone.setEnabled(b);

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
                ImageView imageView = getActivity().findViewById(R.id.imageView);

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
