package net.dm73.plainpress;


import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.flexbox.FlexboxLayout;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.luseen.spacenavigation.SpaceItem;
import com.luseen.spacenavigation.SpaceNavigationView;
import com.luseen.spacenavigation.SpaceOnClickListener;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;
import net.dm73.plainpress.dao.DaoPreference;
import net.dm73.plainpress.fragment.MapEvents;
import net.dm73.plainpress.fragment.Search;
import net.dm73.plainpress.fragment.UserProfile;
import net.dm73.plainpress.fragment.WallEvent;
import net.dm73.plainpress.model.User;
import net.dm73.plainpress.util.ActivityConfig;
import net.dm73.plainpress.util.AlertDialogVerifyEmail;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity implements AlertDialogVerifyEmail.OnVerifyEmailDialog {

    @BindView(R.id.space)
    SpaceNavigationView spaceNavigationView;
    @BindView(R.id.categoryContainer)
    FlexboxLayout categoryContainer;
    @BindView(R.id.navigationUserEmail)
    TextView userEmail;
    @BindView(R.id.nearbySeekBar)
    SeekBar nearByseekBar;
    @BindView(R.id.progressValue)
    TextView progressValue;
    @BindView(R.id.saveSettingsButton)
    Button saveSettingsButton;
    @BindView(R.id.signOut)
    ImageView signOut;
    @BindView(R.id.trackMe)
    Switch trackMe;
    @BindView(R.id.navUserImgae)
    ImageView userImageProfile;
    static public DrawerLayout mDrawerLayout;
    @BindView(R.id.centerButton)
    Button imageView;

    public static String MEDIA_SOURCE = "media_source";
    public static String CAMERA = "camera";
    public static String GALERY = "galery";

    private List<Fragment> fragments = new ArrayList<>();

    public static DatabaseReference mRef;
    public FirebaseAuth mAuth;
    public static User user;

    public static Location userDefaultLocation;

    private DaoPreference daoPreference;
    private PopupWindow popupGuide;

    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    public static GoogleApiClient mGoogleApiClient;
    private static final int ACCESS_FINE_LOCATION_INTENT_ID = 3;
    private static final String BROADCAST_ACTION = "android.location.PROVIDERS_CHANGED";

    private static ActionBarDrawerToggle mDrawerToggle;

    private SubActionButton cameraSubMenu;
    private SubActionButton galerySubMenu;
    private FloatingActionMenu actionMenu;

    private HashMap<Long, Integer> categoriesMemory = new HashMap<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        ActivityConfig.setStatusBarTranslucent(getWindow());


//        if (!persisteData) {
//            persisteData = true;
//            FirebaseDatabase.getInstance().setPersistenceEnabled(true);//todo:make this call for all the app
//        }

        mRef = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        //set up the navigation bar
        setUpNavigationBar(savedInstanceState);
        buildFragmentsList();

        // Set the 0th Fragment to be displayed by default.
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame_fragment, fragments.get(0)).commit();
        spaceNavigationView.changeCurrentItem(0);


        FrameLayout.LayoutParams tvParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        tvParams.setMargins(10, 10, 10, 10);

        //Define the first subAcionMenu
        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);
        ImageView itemIcon = new ImageView(this);
        itemIcon.setImageResource(R.drawable.ic_camera);
        itemIcon.setLayoutParams(tvParams);
        cameraSubMenu = itemBuilder.setContentView(itemIcon).build();
        cameraSubMenu.setBackground(ContextCompat.getDrawable(this, R.drawable.background_round_menu_button));

        //Define the second subActionMenu
        ImageView itemIcon2 = new ImageView(this);
        itemIcon2.setImageResource(R.drawable.ic_galery);
        itemIcon2.setLayoutParams(tvParams);
        galerySubMenu = itemBuilder.setContentView(itemIcon2).build();
        galerySubMenu.setBackground(ContextCompat.getDrawable(this, R.drawable.background_round_menu_button));

        actionMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(cameraSubMenu)
                .addSubActionView(galerySubMenu)
                .attachTo(imageView)
                .setStartAngle(220)
                .setEndAngle(320)
                .setRadius(150)
                .build();

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.string.drawer_open,  /* "open drawer" description */
                R.string.drawer_close  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        daoPreference = new DaoPreference(this);


    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }


    @Override
    protected void onStart() {
        super.onStart();

        if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified())
            FirebaseAuth.getInstance().getCurrentUser().reload();

        spaceNavigationView.setSpaceOnClickListener(new SpaceOnClickListener() {
            @Override
            public void onCentreButtonClick() {
                if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified())
                    imageView.performClick();
                else{
                    FirebaseAuth.getInstance().getCurrentUser().reload();
                    AlertDialogVerifyEmail newFragment = new AlertDialogVerifyEmail();
                    newFragment.show(getSupportFragmentManager(), "alert");
                }
            }

            @Override
            public void onItemClick(int itemIndex, String itemName) {
                Log.e("item bottomnav", itemName + " " + itemIndex);
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.frame_fragment, fragments.get(itemIndex)).commit();
            }

            @Override
            public void onItemReselected(int itemIndex, String itemName) {

            }
        });


        final ValueEventListener categorieListner = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<Object> td = (ArrayList<Object>) dataSnapshot.getValue();
                List<Long> preferedCategories = (List<Long>) user.getSettings().get("categories");

                categoryContainer.removeAllViews();

                for (int i = 0; i < td.size(); i++) {
                    if (td.get(i) != null) {
                        Long categoryId = (Long)((HashMap<String, Object>) td.get(i)).get("id");
                        View categorie = LayoutInflater.from(getApplicationContext()).inflate(R.layout.grid_category, null);
                        final TextView textView = (TextView) categorie.findViewById(R.id.gridCategoryTitle);
                        textView.setText(((HashMap<String, String>) td.get(i)).get("name"));
                        if (preferedCategories!=null && preferedCategories.contains(categoryId)) {
                            textView.setBackgroundResource(R.drawable.background_category_nav_ac);
                            categoriesMemory.put(categoryId, 1);
                        } else {
                            textView.setBackgroundResource(R.drawable.background_category_nav_desac);
                            categoriesMemory.put(categoryId, 0);
                        }

                        categoryContainer.addView(categorie);

                        final Long j = categoryId;
                        textView.setOnClickListener(new View.OnClickListener() {

                            @Override
                            public void onClick(View v) {
                                if ( categoriesMemory.get(j) == 1) {
                                    categoriesMemory.put(j, 0);
                                    textView.setBackgroundResource(R.drawable.background_category_nav_desac);
                                } else if (categoriesMemory.get(j) == 0) {
                                    categoriesMemory.put(j, 1);
                                    textView.setBackgroundResource(R.drawable.background_category_nav_ac);
                                }
                                Log.e("categorieMermory", categoriesMemory.toString());
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


        ValueEventListener userProfileListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);

                if (user != null) {
                    userEmail.setText(user.getEmail());
                    nearByseekBar.setProgress(new BigDecimal((Long)user.getSettings().get("nearby")).intValueExact());
                    progressValue.setText(String.valueOf(new BigDecimal((Long)user.getSettings().get("nearby")).intValueExact()));

                    if(!user.isUserPhotoProfilEmpty()){
                        Glide.with(getApplicationContext())
                                .load(user.getPhotoUrl())
                                .centerCrop()
                                .error(R.drawable.ic_avatar_m)
                                .into(userImageProfile);
                    }

                    if (userDefaultLocation == null) {
                        userDefaultLocation = new Location(LocationManager.GPS_PROVIDER);
                        userDefaultLocation.setLatitude((double) user.getLocation().get("latitude"));
                        userDefaultLocation.setLongitude((double) user.getLocation().get("longitude"));
                        Log.e("muserLocation", userDefaultLocation.toString());
                    }

                    trackMe.setChecked((boolean)user.getSettings().get("trackMe"));

                    mRef.child("categories").addListenerForSingleValueEvent(categorieListner);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addValueEventListener(userProfileListener);


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        spaceNavigationView.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        registerReceiver(gpsLocationReceiver, new IntentFilter(BROADCAST_ACTION));//Register broadcast receiver to check the status of GPS
        if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified())
            FirebaseAuth.getInstance().getCurrentUser().reload();

        nearByseekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressValue.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        saveSettingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
                if(!isNetworkAvailable()){
                    showAlertDialog("We loose the connection! Your changes will be considered in the publish connection.", 0);
                }
            }
        });

        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                finish();
            }
        });

        cameraSubMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent().setClass(MainActivity.this, Camera.class).putExtra(MEDIA_SOURCE, CAMERA));
            }
        });

        galerySubMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent().setClass(MainActivity.this, Camera.class).putExtra(MEDIA_SOURCE, GALERY));
            }
        });

        if(daoPreference.isStarterGuideWatched() == 0 && popupGuide ==null)
            showStarterGuide();

    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //Unregister receiver on destroy
