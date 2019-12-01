package net.dm73.plainpress.util;


import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import net.dm73.plainpress.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlertDialogVerifyEmail extends DialogFragment{

    @BindView(R.id.cancel)
    TextView cancelButton;
    @BindView(R.id.verify)
    TextView verifyButton;
    @BindView(R.id.titleDialog)
    TextView textAlert;

    private static String MESSAGE = "message";

    private OnVerifyEmailDialog mCallback;


    public static AlertDialogVerifyEmail newInstance(String message){

        AlertDialogVerifyEmail fragment = new AlertDialogVerifyEmail();
        Bundle extras = new Bundle();
        extras.putString(MESSAGE, message);
        fragment.setArguments(extras);

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_verify_email, null);
        ButterKnife.bind(this, rootView);

        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.background_dialog_confirm_email);
        if(getArguments()!=null){
            textAlert.setText(getArguments().getString(MESSAGE));
        }
        textAlert.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/myriad_pro_regular.ttf"));

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        verifyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onVerifyEmailDialogResponse(true);
                dismiss();
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mCallback = (OnVerifyEmailDialog) getActivity();
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnVerifyEmailDialog) {
            mCallback = (OnVerifyEmailDialog) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) return;

        if (activity instanceof OnVerifyEmailDialog) {
            mCallback = (OnVerifyEmailDialog) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    public interface OnVerifyEmailDialog{
        void onVerifyEmailDialogResponse(boolean verify);
    }
}
