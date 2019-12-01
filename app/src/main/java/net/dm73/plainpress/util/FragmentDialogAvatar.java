package net.dm73.plainpress.util;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import net.dm73.plainpress.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class FragmentDialogAvatar extends DialogFragment {

    @BindView(R.id.avatarImageView)
    ImageView avatarImageView;
    @BindView(R.id.closeAvatar)
    ImageView closeButton;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;


    private static String AVATAR = "avatar";
    private String avatar;

    public static FragmentDialogAvatar newInstance(String avatar){
        FragmentDialogAvatar fragmentDialogAvatar = new FragmentDialogAvatar();
        Bundle extras = new Bundle();
        extras.putString(AVATAR, avatar);
        fragmentDialogAvatar.setArguments(extras);
        return fragmentDialogAvatar;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null)
            avatar = getArguments().getString(AVATAR);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root  = inflater.inflate(R.layout.dialog_avatar, null, false);
        ButterKnife.bind(this, root);

        Glide.with(getActivity())
                .load(avatar)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        progressBar.setVisibility(View.GONE);
                        return false;
                    }
                })
                .centerCrop()
                .into(avatarImageView);

        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return root;
    }

}
