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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.dm73.plainpress.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AlertDialogForgotPassword extends DialogFragment {

    public static String CONFIRMED_MAIL = "confirmedMail";
    private String confirmedEmail;
    OnEmailCofirmListner mCallback;

    @BindView(R.id.password)
    EditText email;
    @BindView(R.id.confirm)
    Button confirm;
    @BindView(R.id.titleDialog)
    TextView textAlert;

    public AlertDialogForgotPassword() {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (getArguments() != null) {
            confirmedEmail = getArguments().getString(CONFIRMED_MAIL);
        }

        View rootView = inflater.inflate(R.layout.dialog_forgot_password, null);
        ButterKnife.bind(this, rootView);
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.background_dialog_confirm_email);

        textAlert.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/myriad_pro_regular.ttf"));

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(validateConfirmation()){
                    mCallback.onEmailConfirmed(email.getText().toString());
                    dismiss();
                }

            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnEmailCofirmListner) {
            mCallback = (OnEmailCofirmListner) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) return;

        if (activity instanceof OnEmailCofirmListner) {
            mCallback = (OnEmailCofirmListner) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mCallback = (OnEmailCofirmListner) getActivity();
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    private boolean validateConfirmation(){

        boolean valid = true;

        String userEmail = email.getText().toString();
        if(userEmail.isEmpty()){
            email.setError("Requierd.");
            valid = false;
        }else if(!userEmail.contains("@") || !userEmail.contains(".")){
            email.setError("Mail in a bad format.");
            valid = false;
        }

        return valid;
    }


    public interface OnEmailCofirmListner {
        public void onEmailConfirmed(String mail);
    }

}
