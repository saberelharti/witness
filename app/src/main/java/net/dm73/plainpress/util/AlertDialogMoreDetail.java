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


public class AlertDialogMoreDetail extends DialogFragment {

    public static String MESSAGE = "message";
    public static String TITLE = "title";
    private String message;
    private String title;
    OnAlertDialogResponceListener mCallback;

    @BindView(R.id.deny)
    TextView deny;
    @BindView(R.id.confirm)
    TextView confirm;
    @BindView(R.id.titleDialog)
    TextView titleDialog;
    @BindView(R.id.messageDialog)
    TextView messageDialog;

    public AlertDialogMoreDetail() {
    }

    public static AlertDialogMoreDetail newInstance(String title, String message){
        AlertDialogMoreDetail alertDialogConfirmEmail = new AlertDialogMoreDetail();
        Bundle bundle = new Bundle();
        bundle.putString(MESSAGE, message);
        bundle.putString(TITLE, title);
        alertDialogConfirmEmail.setArguments(bundle);
        return alertDialogConfirmEmail;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (getArguments() != null) {
            title = getArguments().getString(TITLE);
            message = getArguments().getString(MESSAGE);
        }

        View rootView = inflater.inflate(R.layout.dialog_more_detail, null);
        ButterKnife.bind(this, rootView);
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.background_dialog_confirm_email);

        titleDialog.setText(title);
        titleDialog.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/myriad_pro_regular.ttf"));

        messageDialog.setText(message);
        messageDialog.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/myriad_pro_regular.ttf"));

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onAlertDialogResponceListening(true);
                dismiss();
            }
        });

        deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onAlertDialogResponceListening(false);
                dismiss();
            }
        });

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        if (context instanceof OnAlertDialogResponceListener) {
            mCallback = (OnAlertDialogResponceListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) return;

        if (activity instanceof OnAlertDialogResponceListener) {
            mCallback = (OnAlertDialogResponceListener) activity;
        } else {
            throw new RuntimeException(activity.toString()
                    + " must implement OnListFragmentInteractionListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mCallback = (OnAlertDialogResponceListener) getActivity();
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }




    public interface OnAlertDialogResponceListener {
        public void onAlertDialogResponceListening(boolean valid);
    }

}
