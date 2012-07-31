

/*
 * Created on Mar 13, 2005
 *
 */

import java.util.Properties;

import com.avian.iaf.filters.InvalidGraphOperationException;
import com.avian.iaf.filters.MemoryBufferManager;
import com.avian.iaf.filters.MuLawWavFileSourceFilter;
import com.avian.iaf.filters.PcmToMuLawEncoderFilter;
import com.avian.iaf.filters.RtpTransmitterSinkFilter;

/**
 * @author Zack
 *
 */
public class TestFilterGraph4 {
	public long clock() { 
		sun.misc.Perf perf = sun.misc.Perf.getPerf();
		long ticksPerSecond = perf.highResFrequency();
		long currTick = perf.highResCounter();
		return ((currTick * 1000) / ticksPerSecond);
	}
	public void doit() { 
		MuLawWavFileSourceFilter wav1 = new MuLawWavFileSourceFilter();
		PcmToMuLawEncoderFilter encoder = new PcmToMuLawEncoderFilter();
		RtpTransmitterSinkFilter rtp = new RtpTransmitterSinkFilter();
		
		MemoryBufferManager m = new MemoryBufferManager();
	
		try {
			wav1.outPin.connect(encoder.inPin);
			encoder.outPin.connect(rtp.inPin);
		} catch (InvalidGraphOperationException e) {
			e.printStackTrace();
		}

		Properties p = new Properties();
		p.put("filename","res/xmit_16bit_8khz.wav");
		p.put("chunkSize","1024");
		
		wav1.initialize(p);
		encoder.initialize(null);
		
		Properties rtpProps = new Properties();
		rtpProps.put("address","127.0.0.1");
		rtpProps.put("port","9004");
		rtp.initialize(rtpProps);
		
		int b;
		long c;
		while(true) {
			c = clock();
			b = wav1.generate();
			System.out.println("Graph latency = " + (clock()-c) + "ms");
			if(b < 1) {
				break;
			}
			
			try {
				Thread.sleep(60);
			} catch (InterruptedException e1) {	}
		}
	}		
}
