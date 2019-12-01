package net.dm73.plainpress;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.TwitterAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.twitter.sdk.android.core.TwitterSession;

import net.dm73.plainpress.util.ActivityConfig;
import net.dm73.plainpress.util.AlertDialogForgotPassword;
import net.dm73.plainpress.util.AlertDialogPolicyChecker;

import butterknife.BindView;
import butterknife.ButterKnife;
import it.sephiroth.android.library.easing.Back;
import it.sephiroth.android.library.easing.EasingManager;

public class SignActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener, AlertDialogForgotPassword.OnEmailCofirmListner, AlertDialogPolicyChecker.OnPolicyFormCheked {

    @BindView(R.id.emailField)
    EditText mEmailField;
    @BindView(R.id.passwordField)
    EditText mPasswordField;
    @BindView(R.id.emailFieldSingUp)
    EditText mEmailFieldSignUp;
    @BindView(R.id.passwordFieldSingUp)
    EditText mPasswordFieldSignUp;
    @BindView(R.id.verificationPasswordFieldSingUp)
    EditText mVerificationPasswordSignUp;
    @BindView(R.id.signIn)
    Button signIn;
    @BindView(R.id.signUp)
    Button signUp;
    @BindView(R.id.custom_fb_button)
    ImageButton customFaebookButton;
    @BindView(R.id.login_button_facebook)
    LoginButton loginFacebookButton;
    @BindView(R.id.google_img)
    ImageView google;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;
    @BindView(R.id.main_container)
    ViewGroup rootLayout;
    @BindView(R.id.forgot_pass)
    TextView forgotPass;
    @BindView(R.id.login_button)
    CardView signInButton;
    @BindView(R.id.singup_button)
    CardView singUpButton;


    private String TAGOOGLE = "Authentification";
    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;
    private static String SIGNIN = "signin";
    private static String SIGNUP = "signup";

    private CallbackManager mCallbackManager;
    private GoogleApiClient mGoogleApiClient;

    private FirebaseAuth mAuth;
    private DatabaseReference mRef;

    private AlertDialogPolicyChecker dialogPolicyChecker;

//    private TwitterLoginButton mLoginButton;
//    private ImageButton customTwitterButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Configure Twitter SDK
//        TwitterAuthConfig authConfig =  new TwitterAuthConfig(
//                getString(R.string.twitter_consumer_key),
//                getString(R.string.twitter_consumer_secret));
//        Fabric.with(this, new Twitter(authConfig));

        setContentView(R.layout.activity_sign);
        ButterKnife.bind(this);

        ActivityConfig.setStatusBarTranslucent(getWindow());

        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference();

        Typeface titiliumWebRegular = Typeface.createFromAsset(getAssets(), "fonts/titilliumweb_regular.ttf");
        Typeface titiliumWebBold = Typeface.createFromAsset(getAssets(), "fonts/titilliumweb_bold.ttf");
        TextView loginIn = (TextView) findViewById(R.id.log_in);
        TextView loginUp = (TextView) findViewById(R.id.log_up);
        loginIn.setTypeface(titiliumWebRegular);
        loginUp.setTypeface(titiliumWebRegular);
        forgotPass.setTypeface(titiliumWebRegular);
        signIn.setTypeface(titiliumWebBold);
        signUp.setTypeface(titiliumWebBold);


        Bundle extratBundle = getIntent().getExtras();
        if (extratBundle != null) {
            pagerSwitcher(extratBundle.getInt("BUTTON_CLICKED"));
        }


        //configuration hashkey facebook
//        try {
//            PackageInfo info = getPackageManager().getPackageInfo(
//                    "net.dm73.plainpress",
//                    PackageManager.GET_SIGNATURES);
//            for (Signature signature : info.signatures) {
//                MessageDigest md = MessageDigest.getInstance("SHA");
//                md.update(signature.toByteArray());
//                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
//            }
//        } catch (PackageManager.NameNotFoundException e) {
//
//        } catch (NoSuchAlgorithmException e) {
//
//        }

    }

    private void pagerSwitcher(int pageId) {
        switch (pageId) {
            case 0:
                showLogIn(signInButton);
                break;
            case 1:
                showSingUp(singUpButton);
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Initialize Google Login button
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
        // END Initialize Google Login button


        // Initialize Facebook Login button
        customFaebookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginFacebookButton.performClick();
            }
        });

        mCallbackManager = CallbackManager.Factory.create();
        loginFacebookButton.setReadPermissions("email", "public_profile");
        loginFacebookButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.e(TAG, "facebook:onSuccess:" + loginResult);
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.e(TAG, "facebook:onError", error);
                if (error.getMessage().equals("net::ERR_INTERNET_DISCONNECTED")) {
                    showAlertMessage("Interrupted connection or unreachable host.");
                }
            }
        });
        // END Initialize Facebook Login button

        // Initialize twitter login
