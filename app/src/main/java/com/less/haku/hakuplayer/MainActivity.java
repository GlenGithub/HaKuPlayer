package com.less.haku.hakuplayer;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.less.haku.hakuplayer.danmaku.CustomCacheStuffer;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import master.flame.danmaku.controller.IDanmakuView;
import master.flame.danmaku.danmaku.loader.ILoader;
import master.flame.danmaku.danmaku.loader.IllegalDataException;
import master.flame.danmaku.danmaku.loader.android.DanmakuLoaderFactory;
import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.DanmakuTimer;
import master.flame.danmaku.danmaku.model.IDanmakus;
import master.flame.danmaku.danmaku.model.IDisplayer;
import master.flame.danmaku.danmaku.model.android.BaseCacheStuffer;
import master.flame.danmaku.danmaku.model.android.DanmakuContext;
import master.flame.danmaku.danmaku.model.android.Danmakus;
import master.flame.danmaku.danmaku.parser.BaseDanmakuParser;
import master.flame.danmaku.danmaku.parser.IDataSource;
import master.flame.danmaku.danmaku.parser.android.BiliDanmukuParser;
import master.flame.danmaku.ui.widget.DanmakuView;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;


public class MainActivity extends AppCompatActivity {

    @Bind(R.id.video_view)
    SurfaceView videoView;
    @Bind(R.id.play_cid)
    EditText playCid;
    @Bind(R.id.play_getUrl)
    TextView play;
    @Bind(R.id.danmaku_text)
    EditText danmaText;
    @Bind(R.id.danmaku_send)
    Button danmaSend;
    @Bind(R.id.danmaku_view)
    DanmakuView danmakuView;

    private IjkMediaPlayer ijkMediaPlayer;
    private SurfaceHolder holder;
    private OkHttpClient client;

    private DanmakuContext context;
    private BaseDanmakuParser mParser;

    private BaseCacheStuffer.Proxy mCacheStufferAdapter = new BaseCacheStuffer.Proxy() {

        private Drawable mDrawable;

        @Override
        public void prepareDrawing(final BaseDanmaku danmaku, boolean fromWorkerThread) {
//            if (danmaku.text instanceof Spanned) { // 根据你的条件检查是否需要需要更新弹幕
//                // FIXME 这里只是简单启个线程来加载远程url图片，请使用你自己的异步线程池，最好加上你的缓存池
//                new Thread() {
//
//                    @Override
//                    public void run() {
////                        String url = "http://www.bilibili.com/favicon.ico";
////                        InputStream inputStream = null;
////                        Drawable drawable = mDrawable;
////                        if(drawable == null) {
////                            try {
////                                URLConnection urlConnection = new URL(url).openConnection();
////                                inputStream = urlConnection.getInputStream();
////                                drawable = BitmapDrawable.createFromStream(inputStream, "bitmap");
////                                mDrawable = drawable;
////                            } catch (MalformedURLException e) {
////                                e.printStackTrace();
////                            } catch (IOException e) {
////                                e.printStackTrace();
////                            } finally {
////                                IOUtils.closeQuietly(inputStream);
////                            }
////                        }
//                        Drawable drawable = getResources().getDrawable(R.drawable.ic_launcher);
//                        if (drawable != null) {
//                            drawable.setBounds(0, 0, 100, 100);
//                            SpannableStringBuilder spannable = createSpannable(drawable);
//                            danmaku.text = spannable;
//                            if(danmakuView != null) {
//                                danmakuView.invalidateDanmaku(danmaku, false);
//                            }
//                            return;
//                        }
//                    }
//                }.start();
//            }
        }

        @Override
        public void releaseResource(BaseDanmaku danmaku) {
            // TODO 重要:清理含有ImageSpan的text中的一些占用内存的资源 例如drawable
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        init();
    }

    private void init() {
        playCid.setText("3885454");

        // 设置最大显示行数
        HashMap<Integer, Integer> maxLinesPair = new HashMap<Integer, Integer>();
        maxLinesPair.put(BaseDanmaku.TYPE_SCROLL_RL, 3); // 滚动弹幕最大显示3行

        // 设置是否禁止重叠
        HashMap<Integer, Boolean> overlappingEnablePair = new HashMap<Integer, Boolean>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, false);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, false);

        context = DanmakuContext.create();

        client = new OkHttpClient();
        holder = videoView.getHolder();
        ijkMediaPlayer = new IjkMediaPlayer();
        ijkMediaPlayer.setKeepInBackground(false);

