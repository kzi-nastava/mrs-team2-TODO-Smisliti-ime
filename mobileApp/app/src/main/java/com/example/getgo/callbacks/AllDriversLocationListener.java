package com.example.getgo.callbacks;

import com.example.getgo.dtos.driver.GetDriverLocationDTO;

public interface AllDriversLocationListener {
    void onDriverLocation(GetDriverLocationDTO location);
}
