package com.ghurabah.jemaah;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.ParseAnalytics;


public class LoginActivity extends AppCompatActivity {

    Boolean isSignUp = true;
    private Button buttonLogIn;
    private TextView textViewToggleLogInMode;
    private EditText editTextConfirmPassword;
    private EditText editTextPassword;
    private EditText editTextUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViewHandles();
        setClickListeners();

        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    private void initViewHandles() {
        buttonLogIn = (Button) findViewById(R.id.button_log_in);
        textViewToggleLogInMode = (TextView) findViewById(R.id.text_view_toggle_log_in_mode);
        editTextConfirmPassword = (EditText) findViewById(R.id.edit_text_confirm_password);
        editTextPassword = (EditText) findViewById(R.id.edit_text_password);
        editTextUsername = (EditText) findViewById(R.id.edit_text_username);
    }

    private void setClickListeners() {

        buttonLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        textViewToggleLogInMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSignUp) {
                    setTextLogin();
                } else {
                    setTextSignUp();
                }
            }
        });

    }

    private void setTextSignUp() {
        buttonLogIn.setText("Sign Up");
        textViewToggleLogInMode.setText("Or, Login");
        editTextConfirmPassword.setVisibility(View.VISIBLE);
        editTextUsername.setHint("New Username");
        editTextPassword.setHint("New Password");
        isSignUp = true;
    }

    private void setTextLogin() {
        buttonLogIn.setText("Login");
        textViewToggleLogInMode.setText("Or, Sign Up");
        editTextConfirmPassword.setVisibility(View.GONE);
        editTextUsername.setHint("Username");
        editTextPassword.setHint("Password");
        isSignUp = false;
    }

}

