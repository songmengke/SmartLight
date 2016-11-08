package com.example.songmengkewangkun.smartlight;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by 王坤 on 2016-11-6.
 *
 */
public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private ToggleButton tb;
    private ToggleButton tb3;
    private ToggleButton tb4;
    private ToggleButton tb5;
    private ImageView img;
    private ImageView img3;
    private ImageView img4;
    private ImageView img5;
    private final static String ON = "1111";
    private final static String OFF = "0000";
    private String lightStatus = "0000";

    //建立socket用来开关灯
    private Socket mSocket;
    private InputStreamReader mInputStreamReader = null;
    private OutputStream mOutputStream = null;
    private PrintWriter pw = null;

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Toast.makeText(MainActivity.this,
                            R.string.cannot_open_and_close, Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.light);

        //初始化控件
        tb = (ToggleButton) findViewById(R.id.toggleButton2);
        tb3 = (ToggleButton) findViewById(R.id.toggleButton3);
        tb4 = (ToggleButton) findViewById(R.id.toggleButton4);
        tb5 = (ToggleButton) findViewById(R.id.toggleButton5);
        img = (ImageView) findViewById(R.id.imageView2);
        img3 = (ImageView) findViewById(R.id.imageView3);
        img4 = (ImageView) findViewById(R.id.imageView4);
        img5 = (ImageView) findViewById(R.id.imageView5);

        //给ToggleButton设置监听器
        tb.setOnCheckedChangeListener(this);
        tb3.setOnCheckedChangeListener(this);
        tb4.setOnCheckedChangeListener(this);
        tb5.setOnCheckedChangeListener(this);
        new OpenTheSocket().execute();

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //当tb被点击时 此方法被执行
        switch (buttonView.getId()){
            case R.id.toggleButton2:
                if(isChecked){
                    lightStatus = ON;
                    img.setBackgroundResource(R.drawable.on);
                    LightControl();
                }else{
                    lightStatus =OFF;
                    img.setBackgroundResource(R.drawable.off);
                    LightControl();
                }
                break;
            case R.id.toggleButton3:
                if(isChecked){
                    lightStatus = ON;
                    img3.setBackgroundResource(R.drawable.on);
//                    LightControl();
                }else{
                    lightStatus =OFF;
                    img3.setBackgroundResource(R.drawable.off);
//                    LightControl();
                }
                break;
            case R.id.toggleButton4:
                if(isChecked){
                    lightStatus = ON;
                    img4.setBackgroundResource(R.drawable.on);
//                    LightControl();
                }else{
                    lightStatus =OFF;
                    img4.setBackgroundResource(R.drawable.off);
//                    LightControl();
                }
                break;
            case R.id.toggleButton5:
                if(isChecked){
                    lightStatus = ON;
                    img5.setBackgroundResource(R.drawable.on);
//                    LightControl();
                }else{
                    lightStatus =OFF;
                    img5.setBackgroundResource(R.drawable.off);
//                    LightControl();
                }
                break;

        }

    }
    public class OpenTheSocket extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Log.i("OpenTheSocket", "doInBackground: ");
                mSocket = new Socket("115.159.64.71", 8888);
                mOutputStream = mSocket.getOutputStream();
                mInputStreamReader = new InputStreamReader(mSocket.getInputStream());
                pw=new PrintWriter(mOutputStream);
            } catch (Exception e) {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
                e.printStackTrace();
            }
            return null;
        }
    }
    private void LightControl() {
        new ControlTheLight().execute();
    }

    public class ControlTheLight extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                //向服务器发送开关灯的命令
                //并且接受服务器的返还消息
                //如果服务器返还了消息，那么就表示开关灯成功
                //就将灯的信息存储到本地
                Log.i("status", lightStatus);
//                mOutputStream.write(lightStatus.getBytes("utf-8"));
                pw.write(lightStatus);
                pw.flush();
                char[] c = new char[4];
                mInputStreamReader.read(c, 0, 4);
                String line = new String(c);
                Log.i("返回值", line);
                if (line.equals(lightStatus)) {
                    SharedPreferences.Editor editor = getSharedPreferences("golbal_data", MODE_PRIVATE).edit();
                    editor.putString("light_window_control", lightStatus);
                    editor.commit();
                } else {
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);
                }
            } catch (Exception e) {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
                e.printStackTrace();
            }
            return null;
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在Activity结束的时候
        //关闭输入输出流和socket
        new CloseTheSocket().execute();
    }

    public class CloseTheSocket extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
//                mOutputStream.write("close".getBytes("utf-8"));
                pw.close();
                mInputStreamReader.close();
                mOutputStream.close();
                mSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
