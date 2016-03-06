package com.tractive.android.etil.sample;

import com.tractive.android.etil.annotations.EtilField;
import com.tractive.android.etil.annotations.EtilTable;

/**
 * Created by stephan on 03/03/16.
 */
@EtilTable("pet")
public class Pet  {

    @EtilField("_id")
    public long id;

    @EtilField("name")
    public String name;

}
