package com.less.haku.hakuplayer;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;


public class MainActivity extends AppCompatActivity {

    @Bind(R.id.video_view)
    SurfaceView videoView;

    private IjkMediaPlayer ijkMediaPlayer;
    private SurfaceHolder holder;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        init();
    }

    private void init() {
        holder = videoView.getHolder();
        ijkMediaPlayer = new IjkMediaPlayer();
        String uri = "http://cn-shcy3-dx.acgvideo.com/vg12/f/9a/3215566hd.mp4?expires=1457420100&ssig=Xr96D4B9sG2ZD2W99rOHng&oi=1961670062&appkey=f3bb208b3d081dc8&or=3026306825&rate=0";
        try {
            ijkMediaPlayer.setDataSource(this, Uri.parse(uri));
            ijkMediaPlayer.setDisplay(holder);
            holder.addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {

                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    ijkMediaPlayer.setDisplay(holder);
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {

                }
            });
            ijkMediaPlayer.prepareAsync();
            ijkMediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        ijkMediaPlayer.setKeepInBackground(false);
    }

    @OnClick(R.id.start)
    void start() {
        ijkMediaPlayer.start();
    }

    @OnClick(R.id.pause)
    void pause() {
        ijkMediaPlayer.pause();
    }

    @OnClick(R.id.stop)
    void stop() {
        ijkMediaPlayer.stop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ijkMediaPlayer.release();
    }
}
