package com.fahadali.intranet.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import com.fahadali.intranet.R;
import com.fahadali.intranet.model.Student;
import com.fahadali.intranet.model.Token;
import com.fahadali.intranet.other.App;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener{

    private Button loginBtn;
    private EditText username, password;
    private String TAG = "LoginActivity";
    private Token token;
    private ProgressBar progressBar;
    private ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        getSupportActionBar().hide();
        username = findViewById(R.id.username_edit_text);
        password = findViewById(R.id.password_edit_text);
        progressBar = findViewById(R.id.login_pbar);
        loginBtn = findViewById(R.id.login_btn);
        loginBtn.setOnClickListener(this);
        logo = findViewById(R.id.ulogo);
        logo.setOnClickListener(this);




        // Uncomment when the student data has successfully been retrieved
//        if(App.instance.isUserSignedIn()) {
//            Log.i(TAG, "onCreate: User is signed in.. Redirecting to MainActivity");
//            Intent intent = new Intent(LoginActivity.this, Main2Activity.class);
//            startActivity(intent);
//            finish();
//        }


    }

    @Override
    public void onClick(View view) {

        if(view == loginBtn) {
            progressBar.setVisibility(View.VISIBLE);
            if(App.isOnline()) {
                // Login - Retrieve token

                if(this.username.getText().toString().trim().isEmpty()) {
                    retrieveToken("fahad@elev.dk", "Baba123.");
                } else {
                    retrieveToken(this.username.getText().toString(), this.password.getText().toString());
                }


            } else {
                App.shortToast(this, "Du er offline!");

            }
        }


        if(view == logo) {
            progressBar.setVisibility(View.VISIBLE);
            if(App.isOnline()) {
                // Login - Retrieve token
                retrieveToken("jp@elev.dk", "Baba123.");


            } else {
                App.shortToast(this, "Du er offline!");
                progressBar.setVisibility(View.INVISIBLE);

            }
        }
    }

    public void manageToken(final Token token) {
        if(token != null) {

            // Save in sharedprefs
            saveTokenLocally(token);

            // Get user data
            retrieveStudentData(token);


        }
    }

    private void retrieveStudentData(Token token) {

        Call<Student> call = App.instance.getUserClient().getStudent(token.getUsername(), token.getBearerToken());

        call.enqueue(new Callback<Student>() {
            @Override
            public void onResponse(Call<Student> call, Response<Student> response) {
                if(response.isSuccessful()) {
                    Student s = response.body();

                    Student.getInstance().setStudent(s);

                    App.shortToast(LoginActivity.this, "Retrieved student data!");
                    Log.i(TAG, "onResponse: Student retrieved = " + Student.getInstance().toString());

                    // Go to home menu
                    Intent intent = new Intent(LoginActivity.this, Main2Activity.class);
                    startActivity(intent);
                    finish();

                } else if(response.code() == 404) {
                    App.instance.signOut(LoginActivity.this);
                }
                else {
                    App.longToast(LoginActivity.this, "Error code: " + response.code());
                    Log.i(TAG, "onResponse: Response not successful. Code = " + response.code());


                }


            }

            @Override
            public void onFailure(Call<Student> call, Throwable t) {
                App.longToast(LoginActivity.this, "Failed to retrieve student data. Message: " + t.getMessage());

            }
        });
    }

    private void retrieveToken(String un, String pwd) {

        this.username.setText(un);
        this.password.setText(pwd);

        String username = this.username.getText().toString().trim();
        String password = this.password.getText().toString().trim();

        Call<Token> call = App.instance.getUserClient().login(username, password, "password");
        call.enqueue(new Callback<Token>() {
            @Override
            public void onResponse(Call<Token> call, Response<Token> response) {
                if(response.isSuccessful()) {
                    token = response.body();
                    manageToken(token);

                    Log.i(TAG, "onResponse: (isSuccessful) = " + token.getAccessToken());
                    App.longToast(LoginActivity.this, "Login response = "+ response.code());
                } else {
                    Log.e(TAG, "onResponse: (!isSuccessful) = " + response.errorBody());
                    App.longToast(LoginActivity.this,"HTTP status code = " + response.code());

                }


            }

            @Override
            public void onFailure(Call<Token> call, Throwable t) {

                App.longToast(LoginActivity.this, "Uknown error! (Check maybe connection or API?) "+t.getMessage());
                Log.e(TAG, "onFailure: " + t.getMessage(),t);

            }
        });

    }

    private void saveTokenLocally(Token token) {
            SharedPreferences sharedPreferences = getSharedPreferences(App.SHARED_PREFS, MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(Token.ACCESS_TOKEN, token.getAccessToken());
            editor.putString(Token.TOKEN_TYPE, token.getTokenType());
            editor.putString(Token.EXPIRES_IN, token.getExpiresIn());
            editor.putString(Token.USERNAME, token.getUsername());
            editor.putString(Token.EXPIRES, token.getExpires());
            editor.apply();
    }

}
