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

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    Toolbar toolbar;
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    ActionBarDrawerToggle actionBarDrawerToggle;

    RecyclerView recyclerView;
    BookListAdapter bookListAdapter;
    List<bookDetails> bookDetailsList;

    FirebaseAuth firebaseAuth;
    GoogleApiClient mGoogleApiClient;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        firebaseAuth = FirebaseAuth.getInstance();
        navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);

        mGoogleApiClient = new GoogleApiClient.Builder(getApplicationContext()) //Use app context to prevent leaks using activity
                //.enableAutoManage(this /* FragmentActivity */, connectionFailedListener)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        setUpToolbar();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Home");
        bookDetailsList = new ArrayList<>();
        bookListAdapter = new BookListAdapter(bookDetailsList);

        recyclerView = findViewById(R.id.userHomeRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));

        databaseReference = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = databaseReference.child("books");

        ref.addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                bookDetails bookD = dataSnapshot.getValue(bookDetails.class);
                bookDetailsList.add(bookD);
                bookListAdapter.notifyDataSetChanged();
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
        recyclerView.setAdapter(bookListAdapter);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.userMyBooks:
                startActivity(new Intent(HomeActivity.this, UserMyBooksActivity.class));
                break;

            case R.id.userHistory:
                startActivity(new Intent(HomeActivity.this, UserHistoryActivity.class));
                break;

            case R.id.userIssueBook:
                startActivity(new Intent(HomeActivity.this, UserIssueBookActivity.class));
                break;

            case R.id.userReturnBook:
                startActivity(new Intent(HomeActivity.this, UserReturnBookActivity.class));
                break;

            case R.id.userProfile:
                startActivity(new Intent(HomeActivity.this, UserProfileActivity.class));
                break;

            case R.id.userSettings:
                startActivity(new Intent(HomeActivity.this, UserSettingActivity.class));
                break;

            case R.id.userSignOut:
                firebaseAuth.signOut();
                if (mGoogleApiClient.isConnected())
                {
                    Auth.GoogleSignInApi.signOut(mGoogleApiClient);
                }
                finish();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                break;
        }
        return false;
    }

    private void setUpToolbar()
    {
        drawerLayout = findViewById(R.id.drawerLayoutHome);
        toolbar = findViewById(R.id.toolbarUser);
        setSupportActionBar(toolbar);


        actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.app_name, R.string.app_name);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
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
