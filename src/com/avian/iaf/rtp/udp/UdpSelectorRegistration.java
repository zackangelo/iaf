/*
 * Created on Mar 30, 2005
 *
 */
package com.avian.iaf.rtp.udp;

import java.nio.channels.SelectionKey;

import com.avian.iaf.rtp.IRtpMediaProcessor;


class UdpSelectorRegistration { 
	public UdpSelectorRegistration(String iface,int port,IRtpMediaProcessor callback) { 
		this.iface = iface;
		this.callback = callback;
		this.port = port;
	}

	public String iface;
	public int port;
	IRtpMediaProcessor callback;
	SelectionKey key;
}