package com.quangedm2202.fithou_chat;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;

public class Screen extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks {
    TextView txtPhienban;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen);
        txtPhienban = (TextView) findViewById(R.id.txtPhienban);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(),0);
            txtPhienban.setText(getString(R.string.phienban) + " " + packageInfo.versionName);

            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent iDangNhap = new Intent(Screen.this,StartActivity.class);
                    startActivity(iDangNhap);
                    finish();
                }
            },2000);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
