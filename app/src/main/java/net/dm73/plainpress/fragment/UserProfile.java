package net.dm73.plainpress.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.rubensousa.gravitysnaphelper.GravitySnapHelper;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import net.dm73.plainpress.EditUserProfile;
import net.dm73.plainpress.EventDetailOwner;
import net.dm73.plainpress.MenuEventClickListner;
import net.dm73.plainpress.R;
import net.dm73.plainpress.UpdateEvent;
import net.dm73.plainpress.adapter.UserEventRecyclerAdapter;
import net.dm73.plainpress.model.Category;
import net.dm73.plainpress.model.User;
import net.dm73.plainpress.util.AlertDialogWarnigRemove;
import net.dm73.plainpress.util.SpasesTopEventDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.facebook.FacebookSdk.getApplicationContext;


public class UserProfile extends Fragment implements MenuEventClickListner, PopupMenu.OnMenuItemClickListener{

    @BindView(R.id.profilUserName)
    TextView userName;
    @BindView(R.id.profilDescription)
    TextView userDescription;
    @BindView(R.id.profilTopEvent)
    TextView topEventTitle;
    //    @BindView(R.id.profilEditButton)
    ImageButton editProfileButton;
    @BindView(R.id.topEventContainer)
    LinearLayout topEventRecyclerView;
    @BindView(R.id.profilImage)
    ImageView userProfileImage;
    @BindView(R.id.emptyEventView)
    LinearLayout emptyEventView;
    @BindView(R.id.profilLocationUser)
    TextView userLocation;


    private static final String USER_ID = "user_id";
    private static final String EDIT_PROFIL = "edit_profil";



    private HashMap<Long,String> listCategories;
    private PopupMenu popup;
    public static String eventId;

    private DatabaseReference mRef;
    private FirebaseAuth mAuth;

    private Typeface titilWeb;
    private int density;

    private ValueEventListener userDataListener;

    private String currentKeyEvent;


    // TODO: Rename and change types of parameters
    private String userId;
    private boolean editProfil;


    public UserProfile() {
        // Required empty public constructor
    }

    public static UserProfile newInstance(String userId, boolean editProfil) {
        UserProfile fragment = new UserProfile();
        Bundle args = new Bundle();
        args.putString(USER_ID, userId);
        args.putBoolean(EDIT_PROFIL, editProfil);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString(USER_ID);
            editProfil = getArguments().getBoolean(EDIT_PROFIL, false);
        }

        titilWeb = Typeface.createFromAsset(getResources().getAssets(), "fonts/titilliumweb_regular.ttf");
        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference();

