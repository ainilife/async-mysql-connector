package org.async.jdbc;

import java.sql.SQLException;

public interface Connection {
	void query(String query) throws SQLException;

	void update(String query) throws SQLException;


}
