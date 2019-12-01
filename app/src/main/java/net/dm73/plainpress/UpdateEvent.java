package net.dm73.plainpress;

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
import android.text.TextUtils;
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
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
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
import net.dm73.plainpress.model.Location;
import net.dm73.plainpress.model.User;
import net.dm73.plainpress.util.ActivityConfig;
import net.dm73.plainpress.util.AlertDialogMoreDetail;
import net.dm73.plainpress.util.DatePickerFragement;
import net.dm73.plainpress.util.EventUtil;
import net.dm73.plainpress.util.TimePickerFragment;
import net.dm73.plainpress.util.UrlEncoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

import static net.dm73.plainpress.Camera.AVATAR_BYTE;
import static net.dm73.plainpress.Camera.AVATAR_DETAIL;
import static net.dm73.plainpress.MainActivity.mRef;
import static net.dm73.plainpress.util.EventUtil.convertDateToTimestampAMPM;
import static net.dm73.plainpress.util.EventUtil.getLocationName;

public class UpdateEvent extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, AlertDialogMoreDetail.OnAlertDialogResponceListener, TimePickerFragment.TimeSelectedListner, DatePickerFragement.DateSelectedListner {

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


    private final int OLD = 0;
    private final int NEW = 1;

    private String oldTitleField;
    private String oldDescriptionField;
    private String oldLocationField;
    private boolean oldAnounymField;
    private LatLng oldLocationChoosed;
    private List<String> oldHashTagField;
    private long oldTimeStampEvent = 0;
    private long oldCategoryChoosed = 0;
    public static Detail oldDetail;

    private static final int REQUEST_CAMERA = 0;
    private static final int REQUEST_CAMERA_PERMISSION = 1;

    private int RC_PHOTO_PICKER = 2;
    private int PLACE_PICKER_REQUEST = 3;
    private int FACE_DETAIL = 4;

    private Object[] listMedia;
    private String[] listOldMediaUrl;
    private byte[] bitmapAvatar;
    private List<CircleImageView> listMediaViews;
    private List<Category> listCategories;
    private HashMap<String, Object> detail;
    private UpdateEvent.CategoryViewPager mAdapter;
    private boolean isAnounym = false;

    private Bundle extrat;
    private int density;
    private Point mSize;

    private User user;
    private Event event;

    private LatLng locationChoosed;
    private long timeStampEvent = 0;

    private RequestQueue queue;
    private String urlUpdateEvent = "/email/event/comment";

    private SubActionButton cameraSubMenu;
    private SubActionButton galerySubMenu;
    private SubActionButton deleteSubMenu;

    private DatabaseReference newRef;
    private String keyEvent;

    private FirebaseStorage storage;
    private FirebaseDatabase mDatabase;
    private Geocoder mGeocoder;
    private boolean bitmapDeleted;

