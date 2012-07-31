/*
 * Created on Mar 31, 2005
 *
 */
package com.avian.iaf.rtp.udp;

import com.avian.iaf.rtp.RtpPacket;
import com.avian.iaf.rtp.RtpPacketFifo;

import java.io.IOException;
import java.nio.channels.*;
import java.nio.*;
import java.net.*;
import java.util.*;

import org.apache.log4j.Logger;

/**
 * NOTE: we need to have a channel dedicated to each rtp transmitter, and then bind that transmitter to the same
 * 		 address and port we're receiving on, some SIP/RTP clients (such as X-Lite,*ARGH*) obey a 
 * 		"reverse udp mapping" rule to help with firewall traversal.
 * @author Zack
 *
 */
public class UdpTransmitterThread extends Thread {

	private final static int FIFO_SIZE = 1024;
	private final static int PACKET_BUFFER_SIZE = 4096;
	Logger logger = Logger.getLogger(UdpTransmitterThread.class);
	DatagramChannel channel;
	
	public UdpTransmitterThread() { 
		super("UdpTransmitterThread");
		
		fifo = new RtpPacketFifo(FIFO_SIZE);
		done = false;
	
//		try {
//			channel = DatagramChannel.open();
//		} catch (IOException e) {
//			e.printStackTrace();
//			return;
//		}
		
		
		buf = ByteBuffer.allocate(PACKET_BUFFER_SIZE);
	}
	
	/**
	 * Thoughts: do we give each thread its own indepedent FIFO and 
	 * distribute the packets in the transmitter? OR do we have them 
	 * share a FIFO instance? (indepedent FIFOs for now)
	 */
	
	RtpPacketFifo fifo;
	boolean done;
	ByteBuffer buf;
	
	public void queueOutboundPacket(RtpPacket p) { 
		synchronized(fifo) {
			fifo.put(p); 
			fifo.notifyAll(); 
		}
	}

	public void shutdown() { 
		done = true;
	}
	
	/**
	 * 
	 * @param packetBuf Temporary buffer space to use to assemble packet
	 * @param p Packet to send
	 */
	private void sendPacket(ByteBuffer packetBuf,DatagramChannel channel,RtpPacket p) 
			throws IOException { 
		
		packetBuf.clear();
		packetBuf.put(p.getHeader());
		packetBuf.put(p.getPayload());
		packetBuf.limit(packetBuf.position());
		packetBuf.position(0);
		
		System.out.println(">>> " + p);
		
		channel.send(packetBuf,p.getAddress());
		
//		socket.send(packetBuf,p.getAddress());
//		DatagramPacket dgramPacket = new DatagramPacket(packetBuf.array(),packetBuf.limit());
//		dgramPacket.setAddress(p.getAddress().getAddress());
		
//		socket.send(dgramPacket);
	}
	
	/**
	 * Cycles through the FIFO, dispatching packets to their respective
	 *  targets.
	 */
	public void run() {
		ByteBuffer packetBuf = ByteBuffer.allocate(PACKET_BUFFER_SIZE);
		DatagramChannel defaultChannel;

		
		try { 
			defaultChannel = DatagramChannel.open();
			defaultChannel.configureBlocking(false);
		} catch (IOException e) { 
			e.printStackTrace();
			return;
		}
		
		//open socket for packets that aren't bound to a specific local address
			while(!done) { 
				try {
					DatagramChannel sendChan;
					InetSocketAddress sourceAddr;
					
					RtpPacket rtpPacket = fifo.get();	//blocks...
					
					sourceAddr = rtpPacket.getSourceAddress();
					
					if(sourceAddr != null) { 
//						sendSock = socketMap.get(sourceAddr.getPort());
						sendChan = UdpIp4RtpStack.get().getChannelForPort(sourceAddr.getPort());
						
						if(sendChan == null) { 
//							sendSock = new DatagramSocket(sourceAddr.getPort(),InetAddress.getByName("0.0.0.0"));
//							socketMap.put(sourceAddr.getPort(),sendSock);
							
							sendChan = defaultChannel;
						}
					} else { 
						sendChan = defaultChannel;
					}
					
					sendPacket(packetBuf,sendChan,rtpPacket);
				} catch (IOException e1) {
					e1.printStackTrace();
					break;
				}
			}
	}
}
