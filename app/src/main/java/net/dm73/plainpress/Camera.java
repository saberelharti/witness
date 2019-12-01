package net.dm73.plainpress;

import android.Manifest;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.desmond.squarecamera.CameraActivity;
import com.desmond.squarecamera.ImageUtility;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;
import net.dm73.plainpress.model.Category;
import net.dm73.plainpress.model.Detail;
import net.dm73.plainpress.model.Event;
import net.dm73.plainpress.model.User;
import net.dm73.plainpress.util.ActivityConfig;
import net.dm73.plainpress.util.AlertDialogMoreDetail;
import net.dm73.plainpress.util.DatePickerFragement;
import net.dm73.plainpress.util.EventUtil;
import net.dm73.plainpress.util.TimePickerFragment;
import net.dm73.plainpress.util.UrlEncoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static net.dm73.plainpress.MainActivity.mRef;
import static net.dm73.plainpress.util.EventUtil.convertDateToTimestampAMPM;


public class Camera extends AppCompatActivity implements AlertDialogMoreDetail.OnAlertDialogResponceListener, CompoundButton.OnCheckedChangeListener, TimePickerFragment.TimeSelectedListner, DatePickerFragement.DateSelectedListner {

    @BindView(R.id.firstMedia)
    CircleImageView firstMedia;
    @BindView(R.id.secondMedia)
    CircleImageView secondMedia;
    @BindView(R.id.thirdMedia)
    CircleImageView thirdMedia;
    @BindView(R.id.addMedia)
    ImageView addMedia;
    @BindView(R.id.deletFirstMedia)
    ImageView deleteFirstMedia;
    @BindView(R.id.deletSecondMedia)
    ImageView deleteSecondMedia;
    @BindView(R.id.deletThirdMedia)
    ImageView deleteThirdMedia;
    @BindView(R.id.goLeftButton)
    ImageView goLeftButton;
    @BindView(R.id.goRightButton)
    ImageView goRightButton;
    @BindView(R.id.categoryChooserPager)
    ViewPager categoriesViewPager;
    @BindView(R.id.nextStep)
    Button publish;

    @BindView(R.id.titleEvent)
    EditText titleField;
    @BindView(R.id.descriptionEvent)
    EditText descriptionField;
    @BindView(R.id.locationEvent)
    EditText locationField;
    @BindView(R.id.timePickerEvent)
    FrameLayout datePicker;
    @BindView(R.id.calendarValue)
    EditText timeField;
    @BindView(R.id.hashTagEvent)
    EditText hashTagField;
    @BindView(R.id.locationContainer)
    FrameLayout locationPicker;
    @BindView(R.id.anonymOption)
    Switch anounymField;
    @BindView(R.id.back_comment)
    ImageView backActivity;

    public static String AVATAR_BYTE = "avatar_byte";
    public static String AVATAR_DETAIL = "avatar_detail";
    private static final int REQUEST_CAMERA = 0;
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private int RC_PHOTO_PICKER = 2;
    private int PLACE_PICKER_REQUEST = 3;
    private int FACE_DETAIL = 4;

    private LatLng locationChoosed;
    private long timeStampEvent = 0;

    private Bitmap[] listMedia;
    private byte[] bitmapAvatar;
    private List<CircleImageView> listMediaViews;
    private List<Category> listCategories;
    private Detail detail;
    private CategoryViewPager mAdapter;
    private boolean isAnounym = false;

    private Bundle extrat;
    private boolean firstOpen = true;
    private int density;
    private Point mSize;

    private User user;

    private SubActionButton cameraSubMenu;
    private SubActionButton galerySubMenu;
    private SubActionButton deleteSubMenu;

    private DatabaseReference newRef;
    private String keyEvent;

    private FirebaseStorage storage;
    private FirebaseDatabase mDatabase;
    private Geocoder mGeoCoder;

