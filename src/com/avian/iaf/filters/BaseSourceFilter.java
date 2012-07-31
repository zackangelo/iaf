/*
 * Created on Mar 2, 2005
 *
 */
package com.avian.iaf.filters;

import java.nio.ByteBuffer;


/**
 * @author zangelo
 *
 */
public abstract class BaseSourceFilter implements ISourceFilter {
	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.IFilter#getType()
	 */
	public int getType() {
		return IFilter.SOURCE;
	}
	
	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.IFilter#processInput(java.nio.ByteBuffer, int, int)
	 */
	public IOutputPin processInput(ByteBuffer src,ByteBuffer dest) throws UnsupportedOperationException {
		throw new UnsupportedOperationException("Sources can't process input");
	}
}
