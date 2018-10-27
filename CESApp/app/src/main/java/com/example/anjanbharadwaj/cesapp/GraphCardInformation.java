package com.example.anjanbharadwaj.cesapp;

import java.util.ArrayList;

/**
 * Created by sreeharirammohan on 10/26/18.
 */

public class GraphCardInformation {

    String which_diagnosis_is_being_tracked;
    ArrayList<Double> percentages = new ArrayList<Double>();
    String title;

    public GraphCardInformation(String which_diagnosis_is_being_tracked,
                                ArrayList<Double> percentages, String title) {
        this.which_diagnosis_is_being_tracked = which_diagnosis_is_being_tracked;
        this.percentages = percentages;
        this.title = title;
    }

    public String getWhich_diagnosis_is_being_tracked() {
        return which_diagnosis_is_being_tracked;
    }

    public void setWhich_diagnosis_is_being_tracked(String which_diagnosis_is_being_trakced) {
        this.which_diagnosis_is_being_tracked = which_diagnosis_is_being_trakced;
    }

    public ArrayList<Double> getPercentages() {
        return percentages;
    }

    public void setPercentages(ArrayList<Double> percentages) {
        this.percentages = percentages;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
