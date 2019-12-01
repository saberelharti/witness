package net.dm73.plainpress.fragment;

import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import net.dm73.plainpress.DetailClickListner;
import net.dm73.plainpress.MainActivity;
import net.dm73.plainpress.OnLoadMoreListener;
import net.dm73.plainpress.R;
import net.dm73.plainpress.adapter.EventRecyclerAdapter;
import net.dm73.plainpress.model.Event;
import net.dm73.plainpress.model.User;
import net.dm73.plainpress.util.PublishedTimeBasedComparator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class WallEvent extends Fragment implements GeoQueryEventListener, DetailClickListner, OnLoadMoreListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    @BindView(R.id.eventRecyclerView)
    RecyclerView mRecyclerView;
    @BindView(R.id.wallProgreeBar)
    ProgressBar wallProgressBar;
    @BindView(R.id.wallEmptyContent)
    RelativeLayout emptyContentView;

    // TODO: Rename parameter arguments, choose names that match
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private EventRecyclerAdapter mAdapter;

    private static GeoFire mGeoFire;
    private static GeoQuery mGeoQuery;

    private Location mLastKnownLocation;

    private List<String> keyList;
    private List<String> newKeyList;
    private List<Event> eventKeyListToShow;
    private List<Event> allEventKeyList;

    public static List<String> viewedEvents;
    private List<String> listViewedEvent;
    private List<Long> choosedCategories;

    private DatabaseReference mRef;

    boolean onGeoQueryReady = false;


    private TextView notifNumberText;
    private RelativeLayout notifEventView;

    private GoogleApiClient googleApiClient;

    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int ACCESS_FINE_LOCATION_INTENT_ID = 3;
    private FusedLocationProviderClient mFusedLocationClient;

    private User user;
    private ValueEventListener userProfileListener;

    public WallEvent() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static WallEvent newInstance(String param1, String param2) {
        WallEvent fragment = new WallEvent();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //create the data
        keyList = new ArrayList<>();
        newKeyList = new ArrayList<>();
        eventKeyListToShow = new ArrayList<>();
        allEventKeyList = new ArrayList<>();
        viewedEvents = new ArrayList<>();

        mRef = FirebaseDatabase.getInstance().getReference();
        mGeoFire = new GeoFire(mRef.child("geoevents"));

        if (googleApiClient == null)
            googleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_wall_events, container, false);
        ButterKnife.bind(this, rootView);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        notifNumberText = (TextView) rootView.findViewById(R.id.textNotifNumber);
        notifEventView = (RelativeLayout) rootView.findViewById(R.id.notifEvent);

        rootView.findViewById(R.id.navDrawer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });

        onGeoQueryReady = false;

        if (googleApiClient != null)
            googleApiClient.connect();

