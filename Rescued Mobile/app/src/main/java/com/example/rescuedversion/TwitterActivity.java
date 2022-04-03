package com.example.rescuedversion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class TwitterActivity extends AppCompatActivity {

    TextInputEditText etEmail;
    TextInputEditText etPassword;
    TextInputEditText etNumber;
    TextView btnTwitter;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter);

        etEmail = findViewById(R.id.emailtext);
        etPassword = findViewById(R.id.passwordtext);
        etNumber = findViewById(R.id.emailtext1);
        btnTwitter = findViewById(R.id.btntwitter);

        btnTwitter.setOnClickListener(view -> {
            createUser();
        });


    }

    private void createUser() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();
        String number = etNumber.getText().toString();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("");
            etEmail.requestFocus();
        } else if (TextUtils.isEmpty(password)) {
            etPassword.setError("");
            etPassword.requestFocus();
        } else if (TextUtils.isEmpty(number)) {
            etNumber.setError("");
            etNumber.requestFocus();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        startActivity(new Intent(TwitterActivity.this, LoginActivity.class));
                    } else {
                        Toast.makeText(TwitterActivity.this, "Registration Error" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

}