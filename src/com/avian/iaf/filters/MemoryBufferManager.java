/*
 * Created on Mar 3, 2005
 *
 */
package com.avian.iaf.filters;

import java.util.*;
import java.nio.*;

import org.apache.log4j.Logger;

/**
 * @author zangelo
 *
 */
public class MemoryBufferManager {
	//allocate 32K buffers
	private final static int STATIC_BUFFER_SIZE = 5120;
	private final static int BUFFER_QUEUE_SIZE = 64;
	private static Logger logger;
	
	private final static Vector<ByteBuffer> freeBuffers = new Vector<ByteBuffer>(BUFFER_QUEUE_SIZE);
	
	static {
		for(int i=0;i<BUFFER_QUEUE_SIZE;i++) { 
			freeBuffers.add(ByteBuffer.allocate(STATIC_BUFFER_SIZE));
		}
		
		logger = Logger.getLogger(MemoryBufferManager.class);
	}
	public synchronized static ByteBuffer requestBuffer() { 
//		ByteBuffer b =  freeBuffers.lastElement(); 
		ByteBuffer b = ByteBuffer.allocate(STATIC_BUFFER_SIZE);
//		freeBuffers.removeElementAt(freeBuffers.size() - 1);
		b.clear();
		b.mark();
		return b;
	}
	
	
	public synchronized static void releaseBuffer(ByteBuffer b) { 
//		freeBuffers.add(b);
	}

}
