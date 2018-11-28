package com.example.esp8266_controllor;

import android.app.Activity;
import android.os.Bundle;

import android.os.AsyncTask;
import android.os.Message;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.net.Socket;
import java.net.UnknownHostException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import android.graphics.Color;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends Activity {
	private Button btnOne,btnConn,btnRcv;
	private Socket socket;
	OutputStream outputStream;
    // 主线程Handler
    // 用于将从服务器获取的消息显示出来
    private Handler mMainHandler;
    
	private ExecutorService mThreadPool;
	
    /**
     * 接收服务器消息 变量
     */
    // 输入流对象
    InputStream is,stInputStream;

    // 输入流读取器对象
    InputStreamReader isr ;
    BufferedReader br ;
    
    // 接收服务器发送过来的消息
    String response;
	
    // 输入需要发送的消息 输入框
    private EditText mEdit,mIpPortNum;
    private TextView receive_message, send_message;
    byte[] ucOpenCode = { 0x01,0x00,0x05,0x00,0x00,0x00 };//打开
    byte[] ucCloseCode = { 0x01,0x00,0x05,0x00,0x01,0x00 };//关闭
    int l32IpPort = 1213;
    int scrollAmount;
    int Led1State = 0;//初始状态位关闭

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        btnOne  = (Button) findViewById(R.id.button1);
        btnConn = (Button) findViewById(R.id.button2);
        btnRcv  = (Button) findViewById(R.id.button3);
        mEdit   = (EditText) findViewById(R.id.editText1);
        receive_message = (TextView) findViewById(R.id.receive_message);
        send_message    = (TextView) findViewById(R.id.send_message);
        mIpPortNum      = (EditText) findViewById(R.id.editText2);
        
        btnConn.setText("连接");

        
        btnOne.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(Led1State == 1){
                	Led1State = 0;
                    btnOne.setBackgroundColor(Color.GREEN);
                    System.out.println("y00416125: Switch OFF");
                	try {
                        outputStream.write(ucOpenCode);
                        send_message.append("y00416125: Switch OFF\n");
                        scrollAmount = send_message.getLayout().getLineTop(send_message.getLineCount()) 
                                - send_message.getHeight();
                        if (scrollAmount > 0)
                        	send_message.scrollTo(0, scrollAmount);
                        else
                        	send_message.scrollTo(0, 0);     
                        // 步骤3：发送数据到服务端
                        outputStream.flush();
                        
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                	Led1State = 1;
                    btnOne.setBackgroundColor(Color.GRAY);
                	System.out.println("y00416125: Switch ON");
                	try {
                        outputStream.write(ucCloseCode);
                        send_message.append("y00416125: Switch ON\n");
                        scrollAmount = send_message.getLayout().getLineTop(send_message.getLineCount()) 
                                - send_message.getHeight();
                        if (scrollAmount > 0)
                        	send_message.scrollTo(0, scrollAmount);
                        else
                        	send_message.scrollTo(0, 0);   
                        
                        // 步骤3：发送数据到服务端
                        outputStream.flush();
                        
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        
        btnConn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(btnConn.getText().toString().equals("连接")){
                	btnConn.setText("断开");
                    System.out.println("y00416125: DisConn ");
                    l32IpPort = Integer.parseInt(mIpPortNum.getText().toString());
                    System.out.println("y00416125: ********BEGIN*CONN*************** ");
                    
                	if((mEdit.getText().toString() != null)
                			&&(l32IpPort != 0))
                	{
                    
                    AsyncTask<Void, String, Void> read = new AsyncTask<Void, String, Void>()
                    		{

								@Override
								protected Void doInBackground(Void... arg0) {
									
	
				                		System.out.println("y00416125: ********start*CONN*************** ");
				                 // 创建Socket对象 & 指定服务端的IP 及 端口号
				                    try {
										socket = new Socket(mEdit.getText().toString(), l32IpPort);
										
					                    // 判断客户端和服务器是否连接成功
					                    System.out.println(socket.isConnected());
					                    
										outputStream = socket.getOutputStream();
	
					                    // 步骤1：创建输入流对象InputStream
										stInputStream = socket.getInputStream();
										
										
									} catch (UnknownHostException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
	
				                    // 步骤2：创建输入流读取器对象 并传入输入流对象
				                    // 该对象作用：获取服务器返回的数据
				                    isr = new InputStreamReader(stInputStream);
				                    br = new BufferedReader(isr);
									
									String line;
									try {
										while((line = br.readLine()) != null)
										{
											publishProgress(line);
										}
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
									return null;
								}

								@Override
								protected void onProgressUpdate(
										String... values) {
									receive_message.append(values[0]);
									super.onProgressUpdate(values);
								}
								
								
                    		};
                    read.execute();
                	}
                    
                }else{
                	btnConn.setText("连接");
                	System.out.println("y00416125: DisConn ");
                	if(socket.isConnected())
                	{
	                	try {
	                        // 断开 客户端发送到服务器 的连接，即关闭输出流对象OutputStream
	                        outputStream.close();
	                		
	                        // 最终关闭整个Socket连接
	                        socket.close();
	                        
	                        // 断开 服务器发送到客户端 的连接，即关闭输入流读取器对象BufferedReader
	                        br.close();
	                        
	                        // 判断客户端和服务器是否已经断开连接
	                        System.out.println(socket.isConnected());
	                        
	                    } catch (IOException e) {
	                        e.printStackTrace();
	                    }
                	}
                }
            	
                
                }
        });
        

        /**
         * 接收 服务器消息
         */
        btnRcv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	//new Thread(MainActivity.this).start();
            }
        });    
        
        
    }

    
}
