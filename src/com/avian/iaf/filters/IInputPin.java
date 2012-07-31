/*
 * Created on Mar 3, 2005
 *
 */
package com.avian.iaf.filters;

import java.nio.ByteBuffer;

/**
 * @author Zack Angelo
 *
 */
public interface IInputPin extends IPin {
	/**
	 * Called by an output pin; tells this filter to process the data in the 
	 * specified buffer.
	 * 
	 * @param buf Buffer containing data to be processed by the parent filter
	 * @param ofs Offset within the buffer
	 * @param len Length of the data (in bytes)
	 */
	public void take(ByteBuffer buf);
	
}
