package com.example.jennifers.jenniferspark;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddParkingLot extends AppCompatActivity {
    private EditText title, address, city,state,zipcode,description;
    private Button addParkingbtn,bckbtn;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_parking_lot);
        initilize();
    }

//***************Help Method********************//

    private void initilize() {

        //Initialize all components

        title = (EditText) findViewById(R.id.parkinglottitletv);
        address = (EditText) findViewById(R.id.parkinglotaddresstv);
        city = (EditText) findViewById(R.id.parkinglotcitytv);
        state = (EditText) findViewById(R.id.parkinglotstatetv);
        zipcode = (EditText) findViewById(R.id.parkinglotziptv);
        description = (EditText) findViewById(R.id.parkingdesctv);
        addParkingbtn = (Button) findViewById(R.id.addparkinglotbtn);
        bckbtn=(Button)findViewById(R.id.addparkinglotbckbtn);

        //Access to Parking table on database
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Parkings");
        mAuth = FirebaseAuth.getInstance();
        bckbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AddParkingLot.this,Map.class));
            }
        });
        //..............................//

        // Set up listener for button
        addParkingbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewParkingLot();
            }
        });
        //............................//
    }
    //Inflate the menu on Activity
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sub_menu, menu);
        return true;
    }

    //Set up options for menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.Profile:
                startActivity(new Intent(this, Profile.class));
                return true;
            case R.id.SignOut:
                Toast.makeText(this, "You have signed out", Toast.LENGTH_SHORT).show();
                mAuth.signOut();
                startActivity(new Intent(this, Login.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addNewParkingLot() {
        if (!validation()) {

        } else {
            String titleval = title.getText().toString();
            String addressval = address.getText().toString();
            String cityval = city.getText().toString();
            String stateval = state.getText().toString();
            String zipcodeval = zipcode.getText().toString();
            String descval = description.getText().toString();
            mDatabase = mDatabase.child(cityval+stateval);
            mDatabase.push().setValue(new Parking(titleval, addressval, cityval,stateval,zipcodeval,descval), new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                    if (databaseError != null) {
                        Toast.makeText(getApplicationContext(), "Data could not be saved " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getApplicationContext(), "Data saved successfully.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(AddParkingLot.this, Map.class));
                    }
                }
            });
        }
    }

    private boolean validation() {
        return true;
    }
}