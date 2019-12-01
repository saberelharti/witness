package net.dm73.plainpress;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import net.dm73.plainpress.fragment.MediaFragmentGalery;
import net.dm73.plainpress.model.Media;
import net.dm73.plainpress.util.ActivityConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.relex.circleindicator.CircleIndicator;

import static net.dm73.plainpress.EventDetail.mediaURL;

public class MediaPlayer extends AppCompatActivity {

//    private JWPlayerView playerView;
    @BindView(R.id.mediaViewPager)
    ViewPager mediaViewPager;
    @BindView(R.id.indicatorMedia)
    CircleIndicator indicator;
    @BindView(R.id.backEventdetail)
    ImageView backButton;


    private static String ACTIVITY_PARENT = "ACTIVITY_PARENT";

//    @BindView(R.id.videoVideo)
//    VideoView videoView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        JWPlayerView.setLicenseKey(this, "p0IGZV3vTX/A8/8FHjuSuSW/BMWQQmYfUxg6SA==");
        setContentView(R.layout.activity_video_player);
        ButterKnife.bind(this);

        ActivityConfig.setStatusBarTranslucent(getWindow());

        DetailMediaAdapter adapter;

        Intent extras = getIntent();
        if(extras!=null && extras.getStringExtra(ACTIVITY_PARENT).equals("owner")){
            adapter = new DetailMediaAdapter(getSupportFragmentManager(), EventDetailOwner.mediaURL);
        }else{
            adapter = new DetailMediaAdapter(getSupportFragmentManager(), mediaURL);
        }

        if (adapter != null) {
            mediaViewPager.setAdapter(adapter);
            if (adapter.getCount() > 1)
                indicator.setViewPager(mediaViewPager);
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



//        Bundle extraData = getIntent().getExtras();
//
//        PlayerConfig playerConfig = new PlayerConfig.Builder()
//                .file(extraData.getString("video_url"))
//                .autostart(true)
//                .build();
//
//        playerView = new JWPlayerView(this, playerConfig);
//
//        ((RelativeLayout) findViewById(R.id.playerContainer)).addView(playerView);

    }


    @Override
    protected void onResume() {
        // Let JW Player know that the app has returned from the background
        super.onResume();
//        playerView.onResume();
    }

    @Override
    protected void onPause() {
        // Let JW Player know that the app is going to the background
//        playerView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        // Let JW Player know that the app is being destroyed
//        playerView.onDestroy();
        super.onDestroy();
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
            return MediaFragmentGalery.newInstance("images", listImages.get(position));
        }

    }


}