        context.setDanmakuStyle(IDisplayer.DANMAKU_STYLE_STROKEN, 3)
                .setDuplicateMergingEnabled(false)
                .setScrollSpeedFactor(1.2f)
                .setScaleTextSize(1.2f)
                .setCacheStuffer(new CustomCacheStuffer(), mCacheStufferAdapter) // 图文混排使用SpannedCacheStuffer
//        .setCacheStuffer(new BackgroundCacheStuffer())  // 绘制背景使用BackgroundCacheStuffer
                .setMaximumLines(maxLinesPair)
                .preventOverlapping(overlappingEnablePair);


    }

    private void setDanmaKu(InputStream inputStream) {
        if (danmakuView != null) {
//            mParser = createParser(null);
//            mParser = createParser(this.getResources().openRawResource(R.raw.comments));
            mParser = createParser(inputStream);
            danmakuView.setCallback(new master.flame.danmaku.controller.DrawHandler.Callback() {
                @Override
                public void updateTimer(DanmakuTimer timer) {
                }

                @Override
                public void drawingFinished() {

                }

                @Override
                public void danmakuShown(BaseDanmaku danmaku) {
//                    Log.d("DFM", "danmakuShown(): text=" + danmaku.text);
                }

                @Override
                public void prepared() {
                    danmakuView.start();
                }
            });
            danmakuView.setOnDanmakuClickListener(new IDanmakuView.OnDanmakuClickListener() {
                @Override
                public void onDanmakuClick(BaseDanmaku latest) {
                    Log.d("DFM", "onDanmakuClick text:" + latest.text);
                }

                @Override
                public void onDanmakuClick(IDanmakus danmakus) {
                    Log.d("DFM", "onDanmakuClick danmakus size:" + danmakus.size());
                }
            });

            danmakuView.prepare(mParser, context);
            danmakuView.showFPS(true);
            danmakuView.enableDanmakuDrawingCache(true);
        }
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

    /**
     *
     * */

    public void execute() throws Exception {
//        ijkMediaPlayer.stop();

        //cid 3885454
        String cid = playCid.getText().toString();
        //直播URL拼接
//        String url = "http://live.bilibili.com/api/playurl?player=1&quality=0&cid=" + cid;


        //点播URL拼接
        String appkey = "f3bb208b3d081dc8";
        String secretkey = "ea85624dfcf12d7cc7b2b3a94fac1f2c";
        String sign_this = string2MD5("appkey=" + appkey + "&cid=" + cid + secretkey);
        String url = "http://interface.bilibili.com/playurl?appkey=" + appkey + "&cid=" + cid + "&sign=" + sign_this;
        Log.d("video request", url);

        Request request = new Request.Builder()
                .url(url)
                .build();

        Response response = client.newCall(request).execute();

        if (response.isSuccessful()) {
            String str = response.body().string();
            Log.d("response", str);
//            String result = str.substring(str.lastIndexOf("[") + 1, str.lastIndexOf("]") - 1);
//            playVideo(result);
        }

//        String uri = "http://cn-zjcz5-dx.acgvideo.com/vg5/7/45/6526400-1.flv?expires=1458226500&ssig=-UYWQOZeJeOApl50MzqOiw&oi=1961670062&appkey=85eb6835b0a1034e&or=3026306825&rate=0";
//        String uri = "http://cn-jsyz6-dx.acgvideo.com/vg0/f/e7/4772447-1.flv?expires=1444760400&ssig=JJcOW4VVGBKaMfbJTBbjBA&oi=12345678&player=1&rate=0";
//        String uri = "http://cn-zjhz5-dx.acgvideo.com/vg11/0/28/3885454-1.flv?expires=1458631800&ssig=cVAFm_SGopybz1k0RsxCmA&oi=1961670062&appkey=f3bb208b3d081dc8&or=3026306826&rate=0";
//        playVideo(uri);
        String uri = Environment.getExternalStorageDirectory().getPath() + "/我们的毕业季.mp4";

//        String commentUri = "http://comment.bilibili.com/" + cid + ".xml";
        String commentUri = "http://comment.bilibili.com/3885454.xml";
//        commentUri = "https://publicobject.com/helloworld.txt";

        Log.d("request_comment url", commentUri);
        Request requestComment = new Request.Builder()
//                .addHeader("Accept-Encoding", "deflate")
                .addHeader("Accept", "application/json, text/javascript, */*; q=0.01")
//                .addHeader("Accept-Encoding", "gzip").
                .addHeader("Accept-Language", "en-US,en;q=0.8,fa;q=0.6,ar;q=0.4")
                .url(commentUri)
                .build();


        Response responseComment = client.newCall(requestComment).execute();

        if (responseComment.isSuccessful()) {
//            InputStream inputStream = responseComment.body()();
//            String inputString = responseComment.body().string();
//            ResponseBody body = responseComment.body();
//            byte[] bytes = responseComment.body().bytes();
//            String string = body.string();
//            Reader reader = body.charStream();
            byte[] result = ZipUtil.decompress(responseComment.body().bytes());

            Log.d("response_comment", new String(result));
            InputStream inputStream =  new ByteArrayInputStream(result);

            setDanmaKu(inputStream);
//            setDanmaKu(this.getResources().openRawResource(R.raw.comments));
//            mParser = createParser(this.getResources().openRawResource(R.raw.comments));
        }

        playVideo(uri);
    }

    private void playVideo(String uri) {
        Log.d("uri", uri);
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

    /***
     * MD5加码 生成32位md5码
     */
    public static String string2MD5(String inStr) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
            return "";
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];
        byte[] md5Bytes = md5.digest(byteArray);
        StringBuffer hexValue = new StringBuffer();
        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16)
                hexValue.append("0");
            hexValue.append(Integer.toHexString(val));
        }
        return hexValue.toString();

    }

    /**
     * ******************  弹幕相关内容  ******************
     * */

    /**
     * 增加弹幕
     */

    void addDanmaku() {
        BaseDanmaku danmaku = context.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL, context);