//        customTwitterButton = (ImageButton) findViewById(R.id.custom_twitter_button);
//        mLoginButton = (TwitterLoginButton) findViewById(R.id.button_twitter_login);
//        customTwitterButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mLoginButton.performClick();
//            }
//        });
//
//        mLoginButton.setCallback(new Callback<TwitterSession>() {
//            @Override
//            public void success(Result<TwitterSession> result) {
//                Log.d(TAG, "twitterLogin:success" + result);
//                handleTwitterSession(result.data);
//            }
//
//            @Override
//            public void failure(TwitterException exception) {
//                Log.w(TAG, "twitterLogin:failure", exception);
//                updateUI(null);
//            }
//        });
        // [END initialize_twitter_login]

    }

    @Override
    protected void onResume() {
        super.onResume();

        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(mEmailField.getText().toString(), mPasswordField.getText().toString());
            }
        });

        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount(mEmailFieldSignUp.getText().toString(), mPasswordFieldSignUp.getText().toString());
            }
        });

        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInGoogle();
            }
        });

        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialogForgotPassword alertDialog = new AlertDialogForgotPassword();
                alertDialog.show(getSupportFragmentManager(), "Forgot_password");

            }
        });

    }

    @Override
    public void onEmailConfirmed(String email) {

        showProgressDialog();

        mAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            showAlertMessage("We have sent you instructions to reset your password!");
                        } else {
                            showAlertMessage("Failed to send reset email!");
                        }

                        hideProgressDialog();
                    }
                });

    }

    private void showProgressDialog() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressDialog() {
        progressBar.setVisibility(View.GONE);
    }

    private void showAlertMessage(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this).setMessage(message);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    /*
    * Sign in & up with email
    */
    //create a new account
    private void createAccount(String email, String password) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm(SIGNUP)) {
            return;
        }

        showProgressDialog();

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            String message = "";
                            try {
                                throw task.getException();
                            } catch (FirebaseNetworkException e) {
                                message = "Interrupted connection or unreachable host.";
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                if (e.getMessage().contains("The email address is badly formatted.")) {
                                    message = "You must enter a valid email address";
                                } else
                                    message = e.getMessage();
                            } catch (FirebaseAuthUserCollisionException e) {
                                message = e.getMessage();
                            } catch (Exception e) {
                                e.printStackTrace();
                                if (message.isEmpty()) {
                                    message = "Sorry, an internal error has occurred. !!";
                                }
                            }
                            //show Alert dialog for the specific error
                            showAlertMessage(message);
                            hideProgressDialog();
                        }

                        // [START_EXCLUDE]

                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
    }

    private void signIn(String email, String password) {
        Log.d(TAG, "signIn:" + email);
        if (!validateForm(SIGNIN)) {
            return;
        }

        showProgressDialog();

        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    updateUI(user);
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                    String message = "";
                    try {
                        throw task.getException();
                    } catch (FirebaseNetworkException e) {
                        message = "Interrupted connection or unreachable host.";
                    } catch (FirebaseAuthInvalidCredentialsException e) {
                        if (e.getMessage().contains("The email address is badly formatted.")) {
                            message = "You must enter a valid email address";
                        } else
                            message = e.getMessage();
                    } catch (FirebaseAuthInvalidUserException e) {
                        message = "There is no user record corresponding to this identifier.";
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (message.isEmpty()) {
                            message = "Sorry, an internal error has occurred. !!";
                        }
                    }
                    //show Alert dialog for the specific error
                    showAlertMessage(message);
                }

                hideProgressDialog();
                // [END_EXCLUDE]
            }
        });
        // [END sign_in_with_email]
    }

    //validate the form
    private boolean validateForm(String signView) {
        boolean valid = true;

        if (signView.equals(SIGNIN)) {

            String email = mEmailField.getText().toString();
            if (TextUtils.isEmpty(email)) {
                mEmailField.setError("Required.");
                valid = false;
            } else {
                mEmailField.setError(null);
            }

            String password = mPasswordField.getText().toString();
            if (TextUtils.isEmpty(password)) {
                mPasswordField.setError("Required.");
                valid = false;
            } else {
                mPasswordField.setError(null);
            }

        } else if (signView.equals(SIGNUP)) {

            String email = mEmailFieldSignUp.getText().toString();
            if (TextUtils.isEmpty(email)) {
                mEmailFieldSignUp.setError("Required.");
                valid = false;
            } else {
                mEmailFieldSignUp.setError(null);
            }

            String password = mPasswordFieldSignUp.getText().toString();
            if (TextUtils.isEmpty(password)) {
                mPasswordFieldSignUp.setError("Required.");
                valid = false;
            } else if (password.length() < 8) {
                mPasswordFieldSignUp.setError("Weak Password.");
                valid = false;
            } else {
                mPasswordFieldSignUp.setError(null);
            }

            String verificationPassword = mVerificationPasswordSignUp.getText().toString();
            if (TextUtils.isEmpty(verificationPassword)) {
                mVerificationPasswordSignUp.setError("Required.");
                valid = false;
            } else if (!verificationPassword.equals(password)) {
                mVerificationPasswordSignUp.setError("Passwords are not identical.");
                valid = false;
            } else {
                mVerificationPasswordSignUp.setError(null);
            }
        }

        return valid;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mGoogleApiClient != null)
            mGoogleApiClient.stopAutoManage(this);
    }

    /*
        *  Sign with google account
        */
    // [START onactivityresult]
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);
                Log.e(TAGOOGLE, "case 1");
            } else {
                // Google Sign In failed, update UI appropriately
                // [START_EXCLUDE]
                showAlertMessage("The connection with your gmail failed, pleaz try again !");
                // [END_EXCLUDE]
                Log.e(TAGOOGLE, "case 2");
            }
        }

        // Pass the activity result back to the Facebook SDK
        mCallbackManager.onActivityResult(requestCode, resultCode, data);

        // Pass the activity result to the Twitter login button.
