package org.async.mysql.facade;

public interface HasState {
	void nextState();

	boolean isOver();
}
