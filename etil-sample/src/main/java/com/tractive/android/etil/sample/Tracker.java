package com.tractive.android.etil.sample;

import com.tractive.android.etil.annotations.EtilField;
import com.tractive.android.etil.annotations.EtilTable;

/**
 * Created by stephan on 03/03/16.
 */
@EtilTable("tracker")
public class Tracker {

    @EtilField("_id")
    public int id;

    @EtilField("number")
    public String trackerNumber;
}
