package com.avian.iaf.filters;

import java.nio.ByteBuffer;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.avian.iaf.rtp.dtmf.DtmfEvent;
import com.avian.iaf.rtp.dtmf.IDtmfHandler;

public class Rfc2833DecoderFilter extends BaseSinkFilter {

	IDtmfHandler handler;
	byte[] buffer;
	public IInputPin inPin;
	Logger logger;
	
	public Rfc2833DecoderFilter() { 
		this.inPin = new MemoryInputPin(this);
		buffer = new byte[4];
		
		logger = Logger.getLogger(this.getClass());
	}
	
	public boolean initialize(Properties p) {
		return true;
	}

	public synchronized void setHandler(IDtmfHandler dtmfHandler) { 
		this.handler = dtmfHandler;
	}
	public IOutputPin processInput(ByteBuffer src, ByteBuffer dest)
			throws UnsupportedOperationException {
		src.reset();
		//SHOULD only be four bytes
		src.get(buffer,0,src.remaining());
		MemoryBufferManager.releaseBuffer(src);
		
//		System.out.println("DTMF EVENT: " + buffer[0]);
		
		DtmfEvent event = new DtmfEvent();
		
		byte val = (byte)((buffer[1] >> 7) & 0xff);
		event.setDigit(buffer[0]);
		event.setEnd((buffer[1] >> 7) == -1);
		event.setVolume(buffer[1] & 0x3f);
		event.setDuration(buffer[3] & 0xff);
		
//		System.out.println(event);
		logger.debug("DTMF: " + event);
		if(handler != null) handler.handleDtmf(event);
		return null;
	}

	public void shutdown() {

	}

}
