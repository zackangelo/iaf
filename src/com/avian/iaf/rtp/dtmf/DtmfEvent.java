package com.avian.iaf.rtp.dtmf;

public class DtmfEvent {
	private int digit;
	private boolean end;
	private int volume;
	private int duration;
	
	public int getDigit() {
		return digit;
	}
	public void setDigit(int digit) {
		this.digit = digit;
	}
	public int getDuration() {
		return duration;
	}
	public void setDuration(int duration) {
		this.duration = duration;
	}
	public boolean isEnd() {
		return end;
	}
	public void setEnd(boolean end) {
		this.end = end;
	}
	public int getVolume() {
		return volume;
	}
	public void setVolume(int volume) {
		this.volume = volume;
	}
	
	public String toString() { 
		return "digit="+digit+",duration="+duration+",end="+end+",volume="+volume;
	}
}
