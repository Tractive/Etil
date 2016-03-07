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
    public long birthday;
    @EtilField("created_at")
    public Long created_at;

    @EtilField("breed_mixed")
    public boolean breed_mixed;
    @EtilField("neuterd")
    public Boolean neuterd;

    @EtilField("weight")
    public float weight;
    @EtilField("height")
    public Float height;

    @EtilField("legs")
    public int legs;
    @EtilField("ears")
    public Integer ears;




}
