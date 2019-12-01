package net.dm73.plainpress.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import net.dm73.plainpress.MainActivity;
import net.dm73.plainpress.R;
import net.dm73.plainpress.model.User;

import butterknife.BindView;
import butterknife.ButterKnife;


public class Search extends Fragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener  {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String SEARCH_MODE = "search_mode";
    public static final int EVENT_SEARCH = 0;
    public static final int LOCATION_SEARCH = 1;

    // TODO: Rename and change types of parameters
    private int searchMode = 0;

    @BindView(R.id.eventContainer)
    TextView eventSearchContainer;
    @BindView(R.id.mapsContainer)
    TextView mapsSearchContainer;
    @BindView(R.id.autoCompleteEvent)
    EditText autoCompleteEvent;
    @BindView(R.id.autoCompleteLocalisation)
    AutoCompleteTextView autoCompleteLocalisation;
    @BindView(R.id.searchButton)
    ImageView searchButton;
    @BindView(R.id.navDrawer)
    ImageView navBarButton;
    @BindView(R.id.searchIndicator)
    TextView searchIndicator;


    private DatabaseReference mRef;
    private int lastSearchMode = 0;
    private LatLng searchableLocation;

    private GoogleApiClient googleApiClient;
    private Location mLastKnownLocation;
    private PlaceArrayAdapter mPlaceArrayAdapter;

    private User user;

    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int ACCESS_FINE_LOCATION_INTENT_ID = 3;
    private FusedLocationProviderClient mFusedLocationClient;

