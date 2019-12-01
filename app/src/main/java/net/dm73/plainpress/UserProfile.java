package net.dm73.plainpress;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SnapHelper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import net.dm73.plainpress.adapter.UserEventRecyclerAdapter;
import net.dm73.plainpress.model.Category;
import net.dm73.plainpress.model.User;
import net.dm73.plainpress.util.SpasesTopEventDecoration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UserProfile extends AppCompatActivity implements MenuEventClickListner {

    @BindView(R.id.profilUserName)
    TextView userName;
    @BindView(R.id.profilLocationUser)
    TextView userLocation;
    @BindView(R.id.profilImage)
    ImageView userProfileImage;
    @BindView(R.id.profilDescription)
    TextView userDescription;
    @BindView(R.id.profilTopEvent)
    TextView topEventTitle;
    @BindView(R.id.backButton)
    ImageButton backButton;
    @BindView(R.id.topEventContainer)
    LinearLayout topEventRecyclerView;
    @BindView(R.id.emptyEventView)
    LinearLayout emptyEventView;


    private HashMap<Long,String> listCategories;

    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private StorageReference mStor;

    private Typeface titilWeb;
    private int density;

    private String userId;
    private ValueEventListener userDataListener;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this);

        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }


        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_profile));
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        userId = getIntent().getStringExtra("userID");
        Log.e("user id", userId);

        titilWeb = Typeface.createFromAsset(getResources().getAssets(), "fonts/titilliumweb_regular.ttf");
        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference();
        mStor = FirebaseStorage.getInstance().getReference();

        density =  (int) getResources().getDisplayMetrics().density;
        listCategories = new HashMap<>();

        userDescription.setTypeface(titilWeb);
        topEventTitle.setTypeface(titilWeb);
        userName.setTypeface(titilWeb);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            String imageTransitionName = getIntent().getExtras().getString("profile");
            userProfileImage.setTransitionName(imageTransitionName);
        }
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
        mRef.child("users").child(userId).addValueEventListener(userDataListener);


        final ValueEventListener eventUserListner = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if(dataSnapshot.hasChildren()){

                    emptyEventView.setVisibility(View.GONE);
                    topEventRecyclerView.removeAllViews();

                    Map<String, Long> groupEvent =  (HashMap<String, Long>)dataSnapshot.getValue();
                    HashMap<Long, List<String>> sortedEvent = new HashMap<>();

                    for(Map.Entry data : groupEvent.entrySet()){
                        if(!sortedEvent.containsKey(data.getValue())){
                            sortedEvent.put((Long)data.getValue(),new ArrayList<String>());
                        }
                        sortedEvent.get(data.getValue()).add(data.getKey().toString());
                    }

                    for(Map.Entry<Long, List<String>> item : sortedEvent.entrySet()){

                        final List<String> listKeyEvent = item.getValue();
                        final Long categoryId = item.getKey();
                        final List<String> keyEvents = new ArrayList<>();
                        for(int i=0; i<listKeyEvent.size(); i++){
                            final int finalI = i;
                            ValueEventListener valueEventListener = new ValueEventListener() {
                                     @Override
                                     public void onDataChange(DataSnapshot dataSnapshot) {
                                         Integer statusEvent =  dataSnapshot.getValue(Integer.class);

                                         if(statusEvent == 2)
                                             keyEvents.add(listKeyEvent.get(finalI));

                                         if(finalI == (listKeyEvent.size()-1) && keyEvents.size()>0)
                                             attachTopEvent(categoryId, keyEvents);

                                     }

                                     @Override
                                     public void onCancelled(DatabaseError databaseError) {

                                     }
                                 };
                            mRef.child("events").child("items").child(listKeyEvent.get(i)).child("status").addListenerForSingleValueEvent(valueEventListener);
                        }
                    }

                }else{
                    //show empty events
                    emptyEventView.setVisibility(View.VISIBLE);
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
                List<Category>  listData = dataSnapshot.getValue(genericTypeIndicator);
                for(Category category : listData){
                    listCategories.put(category.getId(), category.getName());
                }
                mRef.child("users").child(userId).child("events").addListenerForSingleValueEvent(eventUserListner);
            }

            @Override
            public void onCancelled(DatabaseError databaseError){

            }
        };

        mRef.child("categories").addListenerForSingleValueEvent(categoriesDataListner);

    }

    @Override
    public void onResume() {
        super.onResume();
        backButton.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onMenuEventClick(ImageView menuEvent, String keyEvent) {

    }

    @Override
    public void onCardEventClick(String keyEvent) {

        //increment the Views Counter
        incrementView(mRef.child("events").child("items").child(keyEvent).child("views"));

        //send intent
        Intent intent = new Intent();
        intent.setClass(this, net.dm73.plainpress.EventDetail.class);
        intent.putExtra("KEYEVENT", keyEvent);
        startActivity(intent);

    }

    private String setLocationName(HashMap<String,Object> location){

        String value="";

        if(location.get("city")!=null)
            value = value.concat(location.get("city").toString());

        if(location.get("country")!=null)
            value = value.concat(", ").concat(location.get("country").toString());


        return value;
    }

    private void attachTopEvent(long categoryId, List<String> keyEvents) {

        LinearLayout.LayoutParams paramsCategoryName = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsCategoryName.setMargins((20 * density), (20 * density), 0, (10 * density));
        TextView categoryName = new TextView(this);
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
        UserEventRecyclerAdapter mAdapter= new UserEventRecyclerAdapter(keyEvents, this, eventsRef, true);
        mAdapter.setMenuEventClickListner(this);

        RecyclerView snappyRecyclerView = new RecyclerView(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
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
