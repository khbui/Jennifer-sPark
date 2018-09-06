package com.example.jennifers.jenniferspark;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Profile extends ActionBarActivity {
    private TextView profilename, profileemail, changename, changepass;
    private Button backbtn;
    private FirebaseAuth mAuth;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initialize();
    }

    private void initialize() {
        mAuth = FirebaseAuth.getInstance();
        profileemail = (TextView) findViewById(R.id.profileemailtv);
        profilename = (TextView) findViewById(R.id.profilenametv);
        changename = (TextView) findViewById(R.id.profilechangenametv);
        changepass = (TextView) findViewById(R.id.profilechangepasstv);
        getCurrentUserInfo();
        backbtn = (Button) findViewById(R.id.profilebackbtn);
        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Profile.this, Map.class));
            }
        });
        changename.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeName();
            }
        });
        changepass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changePassword();
            }
        });
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

    /**
     * Pop up a window for user input. Users enter their new name
     * and confirm thier decision. If yes, update thier name on server side
     * New name is updated on client as well.
     */
    private void changeName() {
        //Create the layout view with edit text to input name
        final View view = (LayoutInflater.from(Profile.this)).inflate(R.layout.change_user_name, null);
        final EditText nametv = (EditText) view.findViewById(R.id.newnameed);
        //Build dialog and set the layout view
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setCancelable(false);
        alertDialog.setView(view);
        //Set action buttons for dialog
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Cancel
            }
        });
        alertDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Get user name and execute
                final String newname = nametv.getText().toString();
                if (newname.length() == 0) {
                    Toast.makeText(Profile.this, "You cannot use empty name", Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder confirmDialog = new AlertDialog.Builder(Profile.this);
                    confirmDialog.setTitle("Confirm?").setMessage("Are you sure to change your name?");
                    confirmDialog.setCancelable(false);
                    confirmDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });
                    confirmDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid());
                            User updatedUser = new User(newname, currentUser.getEmail(), currentUser.getIsAdmin());
                            databaseReference.setValue(updatedUser);

                            Toast.makeText(Profile.this, "Updated Successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Profile.this,Profile.class));
                        }
                    });
                    Dialog dialogConfirm = confirmDialog.create();
                    dialogConfirm.show();
                }
            }
        });
        Dialog dialogName = alertDialog.create();
        dialogName.show();
    }

    private void changePassword() {
        final View view = (LayoutInflater.from(Profile.this)).inflate(R.layout.change_user_pass, null);
        final EditText oldpassed = (EditText) view.findViewById(R.id.oldpasswordev);
        final EditText newpassed = (EditText) view.findViewById(R.id.newpassworded);
        final EditText confirmpassed = (EditText) view.findViewById(R.id.confirmnewpassworded);
        //Build dialog and set the layout view
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setCancelable(false);
        alertDialog.setView(view);
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Cancel
            }
        });
        alertDialog.setPositiveButton("Update", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Get data from user
                final String oldpass = oldpassed.getText().toString();
                final String newpass = newpassed.getText().toString();
                String confirm = confirmpassed.getText().toString();
                //Check empty data
                if (oldpass.length() == 0 || newpass.length() == 0 || confirm.length() == 0)
                    Toast.makeText(Profile.this, "Data missing", Toast.LENGTH_SHORT).show();
                else {
                    if (!inputValidation(newpass)) {
                        Toast.makeText(Profile.this, "Password must be 8 characters", Toast.LENGTH_SHORT).show();
                        return;
                    } else if (!newpass.equals(confirm)) {
                        Toast.makeText(Profile.this, "Confirm password is not match", Toast.LENGTH_SHORT).show();
                        return;
                    } else {
                        final AlertDialog.Builder confirmDialog = new AlertDialog.Builder(Profile.this);
                        confirmDialog.setTitle("Confirm?").setMessage("Are you sure to change your password?");
                        confirmDialog.setCancelable(false);
                        confirmDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //Do nothing
                            }
                        });
                        confirmDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final FirebaseUser user = mAuth.getInstance().getCurrentUser();
                                // Get auth credentials from the user for re-authentication.
                                AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldpass);

                                // Prompt the user to re-provide their sign-in credentials
                                user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            user.updatePassword(newpass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(Profile.this, "Password updated", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(Profile.this, "Error password not updated", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                        } else {
                                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                                Toast.makeText(Profile.this, "Reauthorization failed", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(Profile.this, "Error in authorization", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                            }
                        });
                        Dialog dialogConfirm = confirmDialog.create();
                        dialogConfirm.show();
                    }
                }
            }
        });
        Dialog dialogName = alertDialog.create();
        dialogName.show();
    }

    //**********************Helper*******************************/
    private void getCurrentUserInfo() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                profileemail.setText(currentUser.getEmail());
                profilename.setText(currentUser.getName());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    private boolean inputValidation(String inputText) {
        boolean valid = false;
        if (inputText.length() >= 8) {
            valid = true;
        }
        return valid;
    }
}
