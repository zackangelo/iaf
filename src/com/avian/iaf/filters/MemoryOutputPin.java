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
public class MemoryOutputPin extends BaseOutputPin {
	public MemoryOutputPin(IFilter parent) { 
		setFilter(parent);
	}
	
	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.IOutputPin#push(java.nio.ByteBuffer, int, int)
	 */
	public void push(ByteBuffer buf) {
		this.destInput.take(buf);
	}
	
	public void flush() { 
		connectedPin.flush();
	}

}
