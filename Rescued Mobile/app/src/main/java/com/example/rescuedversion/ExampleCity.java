package com.example.rescuedversion;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class ExampleCity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example_city);
    }

    /** Called when the user clicks on anything */
    public void sendExampleStore(View view) {
        Intent intent = new Intent(this, ExampleShop.class);
        startActivity(intent);
    }
}