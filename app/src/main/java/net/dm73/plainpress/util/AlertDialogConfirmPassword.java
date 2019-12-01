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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import net.dm73.plainpress.R;
import net.dm73.plainpress.model.User;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.twitter.sdk.android.core.TwitterCore.TAG;


public class AlertDialogConfirmPassword extends DialogFragment {

    public static String NEW_EMAIL = "new_email";
    public static String NEW_PASWWORD = "new_password";
    public static String ACCESS_TOKEN = "access_token";
    private String newEmail;
    private String newPAssword;
    private String accessToken;
    private User user;
    private OnEmailCofirmListner mCallback;
    private RequestQueue queue;

    private String urlEmailUpdated = "/email/user/update-email";
    private String urlPasswordUpdated = "/email/user/update-password";


    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.confirm)
    Button confirm;
    @BindView(R.id.titleDialog)
    TextView textAlert;


    public AlertDialogConfirmPassword() {
    }

    public static AlertDialogConfirmPassword newInstance(String newEmail, String newPassword, String accessToken){
        AlertDialogConfirmPassword alertDialogConfirmEmail = new AlertDialogConfirmPassword();
        Bundle bundle = new Bundle();
        bundle.putString(NEW_EMAIL, newEmail);
        bundle.putString(NEW_PASWWORD, newPassword);
        bundle.putString(ACCESS_TOKEN, accessToken);
        alertDialogConfirmEmail.setArguments(bundle);
        return alertDialogConfirmEmail;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (getArguments() != null) {
            newEmail = getArguments().getString(NEW_EMAIL);
            newPAssword = getArguments().getString(NEW_PASWWORD);
            accessToken = getArguments().getString(ACCESS_TOKEN);
        }

        View rootView = inflater.inflate(R.layout.dialog_confirm_email, null);
        ButterKnife.bind(this, rootView);
        getDialog().getWindow().setBackgroundDrawableResource(R.drawable.background_dialog_confirm_email);

        textAlert.setTypeface(Typeface.createFromAsset(getContext().getAssets(), "fonts/myriad_pro_regular.ttf"));

        queue = Volley.newRequestQueue(getActivity());

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onPasswordConfirmed(validateAuthentification());
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

    private boolean validateAuthentification(){

        final boolean[] valid = {true};

        String userPassword = password.getText().toString();
        if(userPassword.isEmpty()){
            password.setError("Requierd.");
            valid[0] = false;
        }else{
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), userPassword);
            // Prompt the user to re-provide their sign-in credentials
            user.reauthenticate(credential)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                if(!newPAssword.isEmpty())
                                    user.updatePassword(newPAssword).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "Password updated");
                                            } else {
                                                Log.d(TAG, "Error password not updated");
                                            }
                                        }
                                    });

                                if(!newEmail.isEmpty()){
                                    user.updateEmail(newEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "email updated");
                                                Map<String, Object> mail = new HashMap<>();
                                                mail.put("email", newEmail);
                                                FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).updateChildren(mail);
                                            } else {
                                                Log.d(TAG, "Error email not updated");
                                            }
                                        }
                                    });
                                }
                            } else {
                                Log.d(TAG, "Error auth failed");
                            }
                        }
                    });
        }

        return valid[0];
    }


    public interface OnEmailCofirmListner {
        void onPasswordConfirmed(boolean valid);
    }

}
