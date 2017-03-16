package com.bullest.waterkeeper;

import com.orm.SugarRecord;

/**
 * Created by gang on 13/02/2017.
 */

public class WaterRecord extends SugarRecord {
    int amount;
    long time;

    public WaterRecord() {}

    public WaterRecord(int amount, long time) {
        this.amount = amount;
        this.time = time;
    }
}
