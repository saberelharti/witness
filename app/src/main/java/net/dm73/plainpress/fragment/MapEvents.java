package net.dm73.plainpress.fragment;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Interpolator;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import net.dm73.plainpress.MainActivity;
import net.dm73.plainpress.R;
import net.dm73.plainpress.dao.DaoPreference;
import net.dm73.plainpress.model.Event;
import net.dm73.plainpress.model.User;
import net.dm73.plainpress.util.ClusterEvent;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MapEvents extends Fragment implements OnMapReadyCallback, LocationListener, GeoQueryEventListener, ClusterManager.OnClusterItemClickListener<ClusterEvent>, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private final List<Integer> listArrayGreenRessources = Arrays.asList(R.drawable.ic_green_assault,
            R.drawable.ic_green_accident_auto, R.drawable.ic_green_fire, R.drawable.ic_green_break,
            R.drawable.ic_green_gun, R.drawable.ic_green_sus_activity, R.drawable.ic_green_theft,
            R.drawable.ic_green_kidnapping, R.drawable.ic_green_o_categories);

    private final List<Integer> listArrayRedRessources = Arrays.asList(R.drawable.ic_red_assault,
            R.drawable.ic_red_accident_auto, R.drawable.ic_red_fire, R.drawable.ic_red_break,
            R.drawable.ic_red_gun, R.drawable.ic_red_sus_activity, R.drawable.ic_red_theft,
            R.drawable.ic_red_kidnapping, R.drawable.ic_red_o_categories);

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    @BindView(R.id.mapView)
    MapView mapView;
    @BindView(R.id.eventDetailContainer)
    LinearLayout cardDetail;
    @BindView(R.id.eventImage)
    ImageView eventImage;
    @BindView(R.id.eventTitle)
    TextView eventTitle;
    @BindView(R.id.eventLocation)
    TextView eventLocation;
    @BindView(R.id.eventDesc)
    TextView eventDesc;
    @BindView(R.id.closeEvent)
    ImageView closeButton;
    @BindView(R.id.navDrawermaps)
    ImageView navBarButton;
    @BindView(R.id.goToEvent)
    Button goToEvent;
    @BindView(R.id.autoCompleteSearch)
    AutoCompleteTextView searchBar;
    @BindView(R.id.clearSearch)
    ImageView clearSearch;


    private GoogleMap mGoogleMap;
    private CameraPosition mCameraPosition;
    private Location mLastKnownLocation;
    private ClusterManager<ClusterEvent> mClusterManager;

    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final int ACCESS_FINE_LOCATION_INTENT_ID = 3;
    private FusedLocationProviderClient mFusedLocationClient;
    private static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));


    private static final int DEFAULT_ZOOM = 30;
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);//Todo:make default maps the location of his adress
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;
    private LocationRequest mLocationRequest;
    private Circle searchEventCircle;
    private DatabaseReference mRef;
    private Location resultLocation;

    private Geocoder geoCoder;
    private GeoFire mGeoFire;
    private GeoQuery mGeoQuery;
    private HashMap<String, ClusterEvent> markers;

    private DaoPreference mDaoPreference;

    private boolean closeDetail = false;
    private String lastMarkerKey;

    private ValueEventListener userProfileListener;
    private PlaceArrayAdapter mPlaceArrayAdapter;
    private GoogleApiClient googleApiClient;

    private User user;


    // TODO: Rename and change types and number of parameters
    public static MapEvents newInstance(String param1, String param2) {
        MapEvents fragment = new MapEvents();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
        mRef = MainActivity.mRef;
        mGeoFire = new GeoFire(mRef.child("geoevents"));

        markers = new HashMap<>();

        mDaoPreference = new DaoPreference(getActivity());
        geoCoder = new Geocoder(getActivity(), Locale.getDefault());

        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addApi(Places.GEO_DATA_API)
                    .addOnConnectionFailedListener(this).build();
            googleApiClient.connect();
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());

        Log.e("created fragment","true");

        if(savedInstanceState!=null){
            Log.e("savedInstance", savedInstanceState.getFloat("zoom")+"");
        }

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_map_events, container, false);
        ButterKnife.bind(this, v);

        if (googleServicesAvailable()) {
            MapsInitializer.initialize(this.getActivity());
            mapView.onCreate(savedInstanceState);

        } else {
            //show the empty layout
        }

        navBarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Inflate the layout for this fragment
        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloat("zoom", mGoogleMap.getCameraPosition().zoom);
        outState.putDouble("latitude", mGoogleMap.getCameraPosition().target.latitude);
        outState.putDouble("longitude", mGoogleMap.getCameraPosition().target.longitude);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if(savedInstanceState!=null)
            Log.e("data saved", savedInstanceState.getString("zoom")+", ("+savedInstanceState.getDouble("latitude")+", "+savedInstanceState.getDouble("longitude")+")");

        //add globale mLastLocation
        if (googleApiClient != null && !googleApiClient.isConnected())
            googleApiClient.connect();

        mapView.getMapAsync(this);
    }

    public boolean googleServicesAvailable() {

        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(getContext());
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog mDialog = api.getErrorDialog(getActivity(), isAvailable, 0);
            mDialog.show();
        } else {
            Toast.makeText(getContext(), "Cant connect to play services", Toast.LENGTH_SHORT).show();
        }

        return false;
    }

    @Override
    public void onStart() {
        super.onStart();

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cardDetail.setVisibility(View.GONE);
            }
        });

        goToEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                closeDetail = true;

                //increment event views
                incrementView(mRef.child("events").child("items").child(lastMarkerKey).child("views"));

                Intent intent = new Intent();
                intent.setClass(getActivity(), net.dm73.plainpress.EventDetail.class);
                intent.putExtra("KEYEVENT", lastMarkerKey);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(count==0){
                    clearSearch.setVisibility(View.GONE);
                }else{
                    clearSearch.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchBar.setText("");
            }
        });

        Log.e("started fragment", "true");

    }


    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.onResume();
        }

        if (!closeDetail)
            closeDetail = false;

        Log.e("resumed fragment", "true");
    }

    @Override
    public void onPause() {
        super.onPause();
//        mGoogleMap.clear();
        Log.e("paused fragment","true");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mapView != null) {
            mapView.onDestroy();
        }

        if (mGeoQuery != null)
            mGeoQuery.removeAllListeners();

        if(mClusterManager != null)
            mClusterManager.clearItems();

        if(markers != null)
            markers.clear();

        if(searchEventCircle != null)
            searchEventCircle.remove();

        if(mGoogleMap != null)
            mGoogleMap.clear();

        if (searchEventCircle != null)
            searchEventCircle.remove();

        if(userProfileListener != null)
            mRef.removeEventListener(userProfileListener);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (googleApiClient != null)
            googleApiClient.disconnect();

        if (closeDetail)
            cardDetail.setVisibility(View.GONE);

    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mapView != null) {
            mapView.onLowMemory();
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;

        Log.e("googleMap", googleMap.toString());
        Toast.makeText(getContext(), "googleMap ready", Toast.LENGTH_SHORT).show();

    }

    private void updateLocationUI() {
        if (mGoogleMap == null) {
            Log.e("mGoogleMap", "isNull");
            return;
        }

        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        //edit the map location
        if (mLocationPermissionGranted) {
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(true);
        } else {
            mGoogleMap.setMyLocationEnabled(true);
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        }

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


    private void getDeviceLocation() {


        if (ContextCompat.checkSelfPermission(getActivity().getApplicationContext(), ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{ACCESS_FINE_LOCATION}, PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }

        // Set the map's camera position to the current location of the device.
        if (mCameraPosition != null) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
        } else if (mLastKnownLocation != null) {
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), getZoomLevel(mDaoPreference.getNearbyPreference() * 1000)));

