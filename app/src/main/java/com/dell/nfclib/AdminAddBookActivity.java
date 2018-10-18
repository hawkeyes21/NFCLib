package com.dell.nfclib;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.net.Uri;
import android.nfc.NdefMessage;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.UnsupportedEncodingException;

public class AdminAddBookActivity extends AppCompatActivity
{
    String bookName, bookAuthor, bookId, bookNFC, bookQuantity, nfcId;
    Toolbar toolbar;
    String downloadUriString;
    nfcDetails nfcDetails;
    ImageView bookCoverImageView;
    Button addBookButton, scanNFCButton;
    EditText bookNameEditText, bookAuthorEditText, bookIdEditText, bookQuantityEditText;
    TextView bookNFCTextView;
    Uri filePath, downloadUrl;
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    DatabaseReference databaseReference, nfcTableReference;
    Button scanNfcButton;
    bookDetails details = new bookDetails();
    private final int PICK_IMAGE_REQUEST = 71;

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    Tag myTag;
    Context context;

    public static final String ERROR_DETECTED = "No NFC tag detected!";
    public static final String WRITE_SUCCESS = "Text written to the NFC tag successfully!";
    public static final String WRITE_ERROR = "Error during writing, is the NFC tag close enough to your device?";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_book);

        nfcDetails = new nfcDetails();
        bookCoverImageView = findViewById(R.id.bookCoverImageView);
        bookNameEditText = findViewById(R.id.bookNameEditText);
        bookAuthorEditText = findViewById(R.id.bookAuthorEditText);
        bookIdEditText = findViewById(R.id.bookIdEditText);
        bookNFCTextView = findViewById(R.id.scanNfcTextView);
        bookQuantityEditText = findViewById(R.id.bookQuantityEditText);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference();

        addBookButton = findViewById(R.id.addBookButton);

        toolbar = findViewById(R.id.myToolBar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Add Book");

        toolbar.setNavigationIcon(R.drawable.ic_arrow_back_black_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
                startActivity(new Intent(AdminAddBookActivity.this, AdminHomeActivity.class));
            }
        });

        bookCoverImageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                chooseImage();
            }
        });

        addBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                uploadImage();
            }
        });

        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
        }
        readFromIntent(getIntent());

        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };
    }

    /******************************************************************************
     **********************************Read From NFC Tag***************************
     ******************************************************************************/
    private void readFromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            buildTagViews(msgs);
        }
    }
    private void buildTagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) return;

        String text = "";
