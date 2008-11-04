package org.async.jdbc;

public interface HasState {
	void nextState();

	boolean isOver();
}
