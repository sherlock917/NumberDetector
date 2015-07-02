package cn.slzhong.numberdetector;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;


public class MainActivity extends ActionBarActivity implements SurfaceHolder.Callback {

    private RelativeLayout window;
    private RelativeLayout container;
    private LinearLayout focus;
    private LinearLayout canvas;
    private Button shoot;
    private Button restart;
    private ImageView preview;
    private TextView result;

    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Camera camera;
    private Camera.Parameters cameraParameters;

    private int deviceWidth;
    private int deviceHeight;
    private int pictureWidth;
    private int pictureHeight;
    private double pictureRatio;
    private double focusWidthRatio;
    private double focusHeightRatio;

    private boolean previewStarted;

    private List<Bitmap> digits;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        initData();
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
            cameraParameters = camera.getParameters();
            List<Camera.Size> sizes = cameraParameters.getSupportedPictureSizes();
            int min = -1;
            for (int i = 0; i < sizes.size(); i++) {
                if (min == -1 || sizes.get(i).width < sizes.get(min).width) {
                    min = i;
                }
            }
            pictureWidth = sizes.get(min).width;
            pictureHeight = sizes.get(min).height;
            pictureRatio = (double) pictureWidth / pictureHeight;
            resizePreview();
            cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            cameraParameters.setPictureSize(pictureWidth, pictureHeight);
            cameraParameters.setPictureFormat(ImageFormat.JPEG);
            cameraParameters.setRotation(90);
            camera.setDisplayOrientation(90);
            camera.setParameters(cameraParameters);
            camera.startPreview();
            previewStarted = true;
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
            camera.stopPreview();
            camera.release();
        }
    }

    private void initViews() {
        window = (RelativeLayout) findViewById(R.id.rl_window);
        shoot = (Button) findViewById(R.id.bt_shoot);
        shoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doShoot();
            }
        });
        restart = (Button) findViewById(R.id.bt_restart);
        restart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doRestart();
            }
        });

        surfaceView = (SurfaceView) findViewById(R.id.sv_preview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(MainActivity.this);

        focus = (LinearLayout) findViewById(R.id.ll_focus);
        container = (RelativeLayout) findViewById(R.id.rl_container);
        canvas = (LinearLayout) findViewById(R.id.ll_canvas);
        preview = (ImageView) findViewById(R.id.iv_preview);
        result = (TextView) findViewById(R.id.tv_result);
    }

    private void initData() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        deviceWidth = dm.widthPixels;
        deviceHeight = dm.heightPixels;

        focusWidthRatio = 0.8;
        focusHeightRatio = 0.15;

        previewStarted = false;
    }

    private void resizePreview() {
        int width = deviceWidth;
        int height = (int) (deviceWidth * pictureRatio);
        int margin = (int) (-1 * height * focusHeightRatio * 2);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(width, height);
        layoutParams.setMargins(0, margin, 0, margin);
        container.setLayoutParams(layoutParams);

        ViewGroup.LayoutParams focusLayoutParams = focus.getLayoutParams();
        focusLayoutParams.width = (int) (width * focusWidthRatio);
        focusLayoutParams.height = (int) (height * focusHeightRatio);
        focus.setLayoutParams(focusLayoutParams);
    }

    private void doShoot() {
        if (camera != null && previewStarted) {
            previewStarted = false;
            camera.takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(cameraParameters);
                    Bitmap bitmap = createCroppedBitmap(data);
                    process(bitmap);
                }
            });
        }
    }

    private void doRestart() {
        if (camera != null) {
            cameraParameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(cameraParameters);
            camera.startPreview();
            previewStarted = true;
        }
    }

    private Bitmap createCroppedBitmap(byte[] data) {
        Bitmap src = BitmapFactory.decodeByteArray(data, 0, data.length);
        int width = src.getWidth();
        int height = src.getHeight();
        Bitmap dst = Bitmap.createBitmap(src,
                (int) (width * (1 - focusWidthRatio) / 2),
                (int) (height * (1 - focusHeightRatio) / 2),
                (int) (width * focusWidthRatio),
                (int) (height * focusHeightRatio));
        src.recycle();
        return dst;
    }

    private void saveBitmap(Bitmap bitmap, String name) {
        File root;
        if (Environment.isExternalStorageEmulated()) {
            root = Environment.getExternalStorageDirectory();
        } else {
            root = Environment.getDataDirectory();
        }

        byte[] bytes = Processor.bitmapToByteArray(bitmap);

        File picture = new File(root.toString() + "/" + name);
        try {
            FileOutputStream fos = new FileOutputStream(picture.getPath());
            fos.write(bytes);
            fos.close();
            System.out.println("*****saved " + name);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void process(Bitmap bitmap) {
        Detector detector = new Detector(bitmap);
        Bitmap processed = detector.getBitmap();
        preview.setImageBitmap(processed);

        List<Bitmap> bitmapList = detector.getBitmapList();
        canvas.removeAllViews();
        for (int i = 0; i < bitmapList.size(); i++) {
            ImageView imageView = new ImageView(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(40, 40);
            layoutParams.setMargins(2, 0, 2, 0);
            imageView.setLayoutParams(layoutParams);
            imageView.setBackgroundResource(R.drawable.bordered_red);
            imageView.setImageBitmap(bitmapList.get(i));
            canvas.addView(imageView);
        }

        String detected = detector.getResult();
        result.setText(detected);
    }
}
