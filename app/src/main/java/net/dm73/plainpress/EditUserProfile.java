package net.dm73.plainpress;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.hbb20.CountryCodePicker;
import net.dm73.plainpress.fragment.PlaceArrayAdapter;
import net.dm73.plainpress.model.User;
import net.dm73.plainpress.util.ActivityConfig;
import net.dm73.plainpress.util.AlertDialogConfirmPassword;
import net.dm73.plainpress.util.AlertDialogInfo;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditUserProfile extends AppCompatActivity implements AlertDialogConfirmPassword.OnEmailCofirmListner, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    @BindView(R.id.profilEditclose)
    ImageButton closeButton;
    @BindView(R.id.radioGroupeGender)
    RadioGroup genderRadioGroup;
    @BindView(R.id.radioGroupeTrack)
    RadioGroup trackMeRadioGroupe;
    @BindView(R.id.userFirstName)
    EditText firstNameField;
    @BindView(R.id.userLastName)
    EditText lastNameField;
    @BindView(R.id.userNickName)
    EditText nickNameField;
    @BindView(R.id.userDescription)
    EditText description;
    @BindView(R.id.userAdress)
    EditText adress;
    @BindView(R.id.userLocation)
    AutoCompleteTextView userLocation;
    @BindView(R.id.userAge)
    EditText age;
    @BindView(R.id.userPhone)
    EditText phone;
    @BindView(R.id.saveChanges)
    Button saveChanges;
    @BindView(R.id.privateInformationTitle)
    TextView privateInformationTitle;
    @BindView(R.id.editPhoto)
    Button editPhoto;
    @BindView(R.id.profilImage)
    ImageView userImageProfle;
    @BindView(R.id.verifyEmail)
    TextView verifyEmail;
    @BindView(R.id.countryPicker)
    CountryCodePicker coutryCodePicker;
    @BindView(R.id.AdvacedSettingCard)
    CardView AdvancedSetting;
    @BindView(R.id.AdvancedSetting)
    TextView advacedSettingTitle;
    @BindView(R.id.verifiedMessage)
    TextView verifiedMessage;
    @BindView(R.id.editProgressBar)
    ProgressBar progressBar;
    @BindView(R.id.updatePassword)
    TextView updatePassword;
    @BindView(R.id.updateEmail)
    TextView updateEmail;
    @BindView(R.id.locationInformation)
    TextView locationInformation;
    @BindView(R.id.trackInformation)
    TextView trackInformation;


    private static DatabaseReference databaseReference;
    private FirebaseAuth mAuth;

    private GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;

    private Geocoder mGeocoder;
    private HashMap<String, Object> location;

    private int RC_PHOTO_PICKER = 0 ;
    private LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(new LatLng(-34.041458, 150.790100), new LatLng(-33.682247, 151.383362));

    private Bitmap bitmapProfile;
    private String imageProfileUrl;

    // Use the library’s functions "libPohoneNumbe"
    PhoneNumberUtil phoneUtil;
    Phonenumber.PhoneNumber phNumberProto = null;

    private User user;
    private HashMap<String,Object> listUserEvent;
    private String codeNameCountry;

    public static String userAccessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_user_profile);

        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        mAuth = FirebaseAuth.getInstance();
        ButterKnife.bind(this);
        ActivityConfig.setStatusBarTranslucent(getWindow());

        initGoogleAPIClient();

        privateInformationTitle.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/myriad_pro_regular.ttf"));
        saveChanges.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/myriad_pro_regular.ttf"));
        verifyEmail.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/myriad_pro_regular.ttf"));

