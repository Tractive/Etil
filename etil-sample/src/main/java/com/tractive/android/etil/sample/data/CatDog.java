package com.tractive.android.etil.sample.data;

import com.tractive.android.etil.annotations.EtilField;
import com.tractive.android.etil.annotations.MultiEtilTable;

/**
 * Created by stephan on 25/08/16.
 */
@MultiEtilTable
public class CatDog {

    @EtilField("secretly_plots_to_kill_you")
    public boolean secretlyPlotsToKillYou;

    @EtilField("has_a_tracker")
    public boolean hasATracker;
}
