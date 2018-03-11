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

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    //真实距离到view距离的对应关系
    private float scaling_factor_w, scaling_factor_h;


    //实际场景中的宽:w和高:h
    //todo: 设定实际场景中的宽(A-b)和高(A - D)
    private static final double really_w = 5;
    private static final double really_h = 4;

    //view 中的宽w， 高h
    int view_w;
    int view_h;

    //提供的4个AP
    private static final String AccessPoint_A = "TP-LINK_5B3A";
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
    private TextView ap_x, dis_x, ap_y, dis_y, ap_z, dis_z, ap_w, dis_w;
    private DrawView drawView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init_view_button();

        drawView = findViewById(R.id.drawView);

        //获取view的长和宽
        view_w = drawView.getWidth();
        view_h = drawView.getHeight();

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

    //初始化
    private void init_view_button()
    {
        scaling_factor_h = keep2point((float) (view_h * 1.0/really_h));
        scaling_factor_w = keep2point((float) (view_w * 1.0/really_w));
        show_to = findViewById(R.id.get);
        ap_x = findViewById(R.id.ap_x);
        dis_x = findViewById(R.id.dis_x);
        ap_y = findViewById(R.id.ap_y);
        dis_y = findViewById(R.id.dis_y);
        ap_z = findViewById(R.id.ap_z);
        dis_z = findViewById(R.id.dis_z);
        ap_w = findViewById(R.id.ap_w);
        dis_w = findViewById(R.id.dis_w);
    }

    //打开wifi
    private void open_wifi()
    {
        if (!mWifiManager.isWifiEnabled()) {
            mWifiManager.setWifiEnabled(true);
        }
    }

    //判断当前的权限是否已经满足
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

    //请求打开没有授权的权限
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


    public BroadcastReceiver mReceiver = new BroadcastReceiver()
    {

        /*
         * 监听wifi广播，一旦扫描到wifi的结果就调用onReceive
         * */

        @Override
        public void onReceive(Context context, Intent intent) {

            final String action = intent.getAction();

            // wifi已成功扫描到可用wifi
            if (Objects.equals(action, WifiManager.SCAN_RESULTS_AVAILABLE_ACTION))
            {
                resultList = mWifiManager.getScanResults();
                String tem[] = filt_info(resultList);

                //将字符串转化成int
                int tems[] = sting2int(tem);

                //将wifi强度转化成实际中的距离
                float real_distance[] = wifi2distance(tems);
                show_info(tem, real_distance);

                // 求出现实场景中大概的位置信息
                // TODO: 尝试作弊方法时，删除下面两行
                double tem_xy[] = get_xy(real_distance[0], real_distance[1],
                        real_distance[2], real_distance[3]);

                //将现实场景中的位置信息转换成view中的位置信息
                float view_xy[] = real2view(tem_xy);

                //TODO: 尝试作弊方法, 取消下面注释
                //view_xy = get_xy_no_way(real_distance[0],real_distance[1], real_distance[2], real_distance[3]);

                //向自定义View中传递大概的位置信息
                //只有得出的点存在的时候才进行更新
                if (view_xy[0]!=0 && view_xy[1]!=0){
                    Message message = new Message();
                    Bundle bundle = new Bundle();
                    message.what = 0x123;
                    bundle.putFloat("x", view_xy[0]);
                    bundle.putFloat("y", view_xy[1]);
                    message.setData(bundle);
                    drawView.handler.sendMessage(message);
                }
            }
        }

    };


    private void show_info(String[] tem, float[] tem_num)
    {
        for (int i=0;i <= 3; i++){

            if (tem[i].equals("100")){
                tem[i] = "找不到";
            }

        }
        ap_x.setText(tem[0]);
        dis_x.setText(String.valueOf(tem_num[0]));
        ap_y.setText(tem[1]);
        dis_y.setText(String.valueOf(tem_num[1]));
        ap_z.setText(tem[2]);
        dis_z.setText(String.valueOf(tem_num[2]));
        ap_w.setText(tem[3]);
        dis_w.setText(String.valueOf(tem_num[3]));
    }

    private int[] sting2int(String[] string)
    {
        int tems[] = new int[4];
        tems[0] = Integer.valueOf(string[0]);
        tems[1] = Integer.valueOf(string[1]);
        tems[2] = Integer.valueOf(string[2]);
        tems[3] = Integer.valueOf(string[3]);
        return tems;
    }

    private float[] wifi2distance(int[] strength)
    {
        double distance[] = new double[4];
        float diff[] = new float[4];
        for(int i = 0; i <= 3; ++i){
            distance[i] = strength2distance(strength[i]);
            diff[i] = keep2point((float)distance[i]);
        }
        return  diff;
    }

    private double strength2distance(int s)
    {
        if (s==100){
            return -1.0;
        }
        else {
            double tem;
            double fenzi;
            //TODO: 寻找合适的传播理论
            // 参考自：https://tech.meituan.com/mt-wifi-locate-practice-part1.html
            fenzi = (-42 - s) * 1.0 / (10 * 3.33);
            tem = Math.pow(10, fenzi);
            return tem;
        }
    }

    //TODO: 有待商榷
    /**
     * 预设定的场景：
     *              really_w
     *      (0, 0)A----------------------------B(really_w, 0)
     *            \                            \
     *            \                            \
     *            \             X(x, y)        \ really_h
     *            \                            \
     *            \                            \
     * (0, really_h)D----------------------------C(really_w, really_h)
     * @param SA:现实场景中该点离AP点A的大致距离
     * @param SB:现实场景中该点离AP点B的大致距离
     * @param SC:现实场景中该点离AP点C的大致距离
     * @param SD:现实场景中该点离AP点D的大致距离
     * @return int[2], 改点再view中的位置坐标
     * */

    private double[] get_xy(float SA, float SB, float SC, float SD)
    {
        double xy[] = new double[2];

        double []temx_one;
        double []temx_two;
        double []temx_three;

        //不为零的个数
        int count = 0;

        //A,D求交点， D点进行选择
        temx_one = get_xy_one(SA, SB, SD);

        //A,C求交点， B点进行选择
        temx_two = get_xy_two(SA, SB, SC);

        //B，D求交点， C点进行选择
        temx_three = get_xy_three(SB, SC, SD);


        if (temx_one[1]!=0&&temx_one[0]!=0){
            count++;
        }
        if (temx_two[0]!=0&&temx_two[1]!=0){
            count++;
        }
        if (temx_three[0]!=0&&temx_three[1]!=0){
            count++;
        }
        if(count!=0){
            xy[0] = (temx_one[0]+temx_two[0]+temx_three[0])/count;
            xy[1] = (temx_one[1]+temx_two[1]+temx_three[1])/count;
        }else {
            xy[0] = 0;
            xy[1] = 0;
        }
        return xy;
    }

    private double[] get_xy_one(double SA, double SB, double SD)
    {
        double tem[] = new double[2];
        tem[0] = 0; //x
        tem[1] = 0; //y

        if ( (SA + SB) > really_w && SA < really_w && SB < really_w){
            //x=(SA^2-SB^2+really_w^2)/(2*really_w)
            tem[0] = (Math.pow(SA, 2.0) - Math.pow( SB, 2.0) + Math.pow(really_w, 2.0)) / (2 * really_w);
            tem[1] = Math.pow( SA, 2.0) - Math.pow(tem[0], 2.0);

            //其实下面的代码没有必要，只需要取正数就行
            double one = Math.pow(tem[0], 2.0) + Math.pow((tem[1] - really_h), 2.0);
            double two = Math.pow(tem[0], 2.0) + Math.pow((tem[1] + really_h), 2.0);
            if (Math.abs(one - SD ) > Math.abs(two - SD)) {
                tem[1] = -tem[1];
            }
        }
        return tem;
    }

    private double[] get_xy_two(double SA, double SB, double SC)
    {
        //利用A，C点画圆求交，B确定位置

        double tem[] = new double[2];
        tem[0] = 0; //x
        tem[1] = 0; //y

        //确保有交点,且交点在区域内
        if ( (Math.pow((SA + SC),2) > Math.pow(really_w, 2) + Math.pow(really_h, 2))
                &&(Math.pow(SA, 2) < Math.pow(really_w, 2) + Math.pow(really_h, 2))
                &&(Math.pow(SC, 2) < Math.pow(really_w, 2) + Math.pow(really_h, 2))){

            //简化求解方程转化成-->>>a *  y^2 + b * y + c = 0
            double t = Math.pow(SA, 2) + Math.pow(really_w, 2) + Math.pow(really_h, 2) - Math.pow(SC, 2);
            double a = 4 * (Math.pow(really_w, 2) + Math.pow(really_h, 2));
            double b = -4 * really_h * t;
            double c = Math.pow(t, 2) - 4 * (Math.pow((2 * really_w * SA), 2));
            double delte = Math.pow(b, 2) - 4 * a * c;


            if(delte >= 0 ){

                double y1 = (-b + Math.sqrt(delte))/(2 * b);
                double y2 = (-b - Math.sqrt(delte))/(2 * b);
                double x1 = 0,x2 = 0;

                if ( y1 > 0){
                    x1  = Math.sqrt(Math.pow(SA, 2) - Math.pow(y1, 2));
                }

                if (y2 > 0){
                    x2 = Math.sqrt(Math.pow(SA, 2) - Math.pow(y2, 2));
                }

                if(y1 > 0 & y2 >0){
                    double one = Math.pow(x1 - really_w, 2.0) + Math.pow(y1, 2.0);
                    double two = Math.pow(x2 - really_w, 2.0) + Math.pow(y2, 2.0);
                    if (Math.abs(one - SB ) > Math.abs(two - SB)){
                        tem[1] = y2;
                        tem[0] = x2;
                    }else {
                        tem[1] = y1;
                        tem[0] = x1;
                    }
                }else if(y1 < 0 ){
                    tem[0] = x2;
                    tem[1] = y2;
                }else if(y2 < 0){
                    tem[0] = x1;
                    tem[1] = y1;
                }else{
                    tem[0] = 0;
                    tem[1] = 0;
                }
            }
        }
        return tem;
    }

    private double[] get_xy_three(double SB, double SC, double SD)
    {
        //用B，D画圆 ，C确定位置

        double tem[] = new double[2];
        tem[0] = 0; //x
        tem[1] = 0; //y

        //确保有交点,且交点在区域内
        if((Math.pow((SB + SD), 2) > Math.pow(really_h, 2) + Math.pow(really_w, 2))
                &&(Math.pow(SB, 2) < Math.pow(really_h, 2) + Math.pow(really_w, 2))
                &&(Math.pow(SD, 2) < Math.pow(really_h, 2) + Math.pow(really_w, 2))){

            //简化求解方程转化成-->>>a *  y^2 + b * y + c = 0
            double t = Math.pow(really_w, 2) + Math.pow(SD, 2) - Math.pow(SB, 2) - Math.pow(really_h, 2);
            double a = Math.pow(2 * really_h, 2) + Math.pow(2 * really_w, 2);
            double b = 4 * really_h * t - 8 * really_h * Math.pow(really_w, 2);
            double c = Math.pow(t, 2) - Math.pow(2 * really_w * SD, 2) + Math.pow(2 * really_w * really_h, 2);
            double delte = Math.pow(b, 2) - 4 * a * c;

            if(delte > 0) {
                double y1 = (-b + Math.sqrt(delte))/(2 * b);
                double y2 = (-b - Math.sqrt(delte))/(2 * b);
                double x1 = 0,x2 = 0;
                if ( y1 > 0) {
                    x1 = Math.sqrt(Math.pow(SD, 2) - Math.pow(y1 - really_h, 2));
                }
                if (y2 > 0) {
                    x2 = Math.sqrt(Math.pow(SD, 2) - Math.pow(y2 - really_h, 2));
                }

                if(y1 > 0 & y2 >0){
                    double one = Math.pow(x1 - really_w, 2.0) + Math.pow(y1 - really_h, 2.0);
                    double two = Math.pow(x2 - really_w, 2.0) + Math.pow(y2 - really_h, 2.0);
                    if (Math.abs(one - SC ) > Math.abs(two - SC)){
                        tem[1] = y2;
                        tem[0] = x2;
                    }else {
                        tem[1] = y1;
                        tem[0] = x1;
                    }
                }else if(y1 < 0 ){
                    tem[0] = x2;
                    tem[1] = y2;
                }else if(y2 < 0){
                    tem[0] = x1;
                    tem[1] = y1;
                }else{
                    tem[0] = 0;
                    tem[1] = 0;
                }
            }
        }
        return tem;
    }


    /**
     * 作弊方法，在固定的区域如果进行定位，根据4个AP的大小，直接随机到离AP最近的地方
     * */
    private float[] get_xy_no_way(float SA, float SB, float SC, float SD)
    {
        float xy[] = new float[2];
        float fac_h, fac_w;
        fac_h = (SC +  SD)/(SA + SB);
        fac_w = (SB + SC)/(SA + SD);
        xy[0] =  view_w/(1 + fac_w);
        xy[1] = view_h/(1 + fac_h);
        return xy;
    }



    private String[] filt_info(List<ScanResult> resultList)
    {

        /*
         * @param resultList:wifi 扫描的结果
         * @return tem:提取wifi扫描结果中的4个AP点的强度信息，并转换成字符串
         * */

        String tem[] = new String[4];

        for (int i=0;i <= 3; i++){

            tem[i]=String.valueOf(100);

        }

        for (ScanResult sc : resultList) {

            if (sc.SSID.equals(AccessPoint_A)) {

//                int temp = WifiManager.calculateSignalLevel(sc.level,100);

                tem[0] = String.valueOf(sc.level);

            }

            if (sc.SSID.equals(AccessPoint_B)) {

//                int temp = WifiManager.calculateSignalLevel(sc.level,100);

                tem[1] = String.valueOf(sc.level);

            }

            if (sc.SSID.equals(AccessPoint_C)) {

//                int temp = WifiManager.calculateSignalLevel(sc.level,100);

                tem[2] = String.valueOf(sc.level);

            }

            if(sc.SSID.equals(AccessPoint_D)){

//                int temp = WifiManager.calculateSignalLevel(sc.level,100);

                tem[3] = String.valueOf(sc.level);

            }


        }

        return tem;

    }

    private float[] real2view(double[] tem_xy){
        float[] view = new float[2];
        view[0] = keep2point((float) tem_xy[0] * scaling_factor_w);
        view[1] = keep2point((float) tem_xy[1] * scaling_factor_h);
        return view;
    }

    private float keep2point(float ft) {

        int scale = 2;//设置位数
        int roundingMode = 4;//表示四舍五入，可以选择其他舍值方式，例如去尾，等等.
        BigDecimal bd = new BigDecimal(ft);
        bd = bd.setScale(scale, roundingMode);
        ft = bd.floatValue();
        return ft;
    }
}



