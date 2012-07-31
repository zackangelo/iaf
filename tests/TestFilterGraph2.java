/*
 * Created on Mar 3, 2005
 *
 */

import java.util.Properties;

import com.avian.iaf.filters.*;

/**
 * @author zangelo
 *
 */
public class TestFilterGraph2  {
	public long clock() { 
		sun.misc.Perf perf = sun.misc.Perf.getPerf();
		long ticksPerSecond = perf.highResFrequency();
		long currTick = perf.highResCounter();
		return ((currTick * 1000) / ticksPerSecond);
	}
	public void doit() { 
		MuLawWavFileSourceFilter wav1 = new MuLawWavFileSourceFilter();
		MuLawToPcmDecoderFilter decoder = new MuLawToPcmDecoderFilter();
		SpeakerSinkFilter speaker = new SpeakerSinkFilter();
		
		MemoryBufferManager m = new MemoryBufferManager();
		
		Properties p = new Properties();
		p.put("filename","res/dingulaw.wav");
		p.put("chunkSize","64");
		
		wav1.initialize(p);
		decoder.initialize(null);
		speaker.initialize(null);
		
		try {
			wav1.outPin.connect(decoder.inPin);
			decoder.outPin.connect(speaker.inPin);
		} catch (InvalidGraphOperationException e) {
			e.printStackTrace();
		}
		
		int b;
		long c;
		while(true) {
			c = clock();
			b = wav1.generate();
			System.out.println("Graph latency = " + (clock()-c) + "ms");
			if(b < 1) {
				break;
			}
		}
	}
}
