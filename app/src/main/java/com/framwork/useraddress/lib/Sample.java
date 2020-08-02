package com.framwork.useraddress.lib;


import com.framwork.useraddress.lib.base.BaseData;

import java.util.List;

import retrofit2.Call;

/**
 * Created by Aditi Shirsat on 18 Nov 2019 at 19:45.
 */
public class Sample {

    void stopApiCall( List<Call<? extends BaseData>> mCall){
        for (Call<? extends BaseData> call:
             mCall) {
            if (call != null)
                call.cancel();
        }

    }

}
