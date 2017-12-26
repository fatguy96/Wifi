package com.example.cheng.wifi;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {


    //真实距离到显示屏幕的距离
    double scaling_factor = 1;

    //view 的 宽:w和高:h
    int view_w= 200;
    int view_h = 100;
    //提供的4个AP
    private static final String AccessPoint_A = "AC_A";

    private static final String AccessPoint_B = "AC_B";

    private static final String AccessPoint_C = "AC_C";

    private static final String AccessPoint_D = "AC_D";

    List<ScanResult> resultList;



    //检测权限列表

    private static final String[] NEEDED_PERMISSIONS = new String[]{

            Manifest.permission.ACCESS_COARSE_LOCATION,

            Manifest.permission.ACCESS_FINE_LOCATION

    };

    private static final int PERMISSION_REQUEST_CODE = 0;



    private WifiManager mWifiManager;

    private Button show_to;

    private TextView ap_x, ap_y, ap_z, ap_w;

    private DrawView drawView;

    @Override

    protected void onCreate(Bundle savedInstanceState)

    {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        init_view_button();

        drawView = findViewById(R.id.drawView);

        mWifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);

        final boolean mHasPermission = checkPermission();

        if (!mHasPermission) {

            requestPermission();

        }



        show_to.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View view) {



                //打开WIFI

                open_wifi();



                //测试广播，对扫描结果进行监听

                registerBroadcast();



                //启动线程，1秒钟后开启，每隔1秒钟进行WIFI扫描

                Timer timer = new Timer();

                timer.schedule(new TimerTask() {

                    @Override

                    public void run() {

                        mWifiManager.startScan();

                    }

                },1000,100);

            }

        });



    }



    private void init_view_button()

    {

        show_to = findViewById(R.id.get);

        ap_x = findViewById(R.id.ap_x);

        ap_y = findViewById(R.id.ap_y);

        ap_z = findViewById(R.id.ap_z);

        ap_w = findViewById(R.id.ap_w);

    }



    private void open_wifi()

    {

        if (!mWifiManager.isWifiEnabled()) {

            mWifiManager.setWifiEnabled(true);

        }

    }



    private boolean checkPermission()

    {



        for (String permission : NEEDED_PERMISSIONS) {

            if (ActivityCompat.checkSelfPermission(this, permission)

                    != PackageManager.PERMISSION_GRANTED) {

                return false;

            }

        }



        return true;

    }



    private void requestPermission()

    {

        ActivityCompat.requestPermissions(this,

                NEEDED_PERMISSIONS, PERMISSION_REQUEST_CODE);

    }


    //注册广播，用于监听WIFI
    private void registerBroadcast()

    {

        IntentFilter filter = new IntentFilter();

        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

        registerReceiver(mReceiver, filter);

    }


    private void show_info(String[] tem)

    {

        ap_x.setText(tem[0]);

        ap_y.setText(tem[1]);

        ap_z.setText(tem[2]);

        ap_w.setText(tem[3]);

    }



    private int[] sting2int(String[] string) {

        int tems[] = new int[4];

        tems[0] = Integer.valueOf(string[0]);

        tems[1] = Integer.valueOf(string[1]);

        tems[2] = Integer.valueOf(string[2]);

        tems[3] = Integer.valueOf(string[3]);

        return tems;

    }

    private double[] strength2view(int[] strength){
        double view_distance[] = new double[4];
        for(int i = 0; i <= 3; ++i){
            view_distance[i] = scaling_factor * strength2distance(strength[i]);
        }
        return  view_distance;
    }

    private int strength2distance(int s){
        int tem = 0;
        //TODO: switch the strength to distance eg, wifi 100 map 1 m
        return tem;
    }



    /**
     * @param x:改点离AP点A的大致距离
     * @param y:该点离AP点B的大致距离
     * @param z:该点离AP点C的大致距离
     * @param w:该点离AP点D的大致距离
     * @return int[2], 改点再view中的位置坐标
     * */
    private int[] get_xy(int x, int y, int z, int w) {
        int xy[] = new int[2];
        double temx = 0, temy = 0;
        //利用A，B点画圆求交，D点进行选点
        //(XA^2-XB^2+view_w^2)/(2*view_w)
        temx = (Math.pow((double)x, 2.0)-Math.pow((double)y, 2.0)+Math.pow(w, 2.0))/(2*view_w);
        temy = Math.pow((double)x, 2.0) - Math.pow(temx, 2.0);
        //temy 交点坐标的绝对值
        temy = Math.sqrt(temy);
        //通过代入第三点D，进行比较得出比较理想的值
        double one = Math.pow(temx, 2.0) + Math.pow((temy - view_h), 2.0);
        double two = Math.pow(temx, 2.0) + Math.pow((temy + view_h), 2.0);
        if (Math.abs(one - w ) > Math.abs(two - w)){
            temy = - temy;
        }
        xy[0] = (int)temx;
        xy[1] = (int)temy;
        return xy;

    }



    private String[] filt_info(List<ScanResult> resultList){

        /**
        * @param resultList:wifi 扫描的结果
        * @return tem:提取wifi扫描结果中的4个AP点的强度信息，并转换成字符串
        * */

        String tem[] = new String[4];

        for (int i=0;i <= 3; i++){

            tem[i]=String.valueOf(i);

        }

        for (ScanResult sc : resultList) {

            if (sc.SSID.equals(AccessPoint_A)) {

                int temp = WifiManager.calculateSignalLevel(sc.level,100);

                tem[0] = String.valueOf(temp);

            }

            if (sc.SSID.equals(AccessPoint_B)) {

                int temp = WifiManager.calculateSignalLevel(sc.level,100);

                tem[1] = String.valueOf(temp);

            }

            if (sc.SSID.equals(AccessPoint_C)) {

                int temp = WifiManager.calculateSignalLevel(sc.level,100);

                tem[2] = String.valueOf(temp);

            }

            if(sc.SSID.equals(AccessPoint_D)){

                int temp = WifiManager.calculateSignalLevel(sc.level,100);

                tem[3] = String.valueOf(temp);

            }

        }

        return tem;

    }



    public BroadcastReceiver mReceiver = new BroadcastReceiver(){

        /**
         * 监听wifi广播，一旦扫描到wifi的结果就调用onReceive
         * */

        @Override

        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            // wifi已成功扫描到可用wifi

            if (Objects.equals(action, WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {

                resultList = mWifiManager.getScanResults();

                String tem[] = filt_info(resultList);

                show_info(tem);

                //更新view中的X，Y；

                int tems[] = sting2int(tem);

                int tem_xy[] = get_xy(tems[0], tems[1], tems[2], tems[3]);

                Message message = new Message();

                message.what = 0x123;

                message.arg1= tem_xy[0] ;

                message.arg2 = tem_xy[1];


                drawView.handler.sendMessage(message);

            }

        }

    };
}
