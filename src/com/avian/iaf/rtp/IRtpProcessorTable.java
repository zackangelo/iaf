package com.avian.iaf.rtp;

public interface IRtpProcessorTable {
	public IRtpMediaProcessor getProcessorBySyncSource(long syncSource);
}
