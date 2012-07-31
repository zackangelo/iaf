/*
 * Created on Mar 3, 2005
 *
 */
package com.avian.iaf.filters;

import java.nio.ByteBuffer;

/**
 * @author Zack Angelo
 *
 */
public class MemoryInputPin extends BaseInputPin {

	public MemoryInputPin(IFilter parent) { 
		setFilter(parent);
	}
	
	IOutputPin target;
	ByteBuffer targetData;
	
	public void flush() {
		System.out.println("flush() for " + getFilter().getClass());
		if(target != null) {
			if(targetData.limit() > 0) target.push(targetData);
			target.flush();
		}
	}
	
	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.IInputPin#take(byte[], int, int)
	 */
	public void take(ByteBuffer inputData) {
		/*
		 * 		outPin.push(out,0,out.position());
		out = MemoryBufferManager.requestBuffer(); 
		decode(in,ofs,len,out,0);
		MemoryBufferManager.releaseBuffer(in);
		return outPin;
		 */
		
		
		//if non-null, pass the last data processed by this filter 
		//	on to the next
		if(target != null) {
			target.push(targetData);
		}
		
		if(getFilter().getType() == IFilter.SINK) {
			getFilter().processInput(inputData,null);
		} else if(getFilter().getType() == IFilter.TRANSFORM) {
			//after targetData has been pushed down, it will most likely be
			// 	deallocated by the next filter, it is no longer valid.
			//	we must allocate a new buffer
			targetData = MemoryBufferManager.requestBuffer();
			
			
			//process the new incoming data
			target = getFilter().processInput(inputData,targetData);
			

		} 
		
		//release our input buffer
		MemoryBufferManager.releaseBuffer(inputData);
	}
}
