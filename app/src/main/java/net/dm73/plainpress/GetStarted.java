package net.dm73.plainpress;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import net.dm73.plainpress.dao.DaoPreference;
import net.dm73.plainpress.fragment.CategoryPreferenceFrgament;
import net.dm73.plainpress.fragment.DescriptionFragment;
import net.dm73.plainpress.fragment.NearbyFragment;
import net.dm73.plainpress.model.User;
import net.dm73.plainpress.util.ActivityConfig;
import net.dm73.plainpress.util.UrlEncoder;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.relex.circleindicator.CircleIndicator;

public class GetStarted extends FragmentActivity implements NearbyFragment.OnNearbyChoosedListner, CategoryPreferenceFrgament.OnCategoriesChoosedListner {

    @BindView(R.id.getStartedViewPager)
    ViewPager mViewPager;
    @BindView(R.id.next)
    ViewGroup next;
    @BindView(R.id.nextText)
    TextView nextText;
    @BindView(R.id.editProgressBar)
    ProgressBar progressBar;


    private PagerAdapter mPagerAdapter;

    private HashMap<String, Object> settings;
    private String idToken;
    private RequestQueue queue;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DaoPreference mDaoPreference;

    private String categoryType = "categories";
    private String nearbyType = "nearby";

    private String urlEmail = "/email/welcome";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_started);
        ButterKnife.bind(this);

        ActivityConfig.setStatusBarTranslucent(getWindow());

        // Write a message to the database
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        settings = new HashMap<>();
        settings.put("trackMe", true);
        mDaoPreference = new DaoPreference(getApplicationContext());

        queue = Volley.newRequestQueue(this);

        CircleIndicator indicator = (CircleIndicator) findViewById(R.id.indicator);
        mPagerAdapter = new GetStartedAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        indicator.setViewPager(mViewPager);

        Typeface titiliumWeb = Typeface.createFromAsset(getAssets(), "fonts/titilliumweb_regular.ttf");
        nextText.setTypeface(titiliumWeb);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(1);
            }
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

                switch (position){
                    case 0:
                        nextText.setText("Get Started");
                        next.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mViewPager.setCurrentItem(1);
                            }
                        });
                        break;
                    case 1:
                        nextText.setText("Next");
                        next.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mViewPager.setCurrentItem(2);
                            }
                        });
                        break;
                    case 2:
                        nextText.setText("Finish");
                        next.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(settings.get(categoryType) == null || ((List<Long>)settings.get(categoryType)).size() < 2){
                                    Toast.makeText(GetStarted.this, "You should at least choose two categories !", Toast.LENGTH_SHORT).show();
                                }else{
                                    progressBar.setVisibility(View.VISIBLE);
                                    writeNewUser();
    //                                savePrefereces(settings);//Todo:a revoir si il est obligatoir de sauvegarder settings
                                    startActivity(new Intent().setClass(GetStarted.this, EditUserProfile.class).putExtra("activity_from","get_started"));
                                    finish();
                                }
                            }
                        });
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mAuth.getCurrentUser().getIdToken(true).addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
            @Override
            public void onComplete(@NonNull Task<GetTokenResult> task) {
                if(task.isSuccessful()){
                    idToken = task.getResult().getToken();
                }else{
                    idToken = null;
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();

        Glide.with(this)
                .load(R.drawable.get_started_bg)
                .centerCrop()
                .crossFade(500)
                .into((ImageView) findViewById(R.id.getStartedBackground));
    }

    private void writeNewUser(){
        // Create new user
        mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).setValue(new User("Witness User", mAuth.getCurrentUser().getEmail(), mAuth.getCurrentUser().getProviderData().get(1).getProviderId(), settings, idToken)).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(mAuth.getCurrentUser().getEmail() != null) {
                    Log.e("user mail", idToken);
                    sendEmailToUser(idToken, mAuth.getCurrentUser().getUid(), mAuth.getCurrentUser().getDisplayName(), mAuth.getCurrentUser().getEmail());
                }
            }
        });
    }

    private void sendEmailToUser(final String accessToken, final String idUser, final String userName, final String  email){
        Log.e("url welcom", UrlEncoder.decodedURL(urlEmail, idUser, accessToken));
        StringRequest postRequest = new StringRequest(Request.Method.POST, UrlEncoder.decodedURL(urlEmail, idUser, accessToken),
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
                    param = String.format("{\"user\":{\"username\": \"%s\",\"email\": \"%s\"}}", userName, email);
                else
                    param = String.format("{\"user\":{\"username\": \"PlainPress User\",\"email\": \"%s\"}}", email);

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
    public void onNearbyChoosed(int nearbyValue) {
        settings.put(nearbyType, (long) nearbyValue);
        Log.e("nearbyListner", nearbyValue+"");
    }

    @Override
    public void categoriesChoosed(List<Long> preferedcategories) {
        settings.put(categoryType, preferedcategories);
    }

    private void savePrefereces(HashMap<String, Object> setting){
        if(setting != null){
            mDaoPreference.saveCategoriesPreference((List<Integer>)setting.get(categoryType));
            mDaoPreference.saveNearbyPreference( (setting.get(nearbyType)!=null)? (int)setting.get(nearbyType) : 20);
        }
    }


    private class GetStartedAdapter extends FragmentStatePagerAdapter {


        public GetStartedAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return myFragment[position];
        }

        @Override
        public int getCount() {
            return myFragment.length;
        }

        //the fragment pages for the view pager
        Fragment[] myFragment = {
                new DescriptionFragment(),
                new CategoryPreferenceFrgament(),
                new NearbyFragment()
        };
    }
}