//        String tagId = new String(msgs[0].getRecords()[0].getType());
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
        int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"
        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");

        try {
            // Get the Text
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
            bookNFCTextView.setText(text);
        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding", e.toString());
        }

    }

    private void addBookData()
    {
//        String bookName = bookNameEditText.getText().toString().trim();
//        String bookAuthor = bookAuthorEditText.getText().toString().trim();
//        String bookId = bookIdEditText.getText().toString().trim();
//        String bookNFC = bookNFCEditText.getText().toString().trim();
//        String bookQuantity = bookQuantityEditText.getText().toString().trim();
//
//        if(bookName.isEmpty())
//        {
//            bookNameEditText.setError("Book name required");
//            bookNameEditText.requestFocus();
//            return;
//        }
//
//        if(bookAuthor.isEmpty())
//        {
//            bookAuthorEditText.setError("Author name required");
//            bookAuthorEditText.requestFocus();
//            return;
//        }
//
//        if(bookId.isEmpty())
//        {
//            bookIdEditText.setError("Book id required");
//            bookIdEditText.requestFocus();
//            return;
//        }
//
//        if(bookNFC.isEmpty())
//        {
//            bookNFCEditText.setError("Enter NFC manually or scan it");
//            bookNFCEditText.requestFocus();
//            return;
//        }
//
//        if(bookQuantity.isEmpty())
//        {
//            bookQuantityEditText.setError("Book quantity required");
//            bookQuantityEditText.requestFocus();
//            return;
//        }
//
//
//        details.setBookAuthor(bookAuthor);
//        details.setBookNFC(bookNFC);
//        details.setBookId(bookId);
//        details.setBookQuantity(bookQuantity);
//        details.setBookName(bookName);
//        details.setBookCoverURL(downloadUrl.toString());
//
//
//        // Enter the book detail into the database and somehow call the upload image method..
//        databaseReference.child("books").child(bookName).setValue(details).addOnCompleteListener(new OnCompleteListener<Void>() {
//            @Override
//            public void onComplete(@NonNull Task<Void> task)
//            {
//                if(task.isSuccessful())
//                {
//                    Toast.makeText(getApplicationContext(), "Successfully added!", Toast.LENGTH_LONG)
//                            .show();
//                }
//                else
//                {
//                    Toast.makeText(getApplicationContext(), "Error\n", Toast.LENGTH_LONG)
//                            .show();
//                }
//            }
//        });
        uploadImage();

    }

    // This methods starts the Intent to select image from gallery...
    private void chooseImage()
    {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Choose Image"), PICK_IMAGE_REQUEST);
    }

    // This method obtains the image selected from the above intent, and displays it into the ImageView...
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)
        {
            filePath = data.getData();
            try
            {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                bookCoverImageView.setImageBitmap(bitmap);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    /*
     * WORKING IMAGE UPLOAD AND DATABASE ENTRY METHOD
     * This method initially checks if any fields are empty. It then proceeds to upload the selected
     * image into the database, and gets a downloadableURL for the database.
     * It then puts all the details into the database including the downloadImageUrl
     */

    private void uploadImage()
    {
        bookName = bookNameEditText.getText().toString().trim();
        bookAuthor = bookAuthorEditText.getText().toString().trim();
        bookId = bookIdEditText.getText().toString().trim();
        bookNFC = bookNFCTextView.getText().toString().trim();
        bookQuantity = bookQuantityEditText.getText().toString().trim();

        if(bookName.isEmpty())
        {
            bookNameEditText.setError("Book name required");
            bookNameEditText.requestFocus();
            return;
        }

        if(bookAuthor.isEmpty())
        {
            bookAuthorEditText.setError("Author name required");
            bookAuthorEditText.requestFocus();
            return;
        }

        if(bookId.isEmpty())
        {
            bookIdEditText.setError("Book id required");
            bookIdEditText.requestFocus();
            return;
        }

        if(bookNFC.isEmpty())
        {
            bookNFCTextView.setError("Please scan the appropriate NFC.");
            return;
        }

        if(bookQuantity.isEmpty())
        {
            bookQuantityEditText.setError("Book quantity required");
            bookQuantityEditText.requestFocus();
            return;
        }

        // filePath == selected image path
        if(filePath != null)
        {
//            final ProgressDialog progressDialog = new ProgressDialog(this);
//            progressDialog.setTitle("Uploading...");
//            progressDialog.show();

            // the "location" or path where the image will be stored, along with the name of the image...
            final StorageReference ref = storageReference.child("images/"+ filePath.getLastPathSegment());

            // Creating a UploadTask
            UploadTask uploadTask = ref.putFile(filePath);

            //Adding a progress bar which will monitor the upload rate...
//            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>()
//            {
//                @Override
//                public void onProgress(UploadTask.TaskSnapshot taskSnapshot)
//                {
//                    double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
//                                    .getTotalByteCount());
//                            progressDialog.setMessage("Adding Book: "+(int)progress+"%");
//                }
//            });

            // Starting the upload task and getting a downloadUrl
            Task<Uri> task = uploadTask
                    .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>()
            {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception
                {
                    if(!task.isSuccessful())
                    {
                        throw task.getException();
                    }
                    return ref.getDownloadUrl();
                }
            })
            // Task is completed. Time to add everything into database.
            .addOnCompleteListener(new OnCompleteListener<Uri>()
            {
                @Override
                public void onComplete(@NonNull Task<Uri> task)
                {
                    if(task.isSuccessful())
                    {
                        downloadUrl = task.getResult();

                        details.setBookAuthor(bookAuthor);
                        details.setBookNFC(bookNFC);
                        details.setBookId(bookId);
                        details.setBookQuantity(bookQuantity);
                        details.setBookName(bookName);
                        details.setBookCoverURL(downloadUrl.toString());

                        // Entry into database
                        databaseReference.child("books").child(bookName).setValue(details);


                        nfcDetails.setBookName(bookName);
                        nfcDetails.setNfcId(bookNFC);
                        nfcTableReference = FirebaseDatabase.getInstance().getReference().child("nfc").child(bookNFC);
                        nfcTableReference.setValue(nfcDetails);

                        Toast.makeText(getApplicationContext(), "Data added in both tables", Toast.LENGTH_LONG).show();


                        // Terminating the progressDialog...
//                        progressDialog.dismiss();
                        Toast.makeText(getApplicationContext(), "Successfully added!", Toast.LENGTH_LONG)
                                .show();
                        finish();
                        startActivity(getIntent());
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Error\n"+task.getException().getMessage(), Toast.LENGTH_LONG)
                                .show();
                    }
                }
            });
        }
    }

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
