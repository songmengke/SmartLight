package com.example.songmengkewangkun.smartlight;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Created by 王坤 on 2016-11-6.
 */
public class BootScreen extends Activity {

    private StringBuffer lightWindowControl;
    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    Toast.makeText(BootScreen.this, R.string.cannot_connect_the_network, Toast.LENGTH_SHORT).show();
                    break;
                case 2:
                    //跳转到MainActivity
                    Intent intent = new Intent(BootScreen.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                default:
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.boot_screen);

        //从服务器获取状态信息
        new ReceiveInfo().execute();

        //停留在引导界面，给socket 1.5秒接收的时间
        handler.sendEmptyMessageDelayed(2, 1500);

    }

    public class ReceiveInfo extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... params) {
            try {
                //创建Socket
                //向服务器发送消息
                //建立输出流
                //将Socket对应的输入流包装BufferedReader
                Log.i("Socket", "Start..");
                Socket socket = new Socket("115.159.64.71", 8888);
                OutputStream os = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(os);
                String info = "0000";
                pw.write(info);
                pw.flush();

                InputStreamReader is = new InputStreamReader(socket.getInputStream());
                char[] c = new char[255];
                is.read(c, 0, 255);
                String line = new String(c);
                Log.i("wangkun", line);
                //关闭流
                //关闭Socket
                is.close();
                pw.close();
                os.close();
                socket.close();
                Log.i("Scoket", "Closed");
            } catch (Exception e) {
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
                e.printStackTrace();
            }
            return null;
        }

    }
}