//        mLoginButton.onActivityResult(requestCode, resultCode, data);
    }
// [END onactivityresult]

    // [START auth_with_google]
    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());
        // [START_EXCLUDE silent]
        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            String message = "";
                            try {
                                throw task.getException();
                            } catch (FirebaseNetworkException e) {
                                message = "Interrupted connection or unreachable host.";
                            } catch (Exception e) {
                                e.printStackTrace();
                                if (message.isEmpty())
                                    message = "Sorry, Unknown error !!";
                            }

                            showAlertMessage(message);
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }

    // [END auth_with_google]

    // [START signin]
    private void signInGoogle() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
// [END signin]

    /*
    * Sign with facebook account
    */
    // [START auth_with_facebook]
    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:");
        // [START_EXCLUDE silent]
        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());

                            String message = "";
                            try {
                                throw task.getException();
                            } catch (FirebaseNetworkException e) {
                                message = "Interrupted connection or unreachable host.";
                            } catch (Exception e) {
                                e.printStackTrace();
                                if (message.isEmpty())
                                    message = "Sorry, an internal error has occurred. !!";
                            }

                            showAlertMessage(message);
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }
    // [END auth_with_facebook]


    /*
    * Sign with twitter account
    */
    // [START auth_with_twitter]
    private void handleTwitterSession(TwitterSession session) {
        Log.d(TAG, "handleTwitterSession:" + session);
        // [START_EXCLUDE silent]
//        showProgressDialog();
        // [END_EXCLUDE]

        AuthCredential credential = TwitterAuthProvider.getCredential(
                session.getAuthToken().token,
                session.getAuthToken().secret);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(SignActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUI(null);
                        }

                        // [START_EXCLUDE]
//                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
    }
