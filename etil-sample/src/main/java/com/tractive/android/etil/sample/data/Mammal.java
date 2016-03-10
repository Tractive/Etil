package com.tractive.android.etil.sample.data;

import com.tractive.android.etil.annotations.EtilField;
import com.tractive.android.etil.annotations.EtilTable;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by stephan on 10/03/16.
 */
public class Mammal extends Animal {

    @EtilField("classification")
    public String classification;

}
