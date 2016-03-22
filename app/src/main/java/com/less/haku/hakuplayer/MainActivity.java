package com.less.haku.hakuplayer;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.OkHttpClient;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;


public class MainActivity extends AppCompatActivity {

    @Bind(R.id.video_view)
    SurfaceView videoView;
    @Bind(R.id.play_cid)
    EditText playCid;
    @Bind(R.id.play_getUrl)
    TextView play;

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

        testfunc();
    }

    /**
     * 测试功能
     * */
    private void testfunc() {
        play.setText("111111111111111111111111111111111111111111111111");
        Log.d("tttttt", play.getLineCount() + "");
        Toast.makeText(this, play.getHeight() + "", Toast.LENGTH_SHORT).show();
        play.post(new Runnable() {

            @Override
            public void run() {
                Log.d("tttttt after", play.getHeight() + "");
            }
        });
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
//        ijkMediaPlayer.stop();
//
//        String cid = playCid.getText().toString();
//        String url = "http://live.bilibili.com/api/playurl?player=1&quality=0&cid=" + cid;
//        Request request = new Request.Builder()
//                .url(url)
//                .build();
//        Response response = client.newCall(request).execute();
//
//        if (response.isSuccessful()) {
//            String str = response.body().string();
//            Log.d("response", str);
//            String result = str.substring(str.lastIndexOf("[") + 1, str.lastIndexOf("]") - 1);
//            playVideo(result);
//        }

//        String uri = "http://cn-zjcz5-dx.acgvideo.com/vg5/7/45/6526400-1.flv?expires=1458226500&ssig=-UYWQOZeJeOApl50MzqOiw&oi=1961670062&appkey=85eb6835b0a1034e&or=3026306825&rate=0";
//        String uri = "http://cn-jsyz6-dx.acgvideo.com/vg0/f/e7/4772447-1.flv?expires=1444760400&ssig=JJcOW4VVGBKaMfbJTBbjBA&oi=12345678&player=1&rate=0";
        String uri = "http://cn-shcy2-dx.acgvideo.com/vg2/3/c8/6630302hd.mp4?expires=1458227100&ssig=Hnbd8nGJM7HtQeL0HYGJng&oi=1961670062&internal=1&or=3026306826&rate=0";
        playVideo(uri);
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