//        swipeRefresh=(SwipeRefreshLayout)findViewById(R.id.swipeRefresh);
//        swipeRefresh.setOnRefreshListener(this);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if(mAdapter!=null)
            mAdapter.notifyDataSetChanged();

    }

    @Override
    public void onResume() {
        super.onResume();

        notifEventView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadAdapter();
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
//        if (mAdapter != null) {
//            mAdapter.cleanup();
//        }
        if (googleApiClient != null)
            googleApiClient.disconnect();
    }


    @Override
    public void ontDetailClick(int position, ImageView v) {

        Event event =  allEventKeyList.get(position);
        addEventTiUserHistory(event.getEventID());
        event.setViewed(true);
        event.setViews(event.getViews()+1);
        allEventKeyList.set(position, event);
        eventKeyListToShow.set(position, event);

        //increment the views value
        incrementView(mRef.child("events").child("items").child(allEventKeyList.get(position).getEventID()).child("views"));

        Intent intent = new Intent();
        intent.setClass(getActivity(), net.dm73.plainpress.EventDetail.class);
        intent.putExtra("KEYEVENT", allEventKeyList.get(position).getEventID());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        Log.e("key of the json data", allEventKeyList.get(position).getEventID());
    }

    @Override
    public void onProfilClick(int position, View view) {
        Event event =  allEventKeyList.get(position);

        if(event.isAnonymous())
            Toast.makeText(getActivity(), "This user is anonymous", Toast.LENGTH_SHORT).show();
        else {
            Intent intent = new Intent();
            intent.setClass(getActivity(), net.dm73.plainpress.UserProfile.class);
            intent.putExtra("userID", eventKeyListToShow.get(position).getUserInstance().getId());
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), view, "profile");
            startActivity(intent, options.toBundle());
        }
    }

    @Override
    public void onLoadMore() {
        if (eventKeyListToShow.size() < allEventKeyList.size()) {
            eventKeyListToShow.add(null);
            mAdapter.notifyItemInserted(eventKeyListToShow.size() - 1);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    eventKeyListToShow.remove(eventKeyListToShow.size() - 1);
                    mAdapter.notifyItemRemoved(eventKeyListToShow.size());

                    // Generating more data
                    int index = eventKeyListToShow.size();
                    int end = ((index + 3) < allEventKeyList.size()) ? (index + 3) : allEventKeyList.size();
                    for (int i = index; i < end; i++) {
                        eventKeyListToShow.add(allEventKeyList.get(i));
                    }
                    mAdapter.notifyDataSetChanged();
                    mAdapter.setLoaded();
                }
            }, 5000);
        } else {
            Toast.makeText(getActivity(), "Loading data completed", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onKeyEntered(String key, GeoLocation location) {

        if (onGeoQueryReady && !newKeyList.contains(key)) {
            newKeyList.add(key);
            // print the number of the new events
            changeNotifNumber(newKeyList.size());
            Log.e("new event add", "true "+ key);

        } else {
            keyList.add(key);
        }

        Log.e("key entered", "true");
    }

    @Override
    public void onKeyExited(String key) {

        Event removedEvent = foundEventInListByKey(allEventKeyList, key);

        if (removedEvent!=null) {
            Log.e("indexofremoved", keyList.indexOf(key) + "title :" + removedEvent.getTitle());

            if (eventKeyListToShow.contains(removedEvent)) {
                int removedEventIndex = eventKeyListToShow.indexOf(removedEvent);
                eventKeyListToShow.remove(removedEvent);
                mAdapter.notifyItemRemoved(removedEventIndex);
                Log.e("key deleted", "true index:"+ eventKeyListToShow.indexOf(removedEvent));
            }

            if (allEventKeyList.contains(removedEvent)) {
                allEventKeyList.remove(removedEvent);
            }

            keyList.remove(key);
            return;
        }

        if (newKeyList.contains(key)) {
            // print the number of the new events
            newKeyList.remove(key);
            changeNotifNumber(newKeyList.size());
            Log.e("new key deleted", "true");
            return;
        }

    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {

    }

    private Event foundEventInListByKey(List<Event> listEvent, String key){
        for(Event event : listEvent){
            if(event.getEventID().equals(key))
                return event;
        }
        return null;
    }

    @Override
    public void onGeoQueryReady() {

        onGeoQueryReady = true;

        Log.e("geoquery ready", "true");

        //show the progressBar
        wallProgressBar.setVisibility(View.VISIBLE);

        final int[] i = {1};

        if(keyList !=null && keyList.size()>0) {

            for (String key : keyList) {

                ValueEventListener eventListener = new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        Event event = dataSnapshot.getValue(Event.class);
                        Log.e("keyEvent", dataSnapshot.getKey());

//                    event.setEve.ntViewed(eventExistInUserHistory(dataSnapshot.getKey()));
                        Log.e("choosedCategories", choosedCategories.toString());
//                        Log.e("eventCategorie", event.getCategory().getId() + "");
//                        Log.e("contain the id", choosedCategories.contains(event.getCategory().getId()) + "");

                        if (event!=null && event.getStatus() == 2l && choosedCategories.contains(event.getCategory().getId())) {
                            if (eventExistInUserHistory(dataSnapshot.getKey()))
                                event.setViewed(true);
                            event.setEventId(dataSnapshot.getKey());
                            allEventKeyList.add(event);
                        }

                        if (i[0] == keyList.size()) {

                            Log.e("allEventKeyLIst", Arrays.toString(allEventKeyList.toArray()));

                            //sort the data
                            Collections.sort(allEventKeyList, new PublishedTimeBasedComparator());

                            Log.e("allEventKeyLIst", Arrays.toString(allEventKeyList.toArray()));

                            if (allEventKeyList.size() > 2) {
                                for (int j = 0; j < 3; j++) {
                                    eventKeyListToShow.add(allEventKeyList.get(j));
                                }
                            } else {
                                for (int j = 0; j < allEventKeyList.size(); j++) {
                                    eventKeyListToShow.add(allEventKeyList.get(j));
                                }
                            }

                            //hide wallProgressBar
                            wallProgressBar.setVisibility(View.GONE);

                            //attachRecyclerViewAdapter();
                            mAdapter = new EventRecyclerAdapter(mRecyclerView, eventKeyListToShow, getActivity(), mRef);
                            mRecyclerView.setAdapter(mAdapter);
                            mAdapter.setOnLoadMoreListener(WallEvent.this);
                            mAdapter.setDetailClickListner(WallEvent.this);
                        }

                        i[0]++;
                    }

                    @Override

                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                mRef.child("events").child("items").child(key).addListenerForSingleValueEvent(eventListener);
            }

            emptyContentView.setVisibility(View.GONE);

        }else{
            emptyContentView.setVisibility(View.VISIBLE);
            wallProgressBar.setVisibility(View.GONE);
        }

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        Log.e("connected", "true");
        userProfileListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);

                if((boolean) user.getSettings().get("trackMe")){
                    if (checkPermissions()) {
                        showSettingDialog();
                    } else {
                        requestLocationPermission();
                    }
                }else{
                    mLastKnownLocation = new Location(LocationManager.GPS_PROVIDER);
                    mLastKnownLocation.setLatitude((double) user.getLocation().get("latitude"));
                    mLastKnownLocation.setLongitude((double) user.getLocation().get("longitude"));
                    queryGeofireTo(mLastKnownLocation);
                }

                choosedCategories = (List<Long>) user.getSettings().get("categories");

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
                    Toast.makeText(getActivity(), "Location Permission denied.", Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    private void addEventTiUserHistory(String hashEventKey){

        Map<String, Object> viewedEvent = new HashMap<>();
        listViewedEvent = (user.getViewedEvents()!=null) ? user.getViewedEvents() : new ArrayList<String>();

        Log.e("the key :", hashEventKey + " ,"+eventExistInUserHistory(hashEventKey));

        if(!eventExistInUserHistory(hashEventKey)){
            listViewedEvent.add(hashEventKey);
            viewedEvent.put("viewedEvents",listViewedEvent);
            mRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(viewedEvent);
            return;
        }

        return;
    }

    private boolean eventExistInUserHistory(String key){

        if(user.getViewedEvents()!=null && user.getViewedEvents().contains(key))
            return true;

        return false;
    }

    private void incrementView(DatabaseReference ref){

        ref.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData mutableData) {
                if(mutableData.getValue() == null){
                    mutableData.setValue(1);
                }else{
                    Long views = (Long) mutableData.getValue();
                    mutableData.setValue(++views);
                }

                Log.e("updating the views", "true");

                return Transaction.success(mutableData);
            }

            @Override
            public void onComplete(DatabaseError databaseError, boolean b, DataSnapshot dataSnapshot) {
                Log.e("database error", databaseError+"");
            }
        });
    }


    private void reloadAdapter() {

        keyList.addAll(newKeyList);
        Log.e("newkeyList", newKeyList.size()+"");

        //show the progressBar
        wallProgressBar.setVisibility(View.VISIBLE);

        final int[] i = {1};

        for (String key : newKeyList) {
            ValueEventListener eventListener = new ValueEventListener() {

                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Event event = dataSnapshot.getValue(Event.class);

                    eventKeyListToShow.clear();

                    //filtre events
                    if(choosedCategories.contains(event.getCategory().getId())){
                        if(eventExistInUserHistory(dataSnapshot.getKey()))
                            event.setViewed(true);
                        event.setEventId(dataSnapshot.getKey());
                        allEventKeyList.add(event);
                    }

                    if (i[0] == newKeyList.size()) {

                        //sort the data
                        Collections.sort(allEventKeyList, new PublishedTimeBasedComparator());

                        if(allEventKeyList.size()>2) {
                            for (int j = 0; j < 3; j++) {
                                eventKeyListToShow.add(allEventKeyList.get(j));
                            }
                        }else{
                            for (int j = 0; j < allEventKeyList.size(); j++) {
                                eventKeyListToShow.add(allEventKeyList.get(j));
                            }
                        }

                        //hide wallProgressBar
                        wallProgressBar.setVisibility(View.GONE);

                        if(eventKeyListToShow !=null && eventKeyListToShow.size()>0){
                            emptyContentView.setVisibility(View.GONE);
                        }else{
                            emptyContentView.setVisibility(View.VISIBLE);
                        }

                        //attachRecyclerViewAdapter();
                        mAdapter = new EventRecyclerAdapter(mRecyclerView, eventKeyListToShow, getActivity(), mRef);
                        mRecyclerView.setAdapter(mAdapter);
                        mAdapter.setOnLoadMoreListener(WallEvent.this);
                        mAdapter.setDetailClickListner(WallEvent.this);

                        newKeyList.clear();
                        changeNotifNumber(newKeyList.size());
                    }

                    i[0]++;
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            mRef.child("events").child("items").child(key).addListenerForSingleValueEvent(eventListener);
        }

    }

    private void changeNotifNumber(int number) {
        if (number == 0) {
            notifEventView.setVisibility(View.GONE);
        }
        if (number > 0) {
            notifNumberText.setText(String.valueOf(number));
            notifEventView.setVisibility(View.VISIBLE);
        }
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION);//todo: search to found the problem with context
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
                    queryGeofireTo(mLastKnownLocation);
                    Log.e("getLastLocation", mLastKnownLocation.toString());

                } else {
                    Log.w("fusedLocation", "getLastLocation:exception", task.getException());
                    mLastKnownLocation = new Location(LocationManager.GPS_PROVIDER);
                    mLastKnownLocation.setLatitude((double) user.getLocation().get("latitude"));
                    mLastKnownLocation.setLongitude((double) user.getLocation().get("longitude"));
                    queryGeofireTo(mLastKnownLocation);
                    Log.e("geofire", "instantiated");
                }
//                            showSnackbar(getString(R.string.no_location_detected));
            }

        });
    }

    //Query GeoFire at default user location
    public void queryGeofireTo(Location location){

        mLastKnownLocation = location;
        mGeoQuery = mGeoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 100);
        mGeoQuery.addGeoQueryEventListener(WallEvent.this);
    }


}
