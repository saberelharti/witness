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
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import net.dm73.plainpress.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AlertDialogPolicyChecker extends DialogFragment {

    @BindView(R.id.cancel)
    TextView deny;
    @BindView(R.id.confirm)
    TextView confirm;
    @BindView(R.id.textDialog)
    TextView textAlert;
    @BindView(R.id.imageIntro)
    ImageView imageIntro;

    private int status = 0;

    private OnPolicyFormCheked mCallback;

    public AlertDialogPolicyChecker(){
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dialog_policy_checker, null);
        ButterKnife.bind(this, rootView);
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.background_dialog_confirm_email);
        textAlert.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/myriad_pro_regular.ttf"));
        textAlert.setMovementMethod(LinkMovementMethod.getInstance());
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(status==1){
                    mCallback.onPolicyChecked(true);
                    dismiss();
                }else{
                    status = 1;
                    updateUi();
                }
            }
        });

        deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onPolicyChecked(false);
                dismiss();
            }
        });

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnPolicyFormCheked) {
            mCallback = (OnPolicyFormCheked) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) return;

        if (activity instanceof OnPolicyFormCheked) {
            mCallback = (OnPolicyFormCheked) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    private void updateUi(){
        imageIntro.setImageResource(R.drawable.ic_cercle_warning);
        textAlert.setText("I confirm that I am 18 years of age or older");
        confirm.setText("I CONFIRM");
        //adding some animation
    }

    public interface OnPolicyFormCheked{
        void onPolicyChecked(boolean checked);
    }


}
