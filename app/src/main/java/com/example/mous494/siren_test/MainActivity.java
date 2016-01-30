package com.example.mous494.siren_test;


import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "AudioActivity";
    private AudioTrack audioTrack = null;
    protected short[] buffer = null;
    protected int SAMPLERATE = 22050; // [Hz]
    protected static final int CHANNEL = 1;     // 1:MONO, 2:STEREO
    protected static final int BITRATE = 16;    // [bit/sec]

    private int pressbutoon;

    // signal funcion params
    static private double amplification = 1.1235;  // [0.0, 1.0]
    private double duration;       // [sec]
    private double minfriq;
    private double maxfriq;
    private double uptime; //stand up time[sec]
    private double downtime; //stand down time
    private double totaltime;
    private double firsttime;
    private double lasttime;


    private ImageView imageViews[] = new ImageView[10];

    private int stopsignal = 0;
    private boolean signalflag;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gui);

        for (int i = 0; i < 10; i++) {
            imageViews[i] = (ImageView) findViewById(getResources().getIdentifier("imageView" + (i + 1), "id", getPackageName()));
            imageViews[i].setOnClickListener(this);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                SAMPLERATE,  //[Hz]
                AudioFormat.CHANNEL_CONFIGURATION_MONO,
                AudioFormat.ENCODING_PCM_16BIT, //[bit]
                AudioTrack.getMinBufferSize(SAMPLERATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)*2, //[byte]
                AudioTrack.MODE_STREAM);
        audioTrack.setPlaybackPositionUpdateListener(new AudioTrack.OnPlaybackPositionUpdateListener() {
            @Override
            //サイクル終わりの処理（続けるのか終わるのか
            public void onMarkerReached(AudioTrack track) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //停止ボタンが押されているとき（次サイクルが最後
                        if (stopsignal == 1) {
                            totaltime = uptime + duration + lasttime;
                            stopsignal = 2;
                            audioTrack.write(buffer, 0, (int) (totaltime * SAMPLERATE));
                            audioTrack.setNotificationMarkerPosition((int) (totaltime * SAMPLERATE) - (SAMPLERATE / 10));
                        }
                        //最後のサイクルが終わったとき
                        if (stopsignal == 2) {
                            audioTrack.stop();
                            stopsignal = 0;

                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    imageViews[2].setClickable(true);
                                    imageViews[2].setImageResource(R.drawable.twosec);
                                    imageViews[3].setClickable(true);
                                    imageViews[3].setImageResource(R.drawable.foursec);
                                    imageViews[4].setClickable(true);

                                }
                            });
                         //通常のサイクル（次があるとき
                        } else {
                            audioTrack.write(buffer, 0, (int) (totaltime * SAMPLERATE));
                            audioTrack.setNotificationMarkerPosition((int) (totaltime * SAMPLERATE) - (SAMPLERATE / 10));
                        }
                    }
                }).start();

            }

            @Override
            public void onPeriodicNotification(AudioTrack track) {

            }
        });
        //start.setEnabled(true);

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onStop() {
        if (buffer != null) {
            buffer = null;
        }
        if (audioTrack != null) {
            audioTrack.stop();
            audioTrack.release(); // release buffer
            audioTrack = null;
        }
        super.onStop();
    }

    public void onClick(View v) {

        //PRESS 2sec
        if (v == imageViews[2]) {
            //Set params(first)
            maxfriq = 1500;
            uptime = 1;
            downtime = 2;
            duration = 2;
            minfriq = 1100;
            firsttime = 1;
            lasttime = 14;

            if (audioTrack != null) {
                if (audioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
                    totaltime = firsttime + duration + downtime;
                    startsound();
                }
                pressbutoon = 2;
                imageViews[2].setImageResource(R.drawable.twoseca);
                imageViews[2].setClickable(false);
                imageViews[3].setClickable(false);
                imageViews[4].setClickable(false);
            }

        }

        //PRESS 4sec
        if (v == imageViews[3]) {
            //Set params(first)
            maxfriq = 870;
            uptime = 2;
            downtime = 2;
            duration = 4;
            minfriq = 470;
            firsttime = 4;
            lasttime = 14;

            if (audioTrack != null) {
                if (audioTrack.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
                    totaltime = firsttime + duration + downtime;
                    startsound();
                }
                pressbutoon = 4;
                imageViews[3].setImageResource(R.drawable.fourseca);
                imageViews[2].setClickable(false);
                imageViews[3].setClickable(false);
                imageViews[4].setClickable(false);
            }
        }

        //PRESS stop
        if (v == imageViews[9]) {
            if (audioTrack != null) {
                /*if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                    audioTrack.stop();
                    start.setEnabled(true);
                }*/
                if (audioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                    generateBuffer(2);
                    stopsignal = 1;
                    signalflag = true;

                    //ボタン点滅用の処理
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            int i = 2;
                            String s = "";
                            String sa = "";
                            if (pressbutoon == 2) {
                                i = 2;
                                s = "twosec";
                                sa = "twoseca";
                            } else if (pressbutoon == 4) {
                                i = 3;
                                s = "foursec";
                                sa = "fourseca";
                            }

                            if (stopsignal != 0) {
                                if (signalflag == true) {
                                    imageViews[i].setImageResource(getResources().getIdentifier(s, "drawable", getPackageName()));
                                    signalflag = false;
                                    handler.postDelayed(this, 500);

                                } else {
                                    imageViews[i].setImageResource(getResources().getIdentifier(sa, "drawable", getPackageName()));
                                    signalflag = true;
                                    handler.postDelayed(this, 500);
                                }
                            } else {
                                imageViews[i].setImageResource(getResources().getIdentifier(s, "drawable", getPackageName()));
                            }

                        }
                    }, 500);
                    //ここまでボタン点滅用の処理
                }
            }
        }
    }


    public void startsound() {
        generateBuffer(1);
        new Thread(new Runnable() {
            @Override
            public void run() {

                audioTrack.play();
                audioTrack.write(buffer, 0, (int) (totaltime * SAMPLERATE));
                audioTrack.setNotificationMarkerPosition((int) (totaltime * SAMPLERATE) - (SAMPLERATE / 10));
                totaltime = uptime + downtime + duration;
                generateBuffer(0);

            }
        }).start();
    }

    public void generateBuffer(int times) {
        //Log.d(TAG,""+minfriq);
        int SAMPLES = (int) (40.0 * SAMPLERATE);
        buffer = new short[SAMPLES * CHANNEL];
        double signal = 0;
        for (int i = 0; i < SAMPLES; i++) {
            signal = generateSignal(i, times);
            buffer[i] = (short) (signal * Short.MAX_VALUE);
        }
    }

    public double generateSignal(int sample, int times) {
        double t = (double) (sample) / SAMPLERATE;
        double a = 0;
        double friq;

        //立ち上がり処理用
        if (times == 1) {
            if (t > downtime + firsttime + duration)
                return a;
            else if (t < firsttime) {
                friq = (maxfriq - 20) / (2 * firsttime) * t + 0;
                a += 0.77 * amplification * Math.sin(2.0 * Math.PI * friq * t);
                a += 0.12 * amplification * Math.sin(2.0 * Math.PI * friq * 3 * t);
                return a;
            } else if (t > firsttime + duration) {
                friq = (minfriq - maxfriq) / (2 * downtime) * (t - firsttime - duration) + maxfriq;
                a += 0.77 * amplification * Math.sin(2.0 * Math.PI * friq * (t - firsttime - duration));
                a += 0.12 * amplification * Math.sin(2.0 * Math.PI * friq * 3 * (t - firsttime - duration));
                return a;
            } else {
                friq = maxfriq;
                a += 0.77 * amplification * Math.sin(2.0 * Math.PI * friq * t);
                a += 0.12 * amplification * Math.sin(2.0 * Math.PI * friq * 3 * t);
                return a;
            }
        }


        //通常周期用の処理
        if (times == 0) {
            if (t > downtime + uptime + duration)
                return a;
            else if (t < uptime) {
                friq = (maxfriq - minfriq) / (2 * uptime) * t + minfriq;
                a += 0.77 * amplification * Math.sin(2.0 * Math.PI * friq * t);
                a += 0.12 * amplification * Math.sin(2.0 * Math.PI * friq * 3 * t);
                return a;
            } else if (t > uptime + duration) {
                friq = (minfriq - maxfriq) / (2 * downtime) * (t - uptime - duration) + maxfriq;
                a += 0.77 * amplification * Math.sin(2.0 * Math.PI * friq * (t - uptime - duration));
                a += 0.12 * amplification * Math.sin(2.0 * Math.PI * friq * 3 * (t - uptime - duration));
                return a;
            } else {
                friq = maxfriq;
                a += 0.77 * amplification * Math.sin(2.0 * Math.PI * friq * t);
                a += 0.12 * amplification * Math.sin(2.0 * Math.PI * friq * 3 * t);
                return a;
            }
        }

        //立ち下がり処理用
        if (times == 2) {
            if (t > lasttime + uptime + duration)
                return a;
            else if (t < uptime) {
                friq = (maxfriq - minfriq) / (2 * uptime) * t + minfriq;
                a += 0.77 * amplification * Math.sin(2.0 * Math.PI * friq * t);
                a += 0.12 * amplification * Math.sin(2.0 * Math.PI * friq * 3 * t);
                return a;
            } else if (t > uptime + duration) {
                friq = (20 - maxfriq) / (2 * lasttime) * (t - uptime - duration) + maxfriq;
                a += 0.77 * amplification * Math.sin(2.0 * Math.PI * friq * (t - uptime - duration));
                a += 0.12 * amplification * Math.sin(2.0 * Math.PI * friq * 3 * (t - uptime - duration));
                return a;
            } else {
                friq = maxfriq;
                a += 0.77 * amplification * Math.sin(2.0 * Math.PI * friq * t);
                a += 0.12 * amplification * Math.sin(2.0 * Math.PI * friq * 3 * t);
                return a;
            }
        }

        //主要高調波以外の成分
        // a += 0.046 * amplification * Math.sin(2.0 * Math.PI * friq * 2 * t);

        //a += 0.013 * amplification * Math.sin(2.0 * Math.PI * friq * 4 * t);
        //a += 0.011 * amplification * Math.sin(2.0 * Math.PI * 3740 * t);

        return a;
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
}
