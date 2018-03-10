package com.example.cheng.wifi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

public class DrawView extends View{
    public static final String ATG = "UI view";
    public float currentX = 200;
    public float currentY = 200;
    public Handler handler;

    Paint p_x = new Paint();
    Paint p = new Paint();
    Paint p_point = new Paint();

    @SuppressLint("HandlerLeak")
    public DrawView(Context context) {

        super(context);
        handler = new Handler(){

            @Override
            public void handleMessage(Message msg) {

                if(msg.what == 0x123){
                    Bundle bundle = msg.getData();
                    currentX = bundle.getFloat("x");
                    currentY = bundle.getFloat("y");
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
                    Bundle bundle = msg.getData();
                    currentX = bundle.getFloat("x");
                    currentY = bundle.getFloat("y");
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
        p_x.setColor(Color.WHITE);
        p_point.setColor(Color.RED);
        p.setColor(Color.BLACK);
        p.setTextSize(30);

        //画出巨型框，模拟房间
        canvas.drawRect(0,0,this.getWidth(),this.getHeight(),p_x);

        //画出线段AB
        canvas.drawLine(0,0,this.getWidth(),0,p);

        //画出线段AD
        canvas.drawLine(0,0,0,this.getHeight(),p);

        //画出线段BC
        canvas.drawLine(this.getWidth()-1,0,this.getWidth()-1,this.getHeight(),p);

        //画出线段的DC
        canvas.drawLine(0, this.getHeight()-1,this.getWidth(),this.getHeight()-1,p);

        canvas.drawCircle(0,0,10,p_point);
        canvas.drawText("A", 0, 30,p);

        canvas.drawCircle(getWidth(), 0, 10, p_point);
        canvas.drawText("B",this.getWidth()-30,30, p);

        canvas.drawCircle(getWidth(),getHeight(),10,p_point);
        canvas.drawText("C",this.getWidth()-30, getHeight()-5, p);

        canvas.drawCircle(0,getHeight(),10,p_point);
        canvas.drawText("D", 0f, getHeight()-5, p);
        //绘制当前位置

        Log.i(ATG, String.valueOf(getWidth()));
        Log.i(ATG, String.valueOf(getHeight()));
        canvas.drawCircle(currentX, currentY, 10, p);
    }
}
