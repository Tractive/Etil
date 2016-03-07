package com.tractive.android.etil.sample;

import com.tractive.android.etil.annotations.EtilField;
import com.tractive.android.etil.annotations.EtilTable;

/**
 * Created by stephan on 03/03/16.
 */
@EtilTable("pet_detail")
public class PetDetail {


    @EtilField("name")
    public String name;

    @EtilField("birthday")
    public String birthday;

    @EtilField("breed_mixed")
    public boolean breed_mixed;
}
