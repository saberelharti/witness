package net.dm73.plainpress.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import net.dm73.plainpress.R;


public class MediaFragmentGalery extends Fragment {


    private static String MEDIATYPE =  "MEDIA_TYPE";
    private static String MEDIANAME =  "MEDIA_NAME";
    private String mediaType;
    private String mediaName;


    public static MediaFragmentGalery newInstance(String mediaType, String mediaName) {
        MediaFragmentGalery mediaFragment = new MediaFragmentGalery();
        Bundle bundle = new Bundle();
        bundle.putString(MEDIANAME, mediaName);
        bundle.putString(MEDIATYPE, mediaType);
        mediaFragment.setArguments(bundle);
        return mediaFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mediaName = getArguments().getString(MEDIANAME);
            mediaType = getArguments().getString(MEDIATYPE);
        }
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
        }

        if(mediaType.equals("videos")){
            mediaImage.setBackgroundResource(R.color.backgroundGry);
        }

        return rootView;
    }
}
