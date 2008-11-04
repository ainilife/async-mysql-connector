package org.async.mysql.facade;

import java.sql.SQLException;

public interface PreparedQuery {

	void query(PreparedStatement pstmt) throws SQLException;

}
