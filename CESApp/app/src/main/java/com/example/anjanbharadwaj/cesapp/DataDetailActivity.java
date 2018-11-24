package com.example.anjanbharadwaj.cesapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.hsalf.smilerating.BaseRating;
import com.hsalf.smilerating.SmileRating;
import com.klinker.android.sliding.SlidingActivity;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;


public class DataDetailActivity extends SlidingActivity {
    int position = -1;
    int primaryColorDark;
    ProgressBar detailProgressBar;
    TextView diagnosisValue;
    TextView dateValue;
    TextView dateExpectedValue;
    SmileRating feelingValue;
    CardView lastTreatmentCard;
    TextView lastTreatmentValue;
    TextView moreInfoValue;
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference();

    //private int numCopies;

    //the following init method is comparable to the onCreate() method of our other activites
    @Override
    public void init(Bundle savedInstanceState) {
        //Create a book object, and initialize it to a Parcelable object (since Book is a SearchSuggestion and SearchSuggestion is a Parcelable)
        final DataPointProfile dpp = getIntent().getParcelableExtra("DataPointProfile");
        //Set database references that we will use later; we do this here because we need to set them based on user id and book isbn
        String diagnosis = dpp.diagnosis;
        final String date = dpp.date;
        final String unformatdate = dpp.nonformatdate;
        final String location = dpp.location;
        String url = dpp.url;
        Bitmap bitmap = null;
        primaryColorDark = Color.BLACK;
        Glide.with(this)
                .asBitmap()
                .load(url)
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap image, Transition<? super Bitmap> transition) {
                        setImage(image);

                    }
                });
        //Toast.makeText(getApplicationContext(), ""+primaryColorDark, Toast.LENGTH_LONG).show();
                //holds = reference.child("Books").child(dpp.).child("Holds");
        //userHold = reference.child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("BooksOnHold");

        setTitle(date);

        //Glide.with(getApplicationContext()).