    private boolean mailNotSended = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_event);
        ButterKnife.bind(this);

        ActivityConfig.setStatusBarTranslucent(getWindow());

        Display display = getWindowManager().getDefaultDisplay();
        mSize = new Point();
        display.getSize(mSize);
        density = (int) getResources().getDisplayMetrics().density;

        extrat = getIntent().getExtras();
        listMedia = new Object[3];
        listOldMediaUrl = new String[3];
        listMediaViews = new ArrayList<>();
        listMediaViews.add(firstMedia);
        listMediaViews.add(secondMedia);
        listMediaViews.add(thirdMedia);

        keyEvent = extrat.getString("KEYEVENT");//intent will content the key of the event

        listCategories = new ArrayList<>();
        mGeocoder = new Geocoder(this, Locale.getDefault());

        bitmapDeleted = false;

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

                Toast.makeText(getApplicationContext(), "hi", Toast.LENGTH_LONG).show();

            }
        };

        mRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(userValueListener);

        ValueEventListener evenListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                event = dataSnapshot.getValue(Event.class);
                List<String> images;
                oldTitleField = event.getTitle();
                titleField.setText(oldTitleField);
                oldDescriptionField = event.getDescription();
                descriptionField.setText(oldDescriptionField);
                oldLocationField = getLocationName(event.getLocation());
                locationField.setText(oldLocationField);
                oldAnounymField = event.isAnonymous();
                anounymField.setChecked(oldAnounymField);
                oldLocationChoosed = new LatLng(event.getLocation().getLatitude(), event.getLocation().getLongitude());
                locationChoosed = new LatLng(event.getLocation().getLatitude(), event.getLocation().getLongitude());
                oldHashTagField = event.getHashtags();
                hashTagField.setText(getHashtags(oldHashTagField));
                oldTimeStampEvent = event.getEventAt();
                timeStampEvent = event.getEventAt();
                timeField.setText(EventUtil.convertDateToLocalTimeZine(event.getEventAt()));
                oldCategoryChoosed = event.getCategory().getId();
                categoriesViewPager.setCurrentItem((int)oldCategoryChoosed - 1);
                oldDetail = event.getDetail();

                if (event.getMedia() != null && event.getMedia().getImages()!=null) {
                    images = event.getMedia().getImages();
                    for (int i = 0; i < images.size(); i++) {
                        final int finalI = i;
                        listOldMediaUrl[i] = images.get(i);
                        Glide.with(getApplicationContext())
                                .load(images.get(i))
                                .asBitmap()
                                .centerCrop()
                                .into(new SimpleTarget<Bitmap>() {
                                    @Override
                                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                        listMediaViews.get(finalI).setImageBitmap(resource);
                                        listMedia[finalI] = new Object[]{resource, OLD};
                                    }
                                });//todo:clear loading this Glide
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        mRef.child("events").child("items").child(keyEvent).addListenerForSingleValueEvent(evenListener);

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
                    Toast.makeText(UpdateEvent.this, "Nothing to delete !", Toast.LENGTH_SHORT).show();//todo:reformuler le message
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
                if(listMedia[0] != null) {
                    listMedia[0] = null;
                    listOldMediaUrl[0] = "";
                    firstMedia.setImageBitmap(null);
                    hideNullDeleteButtonUI();
                    bitmapDeleted = true;
                }

            }
        });

        deleteSecondMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listMedia[1] != null) {
                    listMedia[1] = null;
                    listOldMediaUrl[1] = "";
                    secondMedia.setImageBitmap(null);
                    hideNullDeleteButtonUI();
                    bitmapDeleted = true;
                }
            }
        });

        deleteThirdMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(listMedia[2] != null) {
                    listMedia[2] = null;
                    listOldMediaUrl[2] = "";
                    thirdMedia.setImageBitmap(null);
                    hideNullDeleteButtonUI();
                    bitmapDeleted = true;
                }
            }
        });

        goRightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (categoriesViewPager.getAdapter() != null && categoriesViewPager.getAdapter().getCount() > 0 && categoriesViewPager.getAdapter().getCount() > categoriesViewPager.getCurrentItem())
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
                if(!verifyForm())
                    return;
                showMoreDetailDIalog();