// [END auth_with_twitter]

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private void updateUI(FirebaseUser user) {

        if (user != null) {

            ValueEventListener userProfileListener = new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (loosedData(dataSnapshot)) {
                        dialogPolicyChecker = new AlertDialogPolicyChecker();
                        dialogPolicyChecker.setCancelable(false);
                        dialogPolicyChecker.show(getSupportFragmentManager(), "check policy");

                    } else {
                        startActivity(new Intent().setClass(SignActivity.this, MainActivity.class));
                    }

                    hideProgressDialog();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            mRef.child("users").child(user.getUid()).addListenerForSingleValueEvent(userProfileListener);


        } else {
            hideProgressDialog();
            startActivity(new Intent().setClass(SignActivity.this, SignActivity.class));
        }
    }

    private boolean loosedData(DataSnapshot dataSnapshot) {

        if (dataSnapshot == null)
            return true;

        if (!dataSnapshot.child("location").exists())
            return true;

        if (!(dataSnapshot.child("location").child("latitude").exists() && dataSnapshot.child("location").child("longitude").exists()))
            return true;

        if (!dataSnapshot.child("settings").exists())
            return true;

        if (!dataSnapshot.child("settings").child("categories").exists())
            return true;

        if (!dataSnapshot.child("settings").child("trackMe").exists())
            return true;

        if (!dataSnapshot.child("settings").child("nearby").exists())
            return true;

        return false;
    }


    public void showSingUp(View view) {
        final CardView animationCircle = (CardView) findViewById(R.id.animation_circle);
        final View animationFirstArist = findViewById(R.id.animation_first_arist);
        final View animationSecondArist = findViewById(R.id.animation_second_arist);
        final View animationSquare = findViewById(R.id.animation_square);
        final LinearLayout squareParent = (LinearLayout) animationSquare.getParent();
//        final TextView animationTV = (TextView) findViewById(R.id.animation_tv);
//        final ImageView twitterImageView = (ImageView) findViewById(R.id.twitter_img);
        final ImageView instagramImageView = (ImageView) findViewById(R.id.google_img);
//        final ImageView facebokImageView = (ImageView) findViewById(R.id.facebook_img);
        final View singupFormContainer = findViewById(R.id.signup_form_container);
        final View loginFormContainer = findViewById(R.id.login_form_container);
        final int backgroundColor = Color.WHITE;
//        final TextView singupTV = (TextView) findViewById(R.id.singup_big_tv);

        final float scale = getResources().getDisplayMetrics().density;

        final int circle_curr_margin = (int) (82 * scale + 0.5f);
        final int circle_target_margin = rootLayout.getWidth() - ((int) (70 * scale + 0.5f));

        final int first_curr_width = (int) (120 * scale + 0.5f);
        final int first_target_width = (int) (rootLayout.getHeight() * 1.3);

        final int first_curr_height = (int) (70 * scale + 0.5f);
        final int first_target_height = rootLayout.getWidth();

        final int first_curr_margin = (int) (40 * scale + 0.5f);
        final int first_target_margin = (int) (35 * scale + 0.5f);
        final int first_expand_margin = (first_curr_margin - first_target_height);

        final int square_target_width = rootLayout.getWidth();
        final int square_target_height = (int) (80 * scale + 0.5f);

//        final float tv_curr_x = findViewById(R.id.singup_tv).getX() + findViewById(R.id.singup_button).getX();
//        final float tv_curr_y = findViewById(R.id.singup_tv).getY() + findViewById(R.id.buttons_container).getY() + findViewById(R.id.singup_container).getY();
//
//        final float tv_target_x = findViewById(R.id.singup_big_tv).getX();
//        final float tv_target_y = findViewById(R.id.singup_big_tv).getY();

        final float tv_curr_size = 16;
        final float tv_target_size = 56;
//
//        final int tv_curr_color = Color.parseColor("#ffffff");
//        final int tv_target_color = Color.parseColor("#5cffffff");

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            Window window = getWindow();
//            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorAccentDark));
//        }

        squareParent.setGravity(Gravity.END);
//        animationTV.setText(R.string.sign_up);

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                int diff_margin = circle_curr_margin - circle_target_margin;
                int margin = circle_target_margin + (int) (diff_margin - (diff_margin * interpolatedTime));

                RelativeLayout.LayoutParams params_circle = (RelativeLayout.LayoutParams) animationCircle.getLayoutParams();
                params_circle.setMargins(0, 0, margin, (int) (40 * scale + 0.5f));
                animationCircle.requestLayout();


                int diff_width = first_curr_width - first_target_width;
                int width = first_target_width + (int) (diff_width - (diff_width * interpolatedTime));

                int diff_height = first_curr_height - first_target_height;
                int height = first_target_height + (int) (diff_height - ((diff_height - first_target_margin) * interpolatedTime));

                diff_margin = first_curr_margin - first_expand_margin;
                margin = first_expand_margin + (int) (diff_margin - (diff_margin * interpolatedTime));
                int margin_r = (int) (-(first_target_width - rootLayout.getWidth()) * interpolatedTime);

                RelativeLayout.LayoutParams params_first = (RelativeLayout.LayoutParams) animationFirstArist.getLayoutParams();
                params_first.setMargins(0, 0, margin_r, margin);
                params_first.width = width;
                params_first.height = height;
                animationFirstArist.requestLayout();

                animationFirstArist.setPivotX(0);
                animationFirstArist.setPivotY(0);
                animationFirstArist.setRotation(-90 * interpolatedTime);


                margin = first_curr_margin + (int) (first_target_margin * interpolatedTime);
                RelativeLayout.LayoutParams params_second = (RelativeLayout.LayoutParams) animationSecondArist.getLayoutParams();
                params_second.setMargins(0, 0, margin_r, margin);
                params_second.width = width;
                animationSecondArist.requestLayout();

                animationSecondArist.setPivotX(0);
                animationSecondArist.setPivotY(animationSecondArist.getHeight());
                animationSecondArist.setRotation(90 * interpolatedTime);

                animationSquare.getLayoutParams().width = (int) (square_target_width * interpolatedTime);
                animationSquare.requestLayout();

//                float diff_x = tv_curr_x - tv_target_x;
////                float x = tv_target_x + (diff_x - (diff_x * interpolatedTime));
//                float diff_y = tv_curr_y - tv_target_y;
////                float y = tv_target_y + (diff_y - (diff_y * interpolatedTime));

//                animationTV.setX(x);
//                animationTV.setY(y);
//                animationTV.requestLayout();

//                if (interpolatedTime >= 0.2f && interpolatedTime < 0.3f) {
//                    twitterImageView.setImageResource(R.drawable.ic_twitter_blue);
//                } else if (interpolatedTime >= 0.45f && interpolatedTime < 0.55f) {
//                    instagramImageView.setImageResource(R.drawable.ic_instagram_blue);
//                } else if (interpolatedTime >= 0.65f && interpolatedTime < 0.75f) {
//                    facebokImageView.setImageResource(R.drawable.ic_facebook_blue);
//                }

                singupFormContainer.setAlpha(interpolatedTime);
                loginFormContainer.setAlpha(1 - interpolatedTime);
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {
                findViewById(R.id.singup_container).setVisibility(View.INVISIBLE);
                animationCircle.setVisibility(View.VISIBLE);
                animationFirstArist.setVisibility(View.VISIBLE);
                animationSecondArist.setVisibility(View.VISIBLE);
                animationSquare.setVisibility(View.VISIBLE);
//                animationTV.setVisibility(View.VISIBLE);
                singupFormContainer.setVisibility(View.VISIBLE);

                animationFirstArist.bringToFront();
                squareParent.bringToFront();
                animationSecondArist.bringToFront();
                animationCircle.bringToFront();
                findViewById(R.id.buttons_container).bringToFront();
                singupFormContainer.bringToFront();
//                singupTV.bringToFront();
//                animationTV.bringToFront();

                animationFirstArist.setBackgroundColor(backgroundColor);
                animationSecondArist.setBackgroundColor(backgroundColor);
                animationCircle.setCardBackgroundColor(backgroundColor);
                animationSquare.setBackgroundColor(Color.TRANSPARENT);
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animationCircle.setVisibility(View.GONE);
                        animationFirstArist.setVisibility(View.GONE);
                        animationSecondArist.setVisibility(View.GONE);
//                        animationTV.setVisibility(View.GONE);
                        animationSquare.setVisibility(View.GONE);
                        findViewById(R.id.log_up).setVisibility(View.VISIBLE);
                    }
                }, 100);
