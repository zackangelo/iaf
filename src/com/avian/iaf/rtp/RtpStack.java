/*
 * Created on Dec 31, 2004
 *
 * 
 */
package com.avian.iaf.rtp;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author Zack
 * 
 * Abstract class that handles sync source management and event dispatching. 
 * Specific transport and I/O multiplexing is handled in derived 
 * concrete classes. 
 */
public abstract class RtpStack implements IRtpPacketDispatcher {
	/**
	 * 
	 * @author Zack
	 *
	 *	Class describing a source of data, keeps statistics for RTCP transmission
	 *	and enforces jitter timing rules.
	 */
	private static class SyncSource { 
		/**
		 * Shared timer instance for jitter status assignment.
		 */
		private static Timer jitterTimer = new Timer(true);
		
		private static class StatusAssignmentTask extends TimerTask {
			SyncSource source;
			int status;
			
			public StatusAssignmentTask(SyncSource source,int status) {
				this.source = source;
				this.status = status;
			}
			
			public void run() {
				source.setStatus(status);

				if(status == SyncSource.STATUS_WAITING) {
					synchronized(RtpStack.playbackLatch) {
						RtpStack.playbackLatch.notify();
					}
				}
			}
			
		}
		
		public static final int STATUS_BUFFERING = 0;	
		public static final int STATUS_BUFFERED = 1;	
		public static final int STATUS_WAITING = 2;
		public static final int STATUS_PLAYING = 3;
		
		private final int QUEUE_SIZE = 256;
	
		private long startTime;
		public void startTimer() { 
			if(startTime != 0) { 
				System.out.println("Starting timer while it is already started.");
			}
			
			startTime = System.nanoTime();

		}
		
		public long stopTimer() { 
			long diff = System.nanoTime()-startTime;
			startTime = 0;
			return diff;
		}
		
		public SyncSource(long id) { 
			this.id = id;
			this.consecDropped = 0;
			this.packetsRecvSinceRebuffer = 0;
			this.status = STATUS_BUFFERING;
			queue = new RtpPacketQueue(QUEUE_SIZE);
		}
		
		public SyncSource(long id,IRtpMediaProcessor p) {
			this(id);
			this.processor = p;
		}
		
		public synchronized int getStatus() { 
			return status;
		}
		
		public synchronized void setStatus(int status) { 
			this.status = status;
		}
		
		/**
		 * status for this source
		 */
		int status;
		
		/**
		 * handler for this media
		 */
		IRtpMediaProcessor processor;
		
		/**
		 * Synchronization source identifier
		 */
		private long id;
		
		/**
		 * Priority queue, sorted by RTP timestamp
		 */
		private RtpPacketQueue queue;
		
		/**
		 * The number of consecutively dropped packets.
		 */
		private int consecDropped;
		
		/**
		 * Consecutive number of rebuffers with no packets 
		 */
		private int consecRebuffers;
		
		/**
		 * The total number of packets received
		 */
		private long packetsRecv;
		
		private long packetsRecvSinceRebuffer;
		
		/**
		 * Schedules a packet for playback, if the arrival time is greater
		 * than the most recent packet in the queue, it is dropped. Once 
		 * the number of dropped packets reaches the specified threshold,
		 * the jitter timer is reset so that the packet buffer can 
		 * reaccumulate.
		 * 
		 * @param p Packet to be scheduled.
		 */
		public void schedule(RtpPacket p) { 
			synchronized(queue) {
				queue.insert(p);
			}
			
			packetsRecvSinceRebuffer++;
			
			//is this our first packet?
			if(packetsRecv++ == 0) { 
				//yes, reset the jitter timer
				rebuffer();
			}
		}
		
		public int rebuffer() { 
			//System.out.println("Rebuffering...");
			
			if(packetsRecvSinceRebuffer == 0) {
				consecRebuffers++;
			}
			
			packetsRecvSinceRebuffer = 0;
			
			synchronized(jitterTimer) {
				jitterTimer.schedule(new StatusAssignmentTask(this,STATUS_WAITING), getJitterDelay());
			}
			
			return consecRebuffers;
		}
		
		public RtpPacket nextPacket() { 
			RtpPacket p = null;
			
			synchronized(queue) {
				p = queue.get();
			}

			return p;
		}
		/**
		 * For now, this is a static value, but in the future it will be 
		 * determined from RTCP statistics kept in this object.
		 *
		 * @return The calculated jitter delay for this synchronization source.
		 */
		private long getJitterDelay() {
			return DEFAULT_JITTER_DELAY;
		}
		