    private RequestQueue queue;
    private String urlAddEvent = "/email/event/add";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);

        ActivityConfig.setStatusBarTranslucent(getWindow());

        Display display = getWindowManager().getDefaultDisplay();
        mSize = new Point();
        display.getSize(mSize);
        density = (int) getResources().getDisplayMetrics().density;

        extrat = getIntent().getExtras();
        listMedia = new Bitmap[3];
        listMediaViews = new ArrayList<>();
        listMediaViews.add(firstMedia);
        listMediaViews.add(secondMedia);
        listMediaViews.add(thirdMedia);

        listCategories = new ArrayList<>();
        mGeoCoder = new Geocoder(this, Locale.getDefault());

        FrameLayout.LayoutParams tvParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);

        storage = FirebaseStorage.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        anounymField.setOnCheckedChangeListener(this);

        queue = Volley.newRequestQueue(this);

        //Define the first subAcionMenu
        SubActionButton.Builder itemBuilder = new SubActionButton.Builder(this);
        ImageView itemIcon = new ImageView(this);
        itemIcon.setImageResource(R.drawable.ic_camera);
        itemIcon.setLayoutParams(tvParams);
        itemIcon.setPadding(8, 8, 8, 8);
        cameraSubMenu = itemBuilder.setContentView(itemIcon).build();
        cameraSubMenu.setBackground(ContextCompat.getDrawable(this, R.drawable.background_round_menu_button_add_event));

        //Define the second subActionMenu
        ImageView itemIcon2 = new ImageView(this);
        itemIcon2.setImageResource(R.drawable.ic_galery);
        itemIcon2.setLayoutParams(tvParams);
        itemIcon2.setPadding(8, 8, 8, 8);
        galerySubMenu = itemBuilder.setContentView(itemIcon2).build();
        galerySubMenu.setBackground(ContextCompat.getDrawable(this, R.drawable.background_round_menu_button_add_event));

        //Define the second subActionMenu
        ImageView itemIcon3 = new ImageView(this);
        itemIcon3.setImageResource(R.drawable.ic_delete);
        itemIcon3.setLayoutParams(tvParams);
        itemIcon3.setPadding(8, 8, 8, 8);
        deleteSubMenu = itemBuilder.setContentView(itemIcon3).build();
        deleteSubMenu.setBackground(ContextCompat.getDrawable(this, R.drawable.background_round_menu_button_add_event));

        FloatingActionMenu actionMenu = new FloatingActionMenu.Builder(this)
                .addSubActionView(cameraSubMenu)
                .addSubActionView(galerySubMenu)
                .addSubActionView(deleteSubMenu)
                .attachTo(addMedia)
                .setStartAngle(-30)
                .setEndAngle(80)
                .setRadius(100)
                .build();

        ValueEventListener categorieListner = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<HashMap<String, Object>> categories = (ArrayList<HashMap<String, Object>>) dataSnapshot.getValue();

                listCategories = new ArrayList<>();
                for (int i = 0; i < categories.size(); i++) {
                    if ((boolean) categories.get(i).get("enabled"))
                        listCategories.add(new Category((String) categories.get(i).get("name"), (String) categories.get(i).get("type"), (String) categories.get(i).get("description"), (boolean) categories.get(i).get("enabled"), (String) categories.get(i).get("picture"), (long) categories.get(i).get("id"), true));
                }

                Log.e("size categories", listCategories.size() + "");

                mAdapter = new CategoryViewPager(listCategories);
                categoriesViewPager.setAdapter(mAdapter);
                categoriesViewPager.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return true;
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        };

        mRef.child("categories").addListenerForSingleValueEvent(categorieListner);

        ValueEventListener userValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                user.setId(dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(userValueListener);

        if (firstOpen && extrat.getString(MainActivity.MEDIA_SOURCE).equals(MainActivity.CAMERA)) {
            requestForCameraPermission();
            firstOpen = false;
        }

        if (firstOpen && extrat.getString(MainActivity.MEDIA_SOURCE).equals(MainActivity.GALERY)) {
            openPhotoPicker();
            firstOpen = false;
        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy  hh:mm aa");
        timeField.setText(format.format(calendar.getTime()));

    }

    @Override
    protected void onResume() {
        super.onResume();

        cameraSubMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isMediaFull())
                    requestForCameraPermission();

                addMedia.performClick();
                hideDeleteButtonUI();
            }
        });

        galerySubMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isMediaFull())
                    openPhotoPicker();

                addMedia.performClick();
                hideDeleteButtonUI();
            }
        });

        deleteSubMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isMediaEmpty()) {
                    Toast.makeText(Camera.this, "Nothing to delete !", Toast.LENGTH_SHORT).show();//todo:reformuler le message
                }

                if (deleteButtonVisible()) {
                    hideDeleteButtonUI();
                } else {
                    showDeleteButtonUI();
                }

                addMedia.performClick();
            }
        });

        deleteFirstMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listMedia[0] = null;
                firstMedia.setImageBitmap(null);
                hideNullDeleteButtonUI();

            }
        });

        deleteSecondMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listMedia[1] = null;
                secondMedia.setImageBitmap(null);
                hideNullDeleteButtonUI();

            }
        });

        deleteThirdMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listMedia[2] = null;
                thirdMedia.setImageBitmap(null);
                hideNullDeleteButtonUI();
            }
        });

        goRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (categoriesViewPager.getAdapter() != null && categoriesViewPager.getAdapter().getCount()>0 && categoriesViewPager.getAdapter().getCount() > categoriesViewPager.getCurrentItem())
                    categoriesViewPager.setCurrentItem(categoriesViewPager.getCurrentItem() + 1);
            }
        });

        goLeftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (categoriesViewPager.getAdapter() != null && 0 <= categoriesViewPager.getCurrentItem() - 1)
                    categoriesViewPager.setCurrentItem(categoriesViewPager.getCurrentItem() - 1);
            }
        });

        publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(verifyForm())
                    showMoreDetailDIalog();
            }
        });

        locationPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

                try {
                    startActivityForResult(builder.build(Camera.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCalendarPickerDialog();
            }
        });

        backActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

