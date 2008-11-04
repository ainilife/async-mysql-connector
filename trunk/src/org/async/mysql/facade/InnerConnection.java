package org.async.mysql.facade;

import java.sql.SQLException;

public interface InnerConnection {
	void query(String query) throws SQLException;

	void update(String query) throws SQLException;

	void query(Query query,Callback callback) throws SQLException;

	void query(SilentQuery query) throws SQLException;

	void executeQuery(int statementId,int[] types,Object[] data) throws SQLException;

	void executeUpdate(int statementId,int[] types,Object[] data) throws SQLException;

	void prepare(String sql) throws SQLException;

	void close(int statementId) throws SQLException;
}
