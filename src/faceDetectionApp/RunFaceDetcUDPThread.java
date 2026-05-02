package faceDetectionApp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

//import sockets.SharedQueue;
import sockets.SharedQueueStr;


import com.example.omc_server.MainServerActivity;
import com.example.omc_server.R;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.media.FaceDetector;
import android.os.Environment;
import android.util.Log;
import android.widget.EditText;

public class RunFaceDetcUDPThread extends Thread {
	private Bitmap mFaceBitmap;
	private int mFaceWidth = 200;
	private int mFaceHeight = 200;   
	private static final int MAX_FACES = 10;
	private static String TAG = "TutorialOnFaceDetect";
	
	long endTimeTot, startTimeTot;
    
	
	EditText etxt;
	long durTime;
	int n;
	String inputFileName;
	Resources resource;
	File myFile ;
	FileOutputStream fOut;
	BufferedWriter bufWriter_out;
	
	boolean keepListening = true; 
	private int sleeptime = 500;
	String localIPstrServer;
	String localIPstrClient;
	private SharedQueueStr Face_Recieve_Q;
    //private SharedQueue Face_Send_Q;
    
    int NumAllocatedTasks=0;
	int tskPic[] ;
        
    private int RECEIVING_PORT_ClientRec = 8888+10;
    DatagramSocket udpsender ;

        
	
    public RunFaceDetcUDPThread(Resources resource, SharedQueueStr Face_Recieve_Q,/* SharedQueue Face_Send_Q,*/ String localIPstrServer, String localIPstrClient)
	 {
        this.n = 1; //n;
        this.resource = resource;
        this.Face_Recieve_Q = Face_Recieve_Q;
        //this.Face_Send_Q = Face_Send_Q;
        this.localIPstrClient = localIPstrClient;
        this.localIPstrServer = localIPstrServer;
        keepListening = true;        
        }
		
	public void stopThread(){
			this.stop();
		}
		
