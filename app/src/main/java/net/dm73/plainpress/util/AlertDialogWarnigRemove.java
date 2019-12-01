package net.dm73.plainpress.util;


import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import net.dm73.plainpress.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AlertDialogWarnigRemove extends DialogFragment {

    public static String KEY_EVENT = "key_event";
    private String keyEvent;
    private DatabaseReference mRef;

    @BindView(R.id.deny)
    TextView deny;
    @BindView(R.id.confirm)
    TextView confirm;
    @BindView(R.id.titleDialog)
    TextView textAlert;

    public AlertDialogWarnigRemove() {
    }

    public static AlertDialogWarnigRemove newInstance(String keyEvent){
        AlertDialogWarnigRemove alertDialogConfirmEmail = new AlertDialogWarnigRemove();
        Bundle bundle = new Bundle();
        bundle.putString(KEY_EVENT, keyEvent);
        alertDialogConfirmEmail.setArguments(bundle);
        return alertDialogConfirmEmail;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (getArguments() != null) {
            keyEvent = getArguments().getString(KEY_EVENT);
        }

        mRef = FirebaseDatabase.getInstance().getReference();

        View rootView = inflater.inflate(R.layout.dialog_wraning_remove, null);
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
                dismiss();
            }
        });

        deny.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRef.child("events").child("items").child(keyEvent).removeValue();
                mRef.child("geoevents").child(keyEvent).removeValue();
                mRef.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("events").child(keyEvent).removeValue();
                Log.e("eventkey", keyEvent);
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


}
