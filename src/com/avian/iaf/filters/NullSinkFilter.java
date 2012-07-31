/*
 * Created on Apr 3, 2005
 *
 */
package com.avian.iaf.filters;

import java.nio.ByteBuffer;
import java.util.Properties;

/**
 * @author Zack
 *
 *	A media sink that does nothing.
 */
public class NullSinkFilter extends BaseSinkFilter {

	public IInputPin inPin;
	
	public NullSinkFilter() { 
		inPin = new MemoryInputPin(this);
	}
	 
	/* (non-Javadoc)
	 * @see com.avian.iaf.filters.IFilter#initialize(java.util.Properties)
	 */
	public boolean initialize(Properties p) {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.avian.iaf.filters.IFilter#processInput(java.nio.ByteBuffer, java.nio.ByteBuffer)
	 */
	public IOutputPin processInput(ByteBuffer src, ByteBuffer dest)
			throws UnsupportedOperationException {
		return null;
	}

	/* (non-Javadoc)
	 * @see com.avian.iaf.filters.IFilter#shutdown()
	 */
	public void shutdown() {}

}
