package org.async.jdbc;

import java.sql.SQLException;

public interface AsyncConnection {
	Statement createStatement() throws SQLException;

	PreparedStatement prepareStatement(String sql) throws SQLException;

	void close() throws SQLException;
	
	int load();
}
