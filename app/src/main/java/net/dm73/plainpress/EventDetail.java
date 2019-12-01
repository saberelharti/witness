package net.dm73.plainpress;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
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
import com.firebase.ui.database.FirebaseListAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import net.dm73.plainpress.fragment.MediaFragment;
import net.dm73.plainpress.model.Comments;
import net.dm73.plainpress.model.Event;
import net.dm73.plainpress.model.Media;
import net.dm73.plainpress.model.User;
import net.dm73.plainpress.util.ActivityConfig;
import net.dm73.plainpress.util.AlertDialogVerifyEmail;
import net.dm73.plainpress.util.AlertDialogWarnigRemove;
import net.dm73.plainpress.util.EventUtil;
import net.dm73.plainpress.util.FragmentDialogAvatar;
import net.dm73.plainpress.util.UrlEncoder;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import me.relex.circleindicator.CircleIndicator;

import static net.dm73.plainpress.util.EventUtil.getLocationName;


public class EventDetail extends AppCompatActivity implements OnMapReadyCallback, AlertDialogVerifyEmail.OnVerifyEmailDialog {

    @BindView(R.id.mediaEvent)
    ViewPager mediaEvent;
    @BindView(R.id.viewsNumber)
    TextView viewNumber;
    @BindView(R.id.commentCounter)
    TextView commentNumber;
    @BindView(R.id.editIcon)
    TextView editEvent;
    @BindView(R.id.detailDescription)
    TextView description;
    @BindView(R.id.detailTitle)
    TextView detailTitle;
    @BindView(R.id.detailAvatar)
    ImageView detailAttacker;
    @BindView(R.id.detailHashtag)
    LinearLayout detailHashtag;
    @BindView(R.id.shareEvent)
    TextView shareButton;
    @BindView(R.id.indicator)
    CircleIndicator indicator;
    @BindView(R.id.detailCommentaireContainer)
    LinearLayout listCommentContainer;
    @BindView(R.id.detailComment)
    TextView detailCommentTitle;
    @BindView(R.id.addCommentEditText)
    EditText commentEditText;
    @BindView(R.id.commentSend)
    ImageView sendComment;
    @BindView(R.id.moreComment)
    Button moreComment;
    @BindView(R.id.eventTitle)
    TextView eventTitle;
    @BindView(R.id.eventLocation)
    TextView eventLocation;
    @BindView(R.id.detailLocation)
    TextView detailComment;
    @BindView(R.id.userProfil)
    CircleImageView userProfile;
    @BindView(R.id.viewPagerContainer)
    RelativeLayout viewPagerContainer;
    @BindView(R.id.backtoWall)
    ImageView backButton;
    @BindView(R.id.emptyContentImage)
    ImageView emptyImage;
    @BindView(R.id.hashTagTitle)
    TextView hashtagTitle;
    @BindView(R.id.categoryName)
    TextView categoryName;
    @BindView(R.id.categoryTitle)
    TextView categoryTitle;
    @BindView(R.id.eventTime)
    TextView eventTime;
    CircleImageView userProfileImage;


    private float density;
    private Typeface titiliumWeb;
    private Typeface titiliumWebBold;

    private DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private FirebaseListAdapter<Comments> mAdapter;

    private Bundle extras;
    private String userId;
    private RequestQueue queue;

    private GoogleMap mGoogleMap;
    private LatLng locationEvent;

    private Event event;
    private Bitmap firstMedia;

