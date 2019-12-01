package net.dm73.plainpress.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import net.dm73.plainpress.AdvancedSetting;
import net.dm73.plainpress.EditUserProfile;
import net.dm73.plainpress.R;
import net.dm73.plainpress.model.User;
import net.dm73.plainpress.util.UrlEncoder;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.twitter.sdk.android.core.TwitterCore.TAG;

public class UpdatePassword extends Fragment {

    @BindView(R.id.oldPassword)
    EditText oldPassword;
    @BindView(R.id.userPassowrd)
    EditText newPassword;
    @BindView(R.id.userPassowrdConfirmaton)
    EditText passowrdConfirmaton;
    @BindView(R.id.backButton)
    ImageView backButton;
    @BindView(R.id.updatePassword)
    Button updatePassword;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private RequestQueue queue;
    private String urlEmailUpdated = "/email/user/update-email";


    public UpdatePassword() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queue = Volley.newRequestQueue(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_update_password, container, false);
        ButterKnife.bind(UpdatePassword.this, view);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });

        updatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserPassword();
            }
        });
    }

    private boolean verifyForm() {
        boolean valid = false;

        String password = oldPassword.getText().toString();
        if (password.isEmpty()) {
            oldPassword.setError("Required.");
            valid = true;
        } else {
            oldPassword.setError(null);
        }

        String userNewPassword = newPassword.getText().toString();
        if (userNewPassword.isEmpty()) {
            newPassword.setError("Required.");
            valid = true;
        } else {
            newPassword.setError(null);
        }

        String userPassowrdConfirmaton = passowrdConfirmaton.getText().toString();
        if (userPassowrdConfirmaton.isEmpty()) {
            passowrdConfirmaton.setError("Required.");
            valid = true;
        } else {
            passowrdConfirmaton.setError(null);
        }

        if (!userNewPassword.isEmpty() && !userPassowrdConfirmaton.isEmpty() && !userNewPassword.equals(userPassowrdConfirmaton)) {
            passowrdConfirmaton.setError("Passwords are not identical.");
            valid = true;
        }

        return valid;
    }

    private void updateUserPassword() {

        if (verifyForm())
            return;

        progressBar.setVisibility(View.VISIBLE);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final User user = dataSnapshot.getValue(User.class);

                Log.e("userAccessToken", user.getAccessToken());

                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword.getText().toString());
                // Prompt the user to re-provide their sign-in credentials
                FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    FirebaseAuth.getInstance().getCurrentUser().updatePassword(newPassword.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressBar.setVisibility(View.GONE);
                                            if (task.isSuccessful()) {
                                                Log.d(TAG, "Password updated");
                                                Toast.makeText(getActivity(), "Password updated", Toast.LENGTH_SHORT).show();
                                                clearInputFormm();
                                                sendEmailToUser(user.getAccessToken() , FirebaseAuth.getInstance().getCurrentUser().getUid(), user.getNickName(), user.getEmail());
                                            } else {
                                                Log.d(TAG, "Error, password not updated");
                                                Toast.makeText(getActivity(), "Error, password not updaed", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                } else {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getActivity(), "Failed to update password", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getActivity(), "Failed to update password", Toast.LENGTH_SHORT).show();
            }
        };

        FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(valueEventListener);

    }

    private void sendEmailToUser(final String accessToken, final String idUser, final String userName, final String  email){
        Log.e("url welcom", UrlEncoder.decodedURL(urlEmailUpdated, idUser, accessToken));
        StringRequest postRequest = new StringRequest(Request.Method.POST, UrlEncoder.decodedURL(urlEmailUpdated, idUser, accessToken),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d("Response", response);
                    }
                },

                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if(error!=null)
                            Log.e("Response Error",error.toString());
                    }
                }
        ){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> header = new HashMap<>();
                header.put("Content-Type", "application/json");
                return header;
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                String param;
                if (userName != null && !userName.isEmpty())
                    param = String.format("{\"user\":{\"username\": \"%s\",\"email\": \"%s\"}}", userName, email);
                else
                    param = String.format("{\"user\":{\"username\": \"PlainPress User\",\"email\": \"%s\"}}", email);

                Log.e("params", param);

                return  param.getBytes();
            }
        };

        queue.add(postRequest.setRetryPolicy(new RetryPolicy() {
            @Override
            public int getCurrentTimeout() {
                return 50000;
            }

            @Override
            public int getCurrentRetryCount() {
                return 50000;
            }

            @Override
            public void retry(VolleyError error) throws VolleyError {

            }
        }));
    }

    private void clearInputFormm(){
        oldPassword.setText("");
        newPassword.setText("");
        passowrdConfirmaton.setText("");
    }

}
