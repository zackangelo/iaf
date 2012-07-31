/*
 * Created on Mar 12, 2005
 *
 */
package com.avian.iaf.filters;

import java.nio.ByteBuffer;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.avian.iaf.rtp.Rtp4j;
import com.avian.iaf.rtp.RtpException;
import com.avian.iaf.rtp.RtpTransmitter;

/**
 * @author Zack Angelo
 *
 */
public class RtpTransmitterSinkFilter extends BaseSinkFilter {
	private RtpTransmitter txer;
	public IInputPin inPin;
	private Logger logger;
	
	public RtpTransmitterSinkFilter() { 
		txer = new RtpTransmitter();
		inPin = new MemoryInputPin(this);
		logger = Logger.getLogger(this.getClass());
	}
	
	public RtpTransmitterSinkFilter(String srcAddr,int srcPort) { 
		txer = new RtpTransmitter(srcAddr,srcPort);
		inPin = new MemoryInputPin(this);
		logger = Logger.getLogger(this.getClass());
	}
	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.IFilter#initialize(java.util.Properties)
	 */
	public boolean initialize(Properties p) {
		try {
//			logger.debug("Adding RTP Target -> "+p.getProperty("address")+":"+p.getProperty("port"));
			txer.addTarget(p.getProperty("address") + ":" + p.getProperty("port"),Rtp4j.UDPIP4,(byte)0);
			return true;
		} catch (RtpException e) {
			e.printStackTrace();
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.IFilter#processInput(java.nio.ByteBuffer, java.nio.ByteBuffer)
	 */
	public IOutputPin processInput(ByteBuffer src, ByteBuffer dest)
			throws UnsupportedOperationException {

		src.reset();
		
		int len = src.limit() - src.position();
		
		//FIXME: use statically allocated buffer (might mean adjusting
		//			RTP transmitter API)
		if(len > 0) {
			byte[] buf = new byte[len];
			src.get(buf,0,len);
			txer.send(buf,len);
		}
		
		return null;
	}

	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.IFilter#shutdown()
	 */
	public void shutdown() {	}

}
