package com.tractive.android.etil;

import com.tractive.android.etil.annotation.EtilField;
import com.tractive.android.etil.annotation.EtilTable;

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