    static Media mediaURL;
    private final String url = "/email/event/comment";
    private User eventUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);
        ButterKnife.bind(this);

        //supportPostponeEnterTransition();
        ActivityConfig.setStatusBarTranslucent(getWindow());

        // initialise the principale variables
        extras = getIntent().getExtras();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        mRef = database.getReference();
        mAuth = FirebaseAuth.getInstance();
        density = getResources().getDisplayMetrics().density;
        queue = Volley.newRequestQueue(this);

        titiliumWeb = Typeface.createFromAsset(getAssets(), "fonts/titilliumweb_regular.ttf");
        titiliumWebBold = Typeface.createFromAsset(getAssets(), "fonts/titilliumweb_bold.ttf");
        viewNumber.setTypeface(titiliumWeb);
        commentNumber.setTypeface(titiliumWeb);
        editEvent.setTypeface(titiliumWeb);
        shareButton.setTypeface(titiliumWeb);
        description.setTypeface(titiliumWeb);
        eventLocation.setTypeface(titiliumWeb);
        eventTitle.setTypeface(titiliumWeb);
        detailTitle.setTypeface(titiliumWebBold);
        eventTime.setTypeface(titiliumWeb);
        hashtagTitle.setTypeface(titiliumWebBold);
        categoryTitle.setTypeface(titiliumWebBold);
        detailCommentTitle.setTypeface(titiliumWebBold);
        detailComment.setTypeface(titiliumWebBold);


        moreComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent().setClass(getApplicationContext(), CommentsEvent.class).putExtra("EVENT_KEY", extras.getString("KEYEVENT")));
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.detailMap);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onStart() {
        super.onStart();

        editEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                edit(extras.getString("KEYEVENT"));
            }
        });

        final ValueEventListener eventUserListner = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                eventUser = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        ValueEventListener eventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                event = dataSnapshot.getValue(Event.class);

                if(event == null || event.isMediaEmpty()){
                    showEmptyMedia();
                }else {
                    hideEmptyMedia();
                    DetailMediaAdapter adapter = new DetailMediaAdapter(getSupportFragmentManager(), event.getMedia());//todo:close the eventlistner in destroy methode
                    mediaURL = event.getMedia();
                    if (adapter != null)
                        mediaEvent.setAdapter(adapter);
                    if (adapter.getCount() > 1)
                        indicator.setViewPager(mediaEvent);
                }

                // Add a marker in Sydney, Australia, and move the camera.
                LatLng location = new LatLng(event.getLocation().getLatitude(), event.getLocation().getLongitude());
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
                mGoogleMap.addMarker(new MarkerOptions().position(location).title(getLocationName(event.getLocation())));

                Log.e("location event ready", "true");

                eventTitle.setText(event.getTitle());
                eventLocation.setText(getLocationName(event.getLocation()));
                userId = event.getUserInstance().getId();
                eventTime.setText(EventUtil.convertDateToRedableDate(event.getEventAt()));
                description.setText(event.getDescription());
                categoryName.setText(event.getCategory().getName());
                customViewNumber(event.getViews());

                if (userId != null) {
                    mRef.child("users").child(event.getUser().get("id").toString()).addValueEventListener(eventUserListner);

                    if(userId.equals(mAuth.getCurrentUser().getUid())){
                        editEvent.setVisibility(View.VISIBLE);
                    }else{
                        editEvent.setVisibility(View.GONE);
                    }
                }

                if (!event.isAnonymous()) {
                    Glide.with(getApplicationContext())
                            .load(event.getUserInstance().getPhotoUrl())
                            .placeholder(R.drawable.ic_avatar_m)
                            .fitCenter()
                            .centerCrop()
                            .into(userProfile);
                } else {
                    Glide.with(getApplicationContext())
                            .load(R.drawable.ic_ghost_profile)
                            .fitCenter()
                            .centerCrop()
                            .into(userProfile);
                }

                if (!event.isDetailEmpty() && !event.isDetailUrlEmpty()) {
                    Log.e("attacker picture", event.getDetail().getPicture());
                    Glide.with(getApplicationContext())
                            .load(event.getDetail().getPicture())
                            .fitCenter()
                            .into(detailAttacker);
                    detailAttacker.setVisibility(View.VISIBLE);

                    //load the avatar dialogfragment
                    loadAvatarDialogFragment(event.getDetail().getPicture());
                }


                if (!event.isHashTagsEmpty()) {
                    //make hashtag title visible
                    hashtagTitle.setVisibility(View.VISIBLE);

                    LinearLayout.LayoutParams paramsHashtag = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    paramsHashtag.setMargins((int) (10 * density), 0, (int) (5 * density), 0);
                    detailHashtag.removeAllViews();
                    for (Object hashtag : event.getHashtags()) {
                        TextView hashtagText = new TextView(getApplicationContext());
                        hashtagText.setPadding((int) (20 * density), (int) (8 * density), (int) (20 * density), (int) (8 * density));
                        hashtagText.setLayoutParams(paramsHashtag);
                        hashtagText.setBackgroundResource(R.drawable.background_hashtag);
                        hashtagText.setText(hashtag.toString());
                        hashtagText.setGravity(Gravity.CENTER_VERTICAL);
                        hashtagText.setTextColor(Color.WHITE);
                        hashtagText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                        hashtagText.setTypeface(titiliumWeb);
                        detailHashtag.addView(hashtagText);
                    }
                }

                if(event.isMediaEmpty())
                    Glide.with(getApplicationContext())
                        .load(event.getFirstMediaImage() == null ? R.drawable.image_holder : event.getFirstMediaImage())
                        .asBitmap()
                        .into(new SimpleTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                                firstMedia = resource;
                            }
                        });//todo:clean Glide loader

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("eventDetail", "loadPost:onCancelled", databaseError.toException());
                // [START_EXCLUDE]
                Toast.makeText(EventDetail.this, "Failed to load post.",
                        Toast.LENGTH_SHORT).show();
                // [END_EXCLUDE]
            }
        };

        mRef.child("events").child("items").child(extras.getString("KEYEVENT")).addValueEventListener(eventListener);


        ValueEventListener commentListener = new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                //update the comments counter
                commentNumber.setText(String.format("%s", dataSnapshot.getChildrenCount()));

                //hide/show more_comment button
                if (dataSnapshot.getChildrenCount() > 4) {
                    moreComment.setVisibility(View.VISIBLE);
                } else {
                    moreComment.setVisibility(View.GONE);
                }

                List<Comments> listComments = new ArrayList<>();

                for (DataSnapshot childSnapshot : dataSnapshot.getChildren()) {
                    listComments.add(childSnapshot.getValue(Comments.class));
                }

                //get the first 4 elements
                if(dataSnapshot.getChildrenCount()>4)
                    listComments = listComments.subList(listComments.size()-4,listComments.size());

                //reverse the list for the last created
                Collections.reverse(listComments);

                //clean list Comment View
                listCommentContainer.removeAllViews();

                //adding comment view
                for (Comments commentItem : listComments) {
                    Log.e("createdAt", commentItem.getCreatedAt() + "");
                    listCommentContainer.addView(createCommentView(commentItem));
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(EventDetail.this, "Failed to load post.", Toast.LENGTH_SHORT).show();
            }
        };

        mRef.child("comments").child("items").child(extras.getString("KEYEVENT")).orderByChild("createdAt").addValueEventListener(commentListener);

    }

    private void hideEmptyMedia(){
        emptyImage.setVisibility(View.GONE);
        mediaEvent.setVisibility(View.VISIBLE);
    }

    private void showEmptyMedia(){
        emptyImage.setVisibility(View.VISIBLE);
        mediaEvent.setVisibility(View.GONE);
    }

    private void edit(String eventKey){
        Intent intent = new Intent();
        intent.setClass(this, UpdateEvent.class);
        intent.putExtra("KEYEVENT", eventKey);
        startActivity(intent);
        Log.e("eventkey", eventKey);
    }

    // this method share your image
    private void shareBitmap (Bitmap bitmap, final String text) {

        if(bitmap != null){
            try {
                File cacheFile = new File(getCacheDir(), "event.jpg");
                FileOutputStream fOut = new FileOutputStream(cacheFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                fOut.flush();
                fOut.close();
//                file.setReadable(true, false);
                Uri uri = FileProvider.getUriForFile(this, "net.dm73.plainpress.provider", cacheFile);

                Intent intent = ShareCompat.IntentBuilder.from(this)
                        .setType("*/*")
                        .setSubject("Event share from PlainPrex")
                        .setText(text)
                        .setStream(uri)
                        .setChooserTitle("Select an app for you share the event!")
                        .createChooserIntent()
                        .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            //todo:add progress bar
            Glide.with(this)
                .load(event.getFirstMediaImage())
                .asBitmap()
                .into(new SimpleTarget<Bitmap>(500,500) {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation glideAnimation) {
                        try {
                            File cacheFile = new File(getCacheDir(), "event.jpg");
                            FileOutputStream fOut = new FileOutputStream(cacheFile);
                            resource.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
                            fOut.flush();
                            fOut.close();
                            Uri uri = FileProvider.getUriForFile(EventDetail.this, "net.dm73.plainpress.provider", cacheFile);

                            Intent intent = ShareCompat.IntentBuilder.from(EventDetail.this)
                                    .setType("*/*")
                                    .setSubject("Event share from PlainPrex")
                                    .setText(text)
                                    .setStream(uri)
                                    .setChooserTitle("Select an app for you share the event!")
                                    .createChooserIntent()
                                    .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
        }

    }

    private View createCommentView(Comments commentItem) {

        RelativeLayout rootview = (RelativeLayout) getLayoutInflater().inflate(R.layout.comment, null, false);
        TextView nickNmae = (TextView) rootview.findViewById(R.id.userNickName);
        userProfileImage = (CircleImageView) rootview.findViewById(R.id.userImage);
        TextView message = (TextView) rootview.findViewById(R.id.userMessage);
        TextView createdAt = (TextView) rootview.findViewById(R.id.timerComment);

        nickNmae.setTypeface(titiliumWebBold);
        message.setTypeface(titiliumWeb);
        createdAt.setTypeface(titiliumWeb);

        nickNmae.setText(commentItem.isNicknameUserEmpty() ? "User" : commentItem.getNickName());
        message.setText(commentItem.getMessage());
        createdAt.setText(EventUtil.getDifferenceTime(commentItem.getCreatedAt()));
        Glide.with(getApplicationContext())
                .load(commentItem.isNicknameUserEmpty() ? R.drawable.ic_avatar_m : commentItem.getUser().get("photoUrl"))
                .centerCrop()
                .placeholder(R.drawable.ic_avatar_m)
                .error(R.drawable.ic_avatar_m)
                .into(userProfileImage); //todo: clean glide

        return rootview;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified())
            mAuth.getCurrentUser().reload();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        sendComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {

                    if (!validateComment()) {
                        return;
                    }

                    Thread t = new Thread(new Runnable() {
                        @Override
                        public void run() {

                            final String message = commentEditText.getText().toString();

                            HashMap<String, String> user = new HashMap<>();
                            user.put("id", mAuth.getCurrentUser().getUid());
                            user.put("nickName", MainActivity.user.getNickName());
                            user.put("photoUrl", MainActivity.user.getPhotoUrl());

                            HashMap<String, Object> comment = new HashMap<>();
                            comment.put("createdAt", ServerValue.TIMESTAMP);
                            comment.put("enabled", true);
                            comment.put("message", message);
                            comment.put("user", user);

                            mRef.child("comments").child("items").child(extras.getString("KEYEVENT")).push().setValue(comment).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful() && !mAuth.getCurrentUser().getEmail().equals(eventUser.getEmail()) ) {
                                        mAuth.getCurrentUser().getIdToken(true).addOnSuccessListener(new OnSuccessListener<GetTokenResult>() {
                                            @Override
                                            public void onSuccess(GetTokenResult getTokenResult) {
                                                Log.e("token", getTokenResult.getToken());
                                                sendEmailToUser(url, getTokenResult.getToken(), mAuth.getCurrentUser().getUid(), eventUser.getNickName(), eventUser.getEmail(), event.getTitle(), event.getFirstMediaImage(), MainActivity.user.getNickName(), MainActivity.user.getPhotoUrl(), message);
                                            }
                                        });
                                    } else if(!task.isSuccessful()) {
                                        Toast.makeText(getApplicationContext(), "An internal error occured", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    commentEditText.setText("");
                                    commentEditText.setHint("Add Comment");
                                }
                            });
                        }
                    });
                    t.start();
                } else {
                    showAlertDialog();
                }

                if (sendComment != null) {
                    InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(sendComment.getWindowToken(), 0);
                }

            }
        });

        userProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (event.isAnonymous()) {
                    Toast.makeText(getApplicationContext(), "This user is anonymous !", Toast.LENGTH_SHORT).show();
                } else {
                    startActivity(new Intent().setClass(getApplicationContext(), UserProfile.class).putExtra("userID", userId));
                }
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(firstMedia!=null)
                    shareBitmap(firstMedia, event.getTitle()+"\n"+"http://www.google.com");
                else
                    Toast.makeText(EventDetail.this, "No Media to share !", Toast.LENGTH_SHORT).show();
            }

        });
    }

    private void sendEmailToUser(String url, final String accessToken, final String idUser, final String userName, final String email, final String nameEvent, final String imageEvent, final String userNameComment, final String photoUrlComment, final String messageComment) {
        Log.e("url add comment", UrlEncoder.decodedURL(url, idUser, accessToken));
        StringRequest postRequest = new StringRequest(Request.Method.POST, UrlEncoder.decodedURL(url, idUser, accessToken),
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
                        if (error != null)
                            Log.e("Response Error", error.toString());

                        String body;
                        //get status code here
                        String statusCode = String.valueOf(error.networkResponse.statusCode);
                        //get response body and parse with appropriate encoding
                        if(error.networkResponse.data!=null) {
                            try {
                                body = new String(error.networkResponse.data,"UTF-8");
                                Log.e("body responce", body);
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                        }

                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> header = new HashMap<>();
                header.put("Content-Type", "application/json");
                return header;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                String param;
                if (userName != null && userName.isEmpty())
                    param = "{\"user\":{\"username\":\"" + "Witness User" + "\",\"email\":\"" + email + "\"}, \"event\":{\"name\":\"" + nameEvent + "\",\"image\":\"" + imageEvent + "\"},\"comments\":[{\"user\":{\"username\":\"" + userNameComment + "\",\"photoUrl\":\"" + photoUrlComment + "\"},\"message\":\"" + messageComment + "\"}]}";
                else
                    param = "{\"user\":{\"username\":\"" + userName + "\",\"email\":\"" + email + "\"}, \"event\":{\"name\":\"" + nameEvent + "\",\"image\":\"" + imageEvent + "\"},\"comments\":[{\"user\":{\"username\":\"" + userNameComment + "\",\"photoUrl\":\"" + photoUrlComment + "\"},\"message\":\"" + messageComment + "\"}]}";

                Log.e("params", param);
                return param.getBytes();
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

    private void showAlertDialog() {
        FirebaseAuth.getInstance().getCurrentUser().reload();
        AlertDialogVerifyEmail newFragment = AlertDialogVerifyEmail.newInstance("You are not allowed to comment. Please let us verify your email on the profile section to get this permission !");
        newFragment.show(getSupportFragmentManager(), "alert");
    }

    private void loadAvatarDialogFragment(String avatar){
        final FragmentDialogAvatar fragmentDialogAvatar = FragmentDialogAvatar.newInstance(avatar);
        detailAttacker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fragmentDialogAvatar.show(getSupportFragmentManager(), "avatar_fragment");
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private boolean validateComment() {
        boolean valid = true;
        String comment = commentEditText.getText().toString();
        if (TextUtils.isEmpty(comment)) {
            commentEditText.setError("Empty comment.");
            valid = false;
        } else {
            commentEditText.setError(null);
        }
        return valid;
    }

    private void customViewNumber(Long viewNumbers) {
        if (viewNumbers < 1000) {
            viewNumber.setText(String.valueOf(viewNumbers));
            viewNumber.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.green));
        } else if (viewNumbers < 1000000) {
//            detailTrendImage.setImageResource(R.drawable.ic_trend_red);
            viewNumber.setCompoundDrawables(ContextCompat.getDrawable(this, R.drawable.ic_trend_green), null, null, null);
            viewNumber.setText(String.valueOf(new DecimalFormat("###.# K").format((viewNumbers / 1000f))));
            viewNumber.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
        } else {
            viewNumber.setCompoundDrawables(ContextCompat.getDrawable(this, R.drawable.ic_trend_red), null, null, null);
//            detailTrendImage.setImageResource(R.drawable.ic_trend_red);
            viewNumber.setText(String.valueOf(new DecimalFormat("###.# M").format((viewNumbers / 1000000f))));
            viewNumber.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.red));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.e("mapready", "true");
        mGoogleMap = googleMap;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (userProfile != null) {
            Glide.clear(userProfile);
        }

        if (userProfileImage != null) {
            Glide.clear(userProfileImage);
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @Override
    public void onVerifyEmailDialogResponse(boolean verify) {

        if(verify)
            sendVerificationEmail();

    }

    private void sendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // email sent
                            AlertDialog.Builder builder = new AlertDialog.Builder(EventDetail.this);
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
                            AlertDialog.Builder builder = new AlertDialog.Builder(EventDetail.this);
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

    private class DetailMediaAdapter extends FragmentStatePagerAdapter {

        private List<String> listImages;


        public DetailMediaAdapter(FragmentManager fragmentManager, Media media) {
            super(fragmentManager);
            listImages = media.getImages();
        }

        @Override
        public int getCount() {
            return listImages.size();
        }

        @Override
        public Fragment getItem(int position) {
            return MediaFragment.newInstance("images", listImages.get(position), "not_owner");
        }

    }

}
