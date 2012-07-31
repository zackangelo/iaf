/*
 * Created on Jan 4, 2005
 *
 * 
 */
package com.avian.iaf.rtp;

import java.net.*;
import java.util.*;

import org.apache.log4j.Logger;

import com.avian.iaf.rtp.udp.UdpIp4RtpStack;

/**
 * @author Zack
 *
 */
public class RtpTransmitter {
	Logger logger;
	InetSocketAddress localAddr;
	
	public RtpTransmitter() { 
		targets = new Vector<InetSocketAddress>();
	
		Random r = new Random(System.currentTimeMillis());
		
		logger = Logger.getLogger(this.getClass());
		//initialize our context
		ctx = new RtpTransmitterContext();
		ctx.payloadType = 0;
		ctx.sequence = (short)(r.nextInt() % Short.MAX_VALUE);
		ctx.syncSourceId = (r.nextInt() % Integer.MAX_VALUE) - Integer.MAX_VALUE;
		ctx.timestamp = (r.nextInt() % Integer.MAX_VALUE) - Integer.MAX_VALUE;
	}
	
	/**
	 * 
	 * @param iface interface to bind on
	 * @param port port to bind on
	 */
	public RtpTransmitter(String iface,int port) { 
		this();
		localAddr = new InetSocketAddress(iface,port);
	}
	
	/**
	 * Can we share a context among many targets, or do we have to have
	 * separate sync sources, timestamp offsets, etc for each one?
	 * (share for now) 
	 * 
	 * FIXME make them separate
	 */
	RtpTransmitterContext ctx;
	
	/** 
	 * 
	 * @param address
	 * @param protoType
	 * @param payloadType
	 * @throws RtpException
	 */
	public void addTarget(String address,int protoType,byte payloadType) throws RtpException {
		switch(protoType) {
			case Rtp4j.UDPIP4:
//				UdpIp4RtpStack.get().addListeningPoint(
//						Rtp4jUtil.parseIpAddress(address,protoType),p
//				);
				
				InetSocketAddress addr = Rtp4jUtil.parseIpAddress(address,protoType);
				targets.add(addr);
				break;
			default: 
				throw new RtpException("Unrecognized protocol specified.");
		}
	}

	private void advanceContext(int numSamples) { 
		ctx.sequence++;
		ctx.timestamp += numSamples;
	}

	/**
	 * Builds a packet from the current context
	 * @return 
	 */
	private RtpPacket buildPacket() { 
		RtpPacket p = new RtpPacket();
	
		p.setPayloadType(ctx.payloadType);
		p.setSequenceNumber(ctx.sequence);
		p.setSyncSource(ctx.syncSourceId);
		p.setMarked(false);
		p.setPayloadPadded(false);
		p.setTimestamp(ctx.timestamp);
		p.setVersion();
		return p;
	}
	
	
	/**
	 * Sends the specified payload to the targets assigned to this
	 * transmitter. 
	 * @param payload Media data to send.
	 * @param len Length of media samples.
	 */
	public void send(byte[] payload,int numSamples) { 
		for(InetSocketAddress target : targets) { 
			advanceContext(numSamples);
			RtpPacket p = buildPacket();
			
			p.setPayload(payload);
			p.setAddress(target);

			if(localAddr != null) { 
				p.setSourceAddress(localAddr);
			}
//			logger.debug("RTP -> " + p);
			UdpIp4RtpStack.get().sendPacket(p);
		}
	}
	
	Vector<InetSocketAddress> targets;
}
