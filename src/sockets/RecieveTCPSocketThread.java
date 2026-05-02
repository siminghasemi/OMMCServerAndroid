package sockets;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import android.os.Environment;

import com.example.omc_server.MainServerActivity;



public class RecieveTCPSocketThread extends Thread{

	long startTime, endTime, durTime , sum=0 , avgBandwidth;
	boolean keepListening = true; 
	private int sleeptime = 100;//500;
	private int sleeptimeRecievingFile = 10;//400;
	final int RECEIVING_PORT = 8888;
	private SharedQueueStr Face_Recieve_Q;
	String localIPstrServer;
	String localIPstrClient;

	private Socket TCPReceiverSocket = null;
    private PrintStream outStrm = null;
    private DataInputStream inStrm = null;

    File myFile ;
	BufferedWriter bufWriter_out;
	

	public RecieveTCPSocketThread(String localIPstrServer, String localIPstrClient , SharedQueueStr Face_Recieve_Q) { 
		this.Face_Recieve_Q = Face_Recieve_Q;	
		this.localIPstrServer = localIPstrServer;
		this.localIPstrClient = localIPstrClient;
		keepListening = true;
	}
	
	public void stopThread(){
		keepListening = false;
        try {            
        	TCPReceiverSocket.close();
        } catch (IOException ex) {
        	ex.printStackTrace();
        }
	}
	
	@Override
	public void run() {
		try {
            TCPReceiverSocket = new Socket(this.localIPstrClient, RECEIVING_PORT);
            outStrm = new PrintStream(TCPReceiverSocket.getOutputStream());
            inStrm = new DataInputStream(TCPReceiverSocket.getInputStream());
        } catch (UnknownHostException e) {
            System.err.println("Don't know about host " + this.localIPstrClient);
        }catch (IOException e) {
            System.err.println("Couldn't get I/O for the connection to the host " + this.localIPstrClient);
        }
		
		
		int c=0;
        String s = "RecieveSocketThread on Server started.";
        byte[] buffer2 = s.getBytes();//new byte[52000];
		int len = s.length();				
		MainServerActivity.UIupdater.obtainMessage(0,len, -1, buffer2).sendToTarget();
		
        //System.out.println(s);
        String RecievedfileName = " ";
        long startTimeTot = System.currentTimeMillis();
        long endTimeTot;
        
        boolean WrToFile = false;
        FileOutputStream foutStrWr = null;
        byte[] buffer = new byte[1024];
        int count;
        
        try {
            while((count = inStrm.read(buffer)) >= 0){ //((inputLine = inStrm.readLine()) != null){//(keepListening){	
                String msgthatReceive = new String(buffer, 0, count);
                if(msgthatReceive != null){
                	
                    //1- first packet contains information about all allocated subtasks and their file name and pic number
                    if(msgthatReceive.indexOf("?!Simin")!= -1){
                        System.out.println("Allocated Files: " + msgthatReceive + ".");
                        s = "Allocated Files: " + msgthatReceive + ".";
                        buffer2 = s.getBytes();
                		len = s.length();				
                		MainServerActivity.UIupdater.obtainMessage(0,len, -1, buffer2).sendToTarget();                        
                        Face_Recieve_Q.add(msgthatReceive);//(receivePacketfileName);
                            synchronized (Face_Recieve_Q) {
                                    Face_Recieve_Q.notify();
                            }                        
                    }                    
                    
                    //2- Start reciveing a file
                    if(msgthatReceive.indexOf("&@&")!= -1){
                        WrToFile = true;
                        c++;
                        RecievedfileName = c + "-file" + ".txt"; 
                        System.out.println("begin reciving " + RecievedfileName);
                        s = "begin reciving " + RecievedfileName;
                        buffer2 = s.getBytes();
                		len = s.length();				
                		MainServerActivity.UIupdater.obtainMessage(0,len, -1, buffer2).sendToTarget();                        
//                        try {
//                            foutStrWr = new FileOutputStream(RecievedfileName);//sdCard???
//                        } catch (FileNotFoundException ex) {
//                        	ex.printStackTrace();
//                        }
                		
                		prepareWritingtoFile(RecievedfileName);
                        
                        startTimeTot = System.currentTimeMillis();
                    }
                    
                    //3- recive file and save it to local RecievedfileName
                    if(WrToFile == true){
                    	bufWriter_out.write(msgthatReceive, 0, count);
                        //foutStrWr.write(buffer, 0, count);
                    }
                    
                    //4- end of file $
                    if(msgthatReceive.indexOf("$$@$$")!= -1){
                        System.out.println("end of file $ "+ RecievedfileName);
                        endTimeTot = System.currentTimeMillis();
                        System.out.println("transferT = " + (endTimeTot-startTimeTot));
                        s = "end of file $ "+ RecievedfileName + "\n transferT = " + (endTimeTot-startTimeTot);
                        buffer2 = s.getBytes();
                		len = s.length();				
                		MainServerActivity.UIupdater.obtainMessage(0,len, -1, buffer2).sendToTarget();
                        //foutStrWr.write(buffer, 0, count);
                        //foutStrWr.close();
                		bufWriter_out.close();
                        WrToFile = false;
                        Face_Recieve_Q.add(RecievedfileName);//(receivePacketfileName);
                        synchronized (Face_Recieve_Q) {
                            Face_Recieve_Q.notify();
                        }
                    }
                    
//                     else{ System.out.print("misData " /*+ msgthatReceive*/); }
                }                
//                try {  this.sleep(sleeptime);
//                } catch (InterruptedException e) { e.printStackTrace();}
            }//while
        } catch (IOException ex) {
        	ex.printStackTrace();
        }
        
        try {
            outStrm.close();
            inStrm.close();
            TCPReceiverSocket.close();
        } catch (IOException ex) {
        	ex.printStackTrace();
        }
        System.out.println("\nudpreceiver finished");
        s = "\nudpreceiver finished.\n";
        buffer2 = s.getBytes();
        len = s.length();
        MainServerActivity.UIupdater.obtainMessage(0,len, -1, buffer2).sendToTarget();		
	}//run

	void prepareWritingtoFile(String filename){
		//Open File Write results
        //write runTime of Algorithm on file "ra_algorithms_runtime_htc.txt" on SdCard
		String sdPath = Environment.getExternalStorageDirectory().toString();
		//String filename = "ra_runtime_htc.txt";
		myFile = new File(sdPath +  File.separator + "/Android/data/" + filename);//("/sdcard/mysdfile.txt");
		if(myFile.exists())	{
		   try{
			   bufWriter_out = new BufferedWriter(new FileWriter(myFile, true)); 
			   
		   } catch(Exception e){
			   e.printStackTrace();
		    }
		} else{
			try {
				myFile.createNewFile();
				bufWriter_out = new BufferedWriter(new FileWriter(myFile, true));
								
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
    
    
}
