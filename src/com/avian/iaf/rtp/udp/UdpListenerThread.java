/*
 * Created on Mar 30, 2005
 *
 */
package com.avian.iaf.rtp.udp;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

import com.avian.iaf.rtp.RtpException;
import com.avian.iaf.rtp.IRtpMediaProcessor;
import com.avian.iaf.rtp.RtpPacket;
import com.avian.iaf.rtp.IRtpPacketDispatcher;


class UdpListenerThread extends Thread {

	private ArrayList<UdpSelectorRegistration> pendingRegistrations;
	private Hashtable<SelectionKey,UdpSelectorRegistration> activeRegistrations;
	private Hashtable<Integer,DatagramSocket> portMap;
	
	private Selector selector;
	private IRtpPacketDispatcher dispatcher;
	private Logger logger;
	
	static final int SOCKET_BUFFER_SIZE = 4096;
	
	/**
	 * Creates the object, initializes the selector.
	 * @param d Tells the listener where to send the packets when it receives them.
	 * @throws IOException
	 */
	public UdpListenerThread(IRtpPacketDispatcher d) throws IOException {
		super("UdpIpV4RtpListener");
		pendingRegistrations = new ArrayList<UdpSelectorRegistration>();
		activeRegistrations = new Hashtable<SelectionKey,UdpSelectorRegistration>();
		this.selector = Selector.open();
		this.dispatcher = d;
		this.portMap = new Hashtable<Integer,DatagramSocket>();
		
		logger = Logger.getLogger(this.getClass());
	}

	public synchronized DatagramSocket getSocketForPort(int port) { 
		return portMap.get(port);
	}
	
	void registerNow(UdpSelectorRegistration r) throws RtpException {
		System.out.println("Registering...");
		if(selector == null) { 
			throw new RtpException("Failed to register listener, I/O selector not created.");
		}
		
		try {
			DatagramChannel dg = DatagramChannel.open();
			dg.configureBlocking(false);
			dg.socket().bind(new InetSocketAddress(r.iface,r.port));
			r.key = dg.register(selector,SelectionKey.OP_READ);
			
			UdpIp4RtpStack.get().setChannelForPort(dg,r.port);
//			portMap.put(r.port,dg.socket());
		} catch (SocketException e2) {
			e2.printStackTrace();
		} catch (ClosedChannelException e2) {
			e2.printStackTrace();
		} catch (IOException e2) {
			e2.printStackTrace();
		}
	}
	
	public void register(UdpSelectorRegistration r) { 
		synchronized(pendingRegistrations) { 
			pendingRegistrations.add(r);
		}

		selector.wakeup();
	}
	
	void addPendingToSelector() throws RtpException { 
		synchronized(pendingRegistrations) {
			Iterator<UdpSelectorRegistration> it = pendingRegistrations.iterator();
			
			while(it.hasNext()) {
				UdpSelectorRegistration r = it.next();
				registerNow(r);
				activeRegistrations.put(r.key,r);
			}
			
			pendingRegistrations.clear();
		}
	}
	
	public void run() {
		ByteBuffer buf = ByteBuffer.allocate(SOCKET_BUFFER_SIZE);
		
		logger.debug("Entering UDP/IPv4 RTP listener thread.");
		for(;;) {
			try {
				try {
					addPendingToSelector();
				} catch (RtpException e) {
					e.printStackTrace();
					break;
				} 
				
				//invoke selector, block for socket data
				selector.select(10000);
				
				Set<SelectionKey> keys = selector.selectedKeys();
				Iterator<SelectionKey> it = keys.iterator();
				
				while(it.hasNext()) { 
					SelectionKey key = it.next();						
					if(key.isReadable()) { 
						//obtain the datagram channel
						DatagramChannel channel = (DatagramChannel) key.channel();
						
						//reset packet buffer postion back to 0
						buf.clear();
						
						//fill the buffer
						SocketAddress sockAddr = channel.receive(buf);
				
						//determine how much data we read by reading the position
						int bytesRead = buf.position();

						if(bytesRead == 0) { 
							//no data? next.
							continue;
						}
						
						//reset the buffer position to the beginning of the data
						buf.position(0);
						
						//read the buffer data into the packet class
						RtpPacket p = new RtpPacket();
						
						buf.get(p.getHeader(),0,12);
						
						//FIXME: this is not taking into account payload-type 
						//extensions or contributing sources, it just
						//assumes the payload is immediately after the header
						int payloadSize = bytesRead - p.getHeader().length;
						p.setPayload(new byte[payloadSize]);
						
						
						buf.get(p.getPayload(),0,payloadSize);
						
						p.setAddress((InetSocketAddress)sockAddr);
						p.setRecvTime(System.nanoTime());
						
						//find the handler that matches
						SelectionKey handlerKey = channel.keyFor(selector);
						//System.out.println("Looking for handler: " + handlerKey);
						
						UdpSelectorRegistration r = activeRegistrations.get(handlerKey);
						IRtpMediaProcessor proc = (IRtpMediaProcessor) r.callback;
					
						p.setProcessor(proc);
					
//						System.out.println("<<< " + p);
						
						dispatcher.dispatchPacket(p,proc);
					}
				}				
			} catch (IOException e1) {
				e1.printStackTrace();
				break;
			}			
		}
	}
}