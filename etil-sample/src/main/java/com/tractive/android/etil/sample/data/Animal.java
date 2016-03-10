package com.tractive.android.etil.sample.data;

import com.tractive.android.etil.annotations.EtilField;
import com.tractive.android.etil.annotations.EtilTable;

/**
 * Created by stephan on 10/03/16.
 */

public class Animal  {

    @EtilField("_id")
    public long id;

    @EtilField("name")
    public String name;

    @EtilField("age")
    public int age;

}