//    @Override
//    protected void onSaveInstanceState(Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putString("eventTitle", titleField.getText().toString());
//        outState.putString("eventDescription", descriptionField.getEditableText().toString());
//        outState.putLong("eventAt", (long)timeStampEvent);
//        outState.putBoolean("eventAnonym", isAnounym);
//        outState.putStringArrayList("eventHashtags", (ArrayList<String>) getListHashtags());
//        if(locationChoosed!=null)
//        outState.putDoubleArray("eventLocation", new double[]{locationChoosed.latitude, locationChoosed.longitude});
//        if(listMedia[0]!=null)
//            outState.putParcelable("eventFirstBitmap", listMedia[0]);
//        if(listMedia[1]!=null)
//            outState.putParcelable("eventSecondBitmap",listMedia[1]);
//        if(listMedia[2]!=null)
//            outState.putParcelable("eventThirdBitmap", listMedia[2]);
//
//    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

    }

    private void saveEvent() {
        final ProgressDialog loading = ProgressDialog.show(this, "Saving your event", "Please wait...", false, false);

        HashMap<String, Object> userHash = new HashMap<>();
        userHash.put("id", user.getId());
        userHash.put("nickName", user.getNickName());
        userHash.put("photoUrl", (user.getPhotoUrl() != null) ? user.getPhotoUrl() : "");

        Log.e("laltitude", locationChoosed.latitude+"");

        try {
            Event event = new Event(titleField.getText().toString(), descriptionField.getEditableText().toString(), (long) timeStampEvent, isAnounym, getCityNameByCoordinates(locationChoosed.latitude, locationChoosed.longitude), listCategories.get(categoriesViewPager.getCurrentItem()), userHash, getListHashtags(), detail);
            newRef = mDatabase.getReference().child("events").child("items").push();
            keyEvent = newRef.getKey();
            newRef.setValue(event).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(Camera.this, "Event saved !", Toast.LENGTH_LONG).show();
                        loading.dismiss();

                        //send mail to user
                        if(listMediaIsEmpty())
                            sendEmailToUser(user.getAccessToken(), user.getId(), user.getNickName(), user.getEmail(), titleField.getText().toString(), "");

                        if(bitmapAvatar!=null)
                            uploadAttackerAvatar(bitmapAvatar);
                        else if(!listMediaIsEmpty())
                            uploadEventImages(listMedia);
                        else if(bitmapAvatar == null && listMediaIsEmpty())
                            finish();


                    } else {
                        Toast.makeText(Camera.this, "Failed to save Event !", Toast.LENGTH_LONG).show();
                        loading.dismiss();
                    }
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(Camera.this, "Failed to save Event !", Toast.LENGTH_LONG).show();
            loading.dismiss();
        }

        mDatabase.getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("events").child(keyEvent).setValue(listCategories.get(categoriesViewPager.getCurrentItem()).getId());
    }

    private void uploadEventImages(Bitmap[] bitmap) {

        final HashMap<String, Object> media = new HashMap<>();
        final List<String> listImages = new ArrayList<>();
        final List<Bitmap> listBitmap = new ArrayList<>();

        for(Bitmap data : bitmap){
            if(data!=null)
                listBitmap.add(data);
        }

        if(listBitmap.size()>0){

            final ProgressDialog loading = ProgressDialog.show(this, String.format("Uploading the first image ..."), "Please wait...", false, false);
            StorageReference mediaRef = storage.getReference().child(String.format("events/%s/%s_%s.jpg", keyEvent, keyEvent, System.currentTimeMillis()));
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            listBitmap.get(0).compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();


            UploadTask firstUploadTask = mediaRef.putBytes(data);
            firstUploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                    Toast.makeText(Camera.this, "Field to upload first image !", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    listImages.add(taskSnapshot.getDownloadUrl().toString());
                    media.put("images", listImages);
                    newRef.child("media").updateChildren(media);
                    //send mail to user
                    sendEmailToUser(user.getAccessToken(), user.getId(), user.getNickName(), user.getEmail(), titleField.getText().toString(), taskSnapshot.getDownloadUrl().toString());
                }
            }).addOnCompleteListener(this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    loading.dismiss();

                    if(listBitmap.size()>1){
                        final ProgressDialog secondloading = ProgressDialog.show(Camera.this, String.format("Uploading The second Image ..."), "Please wait...", false, false);
                        StorageReference mediaRef = storage.getReference().child(String.format("events/%s/%s_%s.jpg", keyEvent, keyEvent, System.currentTimeMillis()));
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        listBitmap.get(1).compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] data = baos.toByteArray();

                        UploadTask secondUploadTask = mediaRef.putBytes(data);
                        secondUploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                Toast.makeText(Camera.this, "Field to upload second image!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                                listImages.add(taskSnapshot.getDownloadUrl().toString());
                                media.put("images", listImages);
                                newRef.child("media").updateChildren(media);
                            }
                        }).addOnCompleteListener(Camera.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                secondloading.dismiss();

                                if(listBitmap.size()>2){
                                    final ProgressDialog thirdloading = ProgressDialog.show(Camera.this, String.format("Uploading the third image ..."), "Please wait...", false, false);
                                    StorageReference mediaRef = storage.getReference().child(String.format("events/%s/%s_%s.jpg", keyEvent, keyEvent, System.currentTimeMillis()));
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    listBitmap.get(2).compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                    byte[] data = baos.toByteArray();

                                    UploadTask thirdUploadTask = mediaRef.putBytes(data);
                                    thirdUploadTask.addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            // Handle unsuccessful uploads
                                            Toast.makeText(Camera.this, "Field to upload third image !", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                                            listImages.add(taskSnapshot.getDownloadUrl().toString());
                                            media.put("images", listImages);
                                            newRef.child("media").updateChildren(media);
                                        }
                                    }).addOnCompleteListener(Camera.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            thirdloading.dismiss();
                                            finish();
                                        }
                                    });
                                }else{
                                    finish();
                                }
                            }
                        });
                    }else{
                        finish();
                    }

                }
            });

        }

    }

    private void uploadAttackerAvatar(byte[] bitmapByte){

        final HashMap<String, Object> avatar = new HashMap<>();

        StorageReference mediaRef = storage.getReference().child(String.format("events/%s/%s_%s.png", keyEvent, keyEvent, System.currentTimeMillis()));
        final ProgressDialog loading = ProgressDialog.show(this, String.format("Uploading Avatar Image ..."), "Please wait...", false, false);
        UploadTask uploadTask = mediaRef.putBytes(bitmapByte);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                loading.dismiss();
                Toast.makeText(Camera.this, "Field to upload avatar image !", Toast.LENGTH_SHORT).show();
                uploadEventImages(listMedia);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                avatar.put("picture", taskSnapshot.getDownloadUrl().toString());
                newRef.child("detail").updateChildren(avatar);
                loading.dismiss();

                if(!listMediaIsEmpty())
                    uploadEventImages(listMedia);
                else
                    finish();
            }
        });
    }


    private boolean listMediaIsEmpty(){
        for(Bitmap bitmap : listMedia)
            if(bitmap!=null)
                return false;
        return true;
    }

    private void showCalendarPickerDialog() {
        DatePickerFragement newFragment = new DatePickerFragement();
        newFragment.show(this.getFragmentManager(), "datePicker");
    }

    private void showMoreDetailDIalog() {
        AlertDialogMoreDetail newFragment = AlertDialogMoreDetail.newInstance( "Add new event","If there is an attacker, you can report his description by clicking the button \"Describe\". If you want to publish anyway, click the button \"Publish\"");
        newFragment.show(getSupportFragmentManager(), "more detail");
    }

    private boolean verifyForm() {
        boolean valid = true;

        String titleEvent = titleField.getText().toString();
        if (titleEvent.isEmpty()) {
            titleField.setError("Required.");
            valid = false;
        } else {
            titleField.setError(null);
        }

        String descEvent = descriptionField.getText().toString();
        if (descEvent.isEmpty()) {
            descriptionField.setError("Required.");
            valid = false;
        } else {
            descriptionField.setError(null);
        }

        if (locationChoosed == null) {
            locationField.setError("Required.");
            valid = false;
        }else{
            locationField.setError(null);
        }

        String hashtags = hashTagField.getText().toString();
        if(!hashtags.isEmpty() && !hashtags.substring(0,1).equals("#")){
            hashTagField.setError("HashTags mal formed.");
            valid = false;
        }else{
            hashTagField.setError(null);
        }

        dateToTimestamp();
        if (timeStampEvent == 0) {
            timeField.setError("Required.");
            valid = false;
        }else{
            timeField.setError(null);
        }

        if(listCategories==null || listCategories.size()==0)
            valid = false;

        return valid;
    }

    private void dateToTimestamp() {
        if (timeField.getText().toString().isEmpty()) {
            timeStampEvent = 0;
            return;
        } else {
            // you can change format of date
            timeStampEvent = EventUtil.convertDateToTimestamp(timeField.getText().toString());
        }
    }

    private void hideDeleteButtonUI() {
        deleteFirstMedia.setVisibility(View.GONE);
        deleteSecondMedia.setVisibility(View.GONE);
        deleteThirdMedia.setVisibility(View.GONE);
    }

    private void hideNullDeleteButtonUI() {
        if (listMedia[0] == null)
            deleteFirstMedia.setVisibility(View.GONE);
        if (listMedia[1] == null)
            deleteSecondMedia.setVisibility(View.GONE);
        if (listMedia[2] == null)
            deleteThirdMedia.setVisibility(View.GONE);
    }

    private void showDeleteButtonUI() {
        if (listMedia[0] != null)
            deleteFirstMedia.setVisibility(View.VISIBLE);
        if (listMedia[1] != null)
            deleteSecondMedia.setVisibility(View.VISIBLE);
        if (listMedia[2] != null)
            deleteThirdMedia.setVisibility(View.VISIBLE);
    }

    private boolean deleteButtonVisible() {
        if (deleteFirstMedia.getVisibility() == View.GONE && deleteSecondMedia.getVisibility() == View.GONE && deleteThirdMedia.getVisibility() == View.GONE)
            return false;

        return true;
    }

    private void openPhotoPicker() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, false);
        startActivityForResult(Intent.createChooser(photoPickerIntent, "Complete Action Using"), RC_PHOTO_PICKER);
    }

    private void addMedia(Bitmap bitmap) {
        for (int i = 0; i < listMedia.length; i++) {
            if (listMedia[i] == null) {
                listMedia[i] = bitmap;
                listMediaViews.get(i).setImageBitmap(listMedia[i]);
                return;
            }
        }
    }

    private boolean isMediaFull() {
        for (int i = 0; i < listMedia.length; i++)
            if (listMedia[i] == null)
                return false;

        return true;
    }

    private boolean isMediaEmpty() {
        for (int i = 0; i < listMedia.length; i++)
            if (listMedia[i] != null)
                return false;

        return true;
    }

    private void sendEmailToUser(final String accessToken, final String idUser, final String userName, final String  email, final String title, final String image){
        Log.e("url welcom", UrlEncoder.decodedURL(urlAddEvent, idUser, accessToken));
        StringRequest postRequest = new StringRequest(Request.Method.POST, UrlEncoder.decodedURL(urlAddEvent, idUser, accessToken),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(error!=null)
                            Log.e("Response Error",error.toString());
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> header = new HashMap<>();
                header.put("Content-Type", "application/json");
                return header;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                String param;
                if (userName != null && !userName.isEmpty())
                    param = String.format("{\"user\":{\"username\": \"%s\",\"email\": \"%s\"}, \"event\": {\"name\":\"%s\", \"image\":\"%s\"}}", userName, email, title, image);
                else
                    param = String.format("{\"user\":{\"username\": \"PlainPress User\",\"email\": \"%s\"}, \"event\": {\"name\":\"%s\", \"image\":\"%s\"}}", userName, email, title, image);

                Log.e("params", param);

                return  param.getBytes();
            }
        };

        queue.add(postRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        }));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) return;

        if (requestCode == REQUEST_CAMERA) {
            Uri photoUri = data.getData();
            // Get the bitmap in according to the width of the device
            Bitmap bitmap = ImageUtility.decodeSampledBitmapFromPath(photoUri.getPath(), mSize.x, mSize.x);
            addMedia(bitmap);
        }

        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK && data != null) {
            Uri pickedImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), pickedImage);
                addMedia(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                locationChoosed = place.getLatLng();
                locationField.setText(place.getName().toString());
            }
        }

        if(requestCode == FACE_DETAIL){
            if(resultCode == RESULT_OK){
                bitmapAvatar = data.getByteArrayExtra(AVATAR_BYTE);
                detail = mapToDetail((HashMap<String, Object>) data.getSerializableExtra(AVATAR_DETAIL));
                Toast.makeText(Camera.this, "bitmap geted succesfuly", Toast.LENGTH_SHORT).show();
                saveEvent();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private net.dm73.plainpress.model.Location getCityNameByCoordinates(double lat, double lon) throws IOException {

        List<Address> addresses = mGeoCoder.getFromLocation(lat, lon, 1);
        if (addresses != null && addresses.size() > 0) {
            return new net.dm73.plainpress.model.Location((addresses.get(0).getLocality()!=null) ? addresses.get(0).getLocality().toString() : "", (addresses.get(0).getCountryName()!=null) ? addresses.get(0).getCountryName().toString() : "", lat, lon);
        }
        return null;
    }

    private List<String> getListHashtags(){

        if(hashTagField.getText().toString().isEmpty())
            return null;

        List<String> listHashtags = Arrays.asList(hashTagField.getText().toString().split("#"));
        listHashtags = new ArrayList<>(listHashtags);

        for (String hashtag : new ArrayList<>(listHashtags)) {
            if(hashtag.trim().isEmpty())
                listHashtags.remove(hashtag);
        }

        return listHashtags;
    }

    private Detail mapToDetail(HashMap<String,Object> mapDetail){
        Detail detail = new Detail();

        if(mapDetail.containsKey("face"))
            detail.setFace((HashMap<String, String>) mapDetail.get("face"));

        if(mapDetail.containsKey("eyes"))
            detail.setEyes((HashMap<String, String>) mapDetail.get("eyes"));

        if(mapDetail.containsKey("beard"))
            detail.setBeard((HashMap<String, String>) mapDetail.get("beard"));

        if(mapDetail.containsKey("hair"))
            detail.setHair((HashMap<String, String>) mapDetail.get("hair"));

        if(mapDetail.containsKey("mouth"))
            detail.setMouth((HashMap<String, String>) mapDetail.get("mouth"));

        if(mapDetail.containsKey("nose"))
            detail.setNose((HashMap<String, String>) mapDetail.get("nose"));

        if(mapDetail.containsKey("moustache"))
            detail.setMoustache((HashMap<String, String>) mapDetail.get("moustache"));

        if(mapDetail.containsKey("eyebrows"))
            detail.setEyes((HashMap<String, String>) mapDetail.get("eyebrows"));

        if(mapDetail.containsKey("picture"))
            detail.setPicture(mapDetail.get("picture").toString());

        if(mapDetail.containsKey("gender"))
            detail.setGender((boolean)mapDetail.get("gender"));

        return detail;
    }


    public void requestForCameraPermission() {
        final String permission = Manifest.permission.CAMERA;
        if (ContextCompat.checkSelfPermission(Camera.this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(Camera.this, permission)) {
                showPermissionRationaleDialog("Test", permission);
            } else {
                requestForPermission(permission);
            }
        } else {
            launch();
        }
    }

    private void showPermissionRationaleDialog(final String message, final String permission) {
        new AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Camera.this.requestForPermission(permission);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create()
                .show();
    }

    private void requestForPermission(final String permission) {
        ActivityCompat.requestPermissions(Camera.this, new String[]{permission}, REQUEST_CAMERA_PERMISSION);
    }

    private void launch() {
        Intent startCustomCameraIntent = new Intent(this, CameraActivity.class);
        startActivityForResult(startCustomCameraIntent, REQUEST_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CAMERA_PERMISSION:
                final int numOfRequest = grantResults.length;
                final boolean isGranted = numOfRequest == 1
                        && PackageManager.PERMISSION_GRANTED == grantResults[numOfRequest - 1];
                if (isGranted) {
                    launch();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onAlertDialogResponceListening(boolean valid) {
        if (valid) {
            Intent intent = new Intent(this, FaceDetail.class);
            if(detail!=null)
                intent.putExtra("UPDATE", detail);
            startActivityForResult(intent, FACE_DETAIL);
            Log.e("moredetail", "true");
        } else {
            //save the event
            saveEvent();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(isChecked) {
            isAnounym = true;
        } else {
            isAnounym = false;
        }
    }

    @Override
    public void onTimeSelected(String time) {
        timeStampEvent = EventUtil.convertDateToTimestamp(timeField.getText().toString().concat(time));
        timeField.setText(convertDateToTimestampAMPM(timeField.getText().toString().concat(time)));
    }

    @Override
    public void onDateSelected(String date) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(this.getFragmentManager(), "timerPicker");
        timeField.setText(date);
    }


    class CategoryViewPager extends PagerAdapter {

        private List<Category> listCategories;

        public CategoryViewPager(List<Category> listCategories) {
            this.listCategories = listCategories;
        }

        @Override
        public int getCount() {
            return (listCategories != null) ? listCategories.size() : 0;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {

            ImageView categoryImage = new ImageView(Camera.this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(density * 80, density * 80);
            categoryImage.setLayoutParams(params);
            Glide.with(Camera.this)
                    .load(listCategories.get(position).getPicture())
                    .fitCenter()
                    .into(categoryImage);

            container.addView(categoryImage);

            return categoryImage;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((ImageView) object);
        }

    }
}
