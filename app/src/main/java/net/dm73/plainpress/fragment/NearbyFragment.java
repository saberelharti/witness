package net.dm73.plainpress.fragment;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.github.channguyen.rsv.RangeSliderView;
import net.dm73.plainpress.R;


public class NearbyFragment extends Fragment {

    private SeekBar nearbyBar;
    private RangeSliderView nearbyRange;
    private TextView nearbyDistance;
    private OnNearbyChoosedListner mListener;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_nearby, container, false);
        nearbyBar = (SeekBar) rootView.findViewById(R.id.descriptionFragmenSeekBar);
        nearbyDistance = (TextView) rootView.findViewById(R.id.descriptionFragmentNearbyDistance);
        Typeface titiliumWeb = Typeface.createFromAsset(getContext().getAssets(), "fonts/titilliumweb_regular.ttf");
        ((TextView) rootView.findViewById(R.id.descriptionFragmentTitle)).setTypeface(titiliumWeb);

        Glide.with(getActivity())
                .load(R.drawable.maps)
                .centerCrop()
                .into((ImageView) rootView.findViewById(R.id.descriptionFragmentHeader));

        mListener.onNearbyChoosed(25);

        return rootView;
    }




    @Override
    public void onResume() {
        super.onResume();


        nearbyBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Log.e("progress", progress + "");
                nearbyDistance.setText((progress+20)+" km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
//                SharedPreferences prefs = getSharedPreferences("settings", 0);
//                SharedPreferences.Editor editor = prefs.edit();
//                editor.putInt("volume_level", seekBar.getProgress());
//                editor.commit();

                mListener.onNearbyChoosed(seekBar.getProgress()+20);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof NearbyFragment.OnNearbyChoosedListner) {
            mListener = (NearbyFragment.OnNearbyChoosedListner) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    public interface OnNearbyChoosedListner{

        void onNearbyChoosed(int nearbyValue);
    }


}