//                if (!verifyForm())
//                    return;
//
//                if (checkModificatedField()) {
//
////                    if (hasDetail((int) listCategories.get(categoriesViewPager.getCurrentItem()).getId())) {
////                        Log.e("category name", listCategories.get(categoriesViewPager.getCurrentItem()).getName());
////                        showMoreDetailDIalog();
////                    } else {
////                        updateEvent();
////                    }
//                }else{
//                    Toast.makeText(getApplicationContext(), "you dont make any update !", Toast.LENGTH_SHORT).show();
//                }
            }
        });

        locationPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(UpdateEvent.this), PLACE_PICKER_REQUEST);
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
    protected void onDestroy() {
        super.onDestroy();
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

        if(requestCode == FACE_DETAIL){
            if(resultCode == RESULT_OK){
                bitmapAvatar = data.getByteArrayExtra(AVATAR_BYTE);
                detail = (HashMap<String, Object>) data.getSerializableExtra(AVATAR_DETAIL);
                Toast.makeText(UpdateEvent.this, "bitmap geted succesfuly", Toast.LENGTH_SHORT).show();
                updateEvent();
            }
        }

        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlacePicker.getPlace(data, this);
                locationChoosed = place.getLatLng();
                locationField.setText(place.getName().toString());
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void updateEvent() {

        HashMap<String, Object> edit = getModificatedField();
        if(edit.size() != 0){
            final ProgressDialog loading = ProgressDialog.show(this, "Saving your event", "Please wait...", false, false);

            if(event.getStatus() != 2)
                newRef = mDatabase.getReference().child("events").child("items").child(keyEvent);
            else{
                mRef.child("events").child("items").child(keyEvent).child("_needsMod").setValue(true);
                newRef = mRef.child("events").child("items").child(keyEvent).child("edit");
            }

            //save the updated event
            newRef.updateChildren(edit).addOnCompleteListener(new OnCompleteListener<Void>() {

                boolean emailNotSended = true;

                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(UpdateEvent.this, "Event updtaed !", Toast.LENGTH_LONG).show();
                        loading.dismiss();

                        Log.e("bpAvatar & newbpAdded", bitmapAvatar +" & "+ newBitmapAdded());

                        if (bitmapAvatar != null) {
                            uploadAttackerAvatarUpdate(bitmapAvatar);
//                            sendEmailToUser();
                        }else if (newBitmapAdded())
                            uploadEventImagesUpdate(listMedia);
                        else if (bitmapDeleted){
                            //edit image array if an image deleted
                            HashMap<String, Object> media = new HashMap<>();
                            List<String> listMedia = new ArrayList<>();
                            for(int i = 0; i< listOldMediaUrl.length; i++){
                                if(listOldMediaUrl[i] !=null && !listOldMediaUrl[i].isEmpty()){
                                    listMedia.add(listOldMediaUrl[i]);
                                }
                            }
                            media.put("images", listMedia);
                            newRef.child("media").updateChildren(media).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    finish();
                                }
                            });
                        }

                    } else {
                        Toast.makeText(UpdateEvent.this, "Failed to save Event !", Toast.LENGTH_LONG).show();
                        loading.dismiss();
                    }
                }
            });

        }else{

            if(event.getStatus() != 2)
                newRef  =  mDatabase.getReference().child("events").child("items").child(keyEvent);
            else{
                mRef.child("events").child("items").child(keyEvent).child("_needsMod").setValue(true);
                newRef = mRef.child("events").child("items").child(keyEvent).child("edit");
            }

            if (bitmapDeleted) {
                //edit image array if an image deleted
                HashMap<String, Object> media = new HashMap<>();
                List<String> listMedia = new ArrayList<>();
                for (int i = 0; i < listOldMediaUrl.length; i++) {
                    if (listOldMediaUrl[i] != null && !listOldMediaUrl[i].isEmpty()) {
                        listMedia.add(listOldMediaUrl[i]);
                    }
                }
                media.put("images", listMedia);
                newRef.child("media").updateChildren(media).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(bitmapAvatar == null)
                            finish();
                    }
                });
            }

            if (bitmapAvatar != null)
                uploadAttackerAvatarUpdate(bitmapAvatar);
            else if (newBitmapAdded())
                uploadEventImagesUpdate(listMedia);
        }
    }

    private void uploadAttackerAvatarUpdate(final byte[] bitmapByte) {

        final HashMap<String, Object> avatar = new HashMap<>();

        StorageReference mediaRef = storage.getReference().child(String.format("events/%s/%s_%s.png", keyEvent, keyEvent, System.currentTimeMillis()));
        final ProgressDialog loading = ProgressDialog.show(this, String.format("Uploading Avatar Image ..."), "Please wait...", false, false);
        UploadTask uploadTask = mediaRef.putBytes(bitmapByte);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                loading.dismiss();
                Toast.makeText(UpdateEvent.this, "Field to upload avatar image !", Toast.LENGTH_SHORT).show();
                if(newBitmapAdded())
                    uploadEventImagesUpdate(listMedia);
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                avatar.put("picture", taskSnapshot.getDownloadUrl().toString());
                newRef.child("detail").updateChildren(avatar);

                if (newBitmapAdded())
                    uploadEventImagesUpdate(listMedia);
                else if(bitmapDeleted){
                    //edit image array if an image deleted
                    HashMap<String, Object> media = new HashMap<>();
                    List<String> listMedia = new ArrayList<>();
                    for(int i = 0; i< listOldMediaUrl.length; i++){
                        if(listOldMediaUrl[i] !=null && !listOldMediaUrl[i].isEmpty()){
                            listMedia.add(listOldMediaUrl[i]);
                        }
                    }
                    media.put("images", listMedia);
                    newRef.child("media").updateChildren(media).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            finish();
                        }
                    });
                }else
                    finish();
            }
        });
    }

    private void uploadEventImagesUpdate(Object[] bitmap) {

        final HashMap<String, Object> media = new HashMap<>();
        final List<String> listUrlImages = new ArrayList<>();
        final List<Bitmap> listBitmap = new ArrayList<>();

        for(int i = 0; i< listOldMediaUrl.length; i++){
            if(listOldMediaUrl[i] !=null && !listOldMediaUrl[i].isEmpty()){
                listUrlImages.add(listOldMediaUrl[i]);
            }
        }

        for (int i=0; i<3; i++) {
            if (bitmap[i] != null && (int)((Object[])bitmap[i])[1] == NEW)
                listBitmap.add((Bitmap)((Object[])bitmap[i])[0]);
        }

        Log.e("listBitmap", listBitmap.toString());
        Log.e("listoldUrl", listUrlImages.toString());

        if (listBitmap.size() > 0){

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
                    Toast.makeText(UpdateEvent.this, "Field to upload first image !", Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    listUrlImages.add(taskSnapshot.getDownloadUrl().toString());
                    media.put("images", listUrlImages);
                    newRef.child("media").setValue(media);
                }
            }).addOnCompleteListener(this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    loading.dismiss();

                    if (listBitmap.size() > 1) {
                        final ProgressDialog secondloading = ProgressDialog.show(UpdateEvent.this, String.format("Uploading The second Image ..."), "Please wait...", false, false);
                        StorageReference mediaRef = storage.getReference().child(String.format("events/%s/%s_%s.jpg", keyEvent, keyEvent, System.currentTimeMillis()));
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        listBitmap.get(1).compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] data = baos.toByteArray();

                        UploadTask secondUploadTask = mediaRef.putBytes(data);
                        secondUploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                Toast.makeText(UpdateEvent.this, "Field to upload second image!", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                                listUrlImages.add(taskSnapshot.getDownloadUrl().toString());
                                media.put("images", listUrlImages);
                                newRef.child("media").setValue(media);
                            }
                        }).addOnCompleteListener(UpdateEvent.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                secondloading.dismiss();

                                if (listBitmap.size() > 2) {
                                    final ProgressDialog thirdloading = ProgressDialog.show(UpdateEvent.this, String.format("Uploading the third image ..."), "Please wait...", false, false);
                                    StorageReference mediaRef = storage.getReference().child(String.format("events/%s/%s_%s.jpg", keyEvent, keyEvent, System.currentTimeMillis()));
                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    listBitmap.get(2).compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                    byte[] data = baos.toByteArray();

                                    UploadTask thirdUploadTask = mediaRef.putBytes(data);
                                    thirdUploadTask.addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception exception) {
                                            // Handle unsuccessful uploads
                                            Toast.makeText(UpdateEvent.this, "Field to upload third image !", Toast.LENGTH_SHORT).show();
                                        }
                                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                            // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                                            listUrlImages.add(taskSnapshot.getDownloadUrl().toString());
                                            media.put("images", listUrlImages);
                                            newRef.child("media").setValue(media);
                                        }
                                    }).addOnCompleteListener(UpdateEvent.this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                            thirdloading.dismiss();
                                            finish();
                                        }
                                    });
                                }else {
                                    finish();
                                }
                            }
                        });
                    } else {
                        finish();
                    }

                }
            });

        }

    }

    private boolean verifyForm(){

        boolean valid = true;

        //verify th required element
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

    private boolean checkModificatedField(){

        boolean isUpdated = false;

        String titleEvent = titleField.getText().toString();
        if(!titleEvent.equals(oldTitleField)){
            isUpdated = true;
        }

        String descEvent = descriptionField.getText().toString();
        if(!descEvent.equals(oldDescriptionField)){
            isUpdated = true;
        }

        Log.e("comparin the location", "new:"+locationChoosed.latitude +" , old:"+oldLocationChoosed.latitude);

        if(locationChoosed.latitude != oldLocationChoosed.latitude || locationChoosed.longitude !=  oldLocationChoosed.longitude){
                isUpdated = true;
        }

        if(timeStampEvent != 0 && oldTimeStampEvent != timeStampEvent){
            isUpdated = true;
        }

        if(oldCategoryChoosed != listCategories.get(categoriesViewPager.getCurrentItem()).getId()){
            isUpdated = true;
        }

        Log.e("compare hashtags", compareLists(oldHashTagField, getListHashtags())+"");
        if(!compareLists(oldHashTagField, getListHashtags())){
            isUpdated = true;
            Log.e("hashtag modified", "true");
        }

        if(anounymField.isChecked() != oldAnounymField) {
            isUpdated = true;
        }

        if(detail != null/*!oldDetail.equals(detail)*/){
            Log.e("detail", "old:"+oldDetail.toString() + "new:"+detail.toString());
            isUpdated = true;
        }

        if(newBitmapAdded()){
            return true;
        }

        if(bitmapDeleted){
            return true;
        }

        return isUpdated;
    }

    private void sendEmailToUser(final String accessToken, final String idUser, final String userName, final String  email, final String title, final String description, final String image){

        if(mailNotSended)
            return;

        mailNotSended = false;
        StringRequest postRequest = new StringRequest(Request.Method.POST, UrlEncoder.decodedURL(urlUpdateEvent, idUser, accessToken),
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
                    param = String.format("{\"user\":{\"username\": \"%s\",\"email\": \"%s\"}, \"event\": {\"name\":\"%s\", \"image\":\"%s\", \"description\":\"%s\"}}", userName, email, title, image);
                else
                    param = String.format("{\"user\":{\"username\": \"PlainPress User\",\"email\": \"%s\"}, \"event\": {\"name\":\"%s\", \"image\":\"%s\", \"description\":\"%s\"}}", userName, email, title, image);

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

    private HashMap<String, Object> getModificatedField(){

        HashMap<String, Object> update = new HashMap<>();

        String titleEvent = titleField.getText().toString();
        if(!titleEvent.equals(oldTitleField)){
            update.put("title", titleField.getText().toString());
        }

        String descEvent = descriptionField.getText().toString();
        if(!descEvent.equals(oldDescriptionField)){
            update.put("description", descriptionField.getText().toString());
        }

        Log.e("comparin the location", "new:"+locationChoosed.latitude +" , old:"+oldLocationChoosed.latitude);

        if(locationChoosed.latitude != oldLocationChoosed.latitude || locationChoosed.longitude !=  oldLocationChoosed.longitude){
            try {
                update.put("location", getCityNameByCoordinates(locationChoosed.latitude, locationChoosed.longitude));
            } catch (IOException e) {
                e.printStackTrace();
                update.put("location", new Location(locationChoosed.latitude, locationChoosed.longitude));
            }
        }

        if(timeStampEvent != 0 && oldTimeStampEvent != timeStampEvent){
            update.put("eventAt", timeStampEvent);
        }

        if(oldCategoryChoosed != listCategories.get(categoriesViewPager.getCurrentItem()).getId()){
            update.put("category", listCategories.get(categoriesViewPager.getCurrentItem()));
        }

        if(anounymField.isChecked() != oldAnounymField) {
            update.put("anonymous", isAnounym);
        }

        if(detail!=null/*!oldDetail.equals(detail)*/){
            Log.e("detail", "old:"+oldDetail.toString() + "new:"+detail.toString());
            update.put("detail", detail);
        }

        if(!compareLists(oldHashTagField, getListHashtags())){
            update.put("hashtags", getListHashtags());
        }

        Log.e("updated info", update.toString());

        return update;
    }

    private net.dm73.plainpress.model.Location getCityNameByCoordinates(double lat, double lon) throws IOException {

        List<Address> addresses = mGeocoder.getFromLocation(lat, lon, 1);
        if (addresses != null && addresses.size() > 0) {
            return new net.dm73.plainpress.model.Location((addresses.get(0).getLocality() != null) ? addresses.get(0).getLocality().toString() : "", (addresses.get(0).getCountryName() != null) ? addresses.get(0).getCountryName().toString() : "", lat, lon);
        }
        return null;
    }

    private List<String> getListHashtags() {

        if (hashTagField.getText().toString().isEmpty())
            return null;

        List<String> listHashtags = Arrays.asList(hashTagField.getText().toString().split("#"));
        listHashtags = new ArrayList<>(listHashtags);

        for (String hashtag : new ArrayList<>(listHashtags)) {
            if (hashtag.trim().isEmpty())
                listHashtags.remove(hashtag);
        }

        return listHashtags;
    }

    private boolean compareLists(List<String> firstList, List<String> secondList){

        if(firstList != null && secondList == null){
            return false;
        }else if(firstList == null && secondList != null)
            return false;

        if(firstList.size() ==0 && secondList.size()==0)
            return true;
        else if(firstList.size() != secondList.size())
            return false;

        for(int i =0; i < firstList.size(); i++){
            if(!secondList.get(i).equals(firstList.get(i)))
                return false;
        }

        return true;
    }

    private boolean compareTwoDetail(HashMap<String, Object> oldDetail, HashMap<String, Object> newDetail){

        if(oldDetail == null || newDetail == null)
            return false;
        else if(oldDetail.size() == 0 || newDetail.size() == 0)
            return false;

        for(Map.Entry data : oldDetail.entrySet()){

            if(!newDetail.containsKey(data.getKey()))
                return false;
            else if(!newDetail.get(data.getKey()).toString().equals(data.getValue().toString()))
                return false;

        }

        return true;

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            isAnounym = true;
        } else {
            isAnounym = false;
        }
    }

    @Override
    public void onAlertDialogResponceListening(boolean valid) {
        if (valid) {
            Intent intent = new Intent(this, FaceDetail.class);
            if (oldDetail != null)
                intent.putExtra("UPDATE", detailToHashmap(oldDetail));//todo for update
            startActivityForResult(intent, FACE_DETAIL);
        } else {
            //save the event
            Log.e("checkmodifcatedfield", checkModificatedField()+"");
            if(checkModificatedField())
                updateEvent();
            else{
                mRef.child("events").child("items").child(keyEvent).child("edit").removeValue();
                mRef.child("events").child("items").child(keyEvent).child("_needsMod").setValue(false);
                Toast.makeText(getApplicationContext(), "you dont make any update !", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private HashMap<String,Object> detailToHashmap(Detail detail){
        HashMap<String, Object> data = new HashMap<>();

        data.put("gender", detail.isGender());


        if (detail.getFace() != null) {
            data.put("face", detail.getFace());
        }

        if (detail.getNose() != null) {
            data.put("nose", detail.getNose());
        }

        if (detail.getEar() != null) {
            data.put("ear", detail.getEar());
        }

        if (detail.getMouth() != null) {
            data.put("mouth", detail.getMouth());
        }

        if (detail.getEyes() != null) {
            data.put("eyes", detail.getEyes());
        }

        if (detail.getEyebrows() != null) {
            data.put("eyebrows", detail.getEyebrows());
        }

        if (detail.getHair() != null) {
            data.put("hair", detail.getHair());
        }

        if (detail.getBeard() != null) {
            data.put("beard", detail.getBeard());
        }

        if (detail.getMoustache() != null) {
            data.put("moustache", detail.getMoustache());
        }

        return data;
    }


    private String getHashtags(List<String> hashtags) {
        if (hashtags != null && hashtags.size() > 0) {
            return "#" + TextUtils.join("#", hashtags);
        } else {
            return "";
        }
    }

    private boolean listMediaIsEmpty() {
        for (Object bitmap : listMedia)
            if (bitmap != null)
                return false;
        return true;
    }

    private boolean newBitmapAdded(){
        for(int i=0; i < listMedia.length; i++)
            if((listMedia[i]) != null && (int)((Object[])listMedia[i])[1] == NEW) {
            bitmapDeleted = false;
                return true;
            }

        return false;
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

    private void showCalendarPickerDialog() {
        DatePickerFragement newFragment = new DatePickerFragement();
        newFragment.show(this.getFragmentManager(), "datePicker");
    }


    private void addMedia(Bitmap bitmap) {
        for (int i = 0; i < listMedia.length; i++) {
            if (listMedia[i] == null) {
                listMedia[i] = new Object[]{bitmap, NEW};
                listMediaViews.get(i).setImageBitmap(bitmap);
                Log.e("media added", i+"");
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

    public void requestForCameraPermission() {
        final String permission = android.Manifest.permission.CAMERA;
        if (ContextCompat.checkSelfPermission(UpdateEvent.this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(UpdateEvent.this, permission)) {
                showPermissionRationaleDialog("Test", permission);//todo: ask about permission
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
                        UpdateEvent.this.requestForPermission(permission);
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
        ActivityCompat.requestPermissions(UpdateEvent.this, new String[]{permission}, REQUEST_CAMERA_PERMISSION);
    }

    private void launch() {
        Intent startCustomCameraIntent = new Intent(this, CameraActivity.class);
        startActivityForResult(startCustomCameraIntent, REQUEST_CAMERA);
    }

    private void showMoreDetailDIalog() {
        AlertDialogMoreDetail newFragment = AlertDialogMoreDetail.newInstance("Update event","If there is an attacker, you can report his description by clicking the button \"Describe\". If you want to publish anyway, click the button \"Publish\"");
        newFragment.show(getSupportFragmentManager(), "more detail");
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

            ImageView categoryImage = new ImageView(UpdateEvent.this);
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(density * 80, density * 80);
            categoryImage.setLayoutParams(params);
            Glide.with(UpdateEvent.this)
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