//                rootLayout.setBackgroundColor(ContextCompat.getColor(SignActivity.this, R.color.colorPrimary));
//                ((View) animationSquare.getParent()).setBackgroundColor(ContextCompat.getColor(SignActivity.this, R.color.colorPrimary));
                findViewById(R.id.login_form_container).setVisibility(View.GONE);
//                findViewById(R.id.login_tv).setVisibility(View.GONE);
                showLoginButton();
            }
        });

        Animation a2 = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                animationSquare.getLayoutParams().height = (int) (square_target_height * interpolatedTime);
                animationSquare.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        ValueAnimator a3 = ValueAnimator.ofFloat(tv_curr_size, tv_target_size);
        a3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
//                animationTV.setTextSize(animatedValue);
            }
        });

//        ValueAnimator a4 = ValueAnimator.ofInt(tv_curr_color, tv_target_color);
//        a4.setEvaluator(new ArgbEvaluator());
//        a4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                int animatedValue = (int) animation.getAnimatedValue();
////                animationTV.setTextColor(animatedValue);
//            }
//        });

        a.setDuration(400);
        a2.setDuration(172);
        a3.setDuration(400);
//        a4.setDuration(400);

//        a4.start();
        a3.start();
        animationSquare.startAnimation(a2);
        animationCircle.startAnimation(a);
        // animationFirstArist.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_first_arist));
        //animationSecondArist.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_second_arist));
        singupFormContainer.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_form));
    }

    public void showLogIn(View view) {
        final CardView animationCircle = (CardView) findViewById(R.id.animation_circle);
        final View animationFirstArist = findViewById(R.id.animation_first_arist);
        final View animationSecondArist = findViewById(R.id.animation_second_arist);
        final View animationSquare = findViewById(R.id.animation_square);
        final LinearLayout squareParent = (LinearLayout) animationSquare.getParent();
//        final TextView animationTV = (TextView) findViewById(R.id.animation_tv);
//        final ImageView twitterImageView = (ImageView) findViewById(R.id.twitter_img);
        final ImageView instagramImageView = (ImageView) findViewById(R.id.google_img);
//        final ImageView facebokImageView = (ImageView) findViewById(R.id.facebook_img);
        final View singupFormContainer = findViewById(R.id.signup_form_container);
        final View loginFormContainer = findViewById(R.id.login_form_container);
//        final TextView loginTV = (TextView) findViewById(R.id.login_tv);
        final int backgrounColor = Color.WHITE;


        final float scale = getResources().getDisplayMetrics().density;

        final int circle_curr_margin = rootLayout.getWidth() - (int) (view.getWidth() - view.getX() - animationCircle.getWidth());
        final int circle_target_margin = 0;

        final int first_curr_width = (int) (108 * scale + 0.5f);
        final int first_target_width = (rootLayout.getHeight() * 2);

        final int first_curr_height = (int) (70 * scale + 0.5f);
        final int first_target_height = rootLayout.getWidth();

        final int first_curr_margin = (int) (40 * scale + 0.5f);
        final int first_target_margin = (int) (35 * scale + 0.5f);
        final int first_expand_margin = (first_curr_margin - first_target_height);
        final int first_curr_margin_r = rootLayout.getWidth() - first_curr_width;


        final int square_target_width = rootLayout.getWidth();
        final int square_target_height = (int) (80 * scale + 0.5f);

        final float tv_curr_x = findViewById(R.id.login_small_tv).getX() + findViewById(R.id.login_button).getX();
        final float tv_curr_y = findViewById(R.id.login_small_tv).getY() + findViewById(R.id.buttons_container).getY() + findViewById(R.id.login_container).getY();

//        final float tv_target_x = findViewById(R.id.login_tv).getX();
//        final float tv_target_y = findViewById(R.id.login_tv).getY();

        final float tv_curr_size = 16;
        final float tv_target_size = 56;

//        final int tv_curr_color = Color.parseColor("#ffccffcc");
//        final int tv_target_color = Color.parseColor("#5cffffff");


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        }

        squareParent.setGravity(Gravity.START);
