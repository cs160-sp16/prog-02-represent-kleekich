package com.example.kangsik.represent;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.Wearable;
import com.squareup.okhttp.internal.http.StatusLine;
import com.twitter.sdk.android.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import io.fabric.sdk.android.Fabric;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener{

    // Note: Your consumer key and secret should be obfuscated in your source code before shipping.
    private static final String TWITTER_KEY = "kleekich";
    private static final String TWITTER_SECRET = "Grant6312";
    private final static int PLAY_SERVICE_RESOLUTION_REQUEST = 1000;


    //FOR CURRENT LOCATION
    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;

    private boolean mRequestLocationUpdates = false;

    private LocationRequest mLocationRequest;

    private static int UPDATE_INTERVAL =10000;
    private static int FATEST_INTERVAL = 5000;
    private static int DISPLACEMENT = 10;

    private TextView textViewCurrentLocation;
    private Button buttonShowLocation;
    private Button buttonStartLocationUpdates;

    private static final String TAG = "myMessage";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TwitterAuthConfig authConfig = new TwitterAuthConfig(TWITTER_KEY, TWITTER_SECRET);
        Fabric.with(this, new Twitter(authConfig));
        setContentView(R.layout.activity_main);

        Log.i(TAG, "onCreate");

        //Sunlight Foundation
        /*
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response = httpclient.execute(new HttpGet(URL));
        StatusLine statusLine = response.getStatusLine();
        if(statusLine.getStatusCode() == HttpStatus.SC_OK){
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            response.getEntity().writeTo(out);
            String responseString = out.toString();
            out.close();
            //..more logic
        } else{
            //Closes the connection.
            response.getEntity().getContent().close();
            throw new IOException(statusLine.getReasonPhrase());
        }
        */

        //getting current location

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API) 
                .addApi(Wearable.API)  // used for data layer API
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();


        //
        textViewCurrentLocation = (TextView) findViewById(R.id.textViewCurrentLocation);
        buttonStartLocationUpdates = (Button) findViewById(R.id.buttonStartLocationUpdates);
        buttonShowLocation = (Button) findViewById(R.id.buttonShowLocation);

        if(this.checkPlayServices()){
            buildGoogleApiClient();
            createLocationRequest();
        }

        buttonShowLocation.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                double latitude = mLastLocation.getLatitude();
                double longitude = mLastLocation.getLongitude();

                textViewCurrentLocation.setText(latitude + ", " + longitude);
            }
        });

        buttonStartLocationUpdates.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                /*
                togglePeriodLocationUpdates();
                */
            }
        });


        Button enterButton = (Button)findViewById(R.id.enterButton);
        Button currentLocationButton = (Button)findViewById(R.id.currentLocationButton);

        enterButton.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        sendMessage(v);
                    }
                }
        );

        currentLocationButton.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View v){
                        sendMessage(v);

                    }
                }
        );







    }


    public void sendMessage(View view){
        Intent congressionalIntent = new Intent(this, CongressionalActivity.class);
        Intent watchIntent = new Intent(getBaseContext(), PhoneToWatchService.class);
        switch(view.getId()){
            case R.id.enterButton:
                EditText editText = (EditText) findViewById(R.id.zipCodeInput);
                String zipCodeInputString = editText.getText().toString();
                congressionalIntent.putExtra("userInputMessage", zipCodeInputString);

                //For Watch
                watchIntent.putExtra("LOCATION", zipCodeInputString);
                break;
            case R.id.currentLocationButton:
                String currLocationString = "Current Location";
                congressionalIntent.putExtra("userInputMessage", currLocationString);

                //For Watch
                watchIntent.putExtra("LOCATION", "94704");
                break;
        }
        startActivity(congressionalIntent);
        startService(watchIntent);
    }


/*
    private void displayLocation(){
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        System.out.print("Display Location button Clicked!!!!");
        Log.i(TAG, "Display Location button Clicked!!!!");

        if(mLastLocation != null){
            double latitude = mLastLocation.getLatitude();
            double longitude = mLastLocation.getLongitude();

            textViewCurrentLocation.setText(latitude + ", " + longitude);

        }else{
            textViewCurrentLocation.setText("Couldn't get the location. Make sure location");
        }

    }
/*
    private void togglePeriodLocationUpdates() {
        if(!mRequestLocationUpdates){
            buttonStartLocationUpdates.setText(getString(R.string.button_stop_location_updates));

            mRequestLocationUpdates = true;

            startLocationUpdates();
        }else{
            buttonStartLocationUpdates.setText(getString(R.string.button_stop_location_updates));

            mRequestLocationUpdates = false;

            stopLocationUpdates();
        }
    }
*/
    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
               .addConnectionCallbacks(this)
               .addOnConnectionFailedListener(this)
               .addApi(LocationServices.API).build();
    }

    protected void createLocationRequest(){
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLACEMENT);

    }

    private boolean checkPlayServices(){
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if(resultCode != ConnectionResult.SUCCESS){
            if(GooglePlayServicesUtil.isUserRecoverableError(resultCode)){
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_RESOLUTION_REQUEST).show();
            }else{
                Toast.makeText(getApplicationContext(), "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;

        }
        return true;
    }
/*
    private void startLocationUpdates(){

        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, new LocationListner());
    }
    protected void stopLocationUpdates(){
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);

    }
    */

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart");
        if(mGoogleApiClient != null){
            mGoogleApiClient.connect();
        }
    }




    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");

        mGoogleApiClient.connect();
    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause");
        mGoogleApiClient.disconnect();

    }


    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop");

        if(mGoogleApiClient.isConnected()){
            mGoogleApiClient.disconnect();
        }
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart");
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState");
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        Log.i(TAG, "onRestoreInstanceState");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        //displayLocation();

        if(mRequestLocationUpdates){
            /*
            startLocationUpdates();
            */
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;

        Toast.makeText(getApplicationContext(), "Location Changed!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

        Log.i(TAG, "Connection Failed: " + connectionResult.getErrorCode());
    }
}