//            mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(mLastKnownLocation.getLatitude(),
//                    mLastKnownLocation.getLongitude()))
//                    .title("Marker My place"));

            searchEventCircle = mGoogleMap.addCircle(new CircleOptions().center(new LatLng(mLastKnownLocation.getLatitude(),
                    mLastKnownLocation.getLongitude())).radius(mDaoPreference.getNearbyPreference() * 1000));

            searchEventCircle.setFillColor(Color.argb(66, 243, 197, 159));
            searchEventCircle.setStrokeColor(Color.argb(66, 60, 79, 94));
            searchEventCircle.setStrokeWidth(3);


            mGeoQuery = mGeoFire.queryAtLocation(new GeoLocation(mLastKnownLocation.getLatitude(),
                    mLastKnownLocation.getLongitude()), 100);

            mGeoQuery.addGeoQueryEventListener(this);

            Log.e("lastposition", "alt: " + mLastKnownLocation.getLatitude() + " long :" + mLastKnownLocation.getLongitude());
        } else {
            Log.d("MAPS_EVENT", "Current location is null. Using defaults.");
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(mLastKnownLocation.getLatitude(),
                            mLastKnownLocation.getLongitude()), getZoomLevel(mDaoPreference.getNearbyPreference() * 1000)));
            mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    public int getZoomLevel(int radius) {
        int zoomLevel = DEFAULT_ZOOM;
        double scale = radius / 500;
        zoomLevel = (int) Math.floor((16 - Math.log(scale) / Math.log(2)));
        return zoomLevel;
    }


    @Override
    public void onLocationChanged(Location location) {
        mLastKnownLocation = location;
    }

    @Override
    public void onKeyEntered(final String key, final GeoLocation location) {

        ValueEventListener markerListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Event markerEvent = dataSnapshot.getValue(Event.class);
                ClusterEvent markerItem = new ClusterEvent(new LatLng(location.latitude, location.longitude), generateMarkerIcon(markerEvent), markerEvent, key);
                markers.put(key, markerItem);
                mClusterManager.addItem(markerItem);
                if((Calendar.getInstance().getTimeInMillis() - (long) markerEvent.getPublishedAt())/1000 <= 3600){
                    Log.e("current timetamp", Calendar.getInstance().getTimeInMillis()+"");
                    addAnimatedCircle((new LatLng(location.latitude, location.longitude)));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mRef.child("events").child("items").child(key).addListenerForSingleValueEvent(markerListener);


    }

    private BitmapDescriptor generateMarkerIcon(Event event) {

        if(event.getViews() > 1000){
            return BitmapDescriptorFactory.fromResource(listArrayRedRessources.get((int)event.getCategory().getId()-1));
        }else{
            return BitmapDescriptorFactory.fromResource(listArrayGreenRessources.get((int)event.getCategory().getId()-1));
        }
    }

    private void addAnimatedCircle(LatLng location){
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setSize(500,500);
        d.setColor(0x66f44242);
        d.setStroke(5, Color.TRANSPARENT);

        Bitmap bitmap = Bitmap.createBitmap(d.getIntrinsicWidth()
                , d.getIntrinsicHeight()
                , Bitmap.Config.ARGB_8888);

        // Convert the drawable to bitmap
        Canvas canvas = new Canvas(bitmap);
        d.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        d.draw(canvas);

        // Radius of the circle
        final int radius = 1000;

        // Add the circle to the map
        final GroundOverlay circle = mGoogleMap.addGroundOverlay(new GroundOverlayOptions()
                .position(location, 2 * radius).image(BitmapDescriptorFactory.fromBitmap(bitmap)));

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setIntValues(0, radius);
        valueAnimator.setDuration(3000);
        valueAnimator.setEvaluator(new IntEvaluator());
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator.getAnimatedFraction();
                circle.setDimensions(animatedFraction * radius * 2);
            }
        });

        valueAnimator.start();
    }

    @Override
    public void onKeyExited(String key) {

        ClusterEvent clusterItem = markers.get(key);
        if (clusterItem != null) {
            mClusterManager.removeItem(clusterItem);
            markers.remove(key);
        }

    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {

//        Marker marker = markers.get(key);
//        if (marker != null) {
//            //animate the marker
//            this.animateMarkerTo(marker, location.latitude, location.longitude);
//        }

    }

    @Override
    public void onGeoQueryReady() {

    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Error")
                .setMessage("There was an unexpected error querying GeoFire: " + error.getMessage())
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    // Animation handler for old APIs without animation support
    private void animateMarkerTo(final Marker marker, final double lat, final double lng) {
        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final long DURATION_MS = 3000;
        final Interpolator interpolator = new AccelerateDecelerateInterpolator();
        final LatLng startPosition = marker.getPosition();
        handler.post(new Runnable() {
            @Override
            public void run() {
                float elapsed = SystemClock.uptimeMillis() - start;
                float t = elapsed / DURATION_MS;
                float v = interpolator.getInterpolation(t);

                double currentLat = (lat - startPosition.latitude) * v + startPosition.latitude;
                double currentLng = (lng - startPosition.longitude) * v + startPosition.longitude;
                marker.setPosition(new LatLng(currentLat, currentLng));

                // if animation is not finished yet, repeat
                if (t < 1) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    @Override
    public boolean onClusterItemClick(final ClusterEvent clusterEvent) {

        cardDetail.setVisibility(View.VISIBLE);
        Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                Event event = clusterEvent.getEvent();
                eventTitle.setText(event.getTitle());
                eventDesc.setText(event.getDescription());
                eventLocation.setText(getLocationName(event.getLocation()));
            }
        });

        Glide.with(getActivity())
                .load(clusterEvent.getEvent().getFirstMediaImage() == null ? R.drawable.image_holder : clusterEvent.getEvent().getFirstMediaImage())
                .centerCrop()
                .placeholder(R.drawable.image_holder)
                .error(R.drawable.image_holder)
                .into(eventImage);

        lastMarkerKey = clusterEvent.getKey();

        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mPlaceArrayAdapter = new PlaceArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, BOUNDS_GREATER_SYDNEY, null);
        searchBar.setOnItemClickListener(mAutocompleteClickListener);
        mPlaceArrayAdapter.setGoogleApiClient(googleApiClient);
        searchBar.setAdapter(mPlaceArrayAdapter);

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
                    mLastKnownLocation = new Location(LocationManager.GPS_PROVIDER);
                    mLastKnownLocation.setLatitude((double) user.getLocation().get("latitude"));
                    mLastKnownLocation.setLongitude((double) user.getLocation().get("longitude"));
                    queryGeofireTo(mLastKnownLocation);
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
//            searchableLocation = place.getLatLng();
            resultLocation = new Location(LocationManager.GPS_PROVIDER);
            resultLocation.setLongitude(queriedLocation.longitude);
            resultLocation.setLatitude(queriedLocation.latitude);
            queryGeofireFromResultLocation(resultLocation);
            Log.e("(lat,lng)", "("+queriedLocation.latitude+" , "+queriedLocation.longitude+")");
        };
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case ACCESS_FINE_LOCATION_INTENT_ID: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    showSettingDialog();

                } else {
                    Toast.makeText(getActivity(), "Location Permission denied.", Toast.LENGTH_SHORT).show();
                }
            }
        }

        updateLocationUI();
    }

    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private String getLocationName(net.dm73.plainpress.model.Location location){

        if(location.getCity()!=null && !location.getCity().isEmpty()){
            return location.getCity();
        }

        if(location.getCountry()!=null && !location.getCountry().isEmpty()){
            return location.getCountry();
        }

        return "Unknoun";
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
                    mLastKnownLocation.setLatitude((double) user.getLocation().get("latitude"));//your coords of course
                    mLastKnownLocation.setLongitude((double) user.getLocation().get("longitude"));
                    queryGeofireTo(mLastKnownLocation);

                    Log.e("geofire", "instantiated");
                }
            }

        });
    }

    public void queryGeofireTo(Location location){

        mLastKnownLocation = location;

        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mLastKnownLocation.getLatitude(),
                mLastKnownLocation.getLongitude()), 9));

