/*
 * Created on Jan 30, 2005
 *
 */
package com.avian.iaf.rtp;


/**
 * @author Zack
 *
 *	Priority queue, ordered by timestamp, implemented by a sorted heap.
 */
public class RtpPacketQueue {
	RtpPacket[] pq;
	int size; 
	
	public RtpPacketQueue(int maxSize) {
		pq = new RtpPacket[maxSize+1];
		size = 0;
	}
	
	private void swim(int k) { 
		while(k > 1 && less(k/2,k)) {
			exch(k,k/2); k = k/2; 
		}
	}
	
	private void sink(int k, int n) { 
		while(2*k <= size) { 
			int j = 2*k;
			if (j < size && less(j,j+1)) j++;
			if (!less(k,j)) break;
			exch(k,j); k = j;
		}
	}
	
	public void insert(RtpPacket p) {
		assert (p == null);
		pq[++size] = p;
		swim(size);
	}
	
	public RtpPacket get() {
		if(size == 0) return null; 
		RtpPacket p = pq[1];
		exch(1,size); 
		sink(1,size--); 
		return p;
	}
	
	private void exch(int i,int j) { 
		RtpPacket t = pq[i];
		pq[i] = pq[j];
		pq[j] = t;
	}
	
	private boolean less(int i,int j) { 
		return (pq[i].getTimestamp() > pq[j].getTimestamp());
	}
	
	public boolean empty() { 
		return size == 0;
	}
	
	public RtpPacket peekMax() { 
		return pq[1];
	}
	
	public RtpPacket peekMin() { 
		RtpPacket r;
		return null;
	}
}
