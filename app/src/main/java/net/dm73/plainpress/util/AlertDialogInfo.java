package net.dm73.plainpress.util;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import net.dm73.plainpress.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AlertDialogInfo extends DialogFragment {

    @BindView(R.id.titleDialog)
    TextView textDialog;
    @BindView(R.id.okButton)
    Button validButton;

    private static String MESSAGE = "message";
    private String message;

    public static AlertDialogInfo newInstance(String message){
        AlertDialogInfo newFragment = new AlertDialogInfo();
        Bundle extras = new Bundle();
        extras.putString(MESSAGE, message);
        newFragment.setArguments(extras);
        return newFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getArguments()!=null){
            message = getArguments().getString(MESSAGE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.dialog_info, null, false);
        ButterKnife.bind(this, root);

        textDialog.setText(message);
        textDialog.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/myriad_pro_regular.ttf"));

        validButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        return root;
    }
}
