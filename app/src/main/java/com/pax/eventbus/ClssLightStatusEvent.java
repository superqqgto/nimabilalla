package com.pax.eventbus;

/**
 * Created by huangmuhua on 2017/3/24.
 */

public class ClssLightStatusEvent {
    private int status;
    public ClssLightStatusEvent(int status){
        this.status=status;
    }

    public int getStatus(){
        return this.status;
    }
}
