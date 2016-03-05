package etil.example;

import etil.EtilField;
import etil.EtilTable;

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
