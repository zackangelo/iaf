/*
 * Created on Mar 2, 2005
 *
 */
package com.avian.iaf.filters;

import java.nio.ByteBuffer;
import java.util.Properties;

/**
 * @author zangelo
 *
 */
public class PcmToMuLawEncoderFilter extends BaseTransformFilter {
	public PcmToMuLawEncoderFilter() { 
		inPin = new MemoryInputPin(this);
		outPin = new MemoryOutputPin(this);
	}
	
	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.IFilter#initialize(java.util.Properties)
	 */
	public boolean initialize(Properties p) {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.IFilter#shutdown()
	 */
	public void shutdown() { }

	private static final boolean ZEROTRAP=true;
	private static final short BIAS=0x84;
	private static final int CLIP=32635;
	private static final int exp_lut1[] ={
	    0,0,1,1,2,2,2,2,3,3,3,3,3,3,3,3,
	    4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,4,
	    5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,
	    5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,5,
	    6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
	    6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
	    6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
	    6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,6,
	    7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
	    7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
	    7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
	    7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
	    7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
	    7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
	    7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,
	    7,7,7,7,7,7,7,7,7,7,7,7,7,7,7,7
	};

	/**
	 * Converts a linear signed 16bit sample to a uLaw byte.
	 * Ported to Java by fb.
	 * <BR>Originally by:<BR>
	 * Craig Reese: IDA/Supercomputing Research Center <BR>
	 * Joe Campbell: Department of Defense <BR>
	 * 29 September 1989 <BR>
	 */
	public static byte encode(int sample) {
		int sign, exponent, mantissa, ulawbyte;

		if (sample>32767) sample=32767;
		else if (sample<-32768) sample=-32768;
		/* Get the sample into sign-magnitude. */
		sign = (sample >> 8) & 0x80;    /* set aside the sign */
		if (sign != 0) sample = -sample;    /* get magnitude */
		if (sample > CLIP) sample = CLIP;    /* clip the magnitude */

		/* Convert from 16 bit linear to ulaw. */
		sample = sample + BIAS;
		exponent = exp_lut1[(sample >> 7) & 0xFF];
		mantissa = (sample >> (exponent + 3)) & 0x0F;
		ulawbyte = ~(sign | (exponent << 4) | mantissa);
		if (ZEROTRAP)
			if (ulawbyte == 0) ulawbyte = 0x02;  /* optional CCITT trap */
		return((byte) ulawbyte);
	}

	public IInputPin inPin;
	public IOutputPin outPin;
	
	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.IFilter#processInput(java.nio.ByteBuffer, java.nio.ByteBuffer)
	 */
	public IOutputPin processInput(ByteBuffer src, ByteBuffer dest) throws UnsupportedOperationException {
		src.reset();
		dest.reset();
		
		while(src.hasRemaining()) {
			dest.put(encode(src.getShort()));
		}

		//be sure to set the limit of this buffer so the next filter knows
		//	where to read up to.
		dest.limit(dest.position());
		
//		System.out.println("output size: " + dest.position() + " bytes");
		return outPin;
	}

	
}
