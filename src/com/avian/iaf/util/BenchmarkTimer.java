package com.avian.iaf.util;

/**
 * Simple utility class for doing quick benchmark timings during development.
 * 
 * @author zangelo
 *
 */
public class BenchmarkTimer {
	private long startTime;
	public void startTimer() { 
		if(startTime != 0) { 
			System.out.println("Starting timer while it is already started.");
		}
		
		startTime = System.nanoTime();

	}
	
	public long stopTimer() { 
		long diff = System.nanoTime()-startTime;
		startTime = 0;
		return diff;
	}
}
