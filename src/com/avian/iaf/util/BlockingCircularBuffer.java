package com.avian.iaf.util;

import org.apache.log4j.Logger;

public class BlockingCircularBuffer {
	private int size;
	private int readPos;
	private int writePos;
	byte[] buffer;
	Logger logger;
	
	public BlockingCircularBuffer(int size) {
		buffer = new byte[size];
		this.size = size;
		logger = Logger.getLogger(this.getClass());
	}
	
	private int readPos() { 
		return readPos % size;
	}
	
	private int writePos() { 
		return writePos % size;
	}
	
	public int availableRead() { 
		return writePos - readPos;
	}
	
	public int availableWrite() { 
		return size - availableRead();
	}
	
	public int read(byte[] dest) { 
		return read(dest,0,dest.length);
	}
	
	public synchronized int read(byte[] dest,int ofs,int len) { 
		while(availableRead() == 0) {
			try { /*logger.debug("Blocking for read()...");*/ wait(); } catch(InterruptedException e) { }
		}
		
		len = Math.min(availableRead(),len);
		
		int bytesLeft = len;
		
		while(bytesLeft > 0) {
			int available = Math.min(availableRead(),bytesLeft);
			while(available > 0) { 
				int bytesToRead = Math.min(available,size - readPos());
				System.arraycopy(buffer,readPos(),dest,ofs,bytesToRead);
				readPos += bytesToRead;
				ofs += bytesToRead;
				available -= bytesToRead;
				bytesLeft -= bytesToRead;
			}
		}
		
		notifyAll();
		
		return len;
	}
	
	public int write(byte[] src) { 
		return write(src,0,src.length);
	}
	
	public synchronized int write(byte[] src,int ofs,int len) { 
		int bytesLeft = len;
		while(bytesLeft > 0) {
			//if there's no room in the buffer to write, block until there is
			while(availableWrite() == 0) {
				try { logger.debug("Blocking for write()..."); wait(); } catch(InterruptedException e) { }
			}
			
			int available = Math.min(availableWrite(),bytesLeft);
			while(available > 0) {
				int bytesToWrite = Math.min(available,size-writePos());
				System.arraycopy(src,ofs,buffer,writePos(),bytesToWrite);
				writePos += bytesToWrite;
				ofs += bytesToWrite;
				available -= bytesToWrite;
				bytesLeft -= bytesToWrite;
			}
			
			notifyAll();
		}
		
		return len;
	}
}