	private void printAtUiupdater(String s){
		byte[] buffer2 = s.getBytes();	int len = s.length();				
		MainServerActivity.UIupdater.obtainMessage(0,len, -1, buffer2).sendToTarget();		
	}
    public void run() {
    	byte[] buffer = new byte[52000];
		String s ;
		
    	long sumExeFace=0;
        int nextPicInd=-1;
    	while(keepListening){	            
    	String poll = null;
        synchronized (Face_Recieve_Q) {
            while (Face_Recieve_Q.isEmpty()) {
                try {
                    Face_Recieve_Q.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
            }
            while(!Face_Recieve_Q.isEmpty()){
                poll = Face_Recieve_Q.remove();//  receivemsgsQ locking and synchroniz
                String receiveedfileName = poll;//.getData().toString();
                inputFileName = receiveedfileName;
                long durationTime = 0;
                
                if(poll.indexOf(",")>=0){
                    //start
                    startTimeTot = System.currentTimeMillis();
                    String arrs[] = poll.split(",");
                    NumAllocatedTasks = arrs.length-1;
                    tskPic = new int[arrs.length-1];
                    for(int i=0; i<arrs.length-1;i++){
                        tskPic[i] = Integer.parseInt(arrs[i]);
                        System.out.println("tskPic[" + i + "] ="+tskPic[i]);
                        s = "tskPic[" + i + "] ="+tskPic[i]+"\n";
                        printAtUiupdater(s);
                    }
                    
                }else{
                    nextPicInd++;
                    int picNum=-1;
                    if(tskPic!=null){
                        picNum = tskPic[nextPicInd];
                        System.out.println("load PicNum " + picNum);
//                        s = "load PicNum " + picNum+"\n";
//                        printAtUiupdater(s);
                    }else{
                        System.out.println("tskPic = null, load PicNum " + picNum);
                        s =  "tskPic = null, load PicNum " + picNum +"\n";
                        printAtUiupdater(s);
                    }
                    
                    if(nextPicInd+1==NumAllocatedTasks){
                        System.out.print("Hey :) All tasks finished!!!!!!!!!!!!");
                        s = "Hey :) All tasks finished!!!!!!!!!!!!" +"\n";
                        printAtUiupdater(s);
                        keepListening = false;
                        break;
                    }
                }
                
                              
            }//while !Face_Recieve_Q.isEmpty
            }//synchronized Face_Recieve_Q

            try {
                //System.out.println("RunFace sleeps.");//("in faceDetect in OMC server: sleeps in run face thread after synchronized " );	        
                this.sleep(sleeptime);
                //System.out.println("RunFace wake up.");
            } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    //e.printStackTrace();
            }
        }//while(keepListening)
        
        for(int i=0; i<NumAllocatedTasks;i++){
            int picNum = tskPic[i];
            System.out.println("load PicNum " + picNum);
            printAtUiupdater("load PicNum " + picNum);
            
         // load the photo
    		Bitmap b = null;
    		String fileName = " ";
    		
            if(picNum == -1){
            	b = BitmapFactory.decodeResource(resource, R.drawable.joannaandjimmy900);
                fileName = "joannaandjimmy900";
                System.out.println("picNum = -1");
                s = "picNum = -1"+"\n";
                printAtUiupdater(s);
            }
            if(picNum == 0){
            	b = BitmapFactory.decodeResource(resource, R.drawable.twogirls2540kbyte); //face3
                    fileName = "twogirls2540kbyte";
            }
            if(picNum == 1){
            	b = BitmapFactory.decodeResource(resource, R.drawable.detektor_award_1mb);
                    fileName = "detektor_award_1mb";
            }
            if(picNum == 2){//(inputFileName.compareToIgnoreCase("joannaandjimmy900") == 0){
            	b = BitmapFactory.decodeResource(resource, R.drawable.joannaandjimmy900);
                    fileName = "joannaandjimmy900";
            }                        
            if(picNum == 3){
            	b = BitmapFactory.decodeResource(resource, R.drawable.oceanseleven380); //face3
                    fileName = "oceanseleven380";
            }                        
            if(picNum == 4){
            	b = BitmapFactory.decodeResource(resource, R.drawable.elinorpicsmall142); //face3
                    fileName = "elinorpicsmall142";
            }
            if(picNum == 5){
            	b = BitmapFactory.decodeResource(resource, R.drawable.elinorpicsmall142); //face3
                    fileName = "elinorpicsmall142";
            }
            if(picNum == 6){
            	b = BitmapFactory.decodeResource(resource, R.drawable.detektor_award_1mb);
                    fileName = "detektor_award_1mb";
            }
            if(picNum == 7){
            	b = BitmapFactory.decodeResource(resource, R.drawable.oceanseleven380); //face3
                    fileName = "oceanseleven380";
            } 
            if(picNum == 8){
            	b = BitmapFactory.decodeResource(resource, R.drawable.joannaandjimmy900);
                    fileName = "joannaandjimmy900";
            } 
            if(picNum == 9){
            	b = BitmapFactory.decodeResource(resource, R.drawable.joannaandjimmy900);
                    fileName = "joannaandjimmy900";
            } 
            if(picNum == 10){
            	b = BitmapFactory.decodeResource(resource, R.drawable.detektor_award_1mb);
                    fileName = "detektor_award_1mb";
            }
            if(picNum == 12){//(inputFileName.compareToIgnoreCase("joannaandjimmy900") == 0){
            	b = BitmapFactory.decodeResource(resource, R.drawable.joannaandjimmy900);
                    fileName = "joannaandjimmy900";
            }                        
            if(picNum == 13){
            	b = BitmapFactory.decodeResource(resource, R.drawable.oceanseleven380); //face3
                    fileName = "oceanseleven380";
            }                        
            if(picNum == 14){
            	b = BitmapFactory.decodeResource(resource, R.drawable.elinorpicsmall142); //face3
                    fileName = "elinorpicsmall142";
            }
            
    		
    		mFaceBitmap = b.copy(Bitmap.Config.RGB_565, true); 
    		b.recycle();
    		mFaceWidth = mFaceBitmap.getWidth();
    		mFaceHeight = mFaceBitmap.getHeight();  
    		
    		// perform face detection and set the feature points
    		for(int j=0; j<this.n; j++){//j<2
    			long durationTime = setFace();
    			sumExeFace += durationTime;
    		}
    		         
        }//for pic
        System.out.println("Running Faces Finished");
        endTimeTot = System.currentTimeMillis();
        long durT = endTimeTot-startTimeTot;
        System.out.println("Total Time on "+localIPstrServer+" is " + durT);
        System.out.println("Total exe time on "+localIPstrServer+" is " + sumExeFace);
        s = "Exe time on "+localIPstrServer+" is " + sumExeFace;
        printAtUiupdater(s);
        s = "Total Time on is " + durT+"\n";
        printAtUiupdater(s);
        
                
    	InetAddress localAddr = null , destIP = null ;
	try {
		localAddr = InetAddress.getByName( localIPstrServer);//192.168.43.83 , emu=127.0.0.0  y=43.1
		destIP = InetAddress.getByName(localIPstrClient); // 192.168.43.1 or 89		
	} catch (UnknownHostException e1) {
		e1.printStackTrace();
	}
	try {
		udpsender = new DatagramSocket(RECEIVING_PORT_ClientRec, localAddr);
	} catch (SocketException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
        
        String sss = NumAllocatedTasks + "," + durT+ "," + localIPstrServer;        
        byte[] fileNameBuf = new byte[1024];
        //System.arraycopy(s.getBytes(), 0, fileNameBuf, 0, s.length());
        fileNameBuf= sss.getBytes();

        DatagramPacket packetfileName = new DatagramPacket(fileNameBuf,fileNameBuf.length ,destIP , RECEIVING_PORT_ClientRec );
        try {
                    udpsender.send(packetfileName);
            } catch (IOException e1) {
                    e1.printStackTrace();
            }
        	
    	  
    }//run
    
    public long setFace() {
		int [] fpx = null;
		int [] fpy = null;
		int count = 0;
		FaceDetector fd;
		FaceDetector.Face [] faces = new FaceDetector.Face[MAX_FACES];
		PointF midpoint = new PointF();
		long startTime = System.currentTimeMillis();
		for(int c=0; c < this.n; c++){
			try {
				fd = new FaceDetector(mFaceWidth, mFaceHeight, MAX_FACES);        
				count = fd.findFaces(mFaceBitmap, faces);
			} catch (Exception e) {
				Log.e(TAG, "setFace(): " + e.toString());
				return 0;
			}		
		}//for

        long endTime = System.currentTimeMillis();
        long durationTime = (endTime - startTime);
        durTime = (endTime - startTime);
        Log.d(getClass().getName(), "faceDetec took " + durationTime + " (ms) and find " + count + "faces.");
        String s = "faceDetec took " + durationTime + " (ms) and find " + count + "faces."+"\n";
        printAtUiupdater(s);
        // check if we detect any faces
		/*if (count > 0) {
			fpx = new int[count];
			fpy = new int[count];
			for (int i = 0; i < count; i++) { 
				try {                 
					faces[i].getMidPoint(midpoint);                  

					fpx[i] = (int)midpoint.x;
					fpy[i] = (int)midpoint.y;
				} catch (Exception e) { 
					Log.e(TAG, "setFace(): face " + i + ": " + e.toString());
				}            
			}      
		}*/
		return durationTime;

	} 

 
    //---call this from the main activity to 
    // shutdown the connection--- a
    public void cancel() {
        //udpReceiversocket.close();
    }
}