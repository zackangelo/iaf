/*
 * Created on Mar 3, 2005
 *
 */
package com.avian.iaf.filters;

/**
 * @author zangelo
 *
 */
public class InvalidGraphOperationException extends Exception {
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	public InvalidGraphOperationException(String message) { 
		super(message);
	}
}
