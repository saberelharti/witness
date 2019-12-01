package net.dm73.plainpress.util;

import android.app.Dialog;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import net.dm73.plainpress.R;

import butterknife.BindView;
import butterknife.ButterKnife;



public class CustomAlertDialog extends DialogFragment {

    public static String MESSAGE = "message";
    private String message;
    private DatabaseReference mRef;

    @BindView(R.id.confirm)
    TextView confirm;
    @BindView(R.id.titleDialog)
    TextView textAlert;

    public CustomAlertDialog() {
    }

    public static CustomAlertDialog newInstance(String message){
        CustomAlertDialog customAlertDialog = new CustomAlertDialog();
        Bundle bundle = new Bundle();
        bundle.putString(MESSAGE, message);
        customAlertDialog.setArguments(bundle);
        return customAlertDialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (getArguments() != null) {
            message = getArguments().getString(MESSAGE);
        }
        View rootView = inflater.inflate(R.layout.dialog_wraning_remove, null);
        ButterKnife.bind(this, rootView);
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.background_dialog_confirm_email);
        textAlert.setText(message);
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

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }


}
