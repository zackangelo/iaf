package com.avian.iaf.rtp;

import java.util.*;
import java.util.concurrent.*;

public class ConcurrentMapProcessorTable implements IRtpProcessorTable {
	Map<Long,IRtpMediaProcessor> map;
	
	public ConcurrentMapProcessorTable() {
		map = new ConcurrentHashMap<Long,IRtpMediaProcessor>();
	}
	
	public IRtpMediaProcessor getProcessorBySyncSource(long syncSource) {
		return map.get(syncSource);
	}

}
