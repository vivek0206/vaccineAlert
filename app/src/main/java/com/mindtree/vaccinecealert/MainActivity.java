package com.mindtree.vaccinecealert;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    Button startBtn,stopBtn;
    EditText pinEdt;

    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        startBtn=findViewById(R.id.startBtn);
        stopBtn=findViewById(R.id.stopBtn);
        pinEdt=findViewById(R.id.pinEdt);

        sharedPreferences = getSharedPreferences("sharedfile", MODE_PRIVATE);
        String storedPin=sharedPreferences.getString("pincode", null);
//        Toast.makeText(MainActivity.this,storedPin,Toast.LENGTH_LONG).show();

        if (storedPin==null)
        {
            pinEdt.setText("333704");
        }else pinEdt.setText(storedPin);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this,foreground.class);
                stopService(intent);
                String pin=pinEdt.getText().toString();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("pincode", pin);
                editor.commit();
//                Log.d("check1", pin);
//                Log.d("check2",sharedPreferences.getString("pincode", null));

                Toast.makeText(MainActivity.this,"Service started",Toast.LENGTH_SHORT).show();

                Intent serviceIntent = new Intent(getApplicationContext(), foreground.class);
                serviceIntent.putExtra("inputExtra", "Running");
                ContextCompat.startForegroundService(MainActivity.this, serviceIntent);
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this,"Service stopped",Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this,foreground.class);
                stopService(intent);
            }
        });
    }

}