//        if (gpsLocationReceiver != null)
//            unregisterReceiver(gpsLocationReceiver);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Pass the event to ActionBarDrawerToggle, if it returns
        // true, then it has handled the app icon touch event
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        // Handle your other action bar items...
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        Fragment f = getSupportFragmentManager().findFragmentById(R.id.frame_fragment);

        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case RESULT_OK:
                        Log.e("Settings", "Result OK");

                        if (f instanceof WallEvent)
                            ((WallEvent) f).getLastLocation();

                        if (f instanceof MapEvents)
                            ((MapEvents) f).getLastLocation();

                        if (f instanceof Search)
                            ((Search) f).getLastLocation();

                        break;
                    case RESULT_CANCELED:
                        Log.e("Settings", "Result Cancel");

                        if (f instanceof WallEvent)
                            ((WallEvent) f).queryGeofireTo(userDefaultLocation);

                        if (f instanceof MapEvents)
                            ((MapEvents) f).queryGeofireTo(userDefaultLocation);

                        if (f instanceof Search)
                            ((Search) f).ChangeLocationTo(userDefaultLocation);

                        break;
                }
                break;
        }
    }

    public static void switchFragment(Fragment fragment, String tag, FragmentManager supportFragmentManager) {
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.frame_fragment, fragment, tag)
                .commit();
    }

    private void setUpNavigationBar(Bundle savedInstanceState) {
        spaceNavigationView = (SpaceNavigationView) findViewById(R.id.space);
        spaceNavigationView.initWithSaveInstanceState(savedInstanceState);
        spaceNavigationView.showIconOnly();
        spaceNavigationView.addSpaceItem(new SpaceItem("EVENT", R.drawable.ic_home));
        spaceNavigationView.addSpaceItem(new SpaceItem("SEARCH", R.drawable.ic_nav_search));
        spaceNavigationView.addSpaceItem(new SpaceItem("MAPS", R.drawable.ic_maps));
        spaceNavigationView.addSpaceItem(new SpaceItem("PROFIL", R.drawable.ic_profil));
    }

    private void buildFragmentsList() {
        fragments.add(new WallEvent());
        fragments.add(new Search());
        fragments.add(new MapEvents());
        fragments.add(new UserProfile());
    }

    //save settings
    private void saveSettings() {
        HashMap<String, Object> settings = new HashMap<>();
        List<Long> choosedCategories = new ArrayList<>();
        for (Map.Entry category : categoriesMemory.entrySet()) {
            if ((int) category.getValue() == 1) {
                choosedCategories.add((long)category.getKey());
            }
        }
        settings.put("categories", choosedCategories);
        settings.put("nearby", nearByseekBar.getProgress());
        settings.put("trackMe", trackMe.isChecked());
        mRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("settings").updateChildren(settings, new DatabaseReference.CompletionListener() {

            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if(databaseError == null){
                    Toast.makeText(MainActivity.this, "Your modification has been submited", Toast.LENGTH_SHORT).show();
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                }else{
                    showAlertDialog("Sorry, an internal error has occurred.", 1);
                }
            }


        });
    }

    private void showAlertDialog(String messafeText, int errorType) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if(errorType == 0){
            builder.setMessage(messafeText);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mDrawerLayout.closeDrawer(Gravity.LEFT);
                }
            });
        }

        if(errorType == 1){
            builder.setMessage(messafeText);
            builder.setPositiveButton("RETRY", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
        }

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // email sent
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("Email verification sent, please check your mailbox !");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }else{
                            // email not sent, so display message and restart the activity or do whatever you wish to do
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setMessage("An internal Error occurred during sending the email verification, please try again !");
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            AlertDialog dialog = builder.create();
                            dialog.show();
                        }
                    }
                });
    }


    @Override
    public void onVerifyEmailDialogResponse(boolean verify) {
        if(verify){
            sendVerificationEmail();
        }
    }

    private void showStarterGuide(){
        final int[] i = {1};

        final View popupView = getLayoutInflater().inflate(R.layout.poupup_starter_guide, null, false);
        popupGuide = new PopupWindow(popupView, RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        Button next = (Button) popupView.findViewById(R.id.nextButton);
        Button skip = (Button) popupView.findViewById(R.id.skipStarterGuide);

        //updtae the font
        next.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/myriad_pro_regular.ttf"));
        skip.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/myriad_pro_regular.ttf"));
        ((TextView) popupView.findViewById(R.id.textView1)).setTypeface(Typeface.createFromAsset(getAssets(), "fonts/myriad_pro_regular.ttf"));
        ((TextView) popupView.findViewById(R.id.textView2)).setTypeface(Typeface.createFromAsset(getAssets(), "fonts/myriad_pro_regular.ttf"));
        ((TextView) popupView.findViewById(R.id.textView3)).setTypeface(Typeface.createFromAsset(getAssets(), "fonts/myriad_pro_regular.ttf"));
        ((TextView) popupView.findViewById(R.id.textView4)).setTypeface(Typeface.createFromAsset(getAssets(), "fonts/myriad_pro_regular.ttf"));
        ((TextView) popupView.findViewById(R.id.textView5)).setTypeface(Typeface.createFromAsset(getAssets(), "fonts/myriad_pro_regular.ttf"));
        ((TextView) popupView.findViewById(R.id.textView6)).setTypeface(Typeface.createFromAsset(getAssets(), "fonts/myriad_pro_regular.ttf"));

        //handler for show popup
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                popupGuide.showAtLocation(findViewById(R.id.mainContent), Gravity.CENTER,0,0);
            }
        }, 100l);

        //set next button work
        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                switch (i[0]){
                    case 1:
                        popupView.findViewById(R.id.imageView1).setVisibility(View.GONE);
                        popupView.findViewById(R.id.textView1).setVisibility(View.GONE);
                        popupView.findViewById(R.id.imageView2).setVisibility(View.VISIBLE);
                        popupView.findViewById(R.id.textView2).setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        popupView.findViewById(R.id.imageView2).setVisibility(View.GONE);
                        popupView.findViewById(R.id.textView2).setVisibility(View.GONE);
                        popupView.findViewById(R.id.imageView3).setVisibility(View.VISIBLE);
                        popupView.findViewById(R.id.textView3).setVisibility(View.VISIBLE);
                        break;
                    case 3:
                        popupView.findViewById(R.id.imageView3).setVisibility(View.GONE);
                        popupView.findViewById(R.id.textView3).setVisibility(View.GONE);
                        popupView.findViewById(R.id.imageView4).setVisibility(View.VISIBLE);
                        popupView.findViewById(R.id.textView4).setVisibility(View.VISIBLE);
                        break;
                    case 4:
                        popupView.findViewById(R.id.imageView4).setVisibility(View.GONE);
                        popupView.findViewById(R.id.textView4).setVisibility(View.GONE);
                        popupView.findViewById(R.id.imageView5).setVisibility(View.VISIBLE);
                        popupView.findViewById(R.id.textView5).setVisibility(View.VISIBLE);
                        break;
                    case 5:
                        popupView.findViewById(R.id.imageView5).setVisibility(View.GONE);
                        popupView.findViewById(R.id.textView5).setVisibility(View.GONE);
                        popupView.findViewById(R.id.imageView6).setVisibility(View.VISIBLE);
                        popupView.findViewById(R.id.textView6).setVisibility(View.VISIBLE);
                        ((Button)popupView.findViewById(R.id.nextButton)).setText("Close");
                        break;
                    default:
                        daoPreference.saveStarterGuideDisplayed();
                        popupGuide.dismiss();
                        break;
                }
                i[0]++;
            }
        });

       skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                daoPreference.saveStarterGuideDisplayed();
                popupGuide.dismiss();
            }
        });
    }


}
