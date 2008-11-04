package org.async.mysql.facade;

import java.sql.SQLException;

public interface InnerConnection {
	void query(String query) throws SQLException;

	void query(Query query,Callback callback) throws SQLException;

	void execute(int statementId,int[] types,Object[] data) throws SQLException;

	void prepare(String sql) throws SQLException;

	void close(int statementId) throws SQLException;
}
