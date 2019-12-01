package net.dm73.plainpress;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.multidex.MultiDex;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import net.dm73.plainpress.util.ActivityConfig;

import butterknife.BindView;
import butterknife.ButterKnife;

public class Splash extends AppCompatActivity{

    @BindView(R.id.sign_in)
    Button signIn;
    @BindView(R.id.sign_up)
    Button signUp;
    @BindView(R.id.spalshtext)
    TextView splashText;
    @BindView(R.id.logo)
    ImageView logo;
    @BindView(R.id.backgroundSplash)
    ImageView backgroundSplash;

    private FirebaseAuth mAuth;
    private DatabaseReference mRef;
    private Handler handler;
    private Animation animation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MultiDex.install(this);
        setContentView(R.layout.activity_splash);
        ActivityConfig.setStatusBarTranslucent(getWindow());
        ButterKnife.bind(this);

//        FirebaseDatabase.getInstance().setPersistenceEnabled(true);//todo:remove the comment
        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference();

        Typeface robotFont = Typeface.createFromAsset(getAssets(), "fonts/roboto_thin.ttf");
        signIn.setTypeface(robotFont);
        signUp.setTypeface(robotFont);

        splashText.setText(R.string.splash_text);
        splashText.setTypeface(robotFont);

        handler = new Handler();
    }

    @Override
    protected void onStart() {
        super.onStart();

        Glide.with(Splash.this)
                .load(R.drawable.splash_background)
                .centerCrop()
                .crossFade(500)
                .into(backgroundSplash);

        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null){

            ValueEventListener userProfileListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if(loosedData(dataSnapshot)){

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent().setClass(Splash.this, GetStarted.class));
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            }
                        }, 5000);

                    }else{

                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(new Intent().setClass(Splash.this, MainActivity.class));
                                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                            }
                        }, 5000);

                    }

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            mRef.child("users").child(user.getUid()).addListenerForSingleValueEvent(userProfileListener);


        }else{
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showSignButton();
                }
            }, 5000);
        }

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent().setClass(Splash.this, SignActivity.class).putExtra("BUTTON_CLICKED", 0));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent().setClass(Splash.this, SignActivity.class).putExtra("BUTTON_CLICKED", 1));
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        handler.post(new Runnable() {
            @Override
            public void run() {
                animation = AnimationUtils.loadAnimation(Splash.this, R.anim.fade_in_out);
                logo.startAnimation(animation);
            }
        });
    }


    private void showSignButton(){
        splashText.setVisibility(View.GONE);
        signIn.setVisibility(View.VISIBLE);
        signUp.setVisibility(View.VISIBLE);
    }

    private boolean loosedData(DataSnapshot dataSnapshot){

        if(!dataSnapshot.child("location").exists())
            return true;


        if(!dataSnapshot.child("location").child("latitude").exists() || !dataSnapshot.child("location").child("longitude").exists())
            return true;

        if(!dataSnapshot.child("settings").exists())
            return true;

        if(!dataSnapshot.child("settings").child("categories").exists())
            return true;

        if(!dataSnapshot.child("settings").child("trackMe").exists())
            return true;

        if(!dataSnapshot.child("settings").child("nearby").exists())
            return true;

        return false;
    }


}
