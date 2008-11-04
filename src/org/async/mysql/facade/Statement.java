package org.async.mysql.facade;

import java.sql.SQLException;

public interface Statement {
	void executeUpdate(String sql, SuccessCallback callback) throws SQLException;

	void executeQuery(String sql, ResultSetCallback callback) throws SQLException;
}
