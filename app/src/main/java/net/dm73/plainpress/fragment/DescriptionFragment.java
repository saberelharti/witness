package net.dm73.plainpress.fragment;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import net.dm73.plainpress.R;


public class DescriptionFragment extends Fragment {



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_description, container, false);
        Typeface titiliumWeb = Typeface.createFromAsset(getContext().getAssets(), "fonts/titilliumweb_regular.ttf");
        ((TextView) rootView.findViewById(R.id.descriptionFragmentTitle)).setTypeface(titiliumWeb);
        ((TextView) rootView.findViewById(R.id.descriptionFragmentText)).setTypeface(titiliumWeb);

        Glide.with(getActivity())
                .load(R.drawable.detective_eye)
                .centerCrop()
                .into((ImageView) rootView.findViewById(R.id.descriptionFragmentHeader));


        return rootView;
    }
}