//        animationTV.setText(R.string.log_in);

        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                int diff_margin = circle_curr_margin - circle_target_margin;
                int margin = circle_target_margin + (int) (diff_margin - (diff_margin * interpolatedTime));

                RelativeLayout.LayoutParams params_circle = (RelativeLayout.LayoutParams) animationCircle.getLayoutParams();
                params_circle.setMargins(0, 0, margin, (int) (40 * scale + 0.5f));
                animationCircle.requestLayout();


                int diff_width = first_curr_width - first_target_width;
                int width = first_target_width + (int) (diff_width - (diff_width * interpolatedTime));

                int diff_height = first_curr_height - first_target_height;
                int height = first_target_height + (int) (diff_height - ((diff_height - first_target_margin) * interpolatedTime));

                diff_margin = first_curr_margin - first_expand_margin;
                margin = first_expand_margin + (int) (diff_margin - (diff_margin * interpolatedTime));
                int margin_r = first_curr_margin_r - (int) (first_curr_margin_r * interpolatedTime);
                int margin_l = rootLayout.getWidth() - width < 0 ? rootLayout.getWidth() - width : 0;

                RelativeLayout.LayoutParams params_first = (RelativeLayout.LayoutParams) animationFirstArist.getLayoutParams();
                params_first.setMargins(margin_l, 0, margin_r, margin);
                params_first.width = width;
                params_first.height = height;
                animationFirstArist.requestLayout();

                animationFirstArist.setPivotX(animationFirstArist.getWidth());
                animationFirstArist.setPivotY(0);
                animationFirstArist.setRotation(90 * interpolatedTime);

                margin = first_curr_margin + (int) (first_target_margin * interpolatedTime);
                RelativeLayout.LayoutParams params_second = (RelativeLayout.LayoutParams) animationSecondArist.getLayoutParams();
                params_second.setMargins(0, 0, margin_r, margin);
                params_second.width = width;
                animationSecondArist.requestLayout();

                animationSecondArist.setPivotX(animationSecondArist.getWidth());
                animationSecondArist.setPivotY(animationSecondArist.getHeight());
                animationSecondArist.setRotation(-(90 * interpolatedTime));

                animationSquare.getLayoutParams().width = (int) (square_target_width * interpolatedTime);
                animationSquare.requestLayout();

//                float diff_x = tv_curr_x - tv_target_x;
//                float x = tv_target_x + (diff_x - (diff_x * interpolatedTime));
//                float diff_y = tv_curr_y - tv_target_y;
//                float y = tv_target_y + (diff_y - (diff_y * interpolatedTime));

//                animationTV.setX(x);
//                animationTV.setY(y);
//                animationTV.requestLayout();

