package net.dm73.plainpress.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import net.dm73.plainpress.MediaPlayer;
import net.dm73.plainpress.R;


public class MediaFragment extends Fragment {


    private static String MEDIATYPE =  "MEDIA_TYPE";
    private static String MEDIANAME =  "MEDIA_NAME";
    private static String ACTIVITY_PARENT = "ACTIVITY_PARENT";
    private String mediaType;
    private String mediaName;
    private String activityParent;


    public static MediaFragment newInstance(String mediaType, String mediaName, String activityParent) {
        MediaFragment mediaFragment = new MediaFragment();
        Bundle bundle = new Bundle();
        bundle.putString(MEDIANAME, mediaName);
        bundle.putString(MEDIATYPE, mediaType);
        bundle.putString(ACTIVITY_PARENT, activityParent);
        mediaFragment.setArguments(bundle);
        return mediaFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mediaName = getArguments().getString(MEDIANAME);
            mediaType = getArguments().getString(MEDIATYPE);
            activityParent = getArguments().getString(ACTIVITY_PARENT);
        }

//        postponeEnterTransition();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            setSharedElementEnterTransition(TransitionInflater.from(getContext()).inflateTransition(android.R.transition.move));
//        }
//        setSharedElementReturnTransition(null);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_media, null);
        ImageView mediaImage = (ImageView) rootView.findViewById(R.id.mediaImage);
        ImageView playVideo = (ImageView) rootView.findViewById(R.id.playVideo);

        if(mediaType.equals("images")){
            Glide.with(getActivity())
                    .load(mediaName)
                    .centerCrop()
                    .into(mediaImage);
            playVideo.setVisibility(View.GONE);
            mediaImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent();
                    intent.setClass(getActivity(), MediaPlayer.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra(ACTIVITY_PARENT, activityParent);
                    startActivity(intent);
                }
            });
        }

        if(mediaType.equals("videos")){
            mediaImage.setBackgroundResource(R.color.backgroundGry);
            playVideo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent().setClass(getActivity(), MediaPlayer.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("video_url", mediaName));
                }
            });
        }

        return rootView;
    }
}
