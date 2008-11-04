package org.async.jdbc;

import java.sql.SQLException;


public interface Query {
	void query(Connection connection) throws SQLException;
}
