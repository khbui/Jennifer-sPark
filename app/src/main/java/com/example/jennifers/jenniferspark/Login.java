package com.example.jennifers.jenniferspark;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity {
    /****************************/
    // VARIABLE DECLARATION
    /****************************/
    private EditText lpassword, lemail;
    private TextView lregister, lforgotpass;
    private Button lsignin;
    private ProgressDialog progress;
    //    private ClientStorage clientStorage;
    private FirebaseAuth mAuth;

    //--------------------------------------------------------//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initialize();
    }

    private void initialize() {
        lpassword = (EditText) findViewById(R.id.LoginPasswordField);
        lemail = (EditText) findViewById(R.id.LoginEmailField);
        lregister = (TextView) findViewById(R.id.LoginRegisterTextView);
        lforgotpass = (TextView) findViewById(R.id.forgotpasswordtv);
        //Set color for lregister textview
        SpannableString content = new SpannableString("Register");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        SpannableString fgpass = new SpannableString("Forgot your password?");
        fgpass.setSpan(new UnderlineSpan(), 0, fgpass.length(), 0);
        lregister.setText(content);
        lforgotpass.setText(fgpass);
        //......................//
        lsignin = (Button) findViewById(R.id.LoginBtn);
        progress = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();

        lregister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Login.this, Register.class));
            }
        });
        lsignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });
        lforgotpass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword();
            }
        });
    }

    private void login() {
        String email = lemail.getText().toString();
        String pass = lpassword.getText().toString();
        if (TextUtils.isEmpty(pass) || TextUtils.isEmpty(email)) {
            if (TextUtils.isEmpty(pass)) {
                lpassword.setError("Please enter your password");
            }
            if (TextUtils.isEmpty(email)) {
                lemail.setError("Please enter your email");
            }
        } else {
            if (!inputValidation(email)) {
                lemail.setError("Your email is invalid. Please check again");
            } else {
                progress.setMessage("Signing in");
                progress.show();
                mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                Toast.makeText(Login.this, "There is no user record" +
                                        "corresponding to this identifier", Toast.LENGTH_SHORT).show();
                            }
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(Login.this, "The password is invalid", Toast.LENGTH_SHORT).show();
                            }
                            progress.dismiss();
                        } else {
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(mAuth.getCurrentUser().getUid());
                            databaseReference.addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    User user = dataSnapshot.getValue(User.class);
                                    Toast.makeText(Login.this, "Signed in. Welcome " + user.getName(), Toast.LENGTH_SHORT).show();
                                    progress.dismiss();
                                    startActivity(new Intent(Login.this, Map.class));
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }
                    }
                });
            }
        }
    }

    private void resetPassword() {
        final View view = (LayoutInflater.from(Login.this)).inflate(R.layout.reset_password, null);
        final EditText resetpwedt = (EditText) view.findViewById(R.id.resetpasswdedt);
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setCancelable(false);
        alertDialog.setView(view);
        alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Cancel
            }
        });
        alertDialog.setPositiveButton("Send Reset Email", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final String email = resetpwedt.getText().toString();
                FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Login.this, "Email Sent", Toast.LENGTH_SHORT).show();
                        }
                        else{
                            Toast.makeText(Login.this, "Sending error.Please check your email", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        Dialog dialogName = alertDialog.create();
        dialogName.show();
    }

    private boolean inputValidation(String inputText) {
        boolean valid = true;
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        if (!inputText.matches(EMAIL_PATTERN)) {
            valid = false;
        }
        return valid;
    }
}
