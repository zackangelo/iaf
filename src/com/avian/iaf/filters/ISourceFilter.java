/*
 * Created on Mar 2, 2005
 *
 */
package com.avian.iaf.filters;

/**
 * @author zangelo
 *
 */
public interface ISourceFilter extends IFilter {
	public int generate();
	public void flush();
}