//            mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(mLastKnownLocation.getLatitude(),
//                    mLastKnownLocation.getLongitude()))
//                    .title("Marker My place"));
//
//        if(searchEventCircle != null)
//            searchEventCircle.remove();

        searchEventCircle = mGoogleMap.addCircle(new CircleOptions().center(new LatLng(location.getLatitude(),
                location.getLongitude())).radius(new BigDecimal((Long)user.getSettings().get("nearby")).intValueExact() * 1000));
        searchEventCircle.setFillColor(Color.argb(66, 243, 197, 159));
        searchEventCircle.setStrokeColor(Color.argb(66, 60, 79, 94));
        searchEventCircle.setStrokeWidth(3);

        mClusterManager = new ClusterManager<>(getActivity(), mGoogleMap);
        mClusterManager.setRenderer(new OwnIconRendered(getActivity(), mGoogleMap, mClusterManager));
        mClusterManager.setOnClusterItemClickListener(this);
        mGoogleMap.setOnCameraIdleListener(mClusterManager);
        mGoogleMap.setOnMarkerClickListener(mClusterManager);

        mGeoQuery = mGeoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 100);
        mGeoQuery.addGeoQueryEventListener(this);
    }

    public void queryGeofireFromResultLocation(Location location){

        mGoogleMap.clear();
        mLastKnownLocation = location;

        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),
                location.getLongitude()), 9));

        searchEventCircle = mGoogleMap.addCircle(new CircleOptions().center(new LatLng(location.getLatitude(),
                location.getLongitude())).radius(new BigDecimal((Long)user.getSettings().get("nearby")).intValueExact() * 1000));
        searchEventCircle.setFillColor(Color.argb(66, 243, 197, 159));
        searchEventCircle.setStrokeColor(Color.argb(66, 60, 79, 94));
        searchEventCircle.setStrokeWidth(3);

        mClusterManager = new ClusterManager<>(getActivity(), mGoogleMap);
        mClusterManager.setRenderer(new OwnIconRendered(getActivity(), mGoogleMap, mClusterManager));
        mClusterManager.setOnClusterItemClickListener(this);
        mGoogleMap.setOnCameraIdleListener(mClusterManager);
        mGoogleMap.setOnMarkerClickListener(mClusterManager);

        mGeoQuery = mGeoFire.queryAtLocation(new GeoLocation(location.getLatitude(), location.getLongitude()), 100);
        mGeoQuery.addGeoQueryEventListener(this);
    }

    class OwnIconRendered extends DefaultClusterRenderer<ClusterEvent> {

        public OwnIconRendered(Context context, GoogleMap map,
                               ClusterManager<ClusterEvent> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(ClusterEvent item, MarkerOptions markerOptions) {
            markerOptions.icon(item.getIcon());
            super.onBeforeClusterItemRendered(item, markerOptions);
        }

    }

}
