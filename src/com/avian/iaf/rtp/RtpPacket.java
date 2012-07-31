/*
 * Created on Dec 31, 2004
 *
 */
package com.avian.iaf.rtp;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * Wrapper class for a single RTP packet, packet is assembled in place to avoid
 * repetitious assignment and assembly.
 * 
 * @author Zack
 *
 */
public class RtpPacket {
	
	
	
	private final static int MANDATORY_HEADER_SIZE = 12;
	
	private byte[] header;		//mandatory header:
	 							//	version,padding,csrc count,
								//	marker, payload type, seq,
								//	timestamp,ssrc
	
	private byte[] payload;		//payload

	private InetSocketAddress address;
	private InetSocketAddress sourceAddress;
	private IRtpMediaProcessor processor;
	
	//TODO: Support for contributing sources
	
	public RtpPacket() {
		header = new byte[MANDATORY_HEADER_SIZE];
		payload = null;
		//setVersion();
	}
	
	public String toString() {
		return "srcaddr="+getSourceAddress()+",addr="+getAddress()+",v="+getVersion()+",p="+isPayloadPadded()+",x=,ccc=,m="+isMarked()
		+",pt="+getPayloadType()+",seq="+getSequenceNumber()+",ts="+getTimestamp()
		+",ssrc="+Integer.toHexString((int)getSyncSource())+",payloadSize="+payload.length;
		
	}
	/**
	 * Resets the version bits to the only acceptable RTP version: 2.
	 */
	public void setVersion() { 
		header[0] |= (byte)(0x2 << 6);
	}
	
	public short getVersion() { 
		return (short) ((header[0] >> 6) & 0x03);
	}
	
	/**
	 * Sets the payload padding bit .
	 */
	public void setPayloadPadded(boolean isPadded) {
		if(isPadded) { 
			header[0] &= 0x20;
		}
	}
	
	/**
	 * 
	 * @return true if the payload padding bit is set, false if it isn't.
	 */
	public boolean isPayloadPadded() {
		return false;
	}
	
	/**
	 * Sets the marker flag. 
	 * @param isMarked true if marked, false if not.
	 */
	public void setMarked(boolean isMarked) {
		
	}
	
	/**
	 * Returns the marker flag.
	 * @return true if marked, false if not.
	 */
	public boolean isMarked() { 
		return false;
	}
	
	/**
	 * Sets the payload type.
	 * @param payloadType 
	 */
	public void setPayloadType(byte payloadType) {
		header[1] |= (payloadType >> 1);
	}
	
	/**
	 * 
	 * @return the payload type.
	 */
	public byte getPayloadType() {
		return (byte)(header[1] & 0x7f);
	}
	
	/**
	 * Sets the sequence number for this packet.
	 * @param seqNum Sequence number to set.
	 */
	public void setSequenceNumber(short seqNum) {
		header[3] = (byte)(seqNum);
		header[2] = (byte)(seqNum >> 8);
	}
	
	/**
	 * 
	 * @return the sequence number associated with this packet.
	 */
	public int getSequenceNumber() {
		return ((header[3] & 0xff) | ((header[2] & 0xff) << 8)) & 0xffff;
	}
	
	/**
	 * Sets the timestamp for this packet.
	 * @param timestamp timestamp to set for this packet.
	 */
	public void setTimestamp(int timestamp) {
//		timestamp &= 0xff;
		header[7] = (byte)timestamp;
		header[6] = (byte)(timestamp >> 8);
		header[5] = (byte)(timestamp >> 16);
		header[4] = (byte)(timestamp >> 24);
	}

	/**
	 * Gets the timestamp for this packet.
	 * @return timestamp for this packet.
	 */
	public long getTimestamp() {
		return ((long)
				((header[7] & 0xff)) | 
				((header[6] & 0xff) << 8) | 
				((header[5] & 0xff) << 16) | 
				((header[4] & 0xff) << 24)) & 0xffffffffL;
	}
	
	/**
	 * Sets the synchronization source identifier for this packet.
	 * @param syncSource synchronization source to set.
	 */
	public void setSyncSource(int syncSource) {
		header[8] = (byte)syncSource;
		header[9] = (byte)(syncSource << 8);
		header[10] = (byte)(syncSource << 16);
		header[11] = (byte)(syncSource << 24);		
	}
	
	/**
	 * Gets the synchronization source identifier for this packet.
	 * @return ths synchronization source for this packet.
	 */
	public long getSyncSource() {
		return ((long)
				((header[11] & 0xff)) | 
				((header[10] & 0xff) << 8) | 
				((header[9] & 0xff) << 16) | 
				((header[8] & 0xff) << 24)) & 0xffffffffL;		
	}
	
	/**
	 * Sets the payload for this packet.
	 * @param payload payload for this packet.
	 */
	public void setPayload(byte[] payload) {
		this.payload = payload;
	}
	
	/**
	 * Gets the payload for this packet.
	 * @return payload for this packet.
	 */
	public byte[] getPayload() {
		return payload;
	}
	
	/**
	 * Directly sets header byte array.
	 * @param header header to assign to this packet.
	 * TODO implement as offset/len pair for copying? 
	 */
	public void setHeader(byte[] header) { 
		this.header = header;
	}
	
	/**
	 * Returns the header array for this packet.
	 * @return header for this packet.
	 */
	public byte[] getHeader() { 
		return header;
	}

	/**
	 * @param address2
	 */
	public void setAddress(InetSocketAddress address) {
		this.address = address;
	}
	
	public InetSocketAddress getAddress() { 
		return this.address;
	}
	
	//reception and processing timing information
	private long recvTime,processTime,decodeTime;


	public long getDecodeTime() {
		return decodeTime;
	}

	public void setDecodeTime(long decodeTime) {
		this.decodeTime = decodeTime;
	}

	public long getProcessTime() {
		return processTime;
	}

	public void setProcessTime(long processTime) {
		this.processTime = processTime;
	}

	public long getRecvTime() {
		return recvTime;
	}

	public void setRecvTime(long recvTime) {
		this.recvTime = recvTime;
	}

	//transmit timing information
	private long queuedTime;
	
	public long getQueuedTime() {
		return queuedTime;
	}

	public void setQueuedTime(long queuedTime) {
		this.queuedTime = queuedTime;
	}	
	
	public void printTiming() {
		System.out.println(
				"recv->process = " + ((processTime-recvTime)/1000000) + "ms, " + 
				"process->decode = " + ((decodeTime-processTime)/1000000) + "ms"
					);
	}

	public IRtpMediaProcessor getProcessor() {
		return processor;
	}

	public void setProcessor(IRtpMediaProcessor processor) {
		this.processor = processor;
	}

	public InetSocketAddress getSourceAddress() {
		return sourceAddress;
	}

	public void setSourceAddress(InetSocketAddress sourceAddress) {
		this.sourceAddress = sourceAddress;
	}
}
