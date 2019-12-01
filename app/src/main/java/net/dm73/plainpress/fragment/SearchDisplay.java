package net.dm73.plainpress.fragment;

import android.app.DialogFragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.github.aakira.expandablelayout.ExpandableRelativeLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import net.dm73.plainpress.CategoryChooser;
import net.dm73.plainpress.ClickListenerResult;
import net.dm73.plainpress.EventDetail;
import net.dm73.plainpress.MainActivity;
import net.dm73.plainpress.R;
import net.dm73.plainpress.adapter.CategorySearchRecyclerAdapter;
import net.dm73.plainpress.adapter.SearchRecyclerAdapter;
import net.dm73.plainpress.model.Category;
import net.dm73.plainpress.model.Event;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;


public class SearchDisplay extends Fragment implements GeoQueryEventListener, AdapterView.OnItemSelectedListener, ClickListenerResult, CategoryChooser {


    private static String LANTITUDE = "lantitude";
    private static String LONGITUDE = "longitude";
    private static String QUERY = "query";
    private static String SEARCH_MODE = "search_mode";

    private double lan;
    private double lng;
    private String query;
    private int searchMode;

    @BindView(R.id.recycleViewSearch)
    RecyclerView recyclerView;
    @BindView(R.id.searchFilter)
    ImageView filtre;
    @BindView(R.id.expandableFiltre)
    ExpandableRelativeLayout mExpandableFiltre;
    @BindView(R.id.categoriesRecyclerview)
    RecyclerView categoriesRecyclerView;
    @BindView(R.id.clearChoise)
    ImageView clearChoise;
    @BindView(R.id.calendarPicker)
    FrameLayout calendarPicker;
    @BindView(R.id.calendarValue)
    TextView calendarValue;
    @BindView(R.id.searchView)
    SearchView searchView;
    @BindView(R.id.spinnerDate)
    Spinner spinnerDate;
    @BindView(R.id.dateTimePicker)
    LinearLayout dateTimePicker;
    @BindView(R.id.backSearchDisplay)
    ImageView backButton;
    @BindView(R.id.nothingToshowView)
    LinearLayout nothingToShowView;
    @BindView(R.id.messageToUser)
    TextView messageToUser;
    @BindView(R.id.okButtonFilter)
    Button okButtonFilter;
    @BindView(R.id.coldViewsSwitch)
    Switch coldSwitch;
    @BindView(R.id.hotViewsSwitch)
    Switch hotSwitch;
    @BindView(R.id.nearbySeekBar)
    SeekBar nearbyseekbar;
    @BindView(R.id.progressValue)
    TextView progressValue;


    private ImageView closeSearch;


    private SearchRecyclerAdapter mAdapter;
    private DatabaseReference mRef;
    private GeoFire mGeoFire;
    private GeoQuery mGeoQuery;

    static TextView calendarStaticValue;


    private HashMap<String,Event> listEvent;
    private HashMap<String,Event> filterListEvent;
    private HashMap<String,Event> queryListEvent;
    private List<Event> events;
    private List<String> keys;

    private int nearby = 100;


    public static boolean dateFilterActive = false;
    private boolean isFiltreActive = false;

    private Long timestamp;
    private boolean customdate = false;
    private CategorySearchRecyclerAdapter mCategoryAdapter;

    private Typeface typeface;


    public static SearchDisplay newInstance(double lat, double lng, String query, int searchMode) {

        SearchDisplay fragment = new SearchDisplay();
        Bundle arg = new Bundle();
        arg.putDouble(LANTITUDE, lat);
        arg.putDouble(LONGITUDE, lng);
        arg.putString(QUERY, query);
        arg.putInt(SEARCH_MODE, searchMode);
        fragment.setArguments(arg);

        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            lan = getArguments().getDouble(LANTITUDE);
            lng = getArguments().getDouble(LONGITUDE);
            query = getArguments().getString(QUERY);
            searchMode = getArguments().getInt(SEARCH_MODE);
        }

        Log.e("fragment searchdisplay", lan + " , " + lng);

        listEvent = new HashMap<>();
        filterListEvent = new HashMap<>();
        queryListEvent = new HashMap<>();

        events = new ArrayList<>();
        keys = new ArrayList<>();

