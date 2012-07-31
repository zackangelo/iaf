package com.avian.iaf.rtp;

import org.apache.log4j.Logger;

public class RtpPacketPlayerThread extends Thread {
	private static int threadIndex;
	
	private RtpPacketScheduler scheduler;
	private Logger logger;
	
	public RtpPacketPlayerThread(RtpPacketScheduler scheduler) { 
		super("RtpPacketPlayer-"+(threadIndex++));
		
		this.scheduler = scheduler;
		this.logger = Logger.getLogger(this.getClass());
	}
	
	public void run() {
		while(true) { 
			RtpPacket p = scheduler.getPacketAndAcquireQueue();
			
			if(p == null) {
				logger.debug("Null packet encountered.");
				continue;
			}
			
			IRtpMediaProcessor processor = p.getProcessor();
		
			p.setProcessTime(System.nanoTime());
			processor.process(p);
			p.setDecodeTime(System.nanoTime());
			
//			System.out.println(p);
//			p.printTiming();
			
			scheduler.releaseQueue(p);
		}
	}
}
