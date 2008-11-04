package org.async.jdbc;

public interface ResultSetCallback extends Callback{
	void onResultSet(ResultSet rs);
}
