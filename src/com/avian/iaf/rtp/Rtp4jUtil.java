/*
 * Created on Jan 11, 2005
 *
 * 
 */
package com.avian.iaf.rtp;
import java.net.InetSocketAddress;

/**
 * @author Zack
 *
 */
public final class Rtp4jUtil {

	protected static InetSocketAddress parseIpAddress(String addrStr,int protoType) {
		/**
		 * FIXME: actually parse IPv4|6 address and port, then create the 
		 * appropriate object, for now we just pass it along to the InetSocketAddress
		 * ctor. (v4 support only)
		 */
		String spl[] = addrStr.split(":");
		return new InetSocketAddress(spl[0],Integer.parseInt(spl[1]));
	}
}
