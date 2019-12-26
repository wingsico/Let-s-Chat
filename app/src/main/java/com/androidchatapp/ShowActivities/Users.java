package com.androidchatapp.ShowActivities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.androidchatapp.R;
import com.androidchatapp.UtilitiesClasses.UserDetails;
import com.androidchatapp.UtilitiesClasses.Utils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Objects;

public class Users extends AppCompatActivity {
    ListView usersList;
    TextView noUsersText;
    ArrayList<String> al;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    ProgressDialog pd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);
        Toolbar mToolbar = (Toolbar) findViewById(R.id.chat_with_toolbar2);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(R.string.app_name);
        Button signout = (Button) findViewById(R.id.sign_out);
        pd = new ProgressDialog(this);
        pd.setMessage("Loading...");
        pd.show();
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    pd.dismiss();
                    Utils.intentWithClear(Users.this, Login.class);
                } else {
                    UserDetails.userID = user.getUid();
                    UserDetails.userEmail = user.getEmail();
                }
            }
        };

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("/users");
        usersList = (ListView) findViewById(R.id.usersList);
        noUsersText = (TextView) findViewById(R.id.noUsersText);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                al = new ArrayList<>();
//                Iterable<DataSnapshot> imagesDir = dataSnapshot.getChildren();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    if (!Objects.equals(child.getValue(), FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                        al.add(String.valueOf(child.getValue()));

                    }
                }
                if (al.isEmpty()) {
                    noUsersText.setVisibility(View.VISIBLE);
                    usersList.setVisibility(View.GONE);
                } else {
                    noUsersText.setVisibility(View.GONE);
                    usersList.setVisibility(View.VISIBLE);
                    usersList.setAdapter(new ArrayAdapter<String>(getApplicationContext(), R.layout.custom_list, al));
                }
                pd.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

        usersList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String checkChild = al.get(position);
                UserDetails.chatwithEmail = checkChild;
                Intent intent = new Intent(Users.this, Chat.class);
                intent.putExtra("chatwithEmail", checkChild);
                startActivity(intent);
            }
        });

        signout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UserDetails.chatwithEmail = "";
                UserDetails.userType = "";
                UserDetails.userEmail = "";
                UserDetails.chatwithID = "";
                UserDetails.userID = "";
                UserDetails.chatRef = null;
                FirebaseAuth.getInstance().signOut();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}