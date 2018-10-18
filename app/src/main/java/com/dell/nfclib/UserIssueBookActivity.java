package com.dell.nfclib;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.UnsupportedEncodingException;

public class UserIssueBookActivity extends AppCompatActivity
{
    ConstraintLayout imageConstraintLayout;
    Toolbar toolbar;
    storingData storingData = new storingData();
    DatabaseReference databaseReference, nfcReference, bookReference;
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    Tag myTag;
    Context context;
    nfcDetails nfcDetails;
    bookDetails bookDetails;
    String nfcId, bookName;
    public static final String ERROR_DETECTED = "No NFC tag detected!";
    public static final String WRITE_SUCCESS = "Text written to the NFC tag successfully!";
    public static final String WRITE_ERROR = "Error during writing, is the NFC tag close enough to your device?";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_issue_book);
        databaseReference = FirebaseDatabase.getInstance().getReference();
        imageConstraintLayout = findViewById(R.id.userScanDetailsConstraintLayout);

        toolbar = findViewById(R.id.myToolBar);
        context = getApplicationContext();
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Issue a Book");
        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
                startActivity(new Intent(UserIssueBookActivity.this, HomeActivity.class));
            }
        });

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null)
        {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
        }
        readFromIntent(getIntent());

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[]{tagDetected};
    }


    /******************************************************************************
     **********************************Read From NFC Tag***************************
     ******************************************************************************/

    // getting and NdefMessage object?
    private void readFromIntent(Intent intent)
    {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action))
        {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null)
            {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++)
                {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            buildTagViews(msgs);
        }
    }

    private void buildTagViews(NdefMessage[] msgs)
    {
        if (msgs == null || msgs.length == 0) return;

        String text = "";
//        String tagId = new String(msgs[0].getRecords()[0].getType());
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
        int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"
        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");

        try
        {
            // Get the Text
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
            imageConstraintLayout.setVisibility(View.VISIBLE);
        } catch (UnsupportedEncodingException e)
        {
            Log.e("UnsupportedEncoding", e.toString());
        }

//        Toast.makeText(getApplicationContext(), text, Toast.LENGTH_LONG)
//                .show();

        nfcId = text;

        nfcReference = FirebaseDatabase.getInstance().getReference().child("nfc").child(text);

        nfcReference.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                nfcDetails = dataSnapshot.getValue(com.dell.nfclib.nfcDetails.class);
                bookReference = FirebaseDatabase.getInstance().getReference().child("books").child(nfcDetails.getBookName());

                bookReference.addListenerForSingleValueEvent(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        bookDetails = dataSnapshot.getValue(bookDetails.class);

                        final ImageView imageView = findViewById(R.id.userScanBookCover);
                        final TextView authorName = findViewById(R.id.userScanAuthorName);
                        final TextView bookName = findViewById(R.id.userScanBookName);

                        bookName.setText(bookDetails.getBookName());
                        authorName.setText(bookDetails.getBookAuthor());
                        Picasso.get().load(bookDetails.getBookCoverURL()).into(imageView);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {
                        Log.e("TAG", databaseError.getMessage());
                    }
                });

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });

//        Toast.makeText(getApplicationContext(), storingData.getBookName(), Toast.LENGTH_LONG)
//                .show();
//        // nfcDisplayTextView.setText(text);
//        nfcId = text;
//
//        databaseReference.child("nfc").child(nfcId).addListenerForSingleValueEvent(new ValueEventListener()
//        {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//            {
//                details = dataSnapshot.getValue(nfcDetails.class);
////                bookName = details.getBookName();
////                Toast.makeText(getApplicationContext(), nfcId+"\n"+bookName, Toast.LENGTH_LONG)
////                        .show();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError)
//            {
////                Toast.makeText(getApplicationContext(), databaseError.toString(), Toast.LENGTH_LONG)
////                        .show();
//            }
//        });
////        nfcReference.addChildEventListener(new ChildEventListener()
////        {
////            @Override
////            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
////            {
////                nfcDetails details = dataSnapshot.getValue(nfcDetails.class);
////                bookName = details.getNfcId();
////                Toast.makeText(getApplicationContext(), nfcId+"\n"+bookName, Toast.LENGTH_LONG)
////                        .show();
////            }
////
////            @Override
////            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
////            {
//////                nfcDetails details = dataSnapshot.getValue(nfcDetails.class);
//////                bookName = details.getBookName();
//////                Toast.makeText(getApplicationContext(), nfcId+"\n"+bookName, Toast.LENGTH_LONG)
//////                        .show();
////            }
////
////            @Override
////            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
////            {
////
////            }
////
////            @Override
////            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
////            {
////
////            }
////
////            @Override
////            public void onCancelled(@NonNull DatabaseError databaseError)
////            {
////
////            }
////        });
//
//        Toast.makeText(getApplicationContext(), nfcId+"\n"+bookName, Toast.LENGTH_LONG)
//                .show();
//
////         Some error below
//
//        bookName = details.getBookName();
//
//        bookReference = FirebaseDatabase.getInstance().getReference().child("books").child(bookName);
//        final ImageView imageView = findViewById(R.id.userScanBookCover);
//        final TextView authorName = findViewById(R.id.userScanAuthorName);
//        final TextView bookName = findViewById(R.id.userScanBookName);
//
//        bookReference.addChildEventListener(new ChildEventListener()
//        {
//            @Override
//            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
//            {
////                imageConstraintLayout.setVisibility(View.VISIBLE);
//                details1 = dataSnapshot.getValue(bookDetails.class);
//
//            }
//
//            @Override
//            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
//            {
//
//            }
//
//            @Override
//            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
//            {
//
//            }
//
//            @Override
//            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
//            {
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError)
//            {
//
//            }
//        });
//
//
//        Picasso.get().load(details1.getBookCoverURL()).into((imageView));
//        authorName.setText(authorName.getText().toString());
//        bookName.setText(bookName.getText().toString());
//
//        Toast.makeText(getApplicationContext(), nfcId+"\n"+bookName, Toast.LENGTH_LONG)
//                .show();
    }

//    private void getNfcId(final firebaseCallback firebaseCallback)
//    {
//        nfcReference = FirebaseDatabase.getInstance().getReference().child("nfc").child(nfcId);
//
//        nfcReference.addListenerForSingleValueEvent(new ValueEventListener()
//        {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
//            {
//                nfcDetails = dataSnapshot.getValue(com.dell.nfclib.nfcDetails.class);
//                firebaseCallback.onCallBack(nfcDetails.getBookName());
//
////                Toast.makeText(getApplicationContext(), nfcDetails.getBookName(), Toast.LENGTH_LONG)
////                .show();
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError)
//            {
//
//            }
//        });
//
//        Toast.makeText(getApplicationContext(), storingData.getBookName(), Toast.LENGTH_LONG)
//                .show();
//    }
//
//    private interface firebaseCallback
//    {
//        void onCallBack(String s);
//    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        setIntent(intent);
        readFromIntent(intent);
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction()))
        {
            myTag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        WriteModeOff();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        WriteModeOn();
    }


    /******************************************************************************
     **********************************Enable Write********************************
     ******************************************************************************/
    private void WriteModeOn()
    {
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }

    /******************************************************************************
     **********************************Disable Write*******************************
     ******************************************************************************/
    private void WriteModeOff()
    {
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
    }
}
