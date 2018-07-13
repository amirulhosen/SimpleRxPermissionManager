package com.example.amirul.simplepermissionmanager;

import android.Manifest;
import android.hardware.Camera;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.amirul.simplepermissionmanager.Permission.Permission;
import com.example.amirul.simplepermissionmanager.Permission.RxPermissions;
import com.jakewharton.rxbinding2.view.RxView;


import java.io.IOException;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    private Disposable disposable;
    private SurfaceView surfaceView;
    private Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.setLogging(true);
        TextView tv = (TextView) findViewById(R.id.text);
        surfaceView = findViewById(R.id.surfaceView);

        RxView.clicks(tv)
                .subscribe(click -> {
                    Toast.makeText(this, "RX clicked", Toast.LENGTH_SHORT).show();
                });

        disposable = RxView.clicks(findViewById(R.id.text))
                // Ask for permissions when button is clicked
                .compose(rxPermissions.ensureEachCombined(Manifest.permission.READ_EXTERNAL_STORAGE,Manifest.permission.CAMERA))
//                .compose(rxPermissions.ensureEach(Manifest.permission.READ_EXTERNAL_STORAGE))
                .subscribe(new Consumer<Permission>() {
                               @Override
                               public void accept(Permission permission) {
                                   Log.i("TAG", "Permission result " + permission);
                                   if (permission.granted) {
                                       releaseCamera();
                                       camera = Camera.open(0);
                                       try {
                                           camera.setPreviewDisplay(surfaceView.getHolder());
                                           camera.startPreview();
                                       } catch (IOException e) {
                                           Log.e("TAG", "Error while trying to display the camera preview", e);
                                       }
                                   } else if (permission.shouldShowRequestPermissionRationale) {
                                       // Denied permission without ask never again
                                       Toast.makeText(MainActivity.this,
                                               "Denied permission without ask never again",
                                               Toast.LENGTH_SHORT).show();
                                   } else {
                                       // Denied permission with ask never again
                                       // Need to go to the settings
                                       Toast.makeText(MainActivity.this,
                                               "Permission denied, can't enable the camera",
                                               Toast.LENGTH_SHORT).show();
                                   }
                               }
                           },
                        new Consumer<Throwable>() {
                            @Override
                            public void accept(Throwable t) {
                                Log.e("TAG", "onError", t);
                            }
                        },
                        new Action() {
                            @Override
                            public void run() {
                                Log.i("TAG", "OnComplete");
                            }
                        });
    }

    private void releaseCamera() {
        if (camera != null) {
            camera.release();
            camera = null;
        }
    }
}

