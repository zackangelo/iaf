package com.avian.iaf.filters;

import java.util.Properties;
import java.nio.*;
/**
 * Outputs 8Khz 16-bit signed PCM tones.
 * 
 * @author zangelo
 *
 */
public class ToneGeneratorFilter extends BaseSourceFilter {
	private static class ToneThread extends Thread { 
		private final static double SAMPLE_RATE = 8000.0d;
		private IOutputPin pin;
		private boolean isRunning;
		private short[] frameBuffer;
		private int[] freq;
		
		public ToneThread(IOutputPin pin, int frameSize, int[] freq) { 
			super("ToneThread");
			frameBuffer = new short[frameSize/2];
			
			this.pin = pin;
			this.freq = freq.clone();
		}
		
		public void run() { 
			isRunning = true;
			
			double t = 0.0d;
			
			while(isRunning) { 
				for(int i=0;i<frameBuffer.length;i++) { 	
					double samp = 0.0d;
					for(int f=0;f<freq.length;f++) { 
						double fc = Math.sin(2 * Math.PI * freq[f] * (t / SAMPLE_RATE));
						
						//scale sample to 16-bit max/min
						fc *= (Short.MAX_VALUE/freq.length);
						
						samp += fc;
					}
					
					frameBuffer[i] = (short)samp;
					
					//stay between 0 and 1 so we don't overflow
					if(t<SAMPLE_RATE) t++; 
					else t = 0.0d;
				}
				
				ByteBuffer graphBuf = MemoryBufferManager.requestBuffer();
				graphBuf.asShortBuffer().put(frameBuffer);
				graphBuf.limit(frameBuffer.length*2);
				graphBuf.position(0);
				
				pin.push(graphBuf);
				
				try {
					sleep(15);
				} catch (InterruptedException e) {
				}
			}
		}
		
		public void stopGenerating() { 
			interrupt();
			isRunning = false;
		}
	}

	private ToneThread genThread;
	private final static int FRAME_SIZE = 320;
	public IOutputPin outPin;
	
	public ToneGeneratorFilter() { 
		outPin = new MemoryOutputPin(this);
	}
	
	public void generateTone(int freq) { 
		genThread = new ToneThread(outPin,FRAME_SIZE,new int[] { freq });
		genThread.start();
	}
	
	public void generateMixedTone(int[] freqs) { 
		genThread = new ToneThread(outPin,FRAME_SIZE,freqs);
		genThread.start();
	}
	
	public void stop() { 
		if(genThread != null) {
			genThread.stopGenerating(); 
			flush();
		}
	}
	public int generate() {	return 0; }
	public void flush() { outPin.flush(); }
	public boolean initialize(Properties p) { return false; }
	public void shutdown() {	}
}
