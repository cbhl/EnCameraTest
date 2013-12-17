package com.encircleapp.encameratest;

import java.util.Arrays;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivity extends Activity {

    TextView cameraList;
    ProgressBar progressBar;
    View root;

    AsyncTask<Void, Integer, Camera> openCameraTask = new AsyncTask<Void, Integer, Camera>() {

        String cameraListString;

        @Override
        protected void onPreExecute() {
            cameraList.setText("");
            progressBar.setIndeterminate(true);
            progressBar.setVisibility(View.VISIBLE);
            super.onPreExecute();
        }

        @Override
        protected Camera doInBackground(Void... params) {
            int numberOfCameras = Camera.getNumberOfCameras();
            cameraListString = String.format("Number of Cameras: %d\n", numberOfCameras);

            CameraInfo ci = new CameraInfo();
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.getCameraInfo(i, ci);
                String face;
                if (ci.facing == CameraInfo.CAMERA_FACING_BACK) {
                    face = "Back";
                } else if (ci.facing == CameraInfo.CAMERA_FACING_FRONT) {
                    face = "Front";
                } else {
                    face = Integer.toString(ci.facing);
                }
                cameraListString += String.format("\n%d: %s %d\n", i, face, ci.orientation);
                try {
                    Camera c = Camera.open(i);
                    Parameters p = c.getParameters();
                    List<String> fm = p.getSupportedFlashModes();
                    cameraListString += String.format(
                            "Flash Modes: %s\n",
                            fm == null ? "None" : Arrays.toString(fm.toArray()));
                    List<String> fom = p.getSupportedFocusModes();
                    cameraListString += String.format(
                            "Focus Modes: %s\n",
                            fom == null ? "None" : Arrays.toString(fom.toArray()));
                    cameraListString += String.format(
                            "Picture Formats: %s\n",
                            Arrays.toString(p.getSupportedPictureFormats().toArray()));
                    cameraListString += "Picture Sizes:\n";
                    for (Size s : p.getSupportedPictureSizes()) {
                        cameraListString += String.format("    - %d x %d\n", s.width, s.height);
                    }
                    cameraListString += String.format(
                            "Preview Formats: %s\n",
                            Arrays.toString(p.getSupportedPreviewFormats().toArray()));
                    cameraListString += "Preview Sizes:\n";
                    for (Size s : p.getSupportedPreviewSizes()) {
                        cameraListString += String.format("    - %d x %d\n", s.width, s.height);
                    }
                    c.release();
                } catch (Exception e) {
                    e.printStackTrace();
                    cameraListString += String.format("Error opening camera %d.\n", i);
                }
                publishProgress(i, numberOfCameras);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Camera result) {
            progressBar.setVisibility(View.GONE);
            cameraListString = String.format("Root Bounds: %d x %d\n%s", root.getRight(), root.getBottom(), cameraListString);
            cameraList.setText(cameraListString);
            super.onPostExecute(result);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            int complete = values[0];
            int total = values[1];
            if ((total > 0) && (complete != total)) {
                progressBar.setIndeterminate(false);
                progressBar.setMax(total);
                progressBar.setProgress(complete);
            } else {
                progressBar.setIndeterminate(true);
            }
        }

    };

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void hideActionBar() {
        getActionBar().hide();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            hideActionBar();
        } else {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
        }
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        root = findViewById(R.id.root_relative_layout);
        cameraList = (TextView) findViewById(R.id.camera_list_tv);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        openCameraTask.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

}
