package com.itsamsung.stdigor.peoplearoundyou;

import android.location.Location;

public interface LocationListenerActivity {

    void locationChanged(Location location);

    void providerDisabled(String provider);

    void providerEnabled(String provider);
}
