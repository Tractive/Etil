package com.tractive.android.etil;

import com.tractive.android.etil.annotation.EtilField;
import com.tractive.android.etil.annotation.EtilTable;

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
