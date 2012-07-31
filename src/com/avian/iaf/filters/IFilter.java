/*
 * Created on Mar 2, 2005
 *
 */
package com.avian.iaf.filters;

import java.util.Properties;
import java.nio.ByteBuffer;

/**
 * @author Zack Angelo
 *
 * <p>Represents a filter interface that all filters in a filter graph must implement. </p>
 *
 */
public interface IFilter {
	public final static int SOURCE = 0;
	public final static int SINK = 1;
	public final static int TRANSFORM = 2;
	public boolean initialize(Properties p);
	public IOutputPin processInput(ByteBuffer src,ByteBuffer dest) throws UnsupportedOperationException;
	public void shutdown();
	public int getType();
}
