package fr.enssat.lanniontech.roadconceptandroid;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.enssat.lanniontech.roadconceptandroid.AbstractActivities.AuthentActivity;
import fr.enssat.lanniontech.roadconceptandroid.Entities.FeatureCollection;
import fr.enssat.lanniontech.roadconceptandroid.Entities.InfosMap;
import fr.enssat.lanniontech.roadconceptandroid.Utilities.OnNeedLoginListener;
import fr.enssat.lanniontech.roadconceptandroid.Utilities.RetrofitInterfaces.RoadConceptMapInterface;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SimulationVisualisationActivity extends AuthentActivity implements OnMapReadyCallback, OnNeedLoginListener, View.OnClickListener {

    private static final int GET_FEATURES_LIST_REQUEST_CODE = 1004;

    @BindView(R.id.buttonBackSImu) Button mButtonBack;
    @BindView(R.id.buttonNextSimu) Button mButtonNext;

    private GoogleMap mMap;
    private String mUuid;
    private int mMapID;
    private int mSamplingRate;
    private int mCurrentTimestamp;
    private FeatureCollection mFeatureCollection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_simulation_visualisation);
        ButterKnife.bind(this);
        mButtonBack.setOnClickListener(this);
        mButtonNext.setOnClickListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Intent intent = getIntent();
        mUuid = intent.getStringExtra(MapSimulationListActivity.INTENT_UUID_SIMULATION);
        mMapID = intent.getIntExtra(MapSimulationListActivity.INTENT_MAPID_SIMULATION,-1);
        mSamplingRate = intent.getIntExtra(MapSimulationListActivity.INTENT_SAMPLINGRATE_SIMULATION,-1);
        mCurrentTimestamp = intent.getIntExtra(MapSimulationListActivity.INTENT_DEPARTURELIVINGS_SIMULATION,-1);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        setTitle(getSecondInStringFormat(mCurrentTimestamp));
        disableElements();
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
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
        getFeaturesCollection();
    }

    private String getSecondInStringFormat(int seconds){
        int h = (int) Math.floor(seconds / 3600);
        int m = (int) Math.floor((seconds % 3600) / 60);
        int s = (seconds % 3600) % 60;
        String hString = String.valueOf(h);
        String mString = String.valueOf(m);
        String sString = String.valueOf(s);
        hString = h<10 ? '0'+hString : hString;
        mString = m<10 ? '0'+mString : mString;
        sString = s<10 ? '0'+sString : sString;
        return hString+ ":"+mString+":"+sString;
    }

    private void getFeaturesCollection(){
        RoadConceptMapInterface roadConceptMapInterface = getRetrofitService(RoadConceptMapInterface.class);
        Call<InfosMap> infosMapCall = roadConceptMapInterface.getMapFeatures(mMapID);
        infosMapCall.enqueue(new Callback<InfosMap>() {
            @Override
            public void onResponse(Call<InfosMap> call, Response<InfosMap> response) {
                if (response.isSuccessful()){
                    mFeatureCollection = response.body().getFeatureCollection();
                    getZones();
                } else {
                    if (response.code() == 401){
                        refreshLogin(SimulationVisualisationActivity.this, GET_FEATURES_LIST_REQUEST_CODE);
                    } else {
                        displayNetworkErrorDialog();
                    }
                }
            }

            @Override
            public void onFailure(Call<InfosMap> call, Throwable t) {
                displayNetworkErrorDialog();
            }
        });
    }

    @Override
    public void onNeedLogin(int code, boolean result) {
        switch (code){
            case GET_FEATURES_LIST_REQUEST_CODE:
                if (result) {
                    getFeaturesCollection();
                } else {
                    goToLogin();
                }
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case (R.id.buttonBackSImu):
                if (mCurrentTimestamp > 0){
                    mCurrentTimestamp -= mSamplingRate;
                    disableElements();
                    updateCongestion();
                }
                break;
            case (R.id.buttonNextSimu):
                if (mCurrentTimestamp < 86400){
                    mCurrentTimestamp += mSamplingRate;
                    disableElements();
                    updateCongestion();
                }
                break;
        }
    }

    private void getZones(){
        //TODO récupérer les zones et mettre les marques

        //A ajouter dans le response.isSuccessfull
        updateCongestion();
    }

    private void updateCongestion(){
        //TODO Récupérer les nouvelles congestions

        //A faire (que la requêtes soit ok ou non
        enableElements();
    }

    private void disableElements(){
        mButtonBack.setEnabled(false);
        mButtonNext.setEnabled(false);
    }

    private void enableElements(){
        mButtonBack.setEnabled(true);
        mButtonNext.setEnabled(true);
    }
}
