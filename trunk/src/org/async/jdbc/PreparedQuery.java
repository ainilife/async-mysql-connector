package org.async.jdbc;

import java.sql.SQLException;

public interface PreparedQuery {

	void query(PreparedStatement pstmt) throws SQLException;

}
