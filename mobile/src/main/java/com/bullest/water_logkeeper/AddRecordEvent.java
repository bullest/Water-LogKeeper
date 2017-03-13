package com.bullest.water_logkeeper;

import java.util.Calendar;

/**
 * Created by gang on 11/02/2017.
 */

public class AddRecordEvent{
    public final int amount;
    public final Calendar time ;


    public AddRecordEvent(int amount, Calendar time){
        this.amount = amount;
        this.time = time;
    }

}
