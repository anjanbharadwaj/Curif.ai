package com.android.anjansree.curifai;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v8.renderscript.Allocation;
import android.support.v8.renderscript.Element;
import android.support.v8.renderscript.RenderScript;
import android.support.v8.renderscript.ScriptIntrinsicBlur;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

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


public class DataDetailActivity extends SlidingActivity {
    int position = -1;
    int primaryColorDark;
    ProgressBar detailProgressBar;
    TextView diagnosisValue;
    TextView dateValue;
    SmileRating feelingValue;
    TextView moreInfoValue;
    TextView locationValue;

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



        String diag = diagnosis;
        diag = diag.replace("Diagnosis ","");

        String diagCopy = HomePage.conversionMap.get(new Integer(diag)).toString();
        diagnosis = (HomePage.conversionMap.get(new Integer(diag)).toString());


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
        feelingValue = (SmileRating)findViewById(R.id.smile_rating);
        moreInfoValue = (TextView)findViewById(R.id.detailMoreInfo);
        locationValue = (TextView)findViewById(R.id.detailLocationInfo);

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
        ImageView icon4 = (ImageView)findViewById(R.id.detailFeelingImageView);
        ImageView icon5 = (ImageView)findViewById(R.id.detailMoreInfoImageView);
        ImageView icon6 = (ImageView)findViewById(R.id.detailLocationImageView);

        //save the color of our button so that if the user holds and un-holds, we can keep this color scheme

        //adjust the color scheme based on the values we extracted earlier - then, set the text/value for each view
        icon1.setColorFilter(primaryColorDark);
        icon2.setColorFilter(primaryColorDark);
        icon4.setColorFilter(primaryColorDark);
        icon5.setColorFilter(primaryColorDark);
        icon6.setColorFilter(primaryColorDark);
        diagnosisValue.setText(diagnosis);
        dateValue.setText(date);
        feelingValue.setSelectedSmile(BaseRating.OKAY);

        if(diagCopy.equals("Seborrheic Keratosis")) {
            moreInfoValue.setText("According to Mayo Clinic, Seborrheic keratosis (seb-o-REE-ik ker-uh-TOE-sis) is one of the most common noncancerous skin growths in older adults.\n" +
                    "\n" +
                    "A seborrheic keratosis usually appears as a brown, black or light tan growth on the face, chest, shoulders or back. The growth has a waxy, scaly, slightly elevated appearance. Seborrheic keratoses don't become cancerous and aren't thought to be related to sun exposure, but they can look like skin cancer.\n" +
                    "\n" +
                    "Seborrheic keratoses are normally painless and require no treatment. You may decide to have them removed if they become irritated by clothing or for cosmetic reasons.");
        } else if(diagCopy.equals("Melanoma")) {
            moreInfoValue.setText("According to Mayo Clinic, Melanoma, the most serious type of skin cancer, develops in the cells (melanocytes) that produce melanin — the pigment that gives your skin its color. Melanoma can also form in your eyes and, rarely, in internal organs, such as your intestines.\n" +
                    "\n" +
                    "The exact cause of all melanomas isn't clear, but exposure to ultraviolet (UV) radiation from sunlight or tanning lamps and beds increases your risk of developing melanoma. Limiting your exposure to UV radiation can help reduce your risk of melanoma.\n" +
                    "\n" +
                    "The risk of melanoma seems to be increasing in people under 40, especially women. Knowing the warning signs of skin cancer can help ensure that cancerous changes are detected and treated before the cancer has spread. Melanoma can be treated successfully if it is detected early.");
        }else if(diagCopy.equals("Basel Cell Carcinoma")) {
            moreInfoValue.setText("According to Mayo Clinic, Basal cell carcinoma is a type of skin cancer. Basal cell carcinoma begins in the basal cells — a type of cell within the skin that produces new skin cells as old ones die off.\n" +
                    "\n" +
                    "Basal cell carcinoma often appears as a slightly transparent bump on the skin, though it can take other forms. Basal cell carcinoma occurs most often on areas of the skin that are exposed to the sun, such as your head and neck.\n" +
                    "\n" +
                    "Most basal cell carcinomas are thought to be caused by long-term exposure to ultraviolet (UV) radiation from sunlight. Avoiding the sun and using sunscreen may help protect against basal cell carcinoma.");
        }else if(diagCopy.equals("Actinic Keratosis")) {
            moreInfoValue.setText("According to Mayo Clinic, An actinic keratosis (ak-TIN-ik ker-uh-TOE-sis) is a rough, scaly patch on your skin that develops from years of exposure to the sun. It's most commonly found on your face, lips, ears, back of your hands, forearms, scalp or neck.\n" +
                    "\n" +
                    "Also known as a solar keratosis, an actinic keratosis enlarges slowly and usually causes no signs or symptoms other than a patch or small spot on your skin. These patches take years to develop, usually first appearing in people over 40.\n" +
                    "\n" +
                    "A small percentage of actinic keratosis lesions can eventually become skin cancer. You can reduce your risk of actinic keratoses by minimizing your sun exposure and protecting your skin from ultraviolet (UV) rays.");
        }
        locationValue.setText(location);
        //diagnosisValue.setTextColor(primaryColorDark);
        dateValue.setTextColor(primaryColorDark);
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
