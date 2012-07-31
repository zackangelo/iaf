/*
 * Created on Mar 3, 2005
 *
 */
package com.avian.iaf.filters;

/**
 * @author zangelo
 *
 */
public abstract class BaseInputPin extends BasePin implements IInputPin {

	IOutputPin srcInput;
	
	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.IPin#connect(com.vogistix.iaf.filters.IPin)
	 */
	public void connect(IPin src) throws InvalidGraphOperationException {
		if(src instanceof IOutputPin) {
			synchronized(src) { 
				super.connect(src);
				srcInput = (IOutputPin) src;
			}
		} else { 
			throw new InvalidGraphOperationException(IPin.INVALID_PIN_CONNECTION_MESSAGE);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.IPin#type()
	 */
	public int direction() {
		return IPin.INPUT;
	}

}
