/*
 * Created on Dec 31, 2004
 *
 * 
 */
package com.avian.iaf.rtp.udp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import org.apache.log4j.Logger;

import com.avian.iaf.rtp.RtpException;
import com.avian.iaf.rtp.IRtpMediaProcessor;
import com.avian.iaf.rtp.RtpPacket;
import com.avian.iaf.rtp.RtpStack;

import java.nio.*;
import java.nio.channels.DatagramChannel;

import java.util.*;
/**
 * @author Zack
 *
 *	Singleton I/O multiplexing UDP/IPv4 stack.
 */
public class UdpIp4RtpStack extends RtpStack {
	UdpListenerThread listenerThread;
	UdpTransmitterThread xmitterThread;
	Logger logger;
	
	//associates local IP ports to datagram channels so the 
	//	transmitter and receiver can share them
	Map<Integer,DatagramChannel> portMap;
	
	
	private UdpIp4RtpStack() {
		super();
		logger = Logger.getLogger(this.getClass());
		portMap = new Hashtable<Integer,DatagramChannel>();
	}
	
	static UdpIp4RtpStack singleton = null;
	
	public static UdpIp4RtpStack get() { 
		if(singleton == null) { 
			singleton = new UdpIp4RtpStack();
		}
		
		return singleton;
	}
	
	public void setChannelForPort(DatagramChannel c,int port) { 
		portMap.put(port,c);
	}
	
	public DatagramChannel getChannelForPort(int port) { 
		return portMap.get(port);
	}
	
	public void addListeningPoint(InetSocketAddress addr,IRtpMediaProcessor proc) throws RtpException {
		UdpSelectorRegistration r = new UdpSelectorRegistration(
				addr.getAddress().getHostAddress(),
				addr.getPort(),
				proc);
		
		logger.debug("Attempting RTP listen on " + addr);
		if(listenerThread == null) {
			try {
				listenerThread = new UdpListenerThread(this);
				listenerThread.register(r);
				listenerThread.start();
			} catch (IOException e) {
				throw new RtpException("Unable to launch listener thread.");
			}
		} else {
			listenerThread.register(r);
		}
	}
	
	public void sendPacket(RtpPacket p) { 
		
		//record packet timing information
		p.setQueuedTime(System.nanoTime());
		
		if(xmitterThread == null) { 
			xmitterThread = new UdpTransmitterThread();
			xmitterThread.queueOutboundPacket(p);
			xmitterThread.start();
		} else { 
			xmitterThread.queueOutboundPacket(p);
		}
	}
	
	protected void removeListeningPoint(InetSocketAddress addr) { 
		
	}
}
