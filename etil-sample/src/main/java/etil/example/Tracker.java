package etil.example;

import etil.EtilField;
import etil.EtilTable;

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
