package com.less.haku.hakuplayer.danmaku;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.text.Layout;
import android.text.Spanned;
import android.text.StaticLayout;
import android.text.TextPaint;

import java.lang.ref.SoftReference;

import master.flame.danmaku.danmaku.model.BaseDanmaku;
import master.flame.danmaku.danmaku.model.android.SimpleTextCacheStuffer;

/**
 * Created by HaKu on 16/3/23.
 * 绘制弹幕背景等
 */
public class CustomCacheStuffer extends SimpleTextCacheStuffer {

    private final static int DANMU_PADDING_INNER = 10;
    private final static int DANMU_RADIUS = 100;

    // 通过扩展SimpleTextCacheStuffer或SpannedCacheStuffer个性化你的弹幕样式
    final Paint paint = new Paint();

    @Override
    public void measure(BaseDanmaku danmaku, TextPaint paint, boolean fromWorkerThread) {
//            danmaku.padding = 20;  // 在背景绘制模式下增加padding
        if (danmaku.text instanceof Spanned) {
            if (mProxy != null) {
                mProxy.prepareDrawing(danmaku, fromWorkerThread);
            }
            CharSequence text = danmaku.text;
            if (text != null) {
                StaticLayout staticLayout = new StaticLayout(text, paint, (int) StaticLayout.getDesiredWidth(danmaku.text, paint), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
                danmaku.paintWidth = staticLayout.getWidth() + 200;
                danmaku.paintHeight = staticLayout.getHeight() + 10;
                danmaku.obj = new SoftReference<>(staticLayout);
                return;
            }
        }

        super.measure(danmaku, paint, fromWorkerThread);
    }

    @Override
    public void drawBackground(BaseDanmaku danmaku, Canvas canvas, float left, float top) {
        paint.setAntiAlias(true);
        if (!danmaku.isGuest) {
            paint.setColor(Color.GRAY);//粉红 楼主
        } else {
            paint.setColor(Color.RED);//黑色 普通
        }
        if (danmaku.isGuest) {//如果是赞 就不要设置背景
            paint.setColor(Color.TRANSPARENT);
        }

        //由于该库并没有提供margin的设置，所以我这边试出这种方法：将danmaku.padding也就是内间距设置大一点，并在这里的RectF中设置绘制弹幕的位置，就可以形成类似margin的效果
        canvas.drawRoundRect(new RectF(left + DANMU_PADDING_INNER, top + DANMU_PADDING_INNER
                        , left + danmaku.paintWidth - DANMU_PADDING_INNER,
                        top + danmaku.paintHeight - DANMU_PADDING_INNER),//+6 主要是底部被截得太厉害了，+6是增加padding的效果
                DANMU_RADIUS, DANMU_RADIUS, paint);
    }

    @Override
    public void drawStroke(BaseDanmaku danmaku, String lineText, Canvas canvas, float left, float top, Paint paint) {
        if (danmaku.obj == null) {
            super.drawStroke(danmaku, lineText, canvas, left, top, paint);
        }
    }

    @Override
    public void drawText(BaseDanmaku danmaku, String lineText, Canvas canvas, float left, float top, TextPaint paint, boolean fromWorkerThread) {
        if (danmaku.obj == null) {
            super.drawText(danmaku, lineText, canvas, left, top, paint, fromWorkerThread);
            return;
        }
        SoftReference<StaticLayout> reference = (SoftReference<StaticLayout>) danmaku.obj;
        StaticLayout staticLayout = reference.get();
        boolean requestRemeasure = 0 != (danmaku.requestFlags & BaseDanmaku.FLAG_REQUEST_REMEASURE);
        boolean requestInvalidate = 0 != (danmaku.requestFlags & BaseDanmaku.FLAG_REQUEST_INVALIDATE);

        if (requestInvalidate || staticLayout == null) {
            if (requestInvalidate) {
                danmaku.requestFlags &= ~BaseDanmaku.FLAG_REQUEST_INVALIDATE;
            } else if (mProxy != null) {
                mProxy.prepareDrawing(danmaku, fromWorkerThread);
            }
            CharSequence text = danmaku.text;
            if (text != null) {
                if (requestRemeasure) {
                    staticLayout = new StaticLayout(text, paint, (int) StaticLayout.getDesiredWidth(danmaku.text, paint), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
                    danmaku.paintWidth = staticLayout.getWidth();
                    danmaku.paintHeight = staticLayout.getHeight();
                    danmaku.requestFlags &= ~BaseDanmaku.FLAG_REQUEST_REMEASURE;
                } else {
                    staticLayout = new StaticLayout(text, paint, (int) danmaku.paintWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
                }
                danmaku.obj = new SoftReference<>(staticLayout);
            } else {
                return;
            }
        }
        boolean needRestore = false;
        if (left != 0 && top != 0) {
            canvas.save();
            canvas.translate(left, top + paint.ascent());
            needRestore = true;
        }
        staticLayout.draw(canvas);
        if (needRestore) {
            canvas.restore();
        }
    }

    @Override
    public void clearCaches() {
        super.clearCaches();
        System.gc();
    }

    @Override
    public void clearCache(BaseDanmaku danmaku) {
        super.clearCache(danmaku);
        if (danmaku.obj instanceof SoftReference<?>) {
            ((SoftReference<?>) danmaku.obj).clear();
        }
    }

    @Override
    public void releaseResource(BaseDanmaku danmaku) {
        clearCache(danmaku);
        super.releaseResource(danmaku);
    }

}
