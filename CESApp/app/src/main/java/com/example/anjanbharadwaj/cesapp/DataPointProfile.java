package com.example.anjanbharadwaj.cesapp;

import android.os.Parcel;
import android.os.Parcelable;

import com.arlib.floatingsearchview.suggestions.model.SearchSuggestion;

public class DataPointProfile implements SearchSuggestion {
    String url;
    String diagnosis;
    String date;

    public DataPointProfile(String url, String diagnosis, String date) {
        this.url = url;
        this.diagnosis = diagnosis;
        this.date = date;
    }

    public DataPointProfile(Parcel parcel) {
        this.url = parcel.readString();
        this.diagnosis = parcel.readString();
        this.date = parcel.readString();

    }
    @Override
    public String getBody() {
        return diagnosis;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeString(url);
        dest.writeString(diagnosis);
        dest.writeString(date);


    }

    //Required field for the interface to be able to create a new Book object from a passed in parcel
    public static final Parcelable.Creator<DataPointProfile> CREATOR = new Parcelable.Creator<DataPointProfile>() {

        @Override
        public DataPointProfile createFromParcel(Parcel parcel) {
            return new DataPointProfile(parcel);
        }

        @Override
        public DataPointProfile[] newArray(int size) {
            return new DataPointProfile[0];
        }
    };

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != this.getClass()) return false;
        DataPointProfile o = (DataPointProfile) obj;

        return date.equals(o.date);
    }

    @Override
    public String toString() {
        return getBody() + " //////////// " + url;
    }

}
