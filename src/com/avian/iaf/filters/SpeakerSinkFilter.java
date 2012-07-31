/*
 * Created on Mar 2, 2005
 *
 */
package com.avian.iaf.filters;

import java.nio.ByteBuffer;
import java.util.Properties;

import javax.sound.sampled.*;

import com.avian.iaf.util.BlockingCircularBuffer;

/**
 * @author zangelo
 *
 * Plays input audio through the default JS SourceDataLine,  
 * accepts 16-bit, 8Khz, PCM, signed, big-endian
 */
public class SpeakerSinkFilter extends BaseSinkFilter {
	private static class SpeakerPlaybackThread extends Thread {
		static AudioFormat audioFmt = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
				8000.0f,		//sampling rate
				16,			//bits per sample
				1,			//channels
				2,			//frame size
				8000.0f,	//frame rate
				true);		//endianness 
		
		SourceDataLine speakerOut;
		int frameSize;
		BlockingCircularBuffer audioData;
		byte[] outputBuffer;
		boolean running;
		
		public SpeakerPlaybackThread(BlockingCircularBuffer audioData, int frameSize) {
			super("SpeakerPlayback");
			try {
				outputBuffer = new byte[frameSize];
				this.audioData = audioData;
				
				speakerOut = (SourceDataLine) AudioSystem.getSourceDataLine(audioFmt);
				speakerOut.open(audioFmt); 
				speakerOut.start();
			} catch (LineUnavailableException e) {
				e.printStackTrace();
			}
		}
		
		public void run() { 
			running = true;
			while(running) { 
				int bytesRead = audioData.read(outputBuffer);	//blocks until data is put in
				if(bytesRead > 0) {
					speakerOut.write(outputBuffer, 0, bytesRead);
				}
			}
			
//			speakerOut.flush();
		}
		
		public synchronized void drainSpeaker() { 
			//FIXME should be wary of flushing a started speaker, may cause a click
//			speakerOut.drain();
			speakerOut.flush();
		}
		
		
		public synchronized void shutdown() { 
			running = false;
		}
	}
	
	public IInputPin inPin;
	byte[] tempBuffer;
	BlockingCircularBuffer circBuf;
	SpeakerPlaybackThread speakerThread;
	
	public SpeakerSinkFilter() { 
		inPin = new MemoryInputPin(this);
		tempBuffer = new byte[1024];
		circBuf = new BlockingCircularBuffer(16384);
	
		speakerThread = new SpeakerPlaybackThread(circBuf,640);
		speakerThread.start();
	}
	
	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.ISink#render(byte[], int, int)
	 */
	public void render(byte[] in, int ofs, int len) {
		
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
	public void shutdown() {
		speakerThread.shutdown();
	}
	
	public void drain() { 
		speakerThread.drainSpeaker();
	}
	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.IFilter#processInput(java.nio.ByteBuffer, int, int)
	 */
	public IOutputPin processInput(ByteBuffer buf,ByteBuffer dest) throws UnsupportedOperationException {
		buf.reset();
//		int rem = buf.remaining();
//		if(rem > 0) buf.get(outputBuffer,0,buf.remaining());
//		render(outputBuffer,0,rem);

		//FIXME: need to make the circular buffer support nio-style buffers.
		int rem = buf.remaining();
		if(rem > 0) buf.get(tempBuffer,0,rem);
		circBuf.write(tempBuffer,0,rem);
		
		//FIXME create a "terminator" pin instead of releasing here and returning null
		MemoryBufferManager.releaseBuffer(buf);
		return null;
	}

}
