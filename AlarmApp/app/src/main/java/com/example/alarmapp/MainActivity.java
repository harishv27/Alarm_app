package com.example.alarmapp;
//hi
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private TextView batteryStatus, thresholdValue;
    private SeekBar seekBar;
    private int threshold = -1;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Runnable batteryCheckRunnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        batteryStatus = findViewById(R.id.battery_status);
        thresholdValue = findViewById(R.id.threshold_value);
        seekBar = findViewById(R.id.seekBar);

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        mediaPlayer = MediaPlayer.create(this, alarmSound);

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                threshold = progress + 1;
                thresholdValue.setText("Threshold: " + threshold + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        startBatteryCheck();
    }

    private void startBatteryCheck() {
        batteryCheckRunnable = new Runnable() {
            @Override
            public void run() {
                checkBatteryStatus();
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(batteryCheckRunnable);
    }

    private void checkBatteryStatus() {
        Intent batteryIntent = registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        if (batteryIntent != null) {
            int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int batteryPct = (int) ((level / (float) scale) * 100);

            batteryStatus.setText("Battery Level: " + batteryPct + "%");

            if (threshold != -1) {
                if (batteryPct <= threshold) {
                    triggerAlarm();
                } else {
                    stopAlarm();
                }
            } else {
                stopAlarm();
            }
        }
    }

    private void triggerAlarm() {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    private void stopAlarm() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            mediaPlayer.seekTo(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
        }
        handler.removeCallbacks(batteryCheckRunnable);
    }
}