        density =  (int) getResources().getDisplayMetrics().density;
        listCategories = new HashMap<>();
        currentKeyEvent = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_user_profile, container, false);
        ButterKnife.bind(this, rootView);

        ((AppCompatActivity) getActivity()).setSupportActionBar((Toolbar) rootView.findViewById(R.id.toolbar_profile));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);

        editProfileButton = (ImageButton) rootView.findViewById(R.id.profilEditButton);
        userDescription.setTypeface(titilWeb);
        topEventTitle.setTypeface(titilWeb);
        userName.setTypeface(titilWeb);
        userLocation.setTypeface(titilWeb);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        userDataListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                userName.setText( user.isUserNickNameEmpty() ?  "User" : user.getNickName());
                userLocation.setText(setLocationName(user.getLocation()));
                userDescription.setText((user.getDescription() != null) ? user.getDescription() : "You dont put a description yet !");

                Glide.with(getApplicationContext())
                        .load(user.isUserPhotoProfilEmpty() ? R.drawable.image_holder : user.getPhotoUrl())
                        .centerCrop()
                        .placeholder(user.getGender() ? R.drawable.ic_avatar_m : R.drawable.ic_avatar_w)
                        .error(user.getGender() ? R.drawable.ic_avatar_m : R.drawable.ic_avatar_w)
                        .into(userProfileImage);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mRef.child("users").child(mAuth.getCurrentUser().getUid()).addValueEventListener(userDataListener);


        final ValueEventListener eventUserListner = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChildren()){

                    emptyEventView.setVisibility(View.GONE);
                    topEventRecyclerView.setVisibility(View.VISIBLE);
                    topEventRecyclerView.removeAllViews();

                    Map<String, Object> groupEvent =  (HashMap<String, Object>) dataSnapshot.getValue();
                    HashMap<Long, List<String>> sortedEvent = new HashMap<>();

                    for(Map.Entry data : groupEvent.entrySet()){
                        if(!sortedEvent.containsKey(data.getValue())){
                            sortedEvent.put((Long)data.getValue(),new ArrayList<String>());
                        }
                        sortedEvent.get(data.getValue()).add(data.getKey().toString());
                    }

                    for(Map.Entry<Long, List<String>> item : sortedEvent.entrySet()){
                        attachTopEvent(item.getKey(), item.getValue());
                    }

                }else{
                    //show empty events
                    emptyEventView.setVisibility(View.VISIBLE);
                    topEventRecyclerView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        ValueEventListener categoriesDataListner = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<Category>> genericTypeIndicator =new GenericTypeIndicator<List<Category>>(){};
                List<Category> listData = dataSnapshot.getValue(genericTypeIndicator);
                for(Category category : listData){
                    listCategories.put(category.getId(), category.getName());
                }
                mRef.child("users").child(mAuth.getCurrentUser().getUid()).child("events").addValueEventListener(eventUserListner);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mRef.child("categories").addListenerForSingleValueEvent(categoriesDataListner);

    }

    @Override
    public void onResume() {
        super.onResume();

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent().setClass(getApplicationContext(), EditUserProfile.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("activity_from","user_profil"));
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();

        if(popup!=null){
            popup.dismiss();
        }

        if(mRef!=null){
            mRef.child("users").child(mAuth.getCurrentUser().getUid()).removeEventListener(userDataListener);
        }
    }

    @Override
    public void onMenuEventClick(ImageView menuEvent, String keyEvent) {

        if(popup != null){
            popup.dismiss();
        }

        currentKeyEvent = keyEvent;
        popup = new PopupMenu(getContext(), menuEvent);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.menu_event, popup.getMenu());
        popup.setOnMenuItemClickListener(this);
        popup.show();
    }


    @Override
    public void onCardEventClick(String keyEvent) {
        //increment the Views Counter
        incrementView(mRef.child("events").child("items").child(keyEvent).child("views"));
        //send intent
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), EventDetailOwner.class);
        intent.putExtra("KEYEVENT", keyEvent);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.editEvent:
                edit(currentKeyEvent);
                return true;
            case R.id.delete:
                delete(currentKeyEvent);
                return true;
            default:
                return false;
        }
    }

    private void delete(String eventKey){
        showMoreDetailDIalog(eventKey);
        //todo:add verification user
    }

    private void edit(String eventKey){
        Intent intent = new Intent();
        intent.setClass(getApplicationContext(), UpdateEvent.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("KEYEVENT", eventKey);
        startActivity(intent);
        Log.e("eventkey", eventKey);
    }

    private void showMoreDetailDIalog(String keyEvent) {
        AlertDialogWarnigRemove newFragment = AlertDialogWarnigRemove.newInstance(keyEvent);
        newFragment.show(getFragmentManager(), "warning");
    }

    private String setLocationName(HashMap<String,Object> location){

        String value="";

        if(location.get("city")!=null)
            value = value.concat(location.get("city").toString());

        if(location.get("country")!=null)
            value = value.concat(", ").concat(location.get("country").toString());

        return value;
    }


    private void attachTopEvent(Long categoryId, List<String> keyEvents) {

        LinearLayout.LayoutParams paramsCategoryName = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsCategoryName.setMargins((20 * density), (20 * density), 0, 0);
        TextView categoryName = new TextView(getApplicationContext());
        categoryName.setPadding((20 * density), (8 * density), (20 * density), (8 * density));
        categoryName.setLayoutParams(paramsCategoryName);
        categoryName.setBackgroundResource(R.drawable.background_hashtag);
        categoryName.setText(listCategories.get(categoryId));
        categoryName.setGravity(Gravity.CENTER_VERTICAL);
        categoryName.setTextColor(Color.WHITE);
        categoryName.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        categoryName.setTypeface(titilWeb);
        topEventRecyclerView.addView(categoryName);

        //TODO:cleanup the adatapter for each created one !!
        DatabaseReference eventsRef = mRef.child("events").child("items");
        UserEventRecyclerAdapter mAdapter= new UserEventRecyclerAdapter(keyEvents, getApplicationContext(), eventsRef, false);
        mAdapter.setMenuEventClickListner(this);

        RecyclerView snappyRecyclerView = new RecyclerView(getApplicationContext());
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        snappyRecyclerView.setLayoutManager(layoutManager);
        SnapHelper snapHelperStart = new GravitySnapHelper(Gravity.START);
        snapHelperStart.attachToRecyclerView(snappyRecyclerView);
        SpasesTopEventDecoration decoration = new SpasesTopEventDecoration(40);
        snappyRecyclerView.addItemDecoration(decoration);
        snappyRecyclerView.setAdapter(mAdapter);
        topEventRecyclerView.addView(snappyRecyclerView);
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


}
