package xaidworkz.selectordemo;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by asit on 8/21/17.
 */

public class BaseActivity extends AppCompatActivity {

    Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
    }


    public void showAlert() {

    }

    public boolean handlePermission(String permission, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{permission}, requestCode);
                return false;
            } else {
                return true;
            }
        }
        return true;
    }

    public void snack(View view, String message){
        Snackbar.make(view,message,Snackbar.LENGTH_LONG).show();
    }
}
