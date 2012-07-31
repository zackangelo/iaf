/*
 * Created on Mar 3, 2005
 *
 */
package com.avian.iaf.filters;

import java.nio.ByteBuffer;

/**
 * @author zangelo
 *
 */
public interface IOutputPin extends IPin {
	/**
	 * Pushes data from this output pin into the connected input pin.
	 * 
	 * The specified buffer must have it's mark at the beginning of
	 * data, and it's limit at the end.
	 * 
	 * @param buf Buffer containing the data
	 */
	//public void push(ByteBuffer buf,int ofs,int len);
	public void push(ByteBuffer buf);
}
