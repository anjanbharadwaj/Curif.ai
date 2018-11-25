package com.example.anjanbharadwaj.cesapp;

import com.github.mikephil.charting.data.Entry;

import java.util.ArrayList;

/**
 * Created by sreeharirammohan on 10/26/18.
 */

public class GraphCardInformation {

    String which_diagnosis_is_being_tracked;
    ArrayList<Entry> percentages = new ArrayList<Entry>();
    ArrayList<Entry> feelings = new ArrayList<>();

    String title;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    String location;
    public GraphCardInformation(String which_diagnosis_is_being_tracked,
                                ArrayList<Entry> percentages, ArrayList<Entry> feelings, String title, String location) {
        this.which_diagnosis_is_being_tracked = which_diagnosis_is_being_tracked;
        this.percentages = percentages;
        this.feelings = feelings;
        this.title = title;
        this.location = location;
    }

    public String getWhich_diagnosis_is_being_tracked() {
        return which_diagnosis_is_being_tracked;
    }

    public void setWhich_diagnosis_is_being_tracked(String which_diagnosis_is_being_trakced) {
        this.which_diagnosis_is_being_tracked = which_diagnosis_is_being_trakced;
    }

    public ArrayList<Entry> getPercentages() {
        return percentages;
    }

    public void setPercentages(ArrayList<Entry> percentages) {
        this.percentages = percentages;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
