package cn.slzhong.numberdetector;

import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;


public class MainActivity extends ActionBarActivity implements SurfaceHolder.Callback {

    private LinearLayout window;
    private Button shoot;
    private Button restart;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        window = (LinearLayout)findViewById(R.id.ll_window);
        shoot = (Button)findViewById(R.id.bt_shoot);
        shoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doShoot();
            }
        });
        restart = (Button)findViewById(R.id.bt_restart);
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRestart();
            }
        });

        surfaceView = (SurfaceView)findViewById(R.id.sv_preview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(MainActivity.this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open();
        try {
            camera.setPreviewDisplay(holder);
            Camera.Parameters parameters = camera.getParameters();
            List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
            int min = -1;
            for (int i = 0; i < sizes.size(); i++) {
                if (min == -1 || sizes.get(i).width < sizes.get(min).width) {
                    min = i;
                }
            }
            parameters.setPictureSize(sizes.get(min).width, sizes.get(min).height);
            parameters.setPictureFormat(ImageFormat.JPEG);
            parameters.setRotation(90);
            camera.setDisplayOrientation(90);
            camera.setParameters(parameters);
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null) {
            camera.startPreview();
            camera.release();
        }
    }

    private void doShoot() {
        camera.takePicture(null, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                AsyncTask<byte[], String, String> task = new AsyncTask<byte[], String, String>() {
                    @Override
                    protected String doInBackground(byte[]... params) {
                        File root;
                        if (Environment.isExternalStorageEmulated()) {
                            root = Environment.getExternalStorageDirectory();
                        } else {
                            root = Environment.getDataDirectory();
                        }

                        File picture = new File(root.toString() + "/test.jpg");
                        try {
                            FileOutputStream fos = new FileOutputStream(picture.getPath());
                            fos.write(params[0]);
                            fos.close();
                            System.out.println("*****done");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }
                };
                task.execute(data);
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            }
        });
    }

    private void doRestart() {
        if (camera != null) {
            camera.startPreview();
        }
    }
}
