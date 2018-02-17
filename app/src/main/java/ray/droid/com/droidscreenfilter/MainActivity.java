package ray.droid.com.droidscreenfilter;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

public class MainActivity extends Activity implements SeekBar.OnSeekBarChangeListener {

    SharedMemory shared;

    SeekBar alphaSeek;
    SeekBar redSeek;
    SeekBar greenSeek;
    SeekBar blueSeek;
    RelativeLayout relLay;
    private View.OnTouchListener onTouchListener;

    int alpha, red, green, blue;

    public final static int REQUEST_CODE = 10101;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (checkDrawOverlayPermission()) {
            initialize();
        }
        else finish();

    }

    private void initialize() {

        stopServiceIfActive();

        shared = new SharedMemory(this);
        alphaSeek = (SeekBar) findViewById(R.id.alphaControl);
        alphaSeek.setOnSeekBarChangeListener(this);
        alpha = shared.getAlpha();
        alphaSeek.setProgress(alpha);
        updateColor();
        relLay = findViewById(R.id.relLay);
        onTouchListener = new TouchListener();
        relLay.setOnTouchListener(onTouchListener);

    }

    private void stopServiceIfActive() {
        if (MainService.STATE == MainService.ACTIVE) {
            Intent i = new Intent(MainActivity.this, MainService.class);
            stopService(i);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        if (seekBar == alphaSeek) {
            alpha = seekBar.getProgress();
        }
        updateColor();
    }

    private void updateColor() {
        int color = SharedMemory.getColor(alpha, red, green, blue);
        ColorDrawable cd = new ColorDrawable(color);
        getWindow().setBackgroundDrawable(cd);
    }

    @Override
    public void onStartTrackingTouch(SeekBar sb) {
        stopServiceIfActive();
    }

    @Override
    public void onStopTrackingTouch(SeekBar sb) {
        shared.setAlpha(alpha);
        Intent i = new Intent(MainActivity.this, MainService.class);
        startService(i);
        stopServiceIfActive();
    }

    private void ApllyFilter() {
        stopServiceIfActive();
        shared.setAlpha(alpha);
        Intent i = new Intent(MainActivity.this, MainService.class);
        startService(i);
        finish();
    }

    private class TouchListener implements View.OnTouchListener {

        private GestureDetector gestureDetector = new GestureDetector(MainActivity.this, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                finish();
                return super.onDoubleTap(e);
            }

            @Override
            public void onLongPress(MotionEvent e) {
                ApllyFilter();
                super.onLongPress(e);
            }


            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {


                return super.onSingleTapConfirmed(e);
            }
        });

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            gestureDetector.onTouchEvent(event);
            return true;
        }

    }

    public boolean checkDrawOverlayPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (!Settings.canDrawOverlays(this)) {
            // if not construct intent to request permission //
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            // request permission via start activity for result //
            startActivityForResult(intent, REQUEST_CODE);
            return false;
        } else {
            return true;
        }
    }


}
