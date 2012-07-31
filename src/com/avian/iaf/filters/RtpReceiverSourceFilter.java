/*
 * Created on Mar 12, 2005
 *
 */
package com.avian.iaf.filters;

import java.util.*;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import com.avian.iaf.rtp.*;

/**
 * @author Zack
 *
 */
public class RtpReceiverSourceFilter extends BaseSourceFilter {

	static class FilterMediaProcessor implements IRtpMediaProcessor {
		
		//sphinx requires a frame size of 80, apparently
		private final static int GRAPH_FRAME_SIZE = 80;
		
		Logger logger;
		
		/**
		 * 
		 * @param pinMap Maps RTP payload types to output pins
		 */
		public FilterMediaProcessor(Map<Integer,IOutputPin> pinMap) { 
			this.pinMap = pinMap;
			logger = Logger.getLogger(this.getClass());
		}
		
		/* (non-Javadoc)
		 * @see com.vogistix.rtp4j.RtpMediaProcessor#process(com.vogistix.rtp4j.RtpPacket)
		 */
		public void process(RtpPacket p) {
//			System.out.println("<<< " + p);
					
			int pt = p.getPayloadType();
			IOutputPin out = pinMap.get(pt);
			if(out != null) {
				
				//FIXME this is nasty, i know
				boolean isDtmfPin = (out.getFilter() instanceof Rfc2833DecoderFilter);
				
				if(!isDtmfPin) {
					//split the incoming data into frames and send it through the graph
					int payloadSize = p.getPayload().length;
					int payloadOfs = 0;
					while(payloadOfs < payloadSize) { 
						int bytesToRead = Math.min(GRAPH_FRAME_SIZE,payloadSize-payloadOfs);
						ByteBuffer b = MemoryBufferManager.requestBuffer();
						b.put(p.getPayload(),payloadOfs,bytesToRead);
						b.limit(b.position());
						payloadOfs += bytesToRead;
						out.push(b);
					}
				} else { 
					ByteBuffer b= MemoryBufferManager.requestBuffer();
					b.put(p.getPayload());
					out.push(b);
				}
			} else { 
				logger.error("Unrecognized RTP payload type encountered.");
			}
		}
		
		Map<Integer,IOutputPin> pinMap;
	}
	
	public RtpReceiverSourceFilter() { 
		rxer = new RtpReceiver();
		ulawPin = new MemoryOutputPin(this);
		dtmfPin = new MemoryOutputPin(this);
	}
	
	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.ISourceFilter#generate()
	 */
	public int generate() {
		return 0;
	}

	private Map<Integer,IOutputPin> createPinMap() { 
		Map<Integer,IOutputPin> pinMap = new HashMap<Integer,IOutputPin>() ;
		pinMap.put(0,ulawPin);
		pinMap.put(101,dtmfPin);
		
		return pinMap;
	}
	
	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.IFilter#initialize(java.util.Properties)
	 */
	public boolean initialize(Properties p) {
		//TODO: wire this source directly into the stack for now,
		//		we'll probably want to properly implement the 
		//		generate() method in the future, have it called
		//		at an externally dictated fixed interval, and buffer
		//		incoming RTP payloads in the media processor.
		
		//create listener
		String addressStr = p.getProperty("address") + ":" + p.getProperty("port");
	
		try {
			rxer.addListeningPoint(addressStr,Rtp4j.UDPIP4, 
					new FilterMediaProcessor(createPinMap()));
			return true;
		} catch (RtpException e) {
			e.printStackTrace();
			return false;
		}
	}

	public IOutputPin ulawPin;
	public IOutputPin dtmfPin;
	
	RtpReceiver rxer;
	
	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.IFilter#shutdown()
	 */
	public void shutdown() { }

	public void flush() {
		this.ulawPin.flush();
		this.dtmfPin.flush();
	}

}
