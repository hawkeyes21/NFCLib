package com.dell.nfclib;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class AdminHomeActivity extends AppCompatActivity
{
    DrawerLayout drawerLayout;
    Toolbar toolbar;
    ActionBarDrawerToggle actionBarDrawerToggle;
    NavigationView navigationView;
    private GoogleApiClient mGoogleApiClient;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();;
    RecyclerView recyclerView;
    List<bookDetails> list;
    BookListAdapter adapter;
    DatabaseReference databaseReference;
    View headerView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);
        setUpToolbar();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Home");

        recyclerView = findViewById(R.id.myRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));

        list = new ArrayList<>();
        adapter = new BookListAdapter(list);
        databaseReference = FirebaseDatabase.getInstance().getReference().child("books");
        databaseReference.addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                bookDetails details = dataSnapshot.getValue(bookDetails.class);
                list.add(details);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
            {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
        recyclerView.setAdapter(adapter);



        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext()).addApi(Auth.GOOGLE_SIGN_IN_API).build();

        navigationView = findViewById(R.id.navigationView);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener()
        {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item)
            {
                switch (item.getItemId())
                {
                    case R.id.configureNFC:
                        startActivity(new Intent(AdminHomeActivity.this, AdminNFCConfigureActivity.class));
                        break;

                    case R.id.addBook:
                        startActivity(new Intent(AdminHomeActivity.this, AdminAddBookActivity.class));
                        break;

                    case R.id.removeBook:
                        startActivity(new Intent(AdminHomeActivity.this, AdminRemoveBookActivity.class));
                        break;

                    case R.id.issuebook:
                        startActivity(new Intent(AdminHomeActivity.this, AdminIssueBookActivity.class));
                        break;

                    case R.id.returnBook:
                        startActivity(new Intent(AdminHomeActivity.this, AdminReturnBookActivity.class));
                        break;

                    case R.id.viewUsers:
                        startActivity(new Intent(getApplicationContext(), UserListActivity.class));
                        break;

                    case R.id.removeUsers:
                        startActivity(new Intent(AdminHomeActivity.this, AdminRemoveUsersActivity.class));
                        break;

                    case R.id.signOut:
                        signOut();
                        break;


                }

                return false;
            }
        });

    }

    private void setUpToolbar()
    {
        // Getting reference to drawerLayout and toolbar and setting the custom toolbar as toolbar...
        drawerLayout = findViewById(R.id.drawerLayout);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // the event listener of the drawerLayout...
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);


//        headerName = navigationView.findViewById(R.id.adminHomeHeaderUserName);
//        headerEmail = navigationView.findViewById(R.id.adminHomeHeaderEmailTextView);
//
//        headerEmail.setText(mAuth.getCurrentUser().getEmail());

//        headerView = navigationView.getHeaderView(0);
//        TextView headerEmail = headerView.findViewById(R.id.adminHomeHeaderEmailTextView);
//        headerEmail.setText(mAuth.getCurrentUser().getEmail());
//        Toast.makeText(getApplicationContext(), mAuth.getCurrentUser().getEmail(), Toast.LENGTH_LONG)
//                .show();
        actionBarDrawerToggle.syncState();

    }

    private void signOut()
    {

        // Sign out code goes here...
        finish();
        mAuth.signOut();
        if (mGoogleApiClient.isConnected())
        {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient);
//                    mGoogleApiClient.disconnect();
//                    mGoogleApiClient.connect();
        }
        startActivity(new Intent(AdminHomeActivity.this, LoginActivity.class));

    }

    @Override
    protected void onStart()
    {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        if (mGoogleApiClient.isConnected())
        {
            mGoogleApiClient.disconnect();
        }
    }
}
