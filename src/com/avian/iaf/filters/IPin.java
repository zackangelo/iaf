/*
 * Created on Mar 3, 2005
 *
 */
package com.avian.iaf.filters;

/**
 * @author zangelo
 *
 */
public interface IPin {
	public static final int OUTPUT = 0;
	public static final int INPUT = 1;
	public static final String INVALID_PIN_CONNECTION_MESSAGE = "Invalid pin connection";
	
	/**
	 * Sets the parent filter
	 * 
	 * @param filter Parent filter
	 */
	public void setFilter(IFilter filter);
	
	/**
	 * Returns the filter that this pin belongs to.
	 * 
	 * @return the filter that this pin belongs to.
	 */
	public IFilter getFilter();
	
	
	/**
	 * Connects this pin to a pin in another filter.
	 * 
	 * (should this by synchronized?)
	 * @param dest Pin to connect to.
	 * @throws InvalidGraphOperationException 
	 */
	public void connect(IPin dest) throws InvalidGraphOperationException;
	
	/**
	 * Returns the pin that this pin is connected to.
	 * 
	 * @return the pin that this pin is connected to.
	 */
	public IPin connectedTo();
	
	/**
	 * Returns the type of pin.
	 * 
	 * @return the type of pin (input/output)
	 */
	public int direction();	
	
	public void flush();
}