		/**
		 * Default jitter delay value in milliseconds.
		 */
		private final static long DEFAULT_JITTER_DELAY = 100;
		
		
		/**
		 * @return Returns the processor.
		 */
		public IRtpMediaProcessor getProcessor() {
			return processor;
		}
		/**
		 * @param processor The processor to set.
		 */
		public void setProcessor(IRtpMediaProcessor processor) {
			this.processor = processor;
		}
		
		/**
		 * @return Returns the consecRebuffers.
		 */
		public int getConsecRebuffers() {
			return consecRebuffers;
		}
	}
	
	private static Object playbackLatch;
	
	/**
	 * 
	 * @author zangelo
	 *
	 * Searches through sync source table looking for tasks that are both
	 * in playback mode and have packets waiting.
	 */
	private static class PlaybackTask implements Runnable {
		Map<Long,SyncSource> table;
		private final static int MAX_REBUFFERS = 5;
		
		public PlaybackTask(Map<Long,SyncSource> table) {
			this.table = table;
		}

		public void run() {
			while(true) { 
				Iterator<SyncSource> it = table.values().iterator();
				int playingCount = 0; 
				while(it.hasNext()) { 
					SyncSource s = it.next();
					if(s.getStatus() == SyncSource.STATUS_WAITING) { 
						RtpPacket p = s.nextPacket();
						s.setStatus(SyncSource.STATUS_PLAYING);

						/**
						 * Have we exhausted the queue? 
						 */
						if(p != null) { //nope, process the next packet
							p.setProcessTime(System.nanoTime());
							s.getProcessor().process(p);
							
							p.setDecodeTime(System.nanoTime());
							
							p.printTiming();
							
							s.setStatus(SyncSource.STATUS_WAITING);
							playingCount++;
						} else {		//yes, rebuffer (wait for more packets to arrive)
							
							//if we've had to continually wait for data to arrive
							// on this sync source, it probably means they're not sending anymore
							// data. Let's remove them from the queue and reset the stats.
							if(s.getConsecRebuffers() >= MAX_REBUFFERS) {
								table.remove(s.id);
							} else {
								s.setStatus(SyncSource.STATUS_BUFFERING);
								s.rebuffer();
							}
						}
					}
				}	//end (it.hasNext())
				
				//were there any buffers waiting to be played? 
				if(playingCount == 0) {
					synchronized(RtpStack.playbackLatch) {
						//nope, wait until there are
						try {
							System.out.println("Blocking...");
							RtpStack.playbackLatch.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}
		} 
	}
	
	/**
	 * Maps synchronization source IDs to session event handlers.
	 */
	private Map<Long,SyncSource> syncSourceTable;
	
	private RtpPacketScheduler scheduler;
	private RtpPacketPlayerThread playerThread;
	
	protected RtpStack() { 
		playbackLatch = new Object();
		syncSourceTable = new ConcurrentHashMap<Long,SyncSource>();

//		new Thread(new PlaybackTask(syncSourceTable),"PlaybackThread").start();
	
		scheduler = new RtpPacketScheduler();
		playerThread = new RtpPacketPlayerThread(scheduler);
		
		playerThread.start();
	}
	
	public void dispatchPacket(RtpPacket p,IRtpMediaProcessor proc) { 
		//TODO Convert RTP timestamp value into 64-bit timestamp value so no
		//		wrap around occurs
		
		//TODO Create application-centric, wall clock based timestamp value 
		//		suitable for mixing purposes
		
//		SyncSource s;
//		if(!syncSourceTable.containsKey(p.getSyncSource())) {
//			 s = new SyncSource(p.getSyncSource(),proc);
//			syncSourceTable.put((long)p.getSyncSource(),s);
//		} else { 
//			s = syncSourceTable.get(p.getSyncSource());
//		}
//		
//		
//		s.schedule(p);
		
		scheduler.schedule(p);
		
	}
	
	protected RtpPacket buildPacket(RtpTransmitterContext target,byte[] payload,int len) {
		RtpPacket p = new RtpPacket();
		
		//update the context
		target.sequence++;
		target.timestamp += len;
		
		p.setSyncSource(target.syncSourceId);
		p.setPayload(payload);
		p.setSequenceNumber((short)target.sequence);
		p.setTimestamp((int)target.timestamp);
		p.setVersion();
		
		return p;
	}
}
