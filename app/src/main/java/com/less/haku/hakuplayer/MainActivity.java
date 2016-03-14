package com.less.haku.hakuplayer;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.EditText;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;


public class MainActivity extends AppCompatActivity {

    @Bind(R.id.video_view)
    SurfaceView videoView;
    @Bind(R.id.play_cid)
    EditText playCid;

    private IjkMediaPlayer ijkMediaPlayer;
    private SurfaceHolder holder;
    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        init();
    }

    private void init() {
        client = new OkHttpClient();
        holder = videoView.getHolder();
        ijkMediaPlayer = new IjkMediaPlayer();
        ijkMediaPlayer.setKeepInBackground(false);

//        String uri = "http://cn-shcy3-dx.acgvideo.com/vg12/f/9a/3215566hd.mp4?expires=1457420100&ssig=Xr96D4B9sG2ZD2W99rOHng&oi=1961670062&appkey=f3bb208b3d081dc8&or=3026306825&rate=0";
//        String uri = "http://live-play.acgvideo.com/live/856/live_14837663_7724658.flv";
//        String uri = "ijkhttphook://devimages.apple.com.edgekey.net/streaming/examples/bipbop_4x3/gear1/prog_index.m3u8";
        String uri = "http://live-play.acgvideo.com/live/977/live_11153765_9369560.flv";

    }

    @OnClick(R.id.play_getUrl)
    void getUrl() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    execute();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void execute() throws Exception {
        String cid = playCid.getText().toString();
        String url = "http://live.bilibili.com/api/playurl?player=1&quality=0&cid=" + cid;
        Request request = new Request.Builder()
                .url(url)
                .build();
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            String str = response.body().string();
            Log.d("tttttt", str);

            String result = str.substring(str.lastIndexOf("[") + 1, str.lastIndexOf("]") - 1);
            Log.d("tttttt", result);
//            playVideo("http://live-play-3.acgvideo.com/live/live_11153765_9369560.flv");
            playVideo(result);
        }
    }

    private void playVideo(String uri) {
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
