/*
 * Created on Mar 2, 2005
 *
 */
package com.avian.iaf.filters;

import java.nio.ByteBuffer;
import java.util.Properties;

import org.apache.log4j.Logger;

/**
 * @author Zack Angelo
 * 
 * 16-bit (big-endian) 8Khz PCM to 8-bit uLaw converter. 
 *
 */
public class MuLawToPcmDecoderFilter extends BaseTransformFilter {
	Logger logger;
	
	public MuLawToPcmDecoderFilter() { 
		inPin = new MemoryInputPin(this);
		outPin = new MemoryOutputPin(this);
		logger = Logger.getLogger(this.getClass());
		
	}
	
	/* u-law to linear conversion table */
	public static short[] u2l = {
	    -32124, -31100, -30076, -29052, -28028, -27004, -25980, -24956,
	    -23932, -22908, -21884, -20860, -19836, -18812, -17788, -16764,
	    -15996, -15484, -14972, -14460, -13948, -13436, -12924, -12412,
	    -11900, -11388, -10876, -10364, -9852, -9340, -8828, -8316,
	    -7932, -7676, -7420, -7164, -6908, -6652, -6396, -6140,
	    -5884, -5628, -5372, -5116, -4860, -4604, -4348, -4092,
	    -3900, -3772, -3644, -3516, -3388, -3260, -3132, -3004,
	    -2876, -2748, -2620, -2492, -2364, -2236, -2108, -1980,
	    -1884, -1820, -1756, -1692, -1628, -1564, -1500, -1436,
	    -1372, -1308, -1244, -1180, -1116, -1052, -988, -924,
	    -876, -844, -812, -780, -748, -716, -684, -652,
	    -620, -588, -556, -524, -492, -460, -428, -396,
	    -372, -356, -340, -324, -308, -292, -276, -260,
	    -244, -228, -212, -196, -180, -164, -148, -132,
	    -120, -112, -104, -96, -88, -80, -72, -64,
	    -56, -48, -40, -32, -24, -16, -8, 0,
	    32124, 31100, 30076, 29052, 28028, 27004, 25980, 24956,
	    23932, 22908, 21884, 20860, 19836, 18812, 17788, 16764,
	    15996, 15484, 14972, 14460, 13948, 13436, 12924, 12412,
	    11900, 11388, 10876, 10364, 9852, 9340, 8828, 8316,
	    7932, 7676, 7420, 7164, 6908, 6652, 6396, 6140,
	    5884, 5628, 5372, 5116, 4860, 4604, 4348, 4092,
	    3900, 3772, 3644, 3516, 3388, 3260, 3132, 3004,
	    2876, 2748, 2620, 2492, 2364, 2236, 2108, 1980,
	    1884, 1820, 1756, 1692, 1628, 1564, 1500, 1436,
	    1372, 1308, 1244, 1180, 1116, 1052, 988, 924,
	    876, 844, 812, 780, 748, 716, 684, 652,
	    620, 588, 556, 524, 492, 460, 428, 396,
	    372, 356, 340, 324, 308, 292, 276, 260,
	    244, 228, 212, 196, 180, 164, 148, 132,
	    120, 112, 104, 96, 88, 80, 72, 64,
	    56, 48, 40, 32, 24, 16, 8, 0
	};
	
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
	
	private void decode(ByteBuffer in,ByteBuffer out) {
		in.reset();
		out.reset();
		
		
		while(in.hasRemaining()) {
			out.putShort(u2l[in.get() & 0xff]);
		}

		//be sure to set the limit of this buffer so the filter knows
		//	where to read up to.
		out.limit(out.position());
	}

	public IInputPin inPin;
	public IOutputPin outPin;
	ByteBuffer out;

	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.IFilter#processInput(byte[], int, int)
	 */
	public IOutputPin processInput(ByteBuffer src, ByteBuffer dest) throws UnsupportedOperationException {
		decode(src,dest);
		return outPin;
	}
}