//        Log.e("provider", mAuth.getCurrentUser().getProviderData());
        if(mAuth.getCurrentUser()!= null){
            Log.e("provider", mAuth.getCurrentUser().getProviderData().get(1).getProviderId());
        }



        mGeocoder = new Geocoder(this, Locale.getDefault());
        location = null;
        phoneUtil = PhoneNumberUtil.getInstance();

        codeNameCountry = coutryCodePicker.getSelectedCountryNameCode();

        ValueEventListener userProvider = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);

                if(!user.getProvider().equals("password")){
                    Log.e("provider", user.getProvider());
                    updateUI();
                }

                listUserEvent = user.getEvents();

                userAccessToken = user.getAccessToken();

                Glide.with(getApplicationContext())
                        .load(user.getPhotoUrl())
                        .centerCrop()
                        .placeholder(R.drawable.ic_avatar_m)
                        .into(userImageProfle);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        if(getIntent()!=null && getIntent().getStringExtra("activity_from").equals("get_started")){
            updateUiForGetStarted();
        }else{
            databaseReference.child(mAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(userProvider);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //update ui if user not authentified

        if(isEmailVerified()) {
            verifyEmail.setVisibility(View.GONE);
            verifiedMessage.setText("Your email is verified");
            verifiedMessage.setCompoundDrawablesWithIntrinsicBounds(null, null, ContextCompat.getDrawable(this, R.drawable.ic_checked), null);
        }else{
            verifyEmail.setVisibility(View.VISIBLE);
            verifiedMessage.setText("Your email is not verified !");
            verifiedMessage.setCompoundDrawablesWithIntrinsicBounds(0,0,0,0);
        }

        saveChanges = (Button) findViewById(R.id.saveChanges);
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveChanges();
                if (!isNetworkAvailable())
                    showAlertDialog("We loose the connection! Your changes will be considered in the publish connection.", "no_connection");
            }
        });

        verifyEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVerificationEmail();
            }
        });

        updatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(EditUserProfile.this, AdvancedSetting.class);
                intent.putExtra("fragmentId", 0);
                startActivity(intent);
            }
        });

        updateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(EditUserProfile.this, AdvancedSetting.class);
                intent.putExtra("fragmentId", 1);
                startActivity(intent);
            }
        });

        locationInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoDialog("location message");
            }
        });

        trackInformation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInfoDialog("track message");
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        //set the max valid number
        final int maxLength = String.valueOf(phoneUtil.getExampleNumber(coutryCodePicker.getSelectedCountryNameCode()).getNationalNumber()).length();
        phone.setFilters(new InputFilter[]{new InputFilter.LengthFilter(maxLength)});

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        editPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPhotoPicker();
            }
        });

        coutryCodePicker.setOnCountryChangeListener(new CountryCodePicker.OnCountryChangeListener() {
            @Override
            public void onCountrySelected() {
                codeNameCountry = coutryCodePicker.getSelectedCountryNameCode();
                phone.setFilters(new InputFilter[]{new InputFilter.LengthFilter(String.valueOf(phoneUtil.getExampleNumber(codeNameCountry).getNationalNumber()).length())});
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mGoogleApiClient != null){
            mGoogleApiClient = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_PHOTO_PICKER && resultCode == RESULT_OK && data != null) {
            Uri pickedImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), pickedImage);
                addMedia(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void addMedia(Bitmap bitmap) {
        userImageProfle.setImageBitmap(bitmap);
        bitmapProfile = bitmap;
    }


    private void openPhotoPicker() {
        Intent photoPickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        photoPickerIntent.setType("image/*");
        photoPickerIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, false);
        startActivityForResult(Intent.createChooser(photoPickerIntent, "Complete Action Using"), RC_PHOTO_PICKER);
    }

    private void sendVerificationEmail()
    {
        FirebaseUser user = mAuth.getCurrentUser();

        progressBar.setVisibility(View.VISIBLE);

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            progressBar.setVisibility(View.GONE);

                            // email sent
                            AlertDialog.Builder builder = new AlertDialog.Builder(EditUserProfile.this);
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

                            progressBar.setVisibility(View.GONE);

                            // email not sent, so display message and restart the activity or do whatever you wish to do
                            AlertDialog.Builder builder = new AlertDialog.Builder(EditUserProfile.this);
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

    private boolean isEmailVerified(){
        if(mAuth.getCurrentUser().isEmailVerified())
            return true;
        return false;
    }

    private void saveChanges() {

        if (!verifyForm())
            return;

        progressBar.setVisibility(View.VISIBLE);

        String uID = mAuth.getCurrentUser().getUid();
        Map<String, Object> user = userToMap();

        databaseReference.child(uID).updateChildren(user, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {

                    updateNickNameAllEvent();

                    EditUserProfile.databaseReference.child(mAuth.getCurrentUser().getUid()).child("settings").child("trackMe").setValue(getTrackMeValue(), new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                            if (databaseError == null) {
                                if(bitmapProfile!=null){
                                    uploadImageProfile(bitmapProfile);
                                    progressBar.setVisibility(View.GONE);
                                }else {
                                    if (getIntent() != null && getIntent().getStringExtra("activity_from").equals("get_started")) {
                                        startActivity(new Intent().setClass(EditUserProfile.this, MainActivity.class));
                                    }
                                    finish();
                                }
                            }else{
                                showAlertDialog("Sorry! track me value not submited ", "error");
                                if(bitmapProfile!=null){
                                    uploadImageProfile(bitmapProfile);
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        }
                    });

                } else {
                    showAlertDialog("Sorry! Your changes could not be saved ", "error");
                }
            }
        });
    }

    private void uploadImageProfile(Bitmap bitmap){

        final HashMap<String, Object> imageProfile = new HashMap<>();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] bitmapByte = baos.toByteArray();

        StorageReference mediaRef = FirebaseStorage.getInstance().getReference().child(String.format("users/%s/image_profile.png", mAuth.getCurrentUser().getUid()));
        final ProgressDialog loading = ProgressDialog.show(this, String.format("Uploading Profile Image ..."), "Please wait...", false, false);
        UploadTask uploadTask = mediaRef.putBytes(bitmapByte);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                loading.dismiss();
                Toast.makeText(EditUserProfile.this, "Field to upload profile image !", Toast.LENGTH_SHORT).show();
                if(getIntent()!=null && getIntent().getStringExtra("activity_from").equals("get_started")) {
                    startActivity(new Intent().setClass(EditUserProfile.this, MainActivity.class));
                }
                finish();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageProfileUrl = taskSnapshot.getDownloadUrl().toString();
                updateImageProfileAllEvent();
                imageProfile.put("photoUrl", imageProfileUrl);
                databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(imageProfile);
                loading.dismiss();
                if(getIntent()!=null && getIntent().getStringExtra("activity_from").equals("get_started")) {
                    startActivity(new Intent().setClass(EditUserProfile.this, MainActivity.class));
                }
                finish();
            }
        });
    }

    private void updateNickNameAllEvent(){

        if(listUserEvent==null)
            return;

        if(!nickNameField.getText().toString().isEmpty()){

            HashMap<String,Object> userData = new HashMap<>();
            if(!nickNameField.getText().toString().isEmpty())
                userData.put("nickName", nickNameField.getText().toString());

            for(Map.Entry event : listUserEvent.entrySet()){
                FirebaseDatabase.getInstance().getReference().child("events").child("items").child(event.getKey().toString()).child("user").updateChildren(userData);
            }
        }
    }

    private void updateImageProfileAllEvent(){

        if(listUserEvent==null)
            return;

        if(imageProfileUrl!=null && !imageProfileUrl.isEmpty()){

            HashMap<String,Object> userData = new HashMap<>();
            if(imageProfileUrl!=null && !imageProfileUrl.isEmpty())
                userData.put("photoUrl", imageProfileUrl);

            for(Map.Entry event : listUserEvent.entrySet()){
                FirebaseDatabase.getInstance().getReference().child("events").child("items").child(event.getKey().toString()).child("user").updateChildren(userData);
            }
        }

    }

    private boolean verifyForm() {

        boolean valid = true;

        if (getIntent() != null && getIntent().getStringExtra("activity_from").equals("get_started")) {

            String userNickName = nickNameField.getText().toString();
            if (userNickName.isEmpty()) {
                nickNameField.setError("Required.");
                valid = false;
            } else {
                nickNameField.setError(null);
            }

            String userDescription = description.getText().toString();
            if (userDescription.isEmpty()) {
                description.setError("Required.");
                valid = false;
            } else {
                description.setError(null);
            }

            if(location != null && location.size()==0){
                userLocation.setError("Required.");
                valid = false;
            }else if(location ==null){
                userLocation.setError("Required.");
                valid = false;
            }else{
                userLocation.setError(null);
            }
            //todo:error phonecountry
        }


        String phoneNumber = phone.getText().toString();
        if(!phoneNumber.isEmpty()){

            try {
                // I set the default region to PH (Philippines)
                // You can find your country code here http://www.iso.org/iso/country_names_and_code_elements
                phNumberProto = phoneUtil.parse(phoneNumber, codeNameCountry);

            } catch (NumberParseException e) {
                // if there’s any error
                System.err.println("NumberParseException was thrown: "+ e.toString());
            }
            // check if the number is valid
            boolean isValid = phoneUtil.isValidNumber(phNumberProto);
            //Log.e("formating phone", phoneFormat.format(userPhone));

            if(!isValid){
                valid = false;
                phone.setError("invalid phone number");
            }
            else
                phone.setError(null);
        }else {
            phone.setError(null);
        }

        return valid;
    }

    //Todo: make it better
    private boolean getGender(){
        if(((RadioButton) findViewById(genderRadioGroup.getCheckedRadioButtonId())).getText().toString().toLowerCase().equals("male")){
            return true;
        }else{
            return false;
        }
    }

    private boolean getTrackMeValue(){
        if(((RadioButton) findViewById(trackMeRadioGroupe.getCheckedRadioButtonId())).getText().toString().toLowerCase().equals("true")){
            return true;
        }else{
            return false;
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void showAlertDialog(String messafeText, String error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(EditUserProfile.this);

        if(error == "no_connection"){
            builder.setMessage(messafeText);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
        }

        if(error == "error"){
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

    private void showInfoDialog(String message){
        AlertDialogInfo alertDialogInfo = AlertDialogInfo.newInstance(message);
        alertDialogInfo.show(getSupportFragmentManager(), "location_info");
    }

    @Override
    public void onPasswordConfirmed(boolean valid) {
        if(valid){
            Map<String, Object> user = new User(firstNameField.getText().toString(), lastNameField.getText().toString(), nickNameField.getText().toString(), description.getText().toString(), getGender(), adress.getText().toString(), age.getText().toString(), phone.getText().toString(), location).toMap();
//            changeUserProfil();
//            Log.e("password", newPassword.getText().toString());
            databaseReference.child(mAuth.getCurrentUser().getUid()).updateChildren(user, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError == null) {
                        finish();
                    } else {
                        showAlertDialog("Sorry! Your changes could not be saved", "error");
                    }
                }
            });
        }

        Map<String, Object> settings = new HashMap<>();
        settings.put("trackMe", getTrackMeValue());
        databaseReference.child(mAuth.getCurrentUser().getUid()).child("settings").updateChildren(settings, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

            }
        });

    }

    private void updateUI(){
        AdvancedSetting.setVisibility(View.GONE);
        advacedSettingTitle.setVisibility(View.GONE);
    }

    private void updateUiForGetStarted(){
        closeButton.setVisibility(View.GONE);
        AdvancedSetting.setVisibility(View.GONE);
        advacedSettingTitle.setVisibility(View.GONE);

//        Log.e("provider", mAuth.getCurrentUser().getProviderId());
//        Log.e("image", mAuth.getCurrentUser().getPhotoUrl().toString());

        nickNameField.setText(mAuth.getCurrentUser().getDisplayName());
        if(mAuth.getCurrentUser().getPhotoUrl() != null && !mAuth.getCurrentUser().getPhotoUrl().toString().isEmpty())
            Glide.with(getApplicationContext())
                    .load(mAuth.getCurrentUser().getPhotoUrl().toString())
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {

                            if(resource!=null) {
                                userImageProfle.setImageBitmap(resource);
                                bitmapProfile = resource;
                            }else
                                userImageProfle.setImageResource(R.drawable.ic_avatar_m);

                        }
                    });
        else
            Glide.with(getApplicationContext())
                .load(R.drawable.ic_avatar_m)
                .centerCrop()
                .into(userImageProfle);
    }

    /* Initiate Google API Client  */
    private void initGoogleAPIClient() {
        //Without Google API Client Auto Location Dialog will not work
        mGoogleApiClient = new GoogleApiClient.Builder(EditUserProfile.this)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
            final String placeId = String.valueOf(item.placeId);
            Log.i("adapter", "Selected: " + item.description);
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            Log.i("adapter", "Fetching details for ID: " + item.placeId);
        }
    };

    private List<String> getCityNameByCoordinates(double lat, double lon) throws IOException {

        List<String> addressData = new ArrayList<>();

        List<Address> addresses = mGeocoder.getFromLocation(lat, lon, 1);
        if (addresses != null && addresses.size() > 0) {
            addressData.add(addresses.get(0).getCountryName());
            addressData.add(addresses.get(0).getLocality());
            return addressData;
        }
        return null;
    }

    private HashMap<String, Object> userToMap(){

        HashMap<String, Object> map = new HashMap<>();
        if(!firstNameField.getText().toString().isEmpty())
            map.put("firstName", firstNameField.getText().toString());

        if(!lastNameField.getText().toString().isEmpty())
            map.put("lastName", lastNameField.getText().toString());

        if(!nickNameField.getText().toString().isEmpty()){
            map.put("nickName", nickNameField.getText().toString());
        }

        if(!description.getText().toString().isEmpty())
            map.put("description",description.getText().toString());

        map.put("gender", getGender());

        if(!adress.getText().toString().isEmpty())
            map.put("adress", adress.getText().toString());

        if(!age.getText().toString().isEmpty())
            map.put("age",age.getText().toString());

        if(!phone.getText().toString().isEmpty())
            map.put("phone","+"+ coutryCodePicker.getSelectedCountryCode() + phone.getText().toString());

        if(location!=null)
            map.put("location",location);

        return map;
    }

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

            location = new HashMap<>();

            try {
                List<String> address = getCityNameByCoordinates(queriedLocation.latitude,queriedLocation.longitude);
                location.put("city", address.get(1)
                );
                location.put("country", address.get(0));
            } catch (IOException e) {
                e.printStackTrace();
            }

            location.put("latitude", queriedLocation.latitude);
            location.put("longitude",queriedLocation.longitude);
//            location.put("value", place.getLocale().getDisplayName());

            try {
                Log.e("place name", getCityNameByCoordinates(queriedLocation.latitude,queriedLocation.longitude).toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            Log.e("(lat,lng)", "("+queriedLocation.latitude+" , "+queriedLocation.longitude+")");

        };
    };

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        if(mGoogleApiClient.isConnected()) {
            Log.e("mGoogleApiClient", "connected");
            mPlaceArrayAdapter = new PlaceArrayAdapter(this, android.R.layout.simple_list_item_1, BOUNDS_GREATER_SYDNEY, null);
            userLocation.setOnItemClickListener(mAutocompleteClickListener);
            mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
            userLocation.setAdapter(mPlaceArrayAdapter);
        }

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
