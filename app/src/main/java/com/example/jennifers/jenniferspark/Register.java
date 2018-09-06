package com.example.jennifers.jenniferspark;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Register extends AppCompatActivity {
    /****************************/
    // VARIABLE DECLARATION
    /****************************/
    private EditText rname, rpassword, remail;
    private TextView rlogin;
    private Button rsignup;
    private ProgressDialog progress;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    //--------------------------------------------------------//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        initialize();
    }

    /**
     * Initialize method: This method initiailizes all components in Register activity.
     * It also sets up all listeners for all available buttons in Register activity.
     */
    private void initialize() {
        //Initialize components
        rname = (EditText) findViewById(R.id.RegisterNameField);
        rpassword = (EditText) findViewById(R.id.RegisterPasswordField);
        remail = (EditText) findViewById(R.id.RegisterEmailField);
        rlogin = (TextView) findViewById(R.id.RegisterLoginTextView);
        progress = new ProgressDialog(this);
        mAuth = FirebaseAuth.getInstance();
        rsignup = (Button) findViewById(R.id.RegisterSignUpBtn);
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");
        //.....................//

        //set underline for rlogin textview
        SpannableString content = new SpannableString("Signed up already? Login");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        rlogin.setText(content);
        //...........//

        //set up listener for buttons or links
        rlogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Register.this, Login.class));
            }
        });

        rsignup.setOnClickListener(new View.OnClickListener() {
                                       @Override
                                       public void onClick(View v) {
                                           register();
                                       }
                                   }
        );
        //...............//
    }

    /**
     * This method register a new user and save user data in database
     * The method will validate all user infomation input. If all are valid,
     * save the data in database. Otherwise, display a error message to user
     */
    private void register() {

        final String name = rname.getText().toString();
        String pass = rpassword.getText().toString();
        String email = remail.getText().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(pass) || TextUtils.isEmpty(email)) {
            if (TextUtils.isEmpty(name)) {
                rname.setError("Please enter your name");
            }
            if (TextUtils.isEmpty(pass)) {
                rpassword.setError("Please enter your password");
            }
            if (TextUtils.isEmpty(email)) {
                remail.setError("Please enter your email");
            }
        } else {
            if (inputValidation(pass, "pass") == 1) {
                rpassword.setError("Must contains at least 8 characters \n" +
                        "Must contains at least one alphabet \n" +
                        "Must contains at least one one digit");
            } else if (inputValidation(email, "email") == 2) {
                remail.setError("Your email is invalid. Please check again");
            } else {
                progress.setMessage("Signing up");
                progress.show();
                mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                Toast.makeText(Register.this, "Email has been already used" +
                                        " by another account", Toast.LENGTH_SHORT).show();
                            }
                            progress.dismiss();
                        } else {
                            String userid = mAuth.getCurrentUser().getUid();
                            String useremail = mAuth.getCurrentUser().getEmail();
                            DatabaseReference userinfo = mDatabase.child(userid);
                            userinfo.child("isAdmin").setValue(0);
                            userinfo.child("name").setValue(name);
                            userinfo.child("email").setValue(useremail);
                            progress.dismiss();
                            Toast.makeText(Register.this, "Signed up successfully", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(Register.this, Login.class));
                        }
                    }
                });
            }
        }
    }

    /**
     * This method check the validation of user information input
     * and return code for the validation
     *
     * @param inputText string value to be validated
     * @param inputType string type of data
     * @return validation code
     */
    private int inputValidation(String inputText, String inputType) {
        int valid = 0;
        if (inputType.equals("pass")) {
            String PASS_PATTERN = "([A-Za-z]?)([0-9]?)"; //Fix later
            if (inputText.length() < 8) {//|| !inputText.matches(PASS_PATTERN)){
                valid = 1;
            }
        } else if (inputType.equals("email")) {
            String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
            if (!inputText.matches(EMAIL_PATTERN)) {
                valid = 2;
            }
        }
        return valid;
    }

}