        mRef = FirebaseDatabase.getInstance().getReference();
        mGeoFire = new GeoFire(mRef.child("geoevents"));
        typeface = Typeface.createFromAsset(getContext().getAssets(), "fonts/titilliumweb_regular.ttf");

        Calendar gmtTime = Calendar.getInstance();
        Log.e("calendar timestamp", gmtTime.getTimeInMillis()+", systemtimestamp"+System.currentTimeMillis()+", timezone:"+ TimeZone.getDefault().getRawOffset());

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_search_display, null);
        ButterKnife.bind(this, rootView);

        mGeoQuery = mGeoFire.queryAtLocation(new GeoLocation(lan, lng), nearby);
        mGeoQuery.addGeoQueryEventListener(this);
        nearbyseekbar.setProgress(100);
        calendarStaticValue = calendarValue;

        String[] plants = new String[]{
                "Select a date...",
                "Today",
                "This week",
                "This month",
                "Custom date"
        };

        ArrayAdapter<CharSequence> arrayAdapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item, plants){
            @Override
            public boolean isEnabled(int position) {
                if(position ==0)
                    return false;
                else return true;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView textView = (TextView) view;
                textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                if(position == 0){
                    textView.setTextColor(Color.GRAY);
                }else{
                    textView.setTextColor(Color.BLACK);
                }

                return view;
            }
        };

        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDate.setAdapter(arrayAdapter);
        spinnerDate.setOnItemSelectedListener(this);

        if(query != null){
            searchView.setQuery(query, true);
        }

        int searchCloseButtonId = searchView.getContext().getResources()
                .getIdentifier("android:id/search_close_btn", null, null);
        closeSearch = (ImageView) searchView.findViewById(searchCloseButtonId);

        ValueEventListener categorieListner = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<HashMap<String, Object>> categories = (ArrayList<HashMap<String, Object>>)dataSnapshot.getValue();

                List<Category> listCategories = new ArrayList<>();
                for(int i=0; i<categories.size(); i++) {
                    if((boolean)categories.get(i).get("enabled"))
                        listCategories.add(new Category((long)categories.get(i).get("id"), (String)categories.get(i).get("name")));
                }

                categoriesRecyclerView.setHasFixedSize(true);
                mCategoryAdapter = new CategorySearchRecyclerAdapter(listCategories, getActivity(), typeface);
                categoriesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
                categoriesRecyclerView.setAdapter(mCategoryAdapter);
                mCategoryAdapter.setCategoryChooserListner(SearchDisplay.this);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mRef.child("categories").addListenerForSingleValueEvent(categorieListner);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if(filterListEvent!=null && filterListEvent.size()>0) {
            hashMapToList(filterListEvent);
        }else {
            hashMapToList(listEvent);
        }

        if(mAdapter == null) {

            mAdapter = new SearchRecyclerAdapter(events, getActivity());
            mAdapter.setCilckResultListener(SearchDisplay.this);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setAdapter(mAdapter);
        }

        nearbyseekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressValue.setText(String.format("%s",progress+20));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                nearby = seekBar.getProgress()+20;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.switchFragment(Search.newInstance(searchMode), "SEARCH", getFragmentManager());
            }
        });

        okButtonFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mExpandableFiltre.toggle();
                isFiltreActive = true;
                filterListEvent.clear();
                mGeoQuery.setRadius(nearby);

                Log.e("choosed categorie", mCategoryAdapter.getListCategoiesChoosed().toString());
                Log.e("location and nearby", mGeoQuery.getCenter().toString()+" , " +nearby);
                Log.e("customdate", customdate+"");
                Log.e("filterdate", dateFilterActive+"");
            }
        });

        filtre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mExpandableFiltre.isExpanded())
                    mExpandableFiltre.toggle();
            }
        });

        calendarPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalendarPickerDialog();
            }
        });


        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(final String query) {
                queryListEvent = resultSearchByQuery(filterListEvent, query);
                hashMapToList(queryListEvent);
                if(events.size()>0) {
                    mAdapter = new SearchRecyclerAdapter(events, getActivity());
                    recyclerView.setAdapter(mAdapter);
                    mAdapter.setDataAdapter(events);
                    hideEmptyResult();
                }else{
                    messageToUser.setText("Sorry we didn't found any event !");
                    showEmptyResult();
                }
                Log.e("listEvent size", listEvent.size()+"");
                Log.e("filterListEvent size", filterListEvent.size()+"");

                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }

        });

        closeSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("search close", "clicked");
                query = null;
                hashMapToList(filterListEvent);
                mAdapter = new SearchRecyclerAdapter(events, getActivity());
                recyclerView.setAdapter(mAdapter);
                searchView.setQuery("", false);
                searchView.clearFocus();
                queryListEvent = new HashMap<>();
                hideEmptyResult();
            }
        });

        clearChoise.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerDate.setSelection(0);
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onKeyEntered(String key, GeoLocation location) {
        listEvent.put(key,null);

    }

    @Override
    public void onKeyExited(String key) {
        listEvent.remove(key);

    }

    @Override
    public void onKeyMoved(String key, GeoLocation location) {

    }

    @Override
    public void onGeoQueryReady() {

        Log.e("geoready", "true");
        Log.e("listEvent", listEvent.toString());

        if(listEvent.isEmpty()){
            showEmptyResult();
            return;
        }

        hideEmptyResult();
        int i = 0;
        if(isFiltreActive) {
            for (final Map.Entry keyEvent : listEvent.entrySet()) {
                i++;
                final int finalI = i;
                ValueEventListener markerListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Event event = dataSnapshot.getValue(Event.class);

                        if(filterData(event))
                            filterListEvent.put(keyEvent.getKey().toString(), event);

                        if (finalI == listEvent.size()) {
                            if(query!=null){
                                queryListEvent = resultSearchByQuery(filterListEvent, query);
                                hashMapToList(queryListEvent);
                            }else{
                                hashMapToList(filterListEvent);
                            }
                            if(events.size()>0) {
                                mAdapter = new SearchRecyclerAdapter(events, getActivity());
                                recyclerView.setAdapter(mAdapter);
                                hideEmptyResult();
                            }else{
                                showEmptyResult();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                mRef.child("events").child("items").child(keyEvent.getKey().toString()).addListenerForSingleValueEvent(markerListener);
            }
        }else {

            if(query!=null)
                Log.e("query", query);

            for (final Map.Entry keyEvent : listEvent.entrySet()) {
                i++;
                final int finalI = i;
                ValueEventListener markerListener = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Event event = dataSnapshot.getValue(Event.class);

                        listEvent.put(keyEvent.getKey().toString(), event);

                        if (finalI == listEvent.size()) {
                            filterListEvent = new HashMap<>(listEvent);
                            if(query!=null){
                                queryListEvent = resultSearchByQuery(filterListEvent, query);
                                hashMapToList(queryListEvent);
                            }else{
                                hashMapToList(listEvent);
                            }

                            if(events.size()>0) {
                                mAdapter = new SearchRecyclerAdapter(events, getActivity());
                                recyclerView.setAdapter(mAdapter);
                                hideEmptyResult();
                            }else{
                                messageToUser.setText("Sorry we didn't found any event !");
                                showEmptyResult();
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                };
                mRef.child("events").child("items").child(keyEvent.getKey().toString()).addListenerForSingleValueEvent(markerListener);
            }
        }


    }

    @Override
    public void onGeoQueryError(DatabaseError error) {
        showEmptyResult();
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

        switch(position){
            case 0:
                dateFilterActive = false;
                calendarPicker.setVisibility(View.GONE);
                customdate = false;
                break;
            case 1:
                timestamp = (long)86400;
                Log.e("timestamp 1",timestamp+"");
                calendarPicker.setVisibility(View.GONE);
                dateFilterActive = true;
                customdate = false;
                break;
            case 2:
                timestamp = (long)691200;
                Log.e("timestamp 2",timestamp+"");
                calendarPicker.setVisibility(View.GONE);
                dateFilterActive = true;
                customdate = false;
                break;
            case 3:
                timestamp = (long)2764800;
                Log.e("timestamp 3",timestamp+"");
                calendarPicker.setVisibility(View.GONE);
                dateFilterActive = true;
                customdate = false;
                break;
            case 4:
                calendarPicker.setVisibility(View.VISIBLE);
                dateFilterActive = true;
                customdate = true;
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void showEmptyResult(){
        if(searchMode == Search.EVENT_SEARCH){
            messageToUser.setText("Sorry we didn't found any event !");
        }else{
            messageToUser.setText("Sorry we didn't found any event in this location !");
        }
        nothingToShowView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    private void hideEmptyResult(){
        nothingToShowView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    private void showCalendarPickerDialog() {
        DialogFragment newFragment = new DatePickerFragement(calendarValue);
        newFragment.show(getActivity().getFragmentManager(), "calendarPicker");
    }

    private boolean filterData(Event event){
        if(event!=null && filterByCategory(event) && filtreByViews(event) && filterByDate(event))
            return true;
        else
            return false;
    }

    private boolean filterByCategory(Event event){

        Log.e("categories memory", mCategoryAdapter.getListCategoiesChoosed().toString());
        Log.e("eventid", event.getCategory().getId()+"");

        if(mCategoryAdapter.getListCategoiesChoosed()!=null &&  mCategoryAdapter.getListCategoiesChoosed().size()==0)
            return true;

        if ( mCategoryAdapter.getListCategoiesChoosed()!=null &&  mCategoryAdapter.getListCategoiesChoosed().size()>0){
            for(Long data :  mCategoryAdapter.getListCategoiesChoosed()){
                if(data.toString().equals(event.getCategory().getId()+""))
                    return true;
            }
        }

        return false;
    }

    private boolean filterByDate(Event event){
        if(dateFilterActive){
            if(customdate) dateToTimestamp();
            Log.e("timestamp", timestamp + "");
            Log.e("publishedtimestamp", event.getPublishedAt() + "");

            Long diffTimestamp = (Calendar.getInstance().getTimeInMillis() - (long) event.getPublishedAt()) / 1000;
            if (diffTimestamp >= timestamp)
                return false;

        }

        return true;
    }

    private boolean filtreByViews(Event event){

        if(coldSwitch.isChecked() && hotSwitch.isChecked())
            return true;

//        if(!coldSwitch.isChecked() && !hotSwitch.isChecked())
//            return true;

        if(coldSwitch.isChecked() && event.getViews()<1000l)
            return true;

        if(hotSwitch.isChecked() && event.getViews()>1000l)
            return true;

        return false;
    }

    private HashMap<String,Event> resultSearchByQuery(HashMap<String,Event> listEvent, String query){

        HashMap<String,Event> result = new HashMap<>();

        for (Map.Entry event : listEvent.entrySet()) {
            if (event.getValue() != null && ((Event)event.getValue()).getTitle().toLowerCase().contains(query.toLowerCase())) {
                result.put(event.getKey().toString(), (Event)event.getValue());
            } else if (event.getValue() != null && ((Event)event.getValue()).getDescription().toLowerCase().contains(query.toLowerCase())) {
                result.put(event.getKey().toString(), (Event)event.getValue());;
            }
        }
        return result;
    }

    private void dateToTimestamp(){
        try {
            DateFormat formatter;
            formatter = new SimpleDateFormat("dd/MM/yyyy");
            if(calendarStaticValue.getText().toString().equals("01/01/1991")){
                dateFilterActive = false;
                return ;
            }else{
                // you can change format of date
                Date date = formatter.parse(calendarStaticValue.getText().toString());
                java.sql.Timestamp timeStampDate = new Timestamp(date.getTime());
                timestamp =  (System.currentTimeMillis() - timeStampDate.getTime())/1000;
            }
        } catch (ParseException e) {
            System.out.println("Exception :" + e);
            dateFilterActive = false;
            return ;
        }
    }

    private void hashMapToList(HashMap<String,Event> hash){

        events.clear();
        keys.clear();

        for(Map.Entry value : hash.entrySet()){
            events.add((Event)value.getValue());
            keys.add(value.getKey().toString());
        }
    }


    @Override
    public void onClickResult(int position) {
        startActivity(new Intent().setClass(getActivity(), EventDetail.class).putExtra("KEYEVENT",keys.get(position)).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    @Override
    public void onCategoryChoosed(int position) {
        mCategoryAdapter.notifyCategoryClicked(position);
    }
}
