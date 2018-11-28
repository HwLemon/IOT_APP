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
    // ���߳�Handler
    // ���ڽ��ӷ�������ȡ����Ϣ��ʾ����
    private Handler mMainHandler;
    
	private ExecutorService mThreadPool;
	
    /**
     * ���շ�������Ϣ ����
     */
    // ����������
    InputStream is,stInputStream;

    // ��������ȡ������
    InputStreamReader isr ;
    BufferedReader br ;
    
    // ���շ��������͹�������Ϣ
    String response;
	
    // ������Ҫ���͵���Ϣ �����
    private EditText mEdit,mIpPortNum;
    private TextView receive_message, send_message;
    byte[] ucOpenCode = { 0x01,0x00,0x05,0x00,0x00,0x00 };//��
    byte[] ucCloseCode = { 0x01,0x00,0x05,0x00,0x01,0x00 };//�ر�
    int l32IpPort = 1213;
    int scrollAmount;
    int Led1State = 0;//��ʼ״̬λ�ر�

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
        
        btnConn.setText("����");

        
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
                        // ����3���������ݵ������
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
                        
                        // ����3���������ݵ������
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
                if(btnConn.getText().toString().equals("����")){
                	btnConn.setText("�Ͽ�");
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
				                 // ����Socket���� & ָ������˵�IP �� �˿ں�
				                    try {
										socket = new Socket(mEdit.getText().toString(), l32IpPort);
										
					                    // �жϿͻ��˺ͷ������Ƿ����ӳɹ�
					                    System.out.println(socket.isConnected());
					                    
										outputStream = socket.getOutputStream();
	
					                    // ����1����������������InputStream
										stInputStream = socket.getInputStream();
										
										
									} catch (UnknownHostException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									} catch (IOException e1) {
										// TODO Auto-generated catch block
										e1.printStackTrace();
									}
	
				                    // ����2��������������ȡ������ ����������������
				                    // �ö������ã���ȡ���������ص�����
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
                	btnConn.setText("����");
                	System.out.println("y00416125: DisConn ");
                	if(socket.isConnected())
                	{
	                	try {
	                        // �Ͽ� �ͻ��˷��͵������� �����ӣ����ر����������OutputStream
	                        outputStream.close();
	                		
	                        // ���չر�����Socket����
	                        socket.close();
	                        
	                        // �Ͽ� ���������͵��ͻ��� �����ӣ����ر���������ȡ������BufferedReader
	                        br.close();
	                        
	                        // �жϿͻ��˺ͷ������Ƿ��Ѿ��Ͽ�����
	                        System.out.println(socket.isConnected());
	                        
	                    } catch (IOException e) {
	                        e.printStackTrace();
	                    }
                	}
                }
            	
                
                }
        });
        

        /**
         * ���� ��������Ϣ
         */
        btnRcv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	//new Thread(MainActivity.this).start();
            }
        });    
        
        
    }

    
}
