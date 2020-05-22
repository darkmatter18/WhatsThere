package com.arkadip.whatsthere;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.WindowManager;

public class MainActivity extends AppCompatActivity {

    private  String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.splash_main);

        new Handler().postDelayed(() -> {
            if (!hasPermissions(MainActivity.this, PERMISSIONS)) {
                int PERMISSION_ALL = 1;
                ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, PERMISSION_ALL);
            } else {
                openActivity();
            }
        }, 1000);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d("PERMISSION", "GIVEN");
        openActivity();
    }

    private void openActivity(){
        Intent i = new Intent(MainActivity.this, LogicActivity.class);
        startActivity(i);
        finish();
    }

    private boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        else{
            return false;
        }
        return true;
    }
}
