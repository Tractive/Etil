package com.tractive.android.etil.sample.data;

import com.tractive.android.etil.annotations.EtilField;
import com.tractive.android.etil.annotations.EtilTable;

/**
 * Created by stephan on 10/03/16.
 */
@EtilTable("dog")
public class Dog extends Mammal {

    @EtilField("has_a_tracker")
    public boolean hasATracker;

}