//                if (interpolatedTime >= 0.2f && interpolatedTime < 0.3f) {
//                    facebokImageView.setImageResource(R.drawable.ic_facebook_pink);
//                } else if (interpolatedTime >= 0.45f && interpolatedTime < 0.55f) {
//                    instagramImageView.setImageResource(R.drawable.ic_instagram_pink);
//                } else if (interpolatedTime >= 0.65f && interpolatedTime < 0.75f) {
//                    twitterImageView.setImageResource(R.drawable.ic_twitter_pink);
//                }

                loginFormContainer.setAlpha(interpolatedTime);
                singupFormContainer.setAlpha(1 - interpolatedTime);
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        a.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation arg0) {
                animationFirstArist.setBackgroundColor(backgrounColor);
                animationSecondArist.setBackgroundColor(backgrounColor);
                animationCircle.setCardBackgroundColor(backgrounColor);
                animationSquare.setBackgroundColor(backgrounColor);

                animationFirstArist.setVisibility(View.VISIBLE);
                findViewById(R.id.login_container).setVisibility(View.INVISIBLE);
                animationSecondArist.setVisibility(View.VISIBLE);
                animationCircle.setVisibility(View.VISIBLE);
                animationSquare.setVisibility(View.VISIBLE);
//                animationTV.setVisibility(View.VISIBLE);
                loginFormContainer.setVisibility(View.VISIBLE);
//                loginTV.setVisibility(View.INVISIBLE);

                animationFirstArist.bringToFront();
                squareParent.bringToFront();
                animationSecondArist.bringToFront();
                animationCircle.bringToFront();
                findViewById(R.id.buttons_container).bringToFront();
                loginFormContainer.bringToFront();
//                loginTV.bringToFront();
//                animationTV.bringToFront();
            }

            @Override
            public void onAnimationRepeat(Animation arg0) {
            }

            @Override
            public void onAnimationEnd(Animation arg0) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        animationCircle.setVisibility(View.GONE);
                        animationFirstArist.setVisibility(View.GONE);
                        animationSecondArist.setVisibility(View.GONE);
//                        animationTV.setVisibility(View.GONE);
                        animationSquare.setVisibility(View.GONE);
//                        findViewById(R.id.login_tv).setVisibility(View.VISIBLE);
//                        findViewById(R.id.login_tv).bringToFront();
                    }
                }, 100);
//                rootLayout.setBackgroundColor(ContextCompat.getColor(SignActivity.this, R.color.colorAccent));
//                ((View) animationSquare.getParent()).setBackgroundColor(Color.WHITE);
                findViewById(R.id.signup_form_container).setVisibility(View.GONE);
//                findViewById(R.id.singup_big_tv).setVisibility(View.GONE);
                showSingupButton();
            }
        });

        Animation a2 = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                animationSquare.getLayoutParams().height = (int) (square_target_height * interpolatedTime);
                animationSquare.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        ValueAnimator a3 = ValueAnimator.ofFloat(tv_curr_size, tv_target_size);
        a3.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float animatedValue = (float) animation.getAnimatedValue();
//                animationTV.setTextSize(animatedValue);
            }
        });

//        ValueAnimator a4 = ValueAnimator.ofInt(tv_curr_color, tv_target_color);
//        a4.setEvaluator(new ArgbEvaluator());
//        a4.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                int animatedValue = (int) animation.getAnimatedValue();
////                animationTV.setTextColor(animatedValue);
//            }
//        });

        a.setDuration(400);
        a2.setDuration(172);
        a3.setDuration(400);
//        a4.setDuration(400);

