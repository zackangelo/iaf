package com.avian.iaf.rtp.dtmf;

public interface IDtmfHandler {
	public final static byte DTMF_0 = 0;
	public final static byte DTMF_1 = 1;
	public final static byte DTMF_2 = 2;
	public final static byte DTMF_3 = 3;
	public final static byte DTMF_4 = 4;
	public final static byte DTMF_5 = 5;
	public final static byte DTMF_6 = 6;
	public final static byte DTMF_7 = 7;
	public final static byte DTMF_8 = 8;
	public final static byte DTMF_9 = 9;
	public final static byte DTMF_STAR = 10;
	public final static byte DTMF_POUND = 11;
	public final static byte DTMF_A = 12;
	public final static byte DTMF_B = 13;
	public final static byte DTMF_C = 14;
	public final static byte DTMF_D = 15;
	public final static byte DTMF_FLASH = 16;
	
	public void handleDtmf(DtmfEvent event);
}
