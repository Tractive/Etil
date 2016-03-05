package etil.example;

import etil.EtilField;
import etil.EtilTable;

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
