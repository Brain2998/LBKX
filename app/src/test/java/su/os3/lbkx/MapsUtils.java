package su.os3.lbkx;

import com.google.android.gms.maps.model.LatLng;

public class MapsUtils {

    public static LatLng AverageLatLng(LatLng position1, LatLng position2){
        return new LatLng((position1.latitude+position2.latitude)/2,(position1.longitude+position2.longitude)/2);
    }
}
