/*
 * Created on Mar 2, 2005
 *
 */
package com.avian.iaf.filters;

import java.util.Properties;

import javax.sound.sampled.*;

import java.io.*;
import java.nio.ByteBuffer;

/**
 * @author zangelo
 *
 */
public class MuLawWavFileSourceFilter extends BaseSourceFilter {
//	static AudioFormat audioFmt = new AudioFormat(AudioFormat.Encoding.ULAW,
//			8000.0f,		//sampling rate
//			8,			//bits per sample
//			1,			//channels
//			1,			//frame size
//			8000.0f,	//frame rate
//			false);		//endianness 

	static AudioFormat audioFmt = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
			8000.0f,		//sampling rate
			16,			//bits per sample
			1,			//channels
			1,			//frame size
			8000.0f,	//frame rate
			true);		//endianness 

	AudioInputStream wavFile;
	String wavFileName;
	int chunkSize;
	byte[] chunk;
	
	byte[] fileBuffer = new byte[102400];
	int fileSize;
	int bufofs;
	
	public MuLawWavFileSourceFilter() {
		outPin = new MemoryOutputPin(this);
	}
	
	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.ISource#generate(byte[], int)
	 */
	public int generate() {
//		try {
			ByteBuffer ulawData = MemoryBufferManager.requestBuffer();
//			int bytesRead = wavFile.read(chunk,0,chunkSize);
//			
//			if(bytesRead > 0) {
//				ulawData.put(chunk);
//				outPin.push(ulawData,0,bytesRead);
//			}

			int bytesRead;
			
//			ulawData.mark();
			
			if((bufofs + chunkSize) >= fileSize) {
				ulawData.put(fileBuffer,bufofs,fileSize-bufofs);
				bufofs = 0;
			} else { 
				ulawData.put(fileBuffer,bufofs,chunkSize);
				bufofs += chunkSize;
			}
			
			bytesRead = ulawData.position();
			ulawData.limit(bytesRead);
			
			outPin.push(ulawData);
			
			return bytesRead;
//		} catch (IOException e) {
//			e.printStackTrace();
//			return 0;
//		}
	}

	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.IFilter#initialize(java.util.Properties)
	 */
	public boolean initialize(Properties p) {
		wavFileName = p.getProperty("filename");
		chunkSize = Integer.parseInt(p.getProperty("chunkSize"));
		chunk = new byte[chunkSize];
		
		try {
			wavFile = AudioSystem.getAudioInputStream(new File(wavFileName));
			fileSize = wavFile.read(fileBuffer);
			bufofs = 0;
			return true;
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	public IOutputPin outPin;
	
	/* (non-Javadoc)
	 * @see com.vogistix.iaf.filters.IFilter#shutdown()
	 */
	public void shutdown() {
	}

	public void flush() {
		this.outPin.flush();
	}

}