//        byte[] byteArray = getIntent().getByteArrayExtra("Image");
//        BitmapFactory.Options options = new BitmapFactory.Options();
//        options.inMutable = true;
//        Bitmap image = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length, options);
//        Bitmap original = image.copy(image.getConfig(), true);
//        the following lines of code alters the color scheme of the top bar of the app and the buttons in our app
//          to match the book cover's color
//
//        image = darkenBitMap(image);
//        image = blur(image);
//        setImage(image);
        //Palette p = Palette.from(original).generate();

        setContent(R.layout.activity_detail);
        //initialize views that will be used later

        diagnosisValue = (TextView)findViewById(R.id.detailDiagnosisText);
        dateValue = (TextView)findViewById(R.id.detailDateText);
        dateExpectedValue = (TextView)findViewById(R.id.detailExpectedDate);
        feelingValue = (SmileRating)findViewById(R.id.smile_rating);
        lastTreatmentCard = (CardView)findViewById(R.id.detailTreatmentCardView);
        lastTreatmentValue = (TextView)findViewById(R.id.detailTreatmentText);
        moreInfoValue = (TextView)findViewById(R.id.detailMoreInfo);
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        reference.child("Users").child(uid).child("Pictures").child(location).child(unformatdate).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try {
                    Integer i = Integer.parseInt(dataSnapshot.child("Feeling").getValue().toString());
                    if(i==1){
                        feelingValue.setSelectedSmile(BaseRating.TERRIBLE, true);

                    } else if(i==2){
                        feelingValue.setSelectedSmile(BaseRating.BAD, true);

                    } else if(i==3){
                        feelingValue.setSelectedSmile(BaseRating.OKAY, true);

                    } else if(i==4){
                        feelingValue.setSelectedSmile(BaseRating.GOOD, true);

                    } else if(i==5){
                        feelingValue.setSelectedSmile(BaseRating.GREAT, true);

                    }
                } catch(Exception e){

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        feelingValue.setOnRatingSelectedListener(new SmileRating.OnRatingSelectedListener() {
            @Override
            public void onRatingSelected(int level, boolean reselected) {
                String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                reference.child("Users").child(uid).child("Pictures").child(location).child(unformatdate).child("Feeling").setValue(level);
            }
        });
        ImageView icon1 = (ImageView)findViewById(R.id.detailDiagnosisImageView);
        ImageView icon2 = (ImageView)findViewById(R.id.detailDateImageView);
        ImageView icon3 = (ImageView)findViewById(R.id.detailExpectedDateImageView);
        ImageView icon4 = (ImageView)findViewById(R.id.detailFeelingImageView);
        ImageView icon5 = (ImageView)findViewById(R.id.detailMoreInfoImageView);

        //save the color of our button so that if the user holds and un-holds, we can keep this color scheme

        //adjust the color scheme based on the values we extracted earlier - then, set the text/value for each view
        icon1.setColorFilter(primaryColorDark);
        icon2.setColorFilter(primaryColorDark);
        icon3.setColorFilter(primaryColorDark);
        icon4.setColorFilter(primaryColorDark);
        icon5.setColorFilter(primaryColorDark);
        diagnosisValue.setText(diagnosis);
        dateValue.setText(date);
        dateExpectedValue.setText("12/1/2018");
        feelingValue.setSelectedSmile(BaseRating.OKAY);
        lastTreatmentValue.setText("10/20/2018");
        moreInfoValue.setText("According to Mayo Clinic, this disease is pretty common!");

        //diagnosisValue.setTextColor(primaryColorDark);
        dateValue.setTextColor(primaryColorDark);
        dateExpectedValue.setTextColor(primaryColorDark);
        lastTreatmentValue.setTextColor(primaryColorDark);
        moreInfoValue.setTextColor(primaryColorDark);


        //final String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();



    }

    //a simple function to convert any integer to a string with a suffix after ("st", "nd", etc)
    private String suffix(int position) {

        int lastDigit = position % 10;
        int tensDigit = (position / 10) % 10;

        if (tensDigit == 1) return "th";

        switch (lastDigit) {
            case 1:
                return "st";
            case 2:
                return "nd";
            case 3:
                return "rd";
            default:
                return "th";
        }
    }

    public void show(View v) {
        v.setVisibility(View.VISIBLE);
    }

    public void hide(View v) {
        v.setVisibility(View.INVISIBLE);
    }

    //this is a function that blurs an image to create a more aesthetic look/portray the cover as a background, not foreground
    public Bitmap blur(Bitmap image) {
        if (image == null) return null;

        Bitmap outputBitmap = Bitmap.createBitmap(image);
        final RenderScript renderScript = RenderScript.create(this);
        Allocation tmpIn = Allocation.createFromBitmap(renderScript, image);
        Allocation tmpOut = Allocation.createFromBitmap(renderScript, outputBitmap);

        //Use the Intrinsic Gausian blur filter on the entire image, and return the editted bitmap
        ScriptIntrinsicBlur intrinsicBlur = ScriptIntrinsicBlur.create(renderScript, Element.U8_4(renderScript));
        intrinsicBlur.setRadius(1);
        intrinsicBlur.setInput(tmpIn);
        intrinsicBlur.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);
        return outputBitmap;
    }

    //this is a function that darkens an image to create a more aesthetic look/portray the cover as a background, not foreground
    private Bitmap darkenBitMap(Bitmap bm) {

        Canvas canvas = new Canvas(bm);
        //The Color.RED value and 0xFF7F7F7F value are used to create a dark filter
        Paint p = new Paint(Color.RED);
        ColorFilter filter = new LightingColorFilter(0xFF7F7F7F, 0x00000000);    // darken
        p.setColorFilter(filter);
        canvas.drawBitmap(bm, new Matrix(), p);

        return bm;
    }

    //This method changes the rgb values by a scalar to either darken/lighten the image
    public static int manipulateColor(int color, float factor) {
        int a = Color.alpha(color);
        int r = Math.round(Color.red(color) * factor);
        int g = Math.round(Color.green(color) * factor);
        int b = Math.round(Color.blue(color) * factor);
        return Color.argb(a,
                Math.min(r, 255),
                Math.min(g, 255),
                Math.min(b, 255));

    }


}
