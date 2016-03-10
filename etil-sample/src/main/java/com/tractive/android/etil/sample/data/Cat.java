package com.tractive.android.etil.sample.data;

import com.tractive.android.etil.annotations.EtilField;
import com.tractive.android.etil.annotations.EtilTable;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by stephan on 10/03/16.
 */

@EtilTable("cat")
public class Cat extends Animal {

    @EtilField("secretly_plots_to_kill_you")
    public boolean secretlyPlotsToKillYou;


}
