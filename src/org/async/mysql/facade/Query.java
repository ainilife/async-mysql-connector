package org.async.mysql.facade;

import java.sql.SQLException;


public interface Query {
	void query(InnerConnection connection) throws SQLException;
}