//        a4.start();
        a3.start();
        animationSquare.startAnimation(a2);
        animationCircle.startAnimation(a);
        loginFormContainer.startAnimation(AnimationUtils.loadAnimation(this, R.anim.rotate_form_reverse));
    }

    private void showLoginButton() {
        final CardView singupButton = (CardView) findViewById(R.id.singup_button);
        final View loginButton = findViewById(R.id.login_button);

        loginButton.setVisibility(View.VISIBLE);
        loginButton.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        findViewById(R.id.login_container).setVisibility(View.VISIBLE);

        final float scale = getResources().getDisplayMetrics().density;
        final int curr_singup_margin = (int) (-35 * scale + 0.5f);
        final int target_singup_margin = -singupButton.getWidth();

        final int curr_login_margin = -loginButton.getMeasuredWidth();
        final int target_login_margin = (int) (-35 * scale + 0.5f);

        EasingManager manager = new EasingManager(new EasingManager.EasingCallback() {

            @Override
            public void onEasingValueChanged(double value, double oldValue) {
                int diff_margin = curr_singup_margin - target_singup_margin;
                int margin = target_singup_margin + (int) (diff_margin - (diff_margin * value));

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) singupButton.getLayoutParams();
                layoutParams.setMargins(0, 0, margin, 0);
                singupButton.requestLayout();

                diff_margin = curr_login_margin - target_login_margin;
                margin = target_login_margin + (int) (diff_margin - (diff_margin * value));

                layoutParams = (LinearLayout.LayoutParams) loginButton.getLayoutParams();
                layoutParams.leftMargin = margin;
                loginButton.requestLayout();
            }

            @Override
            public void onEasingStarted(double value) {
                int diff_margin = curr_singup_margin - target_singup_margin;
                int margin = target_singup_margin + (int) (diff_margin - (diff_margin * value));

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) singupButton.getLayoutParams();
                layoutParams.setMargins(0, 0, margin, 0);
                singupButton.requestLayout();

                diff_margin = curr_login_margin - target_login_margin;
                margin = target_login_margin + (int) (diff_margin - (diff_margin * value));

                layoutParams = (LinearLayout.LayoutParams) loginButton.getLayoutParams();
                layoutParams.setMargins(margin, 0, 0, 0);
                loginButton.requestLayout();
            }

            @Override
            public void onEasingFinished(double value) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) singupButton.getLayoutParams();
                layoutParams.setMargins(0, 0, target_singup_margin, 0);
                singupButton.requestLayout();


                layoutParams = (LinearLayout.LayoutParams) loginButton.getLayoutParams();
                layoutParams.setMargins(target_login_margin, 0, 0, 0);
                loginButton.requestLayout();

                singupButton.setVisibility(View.GONE);
            }
        });

        manager.start(Back.class, EasingManager.EaseType.EaseOut, 0, 1, 600);
    }

    private void showSingupButton() {
        final CardView singupButton = (CardView) findViewById(R.id.singup_button);
        final View loginButton = findViewById(R.id.login_button);

        singupButton.setVisibility(View.VISIBLE);
        singupButton.measure(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        findViewById(R.id.singup_container).setVisibility(View.VISIBLE);

        final float scale = getResources().getDisplayMetrics().density;
        final int curr_singup_margin = -singupButton.getWidth();
        final int target_singup_margin = (int) (-35 * scale + 0.5f);

        final int curr_login_margin = (int) (-35 * scale + 0.5f);
        final int target_login_margin = -loginButton.getMeasuredWidth();

        EasingManager manager = new EasingManager(new EasingManager.EasingCallback() {

            @Override
            public void onEasingValueChanged(double value, double oldValue) {
                int diff_margin = curr_singup_margin - target_singup_margin;
                int margin = target_singup_margin + (int) (diff_margin - (diff_margin * value));

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) singupButton.getLayoutParams();
                layoutParams.setMargins(0, 0, margin, 0);
                singupButton.requestLayout();

                diff_margin = curr_login_margin - target_login_margin;
                margin = target_login_margin + (int) (diff_margin - (diff_margin * value));

                layoutParams = (LinearLayout.LayoutParams) loginButton.getLayoutParams();
                layoutParams.leftMargin = margin;
                loginButton.requestLayout();
            }

            @Override
            public void onEasingStarted(double value) {
                int diff_margin = curr_singup_margin - target_singup_margin;
                int margin = target_singup_margin + (int) (diff_margin - (diff_margin * value));

                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) singupButton.getLayoutParams();
                layoutParams.setMargins(0, 0, margin, 0);
                singupButton.requestLayout();

                diff_margin = curr_login_margin - target_login_margin;
                margin = target_login_margin + (int) (diff_margin - (diff_margin * value));

                layoutParams = (LinearLayout.LayoutParams) loginButton.getLayoutParams();
                layoutParams.setMargins(margin, 0, 0, 0);
                loginButton.requestLayout();
            }

            @Override
            public void onEasingFinished(double value) {
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) singupButton.getLayoutParams();
                layoutParams.setMargins(0, 0, target_singup_margin, 0);
                singupButton.requestLayout();


                layoutParams = (LinearLayout.LayoutParams) loginButton.getLayoutParams();
                layoutParams.setMargins(target_login_margin, 0, 0, 0);
                loginButton.requestLayout();
                loginButton.setVisibility(View.GONE);
            }
        });

        manager.start(Back.class, EasingManager.EaseType.EaseOut, 0, 1, 600);
    }

    @Override
    public void onPolicyChecked(boolean checked) {
        if (checked) startActivity(new Intent().setClass(SignActivity.this, GetStarted.class));
        else {
            Log.e("current user", mAuth.getCurrentUser().getEmail());
            mAuth.getCurrentUser().delete();
        }
    }

}
