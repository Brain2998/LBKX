package su.os3.lbkx;

import android.content.Intent;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker marker1;
    private Marker marker2;
    private int lastMarker = 1;
    private Polygon field;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng innopolis1 = new LatLng(55.7462, 48.743375);
        LatLng innopolis2 = new LatLng(55.7499, 48.747375);
        marker1 = mMap.addMarker(new MarkerOptions().position(innopolis1).title("Confirm location").draggable(true));
        marker2 = mMap.addMarker(new MarkerOptions().position(innopolis2).title("Confirm location").draggable(true));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng((innopolis1.longitude+innopolis2.longitude)/2,(innopolis1.latitude+innopolis2.latitude)/2)));

        LatLng pos1 = marker1.getPosition();
        LatLng pos2 = marker1.getPosition();

        PolygonOptions rectOptions = new PolygonOptions()
                .add(new LatLng(pos1.latitude,pos2.longitude))
                .add(pos1)
                .add(new LatLng(pos2.latitude,pos1.longitude))
                .add(pos2)
                .add(new LatLng(pos1.latitude,pos2.longitude))
                .fillColor(Color.argb(100, 0, 200, 200))
                .strokeColor(Color.argb(255,0,200,200));
        field = mMap.addPolygon(rectOptions);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng((pos1.latitude+pos2.latitude)/2,(pos1.longitude+pos2.longitude)/2)));

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {}
            @Override
            public void onMarkerDrag(Marker marker) {
                LatLng pos1 = marker1.getPosition();
                LatLng pos2 = marker2.getPosition();

                PolygonOptions rectOptions = new PolygonOptions()
                        .add(new LatLng(pos1.latitude,pos2.longitude))
                        .add(pos1)
                        .add(new LatLng(pos2.latitude,pos1.longitude))
                        .add(pos2)
                        .add(new LatLng(pos1.latitude,pos2.longitude))
                        .fillColor(Color.argb(100, 0, 200, 200))
                        .strokeColor(Color.argb(255,0,200,200));
                field.remove();
                field = mMap.addPolygon(rectOptions);
            }
            @Override
            public void onMarkerDragEnd(Marker marker) {}
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener(){
            @Override
            public void onMapClick(LatLng position){
                if(lastMarker == 1) {
                    marker2.remove();
                    marker2 = mMap.addMarker(new MarkerOptions().position(position).title("Confirm location").draggable(true));
                    lastMarker = 2;
                }else{
                    marker1.remove();
                    marker1 = mMap.addMarker(new MarkerOptions().position(position).title("Confirm location").draggable(true));
                    lastMarker = 1;
                }
                LatLng pos1 = marker1.getPosition();
                LatLng pos2 = marker2.getPosition();

                PolygonOptions rectOptions = new PolygonOptions()
                        .add(new LatLng(pos1.latitude,pos2.longitude))
                        .add(pos1)
                        .add(new LatLng(pos2.latitude,pos1.longitude))
                        .add(pos2)
                        .add(new LatLng(pos1.latitude,pos2.longitude))
                        .fillColor(Color.argb(100, 0, 200, 200))
                        .strokeColor(Color.argb(255,0,200,200));
                field.remove();
                field = mMap.addPolygon(rectOptions);
            }
        });

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                Intent intent = new Intent(MapsActivity.this, LocationAdjustmentActivity.class);
                LatLng position1=marker1.getPosition();
                intent.putExtra("latitude1",position1.latitude);
                intent.putExtra("longitude1",position1.longitude);
                LatLng position2=marker2.getPosition();
                intent.putExtra("latitude2",position2.latitude);
                intent.putExtra("longitude2",position2.longitude);
                startActivity(intent);
            }
        });
    }
}


