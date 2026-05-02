package com.example.omc_server;

import java.net.SocketException;
import java.net.UnknownHostException;

import sockets.RecieveTCPSocketThread;
import sockets.NotUsed_RecieveUDPSocketThread;
import sockets.SharedQueueStr;

import com.example.omc_server.R;

import faceDetectionApp.RunFaceDetcUDPThread;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.res.Resources;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.support.v4.app.NavUtils;

public class MainServerActivity extends Activity {

	RunFaceDetcUDPThread runFaceDetcThread;
    RecieveTCPSocketThread recieveSocketThread;
    private SharedQueueStr Face_Recieve_Q;
    String localIPstrServer;
    String localIPstrClient;
    Resources resource;
    static EditText showDone;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final EditText et_localIPstrServer = (EditText) findViewById(R.id.editText_localIPstrServer);
        final EditText et_localIPstrClient = (EditText) findViewById(R.id.editText_localIPstrClient);
        showDone = (EditText) findViewById(R.id.showDone);
        Face_Recieve_Q = new SharedQueueStr();
       resource = getResources();
        Button b = (Button) findViewById(R.id.button_start);
        b.setOnClickListener(new OnClickListener() {
			//@Override
			public void onClick(View v) {
				localIPstrServer = "192.168.43." + et_localIPstrServer.getText().toString();
				localIPstrClient = "192.168.43." + et_localIPstrClient.getText().toString();		        
		        //send and recieve cooperative nodes and discovery  -> find m.
				new CreateRecieveSocketThreadTask().execute();
				new CreateRunFaceDetcThreadTask().execute();
				//new CreateSendSocketThreadTask().execute();
				showDone.setText("all started.\n");
				}
		});			     
    }    
    
    @Override
    public void onBackPressed(){
    	System.out.println("On Back");
    	recieveSocketThread.stopThread();
    	//sendSocketThread.stopThread();
    	super.onBackPressed();
    	return ;
    }

	//---used for updating the UI on the main activity---
	public static Handler UIupdater = new Handler() {
		@Override
		public void handleMessage(Message msg) {              
			int numOfBytesReceived = msg.arg1;
			byte[] buffer = (byte[]) msg.obj;			
			//---convert the entire byte array to string---
			String strReceived = new String(buffer);
			//---extract only the actual string received---
			strReceived = strReceived.substring(0, numOfBytesReceived);
			if(strReceived.length()<50)
				//---display the text received on the TextView---              
				showDone.setText(showDone.getText().toString() + strReceived + "\n");
		}
	};
	
	private class CreateRunFaceDetcThreadTask extends AsyncTask <Void, Integer, Void> {
		@Override
        protected Void doInBackground(Void... params) {            
            //start face app thread on local mobile
			runFaceDetcThread = new RunFaceDetcUDPThread(resource , Face_Recieve_Q,localIPstrServer, localIPstrClient);
			runFaceDetcThread.start();
            return null;
        }
    }
	
	private class CreateRecieveSocketThreadTask extends AsyncTask <Void, Integer, Void> {
		@Override
        protected Void doInBackground(Void... params) {            
            recieveSocketThread = new RecieveTCPSocketThread(localIPstrServer, localIPstrClient, Face_Recieve_Q);			
			recieveSocketThread.start();
            return null;
        }
    }
	
	/*@Override
    public void onResume() {
        super.onResume();
        new CreateCommThreadTask().execute();
    }*/
    
	/*@Override
    public void onPause() {
        super.onPause();
        new CloseSocketTask().execute();
    }*/
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    
}
