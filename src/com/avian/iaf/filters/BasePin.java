/*
 * Created on Mar 3, 2005
 *
 */
package com.avian.iaf.filters;

/**
 * @author zangelo
 *
 */
public abstract class BasePin implements IPin {

	IFilter parentFilter;
	IPin 	connectedPin;
	
	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.IPin#setFilter(com.vogistix.iaf.filters.IFilter)
	 */
	public void setFilter(IFilter filter) {
		parentFilter = filter;
	}

	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.IPin#getFilter()
	 */
	public IFilter getFilter() {
		return parentFilter;
	}

	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.IPin#connect(com.vogistix.iaf.filters.IPin)
	 */
	public void connect(IPin dest) throws InvalidGraphOperationException {
		connectedPin = dest;
	}

	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.IPin#connectedTo()
	 */
	public IPin connectedTo() {
		return connectedPin;
	}
}
