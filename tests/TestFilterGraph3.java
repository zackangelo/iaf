/*
 * Created on Mar 12, 2005
 *
 */


import com.avian.iaf.filters.*;

import java.util.*;

/**
 * @author Zack
 *
 */
public class TestFilterGraph3 {
	public void doit() { 
		RtpReceiverSourceFilter rtpSource = new RtpReceiverSourceFilter();
		MuLawToPcmDecoderFilter mulawDecoder = new MuLawToPcmDecoderFilter();
		SpeakerSinkFilter sinkFilter = new SpeakerSinkFilter();		
	
		//wire 'em up!
		
		try {
			rtpSource.ulawPin.connect(mulawDecoder.inPin);
			mulawDecoder.outPin.connect(sinkFilter.inPin);
		} catch (InvalidGraphOperationException e) {
			e.printStackTrace();
			return;
		}
		
		//init
		
		Properties rtpProps = new Properties();
		rtpProps.put("address","127.0.0.1");
		rtpProps.put("port","9004");
		
		//source goes last, sink first
		sinkFilter.initialize(null);
		mulawDecoder.initialize(null);
		
		//let her rip (it's directly wired into the stack)
		rtpSource.initialize(rtpProps);
		
		while(true) { 
			try {
				Thread.sleep(Long.MAX_VALUE);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
	}
	
}
