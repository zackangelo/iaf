package com.avian.iaf.filters;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.util.Properties;

public class ShortToDoubleConverterFilter extends BaseTransformFilter {

	public IInputPin inPin;
	public IOutputPin outPin;
	
	public ShortToDoubleConverterFilter() {
		super();
		
		inPin 	= new MemoryInputPin(this);
		outPin 	= new MemoryOutputPin(this);
	}

	public boolean initialize(Properties p) {
		return true;
	}

	public IOutputPin processInput(ByteBuffer src, ByteBuffer dest)
			throws UnsupportedOperationException {
		
		src.reset(); dest.reset();
		
		DoubleBuffer destAsDouble = dest.asDoubleBuffer();

		int len = src.limit();
		
		while(src.hasRemaining()) { 
			destAsDouble.put((double)src.getShort());
		}
		
		destAsDouble.limit(destAsDouble.position());
		
		int doublelen = destAsDouble.position();
		//convert limit back to bytes
		
		dest.limit(destAsDouble.position()*8);
		
		return outPin;
	}

	public void shutdown() {
		// TODO Auto-generated method stub
	}

}
