package com.example.safehaven;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SelectContactsActivity extends AppCompatActivity {

    private static final int MAX_CONTACTS = 5;
    private static final int PERMISSION_REQUEST_READ_CONTACTS = 100;

    private ListView contactsListView;
    private Button selectContactButton, saveButton, clearButton;
    private ArrayAdapter<String> contactsAdapter;
    private List<String> selectedContacts;
    private SharedPreferences sharedPreferences;

    // New ActivityResultLauncher for Contact Picker
    private final ActivityResultLauncher<Intent> contactPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri contactUri = result.getData().getData();
                        String contactInfo = getContactInfo(contactUri);

                        if (contactInfo != null && !selectedContacts.contains(contactInfo)) {
                            selectedContacts.add(contactInfo);
                            contactsAdapter.notifyDataSetChanged();
                        }
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contacts);

        selectContactButton = findViewById(R.id.btn_select_contact);
        contactsListView = findViewById(R.id.contacts_list_view);
        saveButton = findViewById(R.id.btn_save_contacts);
        clearButton = findViewById(R.id.btn_clear_contacts);

        sharedPreferences = getSharedPreferences("SafeHavenContacts", Context.MODE_PRIVATE);
        selectedContacts = new ArrayList<>(sharedPreferences.getStringSet("sos_contacts", new HashSet<>()));

        contactsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, selectedContacts);
        contactsListView.setAdapter(contactsAdapter);

        // Select Contact Button Click Listener
        selectContactButton.setOnClickListener(v -> {
            if (selectedContacts.size() >= MAX_CONTACTS) {
                Toast.makeText(this, "You can only select up to 5 contacts", Toast.LENGTH_SHORT).show();
            } else {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                    pickContact();
                } else {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS}, PERMISSION_REQUEST_READ_CONTACTS);
                }
            }
        });

        // Save Contacts Button Click Listener
        saveButton.setOnClickListener(v -> {
            sharedPreferences.edit().putStringSet("sos_contacts", new HashSet<>(selectedContacts)).apply();
            Toast.makeText(this, "Contacts saved successfully!", Toast.LENGTH_SHORT).show();
            finish();
        });

        // Clear Contacts Button Click Listener
        clearButton.setOnClickListener(v -> {
            selectedContacts.clear();
            contactsAdapter.notifyDataSetChanged();
            sharedPreferences.edit().remove("sos_contacts").apply();
            Toast.makeText(this, "All contacts cleared!", Toast.LENGTH_SHORT).show();
        });

        // Remove contact on double-click
        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            private long lastClickTime = 0;
            private int lastClickedPosition = -1;

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                long clickTime = System.currentTimeMillis();
                if (position == lastClickedPosition && clickTime - lastClickTime < 500) {
                    String contact = selectedContacts.get(position);
                    selectedContacts.remove(contact);
                    contactsAdapter.notifyDataSetChanged();
                    Toast.makeText(SelectContactsActivity.this, "Contact removed", Toast.LENGTH_SHORT).show();
                }
                lastClickTime = clickTime;
                lastClickedPosition = position;
            }
        });
    }

    private void pickContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        contactPickerLauncher.launch(intent); // Using the new ActivityResultLauncher
    }

    private String getContactInfo(Uri contactUri) {
        ContentResolver resolver = getContentResolver();
        Cursor cursor = resolver.query(contactUri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int nameIndex = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
            int idIndex = cursor.getColumnIndex(ContactsContract.Contacts._ID);
            String contactName = cursor.getString(nameIndex);
            String contactId = cursor.getString(idIndex);

            Cursor phoneCursor = resolver.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                    new String[]{contactId},
                    null
            );

            if (phoneCursor != null && phoneCursor.moveToFirst()) {
                int phoneNumberIndex = phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                String phoneNumber = phoneCursor.getString(phoneNumberIndex);
                phoneCursor.close();
                cursor.close();
                return contactName + " - " + phoneNumber;
            }
            if (phoneCursor != null) phoneCursor.close();
        }
        if (cursor != null) cursor.close();
        return null;
    }

    // Handle permission request result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_REQUEST_READ_CONTACTS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickContact();
            } else {
                Toast.makeText(this, "Permission denied! Cannot access contacts.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
