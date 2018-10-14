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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity implements View.OnClickListener
{
    Button registerButton;
    TextView loginRedirectTextView;
    EditText nameEditText, passwordEditText, emailEditText;
    private FirebaseAuth mAuth;
    ProgressBar progressBar;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        registerButton = (Button)findViewById(R.id.registerButton);
        loginRedirectTextView = (TextView)findViewById(R.id.loginRedirectTextView);
        nameEditText = (EditText)findViewById(R.id.fullNameEditText);
        passwordEditText = (EditText)findViewById(R.id.passwordEditText);
        emailEditText = (EditText)findViewById(R.id.emailEditText);
        mAuth = FirebaseAuth.getInstance();
        progressBar = (ProgressBar)findViewById(R.id.registrationProgressBar);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        loginRedirectTextView.setOnClickListener(this);
        registerButton.setOnClickListener(this);
    }

    // The registration method which registers the user as well as displays any errors...
    private void registerUser()
    {
        final String fullName = nameEditText.getText().toString().trim();
        final String email = emailEditText.getText().toString().trim();
        final String password = passwordEditText.getText().toString().trim();
        final String designation = "user";


        if(fullName.isEmpty())
        {
            nameEditText.setError("name is required");
            nameEditText.requestFocus();
            return;
        }

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

        // Registers the user with email and password
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        progressBar.setVisibility(View.GONE);
                        if(task.isSuccessful())
                        {
                            // Adding the user to the database...
                            userDetails userDetails = new userDetails();
                            userDetails.setEmail(email);
                            userDetails.setFullName(fullName);
                            userDetails.setDesignation(designation);

                            mDatabase.child("users").child(mAuth.getCurrentUser().getUid()).setValue(userDetails)
                                    .addOnCompleteListener(RegistrationActivity.this, new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if(task.isSuccessful())
                                            {
                                                Toast.makeText(getApplicationContext(), "Successfully Added to database", Toast.LENGTH_LONG)
                                                        .show();
                                            }
                                        }
                                    });
                            finish();
                            Intent intent = new Intent(RegistrationActivity.this, HomeActivity.class);
                            // The flag is added to remove all the top activities from the stack...
                            //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                        else
                        {
                            if(task.getException() instanceof FirebaseAuthUserCollisionException)
                            {
                                Toast.makeText(getApplicationContext(), "You are already registered", Toast.LENGTH_LONG)
                                        .show();
                            }
                            else
                            {
                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG)
                                        .show();
                            }
                        }
                    }
                });
    }

    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.loginRedirectTextView:
                finish();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                break;
            case R.id.registerButton:
                registerUser();
                break;

        }
    }
}
