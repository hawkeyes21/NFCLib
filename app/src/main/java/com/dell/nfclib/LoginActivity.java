package com.dell.nfclib;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener
{
    private DatabaseReference mDatabase;
    private static final int RC_SIGN_IN = 101;
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    SignInButton signInButton;
    Button loginButton;
    TextView registerAccountTextView;
    EditText emailEditText, passwordEditText;
    ProgressBar progressBar;
    userDetails userDetails = new userDetails();
    int flag = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        signInButton = (SignInButton) findViewById(R.id.googleSignInButton);
        loginButton = (Button) findViewById(R.id.loginButton);
        registerAccountTextView = (TextView) findViewById(R.id.registerPageRedirectTextView);
        emailEditText = (EditText)findViewById(R.id.emailEditText);
        passwordEditText = (EditText)findViewById(R.id.passwordEditText);
        progressBar = (ProgressBar)findViewById(R.id.loginProgressBar);


        (findViewById(R.id.adminLoginTextView)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, AdminHomeActivity.class));
            }
        });

        // Redirects to the registration page...
        registerAccountTextView.setOnClickListener(this);
        loginButton.setOnClickListener(this);

        /*
         *  All the code below is related to 'Google Authentication API'
         *  DO NOT EDIT IT UNLESS YOU KNOW WHAT YOU ARE DOING!
         *
         *
         *
         *
         */

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);


        signInButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                signIn();
            }
        });
    } // onCreate() finishes here...*****************

    @Override
    protected void onStart()
    {
        super.onStart();
        /*
         * If the user did not logout the last time from the app, they will be redirected accordingly
         * as per their designation to either user homescreen or admin homescreen.
         */
        if(mAuth.getCurrentUser() != null)
        {
            DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
            DatabaseReference myRef = firebaseDatabase.child("users").child(mAuth.getCurrentUser().getUid());
             myRef.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        userDetails = dataSnapshot.getValue(userDetails.class);
                        if (userDetails.getDesignation().equals("admin"))
                        {
                            finish();
                            Intent intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                            // The flag is added to remove all the top activities from the stack...
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                        else
                        {
                            finish();
                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        }
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {
                        // Empty
                    }
                });
        }
    } // onStart() finishes here... *********************

    // GoogleSignIn Intent
    private void signIn()
    {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN)
        {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess())
            {
                progressBar.setVisibility(View.VISIBLE);
                GoogleSignInAccount account = result.getSignInAccount();
                firebaseAuthWithGoogle(account);

//            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
//            try {
//                // Google Sign In was successful, authenticate with Firebase
//                GoogleSignInAccount account = task.getResult(ApiException.class);
//                firebaseAuthWithGoogle(account);
//            } catch (ApiException e) {
//                // Google Sign In failed, update UI appropriately
//
//                // ...
//            }
            }
        }
    }

    /*
     * This method deals with the GoogleSignIn...
     * It also sees that only the new user data is written into the database, thus avoiding overwriting...
     *
     */

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct)
    {
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful())
                        {
                            // Sign in success, update UI with the signed-in user's information
                            // Get user details from the 'user' object..

                            FirebaseUser user = mAuth.getCurrentUser();
                            mDatabase = FirebaseDatabase.getInstance().getReference();

                            String email = user.getEmail();
                            String fullName = user.getDisplayName();
                            String designation = "user";

//                            int flag = 4;

                            final userDetails userDetails = new userDetails();
                            userDetails.setEmail(email);
                            userDetails.setFullName(fullName);
                            userDetails.setDesignation(designation);

                            boolean isNewUser = task.getResult().getAdditionalUserInfo().isNewUser();

                            /* The user details will only be inserted if the user is logging for the FIRST TIME...
                             * A new user will always have designation as 'user', and thus will always be redirected
                             * to the homepage by default
                             */
                            if(isNewUser)
                            {
                                mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).setValue(userDetails)
                                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<Void>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if(task.isSuccessful())
                                                {
                                                    Toast.makeText(getApplicationContext(), "Successfully Added to database", Toast.LENGTH_LONG)
                                                            .show();
                                                }
                                                else
                                                {
                                                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG)
                                                            .show();
                                                }
                                            }
                                        });
                            }

                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
                            DatabaseReference ref = databaseReference.child("users").child(mAuth.getCurrentUser().getUid());

                            /* Signing in as per the designation... As mentioned above, a new user will always
                             * be assigned designation as 'user' by default.
                             * This, it will launch activity accordingly...
                             *
                            */
                            ref.addListenerForSingleValueEvent(new ValueEventListener()
                            {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                {
                                    userDetails details = dataSnapshot.getValue(userDetails.class);
                                    if(details.getDesignation().equals("admin"))
                                    {
                                        finish();
                                        Intent intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                                        // The flag is added to remove all the top activities from the stack...
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    }
                                    else
                                    {
                                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                                        finish();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                }
                            });

                        }
                        else
                        {
                            // If sign in fails, display a message to the user.

                            Toast.makeText(getApplicationContext(), "Error", Toast.LENGTH_LONG).show();
                        }
                        // ...
                    }
                });
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.registerPageRedirectTextView:
                startActivity(new Intent(getApplicationContext(), RegistrationActivity.class));
                finish();
                break;

            case R.id.loginButton:
                userLogin();
                break;
        }
    }



    /*
     *
     * The below code deals with Email-Password Login...
     *
     *
     *
     *
     *
     *
     *
     */
    private void userLogin()
    {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if(email.isEmpty())
        {
            emailEditText.setError("email is required");
            emailEditText.requestFocus();
            return;
        }

        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            emailEditText.setError("enter a valid email");
            emailEditText.requestFocus();
            return;
        }

        if(password.isEmpty())
        {
            passwordEditText.setError("password is required");
            passwordEditText.requestFocus();
            return;
        }

        if(password.length() < 6)
        {
            passwordEditText.setError("password should be atleast 6 digits");
            passwordEditText.requestFocus();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>()
        {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task)
            {
                progressBar.setVisibility(View.GONE);

                if(task.isSuccessful())
                {
                    // The code below checks the user designation and signs in accordingly... it works.

                    DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference();
                    DatabaseReference myRef = firebaseDatabase.child("users").child(mAuth.getCurrentUser().getUid());

                    myRef.addListenerForSingleValueEvent(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            /*
                             * The NullPointerException can occur here in case when the user has registered successfully in the past,
                             * but there is no corresponding entry for the user in the database. Thus, the Firebase auth will pass the
                             * user verification test. But there will be no user entry in the database, thus throwing a NullPointer...
                             * The only case when this can occur that I can think of is when the database is edited manually, wherein
                             * a user entry is deleted... In any case, the user is logged out explicitly, so there will be no bugs while
                             * launching the app again...
                             */
                            try
                            {
                                userDetails = dataSnapshot.getValue(userDetails.class);
                                if(userDetails.getDesignation().equals("admin"))
                                {
                                    finish();
                                    Intent intent = new Intent(LoginActivity.this, AdminHomeActivity.class);
                                    // The flag is added to remove all the top activities from the stack...
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                                else
                                {
                                    finish();
                                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                                    // The flag is added to remove all the top activities from the stack...
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                }
                            }
                            catch(NullPointerException e)
                            {
                                Toast.makeText(getApplicationContext(), "Some error.\n"+e.getMessage(), Toast.LENGTH_LONG)
                                        .show();
                                mAuth.signOut();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {
                            // Empty
                        }
                    });



//                    boolean designation = firebaseDatabase.equals("admin");
//
//                    if(designation)
//                    {
//                        finish();
//                        startActivity(new Intent(LoginActivity.this, AdminHomeActivity.class));
//                    }
//                    else
//                    {
//                        finish();
//                        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
//                        // The flag is added to remove all the top activities from the stack...
//                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                        startActivity(intent);
//                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG)
                            .show();
                }
            }
        });
    }


    /*
     * Some problem in this method as it wasn't working in onStart()
     * The code here is being duplicated atleast 3 times.
     * The method would serve a great purpose if corrected...
     */
    private boolean isUserAdmin(DatabaseReference ref)
    {
        ref.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                userDetails data = dataSnapshot.getValue(userDetails.class);
                if(data.getDesignation().equals("admin"))
                {
                    flag = 1;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
        return flag == 1;
    }
}