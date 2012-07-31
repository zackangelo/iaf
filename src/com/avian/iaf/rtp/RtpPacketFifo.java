/*
 * Created on Mar 31, 2005
 *
 */
package com.avian.iaf.rtp;

/**
 * @author Zack
 *
 * Simple array-backed packet FIFO. Based on Sedgewick.
 */
public class RtpPacketFifo {
	private RtpPacket[] q; 
	private int N,head,tail;
	
	public RtpPacketFifo(int maxSize) { 
		q = new RtpPacket[maxSize+1];
		N = maxSize + 1;
		head = N;
		tail = 0;
	}
	
	public boolean empty() { 
		return ((head % N) == tail);
	}
	
	public synchronized void put(RtpPacket p) { 
		q[tail++] = p;
		tail = tail % N;
		
		notifyAll();
	}
	
	public synchronized RtpPacket get() { 
		while(true) {
			if(empty()) {
				try {
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else {
				break;
			}
		}
		
		head = head % N;
		return q[head++];
	}
}
