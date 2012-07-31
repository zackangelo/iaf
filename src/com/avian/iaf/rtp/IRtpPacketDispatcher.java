/*
 * Created on Mar 30, 2005
 *
 */
package com.avian.iaf.rtp;

/**
 * @author Zack
 * 
 * Dispatches packet to appropriate handler and fills in any secondary fields.
 * 
 *
 */
public interface IRtpPacketDispatcher {
	public void dispatchPacket(RtpPacket p,IRtpMediaProcessor proc);
}
