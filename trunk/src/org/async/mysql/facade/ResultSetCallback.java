package org.async.mysql.facade;

public interface ResultSetCallback extends Callback{
	void onResultSet(ResultSet rs);
}
