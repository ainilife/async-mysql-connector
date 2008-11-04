package org.async.mysql.facade;

import java.sql.SQLException;

public interface AsyncConnection {
	Statement createStatement();

	PreparedStatement prepareStatement(String sql) throws SQLException;
}
