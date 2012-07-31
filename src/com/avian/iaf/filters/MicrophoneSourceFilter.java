package com.avian.iaf.filters;

import java.nio.ByteBuffer;
import java.util.Properties;
import javax.sound.sampled.*;

public class MicrophoneSourceFilter extends BaseSourceFilter {
	public static class MicrophoneThread extends Thread { 
		private final static int GRAPH_FRAME_SIZE = 320; //we want 160 byte packets; we'll get just that with 320 and ulaw companding
		private final static int BUFFER_SIZE = 1024;
		private final static AudioFormat audioFmt = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
				8000.0f,		//sampling rate
				16,			//bits per sample
				1,			//channels
				2,			//frame size
				8000.0f,	//frame rate
				true);		//endianness 
		
		TargetDataLine micIn;
		boolean running;
		byte[] buffer;
		IOutputPin outPin;
		int bufOfs;
		
		public MicrophoneThread(IOutputPin pin) { 
			super("Microphone");
			
			try {
				buffer = new byte[BUFFER_SIZE];
				micIn = (TargetDataLine) AudioSystem.getTargetDataLine(audioFmt);
				outPin = pin;
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
		}
		
		public void run() { 
			try {
				micIn.open(audioFmt);
			} catch (LineUnavailableException e) {
				e.printStackTrace();
				return;
			}
			
			micIn.start();
			running = true;
			while(running) { 
				
				int bytesRead = micIn.read(buffer,bufOfs,GRAPH_FRAME_SIZE-bufOfs);
				
				ByteBuffer b = MemoryBufferManager.requestBuffer();
				b.put(buffer,0,bytesRead);
				b.position(0);
				b.limit(bytesRead);
				//then send it down the graph
				outPin.push(b);
			}
		}
	}
	
	
	public IOutputPin outPin;
	private MicrophoneThread micThread;
	
	public MicrophoneSourceFilter() { 
		outPin = new MemoryOutputPin(this);
	}
	
	public int generate() {	return 0; }

	public void flush() {}

	public boolean initialize(Properties p) {
		micThread = new MicrophoneThread(outPin);
		micThread.start();
		return true;
	}

	public void shutdown() {	}

}
