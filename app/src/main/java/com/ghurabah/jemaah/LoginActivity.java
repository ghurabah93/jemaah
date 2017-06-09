package com.ghurabah.jemaah;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;


public class LoginActivity extends AppCompatActivity {

    private Boolean isSignUp = true;

    private RelativeLayout relativeLayoutLogin;

    private ImageView imageViewJemaahLogo;

    private TextView textViewJemaahLogo;
    private TextView textViewToggleLogInMode;

    private EditText editTextConfirmPassword;
    private EditText editTextPassword;
    private EditText editTextUsername;

    private Button buttonLogIn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initViewHandles();
        setClickListeners();
        redirectIfLoggedIn();
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }

    private void initViewHandles() {
        relativeLayoutLogin = (RelativeLayout) findViewById(R.id.relative_layout_login);
        imageViewJemaahLogo = (ImageView) findViewById(R.id.image_view_jemaah_logo);
        textViewJemaahLogo = (TextView) findViewById(R.id.text_view_jemaah_logo);
        textViewToggleLogInMode = (TextView) findViewById(R.id.text_view_toggle_log_in_mode);
        editTextConfirmPassword = (EditText) findViewById(R.id.edit_text_confirm_password);
        editTextPassword = (EditText) findViewById(R.id.edit_text_password);
        editTextUsername = (EditText) findViewById(R.id.edit_text_username);
        buttonLogIn = (Button) findViewById(R.id.button_log_in);
    }

    private void setClickListeners() {

        relativeLayoutLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
            }

        });

        imageViewJemaahLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
            }
        });

        textViewJemaahLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboard();
            }
        });

        buttonLogIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isSignUp) {
                    if (editTextPassword.getText().toString().equals(editTextConfirmPassword.getText().toString())) {
                        ParseUser user = new ParseUser();

                        user.setUsername(editTextUsername.getText().toString());

                        user.setPassword(editTextPassword.getText().toString());

                        user.signUpInBackground(new SignUpCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e == null) {
                                    Log.i("Info", "user signed up");
                                    redirectIfLoggedIn();
                                } else {
                                    String message = e.getMessage();

                                    if (message.toLowerCase().contains("java")) {
                                        message = e.getMessage().substring(e.getMessage().indexOf(" "));
                                    }
                                    Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(LoginActivity.this, "Password does not match the confirm password", Toast.LENGTH_SHORT).show();
                    }


                } else {


                    ParseUser.logInInBackground(editTextUsername.getText().toString(), editTextPassword.getText().toString(), new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if (e == null) {
                                Log.i("Info", "user logged in");
                                redirectIfLoggedIn();
                            } else {
                                String message = e.getMessage();

                                if (message.toLowerCase().contains("java")) {
                                    message = e.getMessage().substring(e.getMessage().indexOf(" "));
                                }
                                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
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

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public void redirectIfLoggedIn() {
        if (ParseUser.getCurrentUser() != null) {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        }
    }

}

