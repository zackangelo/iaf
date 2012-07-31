/*
 * Created on Mar 2, 2005
 *
 */
package com.avian.iaf.filters;



/**
 * @author zangelo
 *
 */
public abstract class BaseSinkFilter implements ISinkFilter {
	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.IFilter#getType()
	 */
	public int getType() {
		return IFilter.SINK;
	}

}
