package com.example.cheng.wifi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.View;

public class DrawView extends View{


    public float currentX = 200;

    public float currentY = 200;

    public Handler handler;



    Paint p = new Paint();



    @SuppressLint("HandlerLeak")
    public DrawView(Context context) {

        super(context);

        handler = new Handler(){

            @Override

            public void handleMessage(Message msg) {

                if(msg.what == 0x123){

                    currentX = msg.arg1;

                    currentY = msg.arg2;

                    invalidate();

                }

                super.handleMessage(msg);

            }

        };

    }



    @SuppressLint("HandlerLeak")
    public DrawView(Context context, AttributeSet set){

        super(context, set);

        handler = new Handler(){

            @Override

            public void handleMessage(Message msg) {

                if(msg.what == 0x123){

                    currentX = msg.arg1;

                    currentY = msg.arg2;

                    invalidate();

                }

                super.handleMessage(msg);

            }

        };

    }


    @Override

    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        //设置画笔的颜色

        p.setColor(Color.RED);

        //绘制一个小点

        canvas.drawCircle(currentX, currentY, 10, p);

    }

}
