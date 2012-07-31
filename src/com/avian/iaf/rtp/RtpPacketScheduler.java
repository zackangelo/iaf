package com.avian.iaf.rtp;

import java.util.*;
import java.util.concurrent.*;

import org.apache.log4j.Logger;

/**
 * This class maintains a series of queues for each sync source
 * that contain packets sorted by 
 * timestamp.  It will block when no data is available from any of the queues.
 *  
 * @author zangelo
 *
 */
public class RtpPacketScheduler {
	//a map of packet queues, keyed by synchronization source
	Map<Long,RtpPacketQueue> queueMap;
	private final static int QUEUE_SIZE = 512;
	
	//which of our queues are active?
	private List<RtpPacketQueue> activeList;
	private final static int ACTIVE_LIST_SIZE = 64;
	
	Logger logger;
	
	public RtpPacketScheduler() { 
		logger = Logger.getLogger(this.getClass());
		
		activeList = new ArrayList<RtpPacketQueue>(ACTIVE_LIST_SIZE);
		queueMap = new ConcurrentHashMap<Long,RtpPacketQueue>(ACTIVE_LIST_SIZE);
	}
	
	public synchronized void schedule(RtpPacket p) { 
		//do we have a queue for this sync source?
		RtpPacketQueue q = queueMap.get(p.getSyncSource());
		boolean queueExists = (q != null) ;
		
		if(!queueExists) { 
			//no, let's create one
			logger.debug("Creating new packet queue for sync source: " + p.getSyncSource());
			q = new RtpPacketQueue(QUEUE_SIZE);
			queueMap.put(p.getSyncSource(),q);
		} 
		
		//schedule the packet in its appropriate queue
		q.insert(p);
		
		//add this queue to the active list so that it may be retrieved
		if(!activeList.contains(q)) {
			activeList.add(q);
		}
		
		notifyAll();
	}
	
	public synchronized RtpPacket getPacketAndAcquireQueue() {
		//block until some packets are ready
		while(true) {
			if(activeList.isEmpty()) { 
				try {
//					logger.debug("blocking!");
					wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} else { 
				break;
			}
		}
		
		//remove and save the top item
		RtpPacketQueue q = activeList.remove(0);
		
		//return the next packet in the queue
		return q.get();
	}
	
	public synchronized void releaseQueue(RtpPacket p) { 
		RtpPacketQueue q = queueMap.get(p.getSyncSource());

		//if this queue isn't empty after they're done with it, put it
		//back in the active list
		if(!q.empty()) { 
			activeList.add(q);
		}
	}

}
