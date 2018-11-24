package com.example.anjanbharadwaj.cesapp;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.text.Html;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.arlib.floatingsearchview.FloatingSearchView;
import com.arlib.floatingsearchview.suggestions.SearchSuggestionsAdapter;
import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.ml.custom.FirebaseModelDataType;
import com.google.firebase.ml.custom.FirebaseModelInputOutputOptions;
import com.google.firebase.ml.custom.FirebaseModelInputs;
import com.google.firebase.ml.custom.FirebaseModelInterpreter;
import com.google.firebase.ml.custom.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseModelOptions;
import com.google.firebase.ml.custom.FirebaseModelOutputs;
import com.google.firebase.ml.custom.model.FirebaseLocalModelSource;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class HomePage extends AppCompatActivity implements ProfileFragment.OnFragmentInteractionListener, HomeFragment.OnFragmentInteractionListener, ProgressFragment.OnFragmentInteractionListener,
NetworkFragment.OnFragmentInteractionListener{
    static boolean noReload = false;
    TabLayout tabLayout;
    ViewPager viewPager;
    Toolbar toolbar;
    FloatingSearchView searchView;
    CoordinatorLayout coordinatorLayout;
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference();
    static FloatingActionButton fab;
    static ArrayList<PersonSearchItem> suggestions = new ArrayList<>();

    final int VOICE_SEARCH_CODE = 3012;

    private static final int NOTIFICATION_ID = 12345;


    private Context mContext=HomePage.this;
    private static final int REQUEST = 112;
    RecyclerViewClickListener listener;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        Context context = getApplicationContext();

        StrictMode.VmPolicy.Builder builder1 = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder1.build());
        if (Build.VERSION.SDK_INT >= 23) {
            String[] PERMISSIONS = {android.Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (!hasPermissions(mContext, PERMISSIONS)) {
                ActivityCompat.requestPermissions((Activity) mContext, PERMISSIONS, REQUEST );
            } else {
                //do here
            }
        } else {
            //do here
        }
        searchView = (FloatingSearchView) findViewById(R.id.searchView);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        coordinatorLayout.bringToFront();

        viewPager = (ViewPager) findViewById(R.id.viewPager);

        //set adapter to your ViewPager
        viewPager.setAdapter(new PageAdapter(getSupportFragmentManager()));

        //intialize the tab layout
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        //add 3 new tabs.
        tabLayout.addTab(tabLayout.newTab());
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setupWithViewPager(viewPager);

        //default tab that it loads on is the middle tab
        viewPager.setCurrentItem(0);


        //create page listener to return the tab at a certain position.
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Log.v("SELECTION", HomeFragment.mode);

                if(HomeFragment.mode.equals("view")) {
                    takePhoto(view);
                } else {
                    createAndSendReport();
                }
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //      .setAction("Action", null).show();
            }
        });
        //set the behavior when the menu is clicked.
        searchView.setOnMenuItemClickListener(new FloatingSearchView.OnMenuItemClickListener() {
            @Override
            public void onActionMenuItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.voice:
                        // Voice search
                        searchView.setSearchFocused(true);
                        break;
                    case R.id.feedback:
                        //Send a bug report via email using email intent.
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/html");
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"cesapp@gmail.com"});
                        intent.putExtra(Intent.EXTRA_SUBJECT, "CES App Bug Report");
                        intent.putExtra(Intent.EXTRA_TEXT, "My bug...");

                        startActivity(Intent.createChooser(intent, "Send Email"));
                        break;
                    case R.id.logout:
                        Toast.makeText(HomePage.this, "LOGOUT", Toast.LENGTH_LONG).show();
                        //use our authentication database to sign out.
                        FirebaseAuth auth = FirebaseAuth.getInstance();
                        auth.signOut();
                        //move user to sign in page once signed out.
                        Intent i = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(i);
                        break;
                    default:
                        break;
                }
            }
        });

        searchView.setOnQueryChangeListener(new FloatingSearchView.OnQueryChangeListener() {
            @Override
            public void onSearchTextChanged(String oldQuery, final String newQuery) {
                updateSearches(newQuery);
                Log.v("searchingplzwork", "setOnQueryChangeListener");

            }

        });

        searchView.setOnFocusChangeListener(new FloatingSearchView.OnFocusChangeListener() {
            @Override
            public void onFocus() {
                updateSearches(searchView.getQuery());
                Log.v("searchingplzwork", "onFocus");
            }

            @Override
            public void onFocusCleared() {
                Log.v("searchingplzwork", "onFocusCleared");
            }
        });

        //manages search query suggestions.
        searchView.setOnBindSuggestionCallback(new SearchSuggestionsAdapter.OnBindSuggestionCallback() {
            @Override
            public void onBindSuggestion(View suggestionView, ImageView leftIcon, TextView textView, SearchSuggestion item, int itemPosition) {

                Log.v("searchingplzwork", "onBindSuggestion");

            }

        });

        searchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSuggestionClicked(SearchSuggestion searchSuggestion) {
                searchView.setSearchFocused(false);
                Log.v("searchingplzwork2", "onSuggestionClicked: " + searchSuggestion.getBody());

                //launch an intent to send a text message.

//                String phone1 =  dataSnapshot.child("Phone").getValue().toString();
//                String body1 = "Hi " + user.getName() + ", \n\n";
//                Intent sharingIntent = new Intent(android.content.Intent.ACTION_VIEW);
//                sharingIntent.setType("vnd.android-dir/mms-sms");
//                sharingIntent.setData(Uri.parse("sms:"+phone1));
//
//                sharingIntent.putExtra("sms_body", body1);
//                startActivity(Intent.createChooser(sharingIntent, "Share"));


            }

            @Override
            public void onSearchAction(String currentQuery) {
                Log.v("searchingplzwork", "onSearchAction");

            }
        });



    }



    private void createAndSendReport() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                500);


        Log.v("SELECTION", "In create and send report");

        Intent emailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL  , new String[]{"[doctor's email]@gmail.com"});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "[Patient Name]'s Lesion Report");
        String body = "";

        Log.v("SELECTION", ""+HomeFragment.selectedInformation.size());
        ArrayList<Uri> uris = new ArrayList<Uri>();
        for(int i = 0; i < HomeFragment.selectedInformation.size(); i++) {
            DiagnosisListItemInfo selectedItem = HomeFragment.selectedInformation.get(i);

            String diagnosis = selectedItem.getDiagnosis();
            String date = selectedItem.getDate();
            Bitmap photo = selectedItem.getPhoto();

            Log.v("SELECTION", diagnosis + " - " + date);

            body += diagnosis + " - " + date + "(photo " + i + " attached):" + "\n";
            File file = saveToInternalStorage(diagnosis.trim() + "-" + date.replaceAll("/","_").trim(),photo);

           // String saved_photo_directory = saveToInternalStorage(photo);

          //  String[] s = new String[2];

// Type the path of the files in here
          //  s[0] = saved_photo_directory;
            //s[1] = inputPath + "/textfile.txt"; // /sdcard/ZipDemo/textfile.txt

// first parameter is d files second parameter is zip file name

// calling the zip function
           // String zipDir = saved_photo_directory;
           // zipDir = saved_photo_directory.substring(0,saved_photo_directory.lastIndexOf("/"))+"/"+System.currentTimeMillis();
           // zip(s, zipDir);
           // Toast.makeText(getApplicationContext(),"Dir: " + zipDir, Toast.LENGTH_LONG).show();
           // File file = new File(saved_photo_directory,"profile.jpg");
            Uri pngUri = Uri.fromFile(file);
            uris.add(pngUri);
        }
        emailIntent.putExtra(Intent.EXTRA_STREAM, uris);

        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        startActivityForResult(Intent.createChooser(emailIntent, "Send report..."),12);

    }

    private File saveToInternalStorage(String name, Bitmap bitmapImage){
        File file = new File(Environment.getExternalStorageDirectory() + File.separator + name + "-- " + System.currentTimeMillis() + ".jpg");
        OutputStream os = null;
        try {
            os = new BufferedOutputStream(new FileOutputStream(file));
            bitmapImage.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;


    }

    public void onFragmentInteraction(Uri uri) {

    }
    public void zip(String[] _files, String zipFileName) {
        int BUFFER = 2048;

        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(zipFileName);
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            byte data[] = new byte[BUFFER];

            for (int i = 0; i < _files.length; i++) {
                Log.v("Compress", "Adding: " + _files[i]);
                FileInputStream fi = new FileInputStream(_files[i]);
                origin = new BufferedInputStream(fi, BUFFER);

                ZipEntry entry = new ZipEntry(_files[i].substring(_files[i].lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;

                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }

            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateSearches(String query) {

        searchView.showProgress();

        final String newQuery = query;
        final DatabaseReference searchRef = database.getReference().child("Users");
               // .child(FirebaseAuth.getInstance().getCurrentUser().getUid().toString());

        searchRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                suggestions.clear();

                Log.v("viewPager", ""+viewPager.getCurrentItem());

                switch (viewPager.getCurrentItem()) {
                    case 0:
                        //home search
                        //suggestions.add(new DataPointProfile("url", "unimplemented", "9/12/12", "29239023", "butt"));
                    case 1:
                        //progress search
                        //suggestions.add(new DataPointProfile("url", "constipation", "9/12/12", "29239023", "butt"));
                    case 2:
                        //network search

                        for (DataSnapshot s : dataSnapshot.getChildren()) {

                            String UID = s.getKey();

                            String name = s.child("Name").getValue().toString();
                            String email = s.child("Email").getValue().toString();
                            String phone = s.child("Phone").getValue().toString();

                            String lQuery = newQuery.toLowerCase();
                            StringTokenizer st = new StringTokenizer(lQuery);

                            boolean allTokens = false;

                            while (st.hasMoreTokens()) {
                                String currToken = st.nextToken();
                                if (name.toLowerCase().contains(currToken) || email.toLowerCase().contains(currToken) || phone.toLowerCase().contains(currToken)) {
                                    allTokens = true;
                                } else {
                                    allTokens = false;
                                    break;
                                }
                            }

                            if (allTokens) {
                                suggestions.add(new PersonSearchItem(UID, name, email, phone));
                            }

                        }

                    case 3:
                        //profile search
                        //suggestions.add(new DataPointProfile("url", "unimplemented", "9/12/12", "29239023", "butt"));
                    default:
                        //default search
                }





                searchView.swapSuggestions(suggestions);
                searchView.hideProgress();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    //starts android voice recognition Intent.
    public void startVoiceRecognition() {
        Intent intent = new Intent("android.speech.action.RECOGNIZE_SPEECH");
        intent.putExtra("android.speech.extra.LANGUAGE_MODEL", "free_form");
        intent.putExtra("android.speech.extra.PROMPT", "Speak Now");
        this.startActivityForResult(intent, VOICE_SEARCH_CODE);
    }

    private static boolean hasPermissions(Context context, String... permissions) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home_page, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        //if (id == R.id.action_settings) {
         //   return true;
        //}

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_home_page, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText("Hi");
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }
    }

    private static final int TAKE_PICTURE = 1;
    private Uri imageUri;

    public void takePhoto(View view) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File photo = new File(Environment.getExternalStorageDirectory(),  "Pic.jpg");
        intent.putExtra(MediaStore.EXTRA_OUTPUT,
                Uri.fromFile(photo));
        imageUri = Uri.fromFile(photo);
        startActivityForResult(intent, TAKE_PICTURE);
    }

    public Bitmap getResizedBitmap(Bitmap image, int bitmapWidth, int bitmapHeight) {
        return Bitmap.createScaledBitmap(image, bitmapWidth, bitmapHeight, true);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    Uri selectedImage = imageUri;
                    getContentResolver().notifyChange(selectedImage, null);
                    ContentResolver cr = getContentResolver();

                    new MaterialDialog.Builder(this)
                            .title("Location of Infection")
                            .content("")
                            .inputType(InputType.TYPE_CLASS_TEXT)
                            .input("Where was the wound found", "", new MaterialDialog.InputCallback() {
                                @Override
                                public void onInput(MaterialDialog dialog, CharSequence charSequence) {
                                    Log.v("dialog_output", charSequence.toString());
                                    String location_of_wound = charSequence.toString();


                                    try {
                                        Bitmap bitmap = android.provider.MediaStore.Images.Media
                                                .getBitmap(cr, selectedImage);

                                        FirebaseLocalModelSource localSource = new FirebaseLocalModelSource.Builder("my_local_model")
                                                .setAssetFilePath("quantized_model.tflite")  // Or setFilePath if you downloaded from your host
                                                .build();
                                        FirebaseModelManager.getInstance().registerLocalModelSource(localSource);

                                        FirebaseModelOptions options = new FirebaseModelOptions.Builder()
                                                .setLocalModelName("my_local_model")
                                                .build();
                                        FirebaseModelInterpreter firebaseInterpreter =
                                                FirebaseModelInterpreter.getInstance(options);

                                        FirebaseModelInputOutputOptions inputOutputOptions =
                                                new FirebaseModelInputOutputOptions.Builder()
                                                        .setInputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 56, 75, 3})
                                                        .setOutputFormat(0, FirebaseModelDataType.FLOAT32, new int[]{1, 4})
                                                        .build();

                                        Bitmap scaled_bitmap = getResizedBitmap(bitmap, 56, 75);


                                        float[][][][] input = new float[1][56][75][3];

                                        //Converting bitmap to byte array for ML processing
                                        for (int y = 0; y < scaled_bitmap.getHeight(); y++) {
                                            for (int x = 0; x < scaled_bitmap.getWidth(); x++) {

                                                int color = scaled_bitmap.getPixel(x, y);

                                                float red = Color.red(color);
                                                float blue = Color.blue(color);
                                                float green = Color.green(color);
                                                float alpha = Color.alpha(color);

                                                input[0][x][y][0] = red;
                                                input[0][x][y][1] = green;
                                                input[0][x][y][2] = blue;
                                            }
                                        }

                                        System.out.println("Here before floating point model");

                                        // Floating-point model:
                                        float[][][][] postNormalizedInput = new float[1][56][75][3];
                                        for (int y = 0; y < scaled_bitmap.getHeight(); y++) {
                                            for (int x = 0; x < scaled_bitmap.getWidth(); x++) {
                                                for (int c = 0; c < 3; c++) {
                                                    // Normalize channel values to [-1.0, 1.0]
                                                    postNormalizedInput[0][x][y][c] = input[0][x][y][c] / 255.0f;
                                                }
                                            }
                                        }

                                        System.out.println("Normalized Data");


                                        FirebaseModelInputs inputs = new FirebaseModelInputs.Builder()
                                                .add(postNormalizedInput)  // add() as many input arrays as your model requires
                                                .build();
                                        Task<FirebaseModelOutputs> result =
                                                firebaseInterpreter.run(inputs, inputOutputOptions)
                                                        .addOnSuccessListener(
                                                                new OnSuccessListener<FirebaseModelOutputs>() {
                                                                    @Override
                                                                    public void onSuccess(FirebaseModelOutputs result) {
                                                                        System.out.println("Successfully got a result from ML Model");
                                                                        float[][] output = result.<float[][]>getOutput(0);
                                                                        float[] probabilities = output[0];

                                                                        Log.e("PROBS", Arrays.toString(probabilities));
                                                                        System.out.println(Arrays.toString(probabilities));
                                                                        noReload = true;
                                                                        //Toast.makeText(mContext, "Prediction Made!", Toast.LENGTH_LONG).show();
                                                                        saveData(probabilities, bitmap, location_of_wound);

                                                                    }
                                                                })
                                                        .addOnFailureListener(
                                                                new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        e.printStackTrace();
                                                                    }
                                                                });


                                    } catch (Exception e) {
                                        Toast.makeText(HomePage.this, "Failed to load", Toast.LENGTH_SHORT)
                                                .show();
                                        Log.e("Camera", e.toString());
                                    }
                                }


                            }).show();


                }
        }

    }
    public void saveData(float[] probabilities, Bitmap bitmap, String wound_location){
        final String time = ""+System.currentTimeMillis();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid().toString();

        int maxIndex = 0;
        for(int i = 0; i<probabilities.length; i++){
            if(probabilities[i]>probabilities[maxIndex]){
              maxIndex = i;
            }
            ref.child("Users").child(uid).child("Pictures").child(wound_location).child(time).child("FullPredictions").child("Diagnosis "+(i+1)).setValue(probabilities[i]);
        }
        maxIndex+=1;
        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference().child("Users").child(uid).child("Pictures").child(wound_location);
        ref.child(time).child("Feeling").setValue(3);
        ref.child(time).child("Diagnosis").setValue(""+maxIndex);

        final StorageReference imagesRef = FirebaseStorage.getInstance().getReference().child("Users").child(uid).child("Pictures").child(wound_location).child(time);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 25, baos);
        byte[] data = baos.toByteArray();
        UploadTask uploadTask = imagesRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                noReload = false;
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {


                imagesRef.getDownloadUrl().addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        ref.child(time).child("URL").setValue(task.getResult().toString());
                        noReload = false;
                    }
                });
            }
        });

    }
    private class PageAdapter extends FragmentPagerAdapter {

        PageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return new HomeFragment();
                case 1:
                    return new ProgressFragment();
                case 2:
                    return new NetworkFragment();
                case 3:
                    return new ProfileFragment();
                default:
                    return new PlaceholderFragment();
            }
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Home";
                case 1:
                    return "Progress";
                case 2:
                    return "Network";
                case 3:
                    return "Profile";
                default:
                    return "Placeholder";
            }
        }
    }



}


