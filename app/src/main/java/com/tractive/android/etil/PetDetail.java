package com.tractive.android.etil;

import com.tractive.android.etil.annotation.EtilField;
import com.tractive.android.etil.annotation.EtilTable;

/**
 * Created by stephan on 03/03/16.
 */
@EtilTable("pet_detail")
public class PetDetail {


    @EtilField("name")
    public String name;

    @EtilField("birthday")
    public String birthday;
}
