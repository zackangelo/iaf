/*
 * Created on Jan 4, 2005
 *
 * 
 */
package com.avian.iaf.rtp;

import com.avian.iaf.rtp.udp.UdpIp4RtpStack;

/**
 * @author Zack
 *
 */
public class RtpReceiver {
	public void addListeningPoint(String address,int protoType,IRtpMediaProcessor p) throws RtpException {
		switch(protoType) {
			case Rtp4j.UDPIP4:
				UdpIp4RtpStack.get().addListeningPoint(
						Rtp4jUtil.parseIpAddress(address,protoType),p
				);
				break;
			default: 
				throw new RtpException("Unrecognized protocol specified.");
		}
	}
}