//        BaseDanmaku danmaku = context.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL);
        if (danmaku == null || danmakuView == null) {
            return;
        }
        // for(int i=0;i<100;i++){
        // }
        danmaku.text = "这是一条弹幕 ++++ \n" + System.nanoTime();
        danmaku.padding = 5;
        danmaku.priority = 0;  // 可能会被各种过滤器过滤并隐藏显示
        danmaku.isLive = false;
        danmaku.time = danmakuView.getCurrentTime() + 1200;
        danmaku.textSize = 25f;
//        danmaku.textSize = 25f * (mParser.getDisplayer().getDensity() - 0.6f);
        danmaku.textColor = Color.RED;
        danmaku.textShadowColor = Color.WHITE;
        // danmaku.underlineColor = Color.GREEN;
        danmaku.borderColor = Color.GREEN;
        danmakuView.addDanmaku(danmaku);

    }

    @OnClick(R.id.danmaku_send)
    void addMulti() {
        addDanmaKuShowTextAndImage();
//        for (int i = 0; i < 5; i++)  {
//            addDanmaKuShowTextAndImage();
//        }
    }

    void addDanmaKuShowTextAndImage() {
        BaseDanmaku danmaku = context.mDanmakuFactory.createDanmaku(BaseDanmaku.TYPE_SCROLL_RL, context);
        Drawable drawable = getResources().getDrawable(R.drawable.ic_launcher);
        drawable.setBounds(0, 0, 100, 100);
//        SpannableStringBuilder spannable = createSpannable(drawable);

        danmaku.text = danmaText.getText();
        danmaku.padding = 5;
        danmaku.priority = 1;  // 一定会显示, 一般用于本机发送的弹幕
        danmaku.isLive = false;
        danmaku.time = danmakuView.getCurrentTime() + 1200;
        danmaku.textSize = 25f * (mParser.getDisplayer().getDensity() - 0.6f);
        danmaku.textColor = Color.RED;
        danmaku.textShadowColor = 0; // 重要：如果有图文混排，最好不要设置描边(设textShadowColor=0)，否则会进行两次复杂的绘制导致运行效率降低
//        danmaku.underlineColor = Color.GREEN;
        danmakuView.addDanmaku(danmaku);

//        danmuText.setText(spannable);
    }

    private SpannableStringBuilder createSpannable(Drawable drawable) {
        String text = "bitmap";
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(text);
        ImageSpan span = new ImageSpan(drawable);//ImageSpan.ALIGN_BOTTOM);
        spannableStringBuilder.setSpan(span, 2, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        spannableStringBuilder.append("图文混排111   " + System.nanoTime());

//        spannableStringBuilder.setSpan(span, 2, 3, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
//        spannableStringBuilder.append(spannableStringBuilder);
//        spannableStringBuilder.setSpan(new BackgroundColorSpan(Color.parseColor("#8A2233B1")), 0, spannableStringBuilder.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return spannableStringBuilder;
    }

    private BaseDanmakuParser createParser(InputStream stream) {

        if (stream == null) {
            return new BaseDanmakuParser() {

                @Override
                protected Danmakus parse() {
                    return new Danmakus();
                }
            };
        }

        ILoader loader = DanmakuLoaderFactory.create(DanmakuLoaderFactory.TAG_BILI);

        try {
            loader.load(stream);
        } catch (IllegalDataException e) {
            e.printStackTrace();
        }
        BaseDanmakuParser parser = new BiliDanmukuParser();
        IDataSource<?> dataSource = loader.getDataSource();
        parser.load(dataSource);
        return parser;

    }
}
