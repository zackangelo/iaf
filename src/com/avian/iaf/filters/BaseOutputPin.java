/*
 * Created on Mar 3, 2005
 *
 */
package com.avian.iaf.filters;

/**
 * @author zangelo
 *
 */
public abstract class BaseOutputPin extends BasePin implements IOutputPin {

	IInputPin destInput;
	
	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.IPin#connect(com.vogistix.iaf.filters.IPin)
	 */
	public void connect(IPin dest) throws InvalidGraphOperationException {
		if(dest instanceof IInputPin) {
			synchronized(dest) { 
				super.connect(dest);
				destInput = (IInputPin) dest;
			}
		} else { 
			throw new InvalidGraphOperationException(IPin.INVALID_PIN_CONNECTION_MESSAGE);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.IPin#type()
	 */
	public int direction() {
		return IPin.OUTPUT;
	}

}