    private ValueEventListener userProfileListener;

    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));


    public Search() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static Search newInstance(int searchMode) {
        Search fragment = new Search();
        Bundle args = new Bundle();
        args.putInt(SEARCH_MODE, searchMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            searchMode = getArguments().getInt(SEARCH_MODE);
        }else{
            searchMode = EVENT_SEARCH;
        }

        mRef = FirebaseDatabase.getInstance().getReference();

        if (googleApiClient == null)
            googleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addApi(Places.GEO_DATA_API)
                    .addOnConnectionFailedListener(this).build();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_search, container, false);
        ButterKnife.bind(this, rootView);

        navBarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        updateUi(searchMode);

        if (googleApiClient != null)
            googleApiClient.connect();

        searchableLocation = null;

        eventSearchContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastSearchMode != EVENT_SEARCH) {
                    updateUi(EVENT_SEARCH);

                }
            }
        });

        mapsSearchContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (lastSearchMode != LOCATION_SEARCH) {
                    updateUi(LOCATION_SEARCH);
                }
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                search();
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        if(googleApiClient != null){
            googleApiClient = null;
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mRef.removeEventListener(userProfileListener);
    }

    private void activeSearch(int key) {
        switch (key) {
            case 0:
                eventSearchContainer.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.search_active));
                break;
            case 1:
                mapsSearchContainer.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.search_active));
                break;
        }
    }

    private void desactiveSearch(int key) {
        switch (key) {
            case 0:
                eventSearchContainer.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.search_desactive));
                break;
            case 1:
                mapsSearchContainer.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable.search_desactive));
                break;
        }
    }

    private void updateUi(int searchMode){
        switch (searchMode){
            case EVENT_SEARCH:
                desactiveSearch(lastSearchMode);
                activeSearch(EVENT_SEARCH);
                lastSearchMode = EVENT_SEARCH;
                autoCompleteEvent.setVisibility(View.VISIBLE);
                autoCompleteLocalisation.setVisibility(View.GONE);
                autoCompleteLocalisation.setText("");
                searchableLocation = null;
                searchIndicator.setText("Eg: crash");
                break;
            case LOCATION_SEARCH:
                desactiveSearch(lastSearchMode);
                activeSearch(LOCATION_SEARCH);
                lastSearchMode = LOCATION_SEARCH;
                autoCompleteLocalisation.setVisibility(View.VISIBLE);
                autoCompleteEvent.setVisibility(View.GONE);
                searchIndicator.setText("Eg: San Francisco");
                break;
        }
    }

    private void search(){

        switch(lastSearchMode){
            case EVENT_SEARCH:
                if(!TextUtils.isEmpty(autoCompleteEvent.getText().toString()) && mLastKnownLocation!=null){
                    MainActivity.switchFragment(SearchDisplay.newInstance(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude(), autoCompleteEvent.getText().toString(), lastSearchMode), "search_display", getFragmentManager());
                }
                break;
            case LOCATION_SEARCH:
                if(searchableLocation != null){
                    MainActivity.switchFragment(SearchDisplay.newInstance(searchableLocation.latitude, searchableLocation.longitude, null, lastSearchMode), "search_display", getFragmentManager());
                } else{//Todo: a refaire traiter le cas du delete data empty location
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                            .setMessage("Fieled to get the cerrect location, try again please !")
                            .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    autoCompleteLocalisation.setText("");
                                    dialog.dismiss();
                                }
                            });
                    Dialog dialog = builder.create();
                    dialog.show();
                }
                break;
        }
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i("adapter", "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(googleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            Log.i("adapter", "Fetching details for ID: " + item.placeId);


        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                Log.e("adapter", "Place query did not complete. Error: " +
                        places.getStatus().toString());
                return;
            }
            // Selecting the first object buffer.
            final Place place = places.get(0);
            LatLng queriedLocation = place.getLatLng();
            searchableLocation = place.getLatLng();
            Log.e("(lat,lng)", "("+queriedLocation.latitude+" , "+queriedLocation.longitude+")");

        };
    };


    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mPlaceArrayAdapter = new PlaceArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, BOUNDS_GREATER_SYDNEY, null);
        autoCompleteLocalisation.setOnItemClickListener(mAutocompleteClickListener);
        mPlaceArrayAdapter.setGoogleApiClient(googleApiClient);
        autoCompleteLocalisation.setAdapter(mPlaceArrayAdapter);

        Log.e("connected", "true");
        userProfileListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);

                if((boolean)user.getSettings().get("trackMe")){
                    if (checkPermissions()) {
                        showSettingDialog();
                    } else {
                        requestLocationPermission();
                    }
                }else{
                    mLastKnownLocation = new Location("user_location");
                    mLastKnownLocation.setLatitude((double) user.getLocation().get("latitude"));//your coords of course
                    mLastKnownLocation.setLongitude((double) user.getLocation().get("longitude"));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(userProfileListener);


    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case ACCESS_FINE_LOCATION_INTENT_ID: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showSettingDialog();
                } else {
//                    updateGPSStatus("Location Permission denied.");
                    Toast.makeText(getActivity(), "Location Permission denied.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private boolean checkPermissions() {
            int permissionState = ActivityCompat.checkSelfPermission(getActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION);
            return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    /*  Show Popup to access User Permission  */
    private void requestLocationPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_INTENT_ID);
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_INTENT_ID);
        }
    }

    /* Show Location Access Dialog */
    private void showSettingDialog() {
        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);//Setting priotity of Location request to high
        locationRequest.setInterval(30 * 1000);
        locationRequest.setFastestInterval(5 * 1000);//5 sec Time interval for location update
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true); //this is the key ingredient to show dialog always when GPS is off

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult result) {
                final Status status = result.getStatus();
                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location
                        // requests here.
//                        updateGPSStatus("GPS is Enabled in your device");
                        getLastLocation();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        // Location settings are not satisfied. But could be fixed by showing the user
                        // a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(getActivity(), REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException e) {
                            e.printStackTrace();
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. However, we have no way to fix the
                        // settings so we won't show the dialog.
                        break;
                }
            }
        });
    }

    @SuppressWarnings("MissingPermission")
    public void getLastLocation() {
        mFusedLocationClient.getLastLocation().addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful() && task.getResult() != null) {

                    mLastKnownLocation = task.getResult();
                    Log.e("getLastLocation", mLastKnownLocation.toString());

                } else {

                    Log.w("fusedLocation", "getLastLocation:exception", task.getException());
                    mLastKnownLocation = new Location("user_location");
                    mLastKnownLocation.setLatitude((double) user.getLocation().get("latitude"));//your coords of course
                    mLastKnownLocation.setLongitude((double) user.getLocation().get("longitude"));

                }
            }

        });
    }

    public void ChangeLocationTo(Location location){
        mLastKnownLocation = location;
    }
}
