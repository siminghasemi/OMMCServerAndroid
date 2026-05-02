package sockets;

import java.net.DatagramPacket;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SharedQueueStr {
ConcurrentLinkedQueue<String> clq= new ConcurrentLinkedQueue<String>();
	
	public void add(String s){
		clq.add(s);
	}
	
	public String  remove(){
		return clq.poll();
	}
	
	public boolean isEmpty(){
		return clq.isEmpty();
	}
	
	public void print(){
		Iterator<String> itr= clq.iterator();
		while(itr.hasNext()){
			System.out.println("in AddThread "+itr.next());
		}
	}
	
	public void print2(){
		Iterator<String> itr= clq.iterator();
		while(itr.hasNext()){
			System.out.println(" , "+itr.next());
		}
	}

}
