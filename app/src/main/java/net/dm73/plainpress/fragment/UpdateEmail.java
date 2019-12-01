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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import net.dm73.plainpress.R;
import net.dm73.plainpress.model.User;
import net.dm73.plainpress.util.UrlEncoder;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;


public class UpdateEmail extends Fragment {

    @BindView(R.id.backButton)
    ImageView backButton;
    @BindView(R.id.oldPassword)
    EditText oldPassword;
    @BindView(R.id.newEmail)
    EditText newEmail;
    @BindView(R.id.updateEmail)
    Button updateEmail;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    private RequestQueue queue;
    private String urlPasswordUpdated = "/email/user/update-password";

    public UpdateEmail() {
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
        View view =inflater.inflate(R.layout.fragment_update_email, container, false);
        ButterKnife.bind(UpdateEmail.this, view);
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

        updateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateUserEmail();
            }
        });
    }

    private boolean verifyForm() {
        boolean valid = false;

        String password = oldPassword.getText().toString();
        if(password.isEmpty()){
            oldPassword.setError("Required.");
            valid = true;
        }else{
            oldPassword.setError(null);
        }

        String userEmail = newEmail.getText().toString();
        if(userEmail.isEmpty()){
            newEmail.setError("Required");
            valid = true;
        }else if(!userEmail.contains("@") || !userEmail.contains(".")){
            newEmail.setError("Email mal formed.");
            valid = true;
        }else{
            newEmail.setError(null);
        }

        return valid;

    }

    private void updateUserEmail(){

        if(verifyForm())
            return;

        progressBar.setVisibility(View.VISIBLE);

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                final User user = dataSnapshot.getValue(User.class);

                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword.getText().toString());
                // Prompt the user to re-provide their sign-in credentials
                FirebaseAuth.getInstance().getCurrentUser().reauthenticate(credential)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    FirebaseAuth.getInstance().getCurrentUser().updateEmail(newEmail.getText().toString()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            progressBar.setVisibility(View.GONE);
                                            if (task.isSuccessful()) {
                                                Map<String, Object> mail = new HashMap<>();
                                                mail.put("email", newEmail.getText().toString());
                                                FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).updateChildren(mail);
                                                sendEmailToUser(user.getAccessToken(), FirebaseAuth.getInstance().getCurrentUser().getUid(), user.getNickName(), newEmail.getText().toString());
                                                clearInput();
                                                Toast.makeText(getActivity(), "Email updated", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getActivity(), "Failed to update your email", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                                }else {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(getActivity(), "Failed to update your email", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).addListenerForSingleValueEvent(valueEventListener);
    }

    private void sendEmailToUser(final String accessToken, final String idUser, final String userName, final String  email){
        Log.e("url welcom", UrlEncoder.decodedURL(urlPasswordUpdated, idUser, accessToken));
        StringRequest postRequest = new StringRequest(Request.Method.POST, UrlEncoder.decodedURL(urlPasswordUpdated, idUser, accessToken), new Response.Listener<String>() {
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

    private void clearInput(){
        oldPassword.setText("");
        newEmail.setText("");
    }

}
