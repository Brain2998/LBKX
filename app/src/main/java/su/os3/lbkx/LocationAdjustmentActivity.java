package su.os3.lbkx;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;

public class LocationAdjustmentActivity extends AppCompatActivity {

    private TextView mLatitude;
    private TextView mLongitude;
    private TextView mLatitudeScale;
    private TextView mLongitudeScale;
    private TextView mLatitudeOffset;
    private TextView mLongitudeOffset;
    public static double latOffset = 0;
    public static double longOffset = 0;
    public static double latScale = 0;
    public static double longScale = 0;
    public static double latitude = 0;
    public static double longitude = 0;
    public static long latitudeTrans = 0;
    public static long longitudeTrans = 0;
    private double latitude1 = 0;
    private double longitude1 = 0;
    private double latitude2 = 0;
    private double longitude2 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_adjustment);

        mLatitude = findViewById(R.id.latitude);
        mLatitudeScale = findViewById(R.id.latScale);
        mLatitudeOffset = findViewById(R.id.latOff);
        mLongitude = findViewById(R.id.longitude);
        mLongitudeScale = findViewById(R.id.longScale);
        mLongitudeOffset = findViewById(R.id.longOff);

        Intent intent = getIntent();
        latitude1=intent.getDoubleExtra("latitude1",0);
        longitude1=intent.getDoubleExtra("longitude1",0);

        latitude2=intent.getDoubleExtra("latitude2",0);
        longitude2=intent.getDoubleExtra("longitude2",0);

        latitude=(latitude1+latitude2)/2;
        longitude=(longitude1+longitude2)/2;

        mLongitude.setText(String.valueOf(longitude));
        mLatitude.setText(String.valueOf(latitude));

        latScale = Math.abs(latitude2-latitude1);
        longScale = Math.abs(longitude2-longitude1);

        mLatitudeScale.setText(String.valueOf(latScale));
        mLongitudeScale.setText(String.valueOf(longScale));

        try {
            latOffset = calcOffset(latitude, latScale);
            mLatitudeOffset.setText(String.valueOf(latOffset));
        } catch (Exception e){
            e.printStackTrace();
        }

        try {
            longOffset = calcOffset(longitude, longScale);
            mLongitudeOffset.setText(String.valueOf(longOffset));
        } catch (Exception e){
            e.printStackTrace();
        }

        mLatitudeScale.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {    }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {    }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    latScale = Double.parseDouble(mLatitudeScale.getText().toString());
                    latOffset = calcOffset(latitude, latScale);
                    mLatitudeOffset.setText(String.valueOf(latOffset));
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        mLongitudeScale.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {    }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {    }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    longScale = Double.parseDouble(mLongitudeScale.getText().toString());
                    longOffset = calcOffset(longitude, longScale);
                    mLongitudeOffset.setText(String.valueOf(longOffset));
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

        latitudeTrans = (int)((latitude+latOffset)/latScale);
        longitudeTrans = (int)((longitude+longOffset)/longScale);
    }

    private double calcOffset(double coord, double coordScale){
        int transformed=(int) (coord/coordScale);
        return (0.5 - (coord / coordScale - transformed)) * coordScale;
    }

    public void confirm(View v){
        Intent intent = new Intent(this, SecretActivity.class);
        startActivity(intent);
    }
}