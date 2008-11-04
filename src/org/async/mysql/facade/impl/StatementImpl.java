package org.async.mysql.facade.impl;

import java.sql.SQLException;

import org.async.mysql.facade.InnerConnection;
import org.async.mysql.facade.Query;
import org.async.mysql.facade.ResultSetCallback;
import org.async.mysql.facade.Statement;
import org.async.mysql.facade.SuccessCallback;

public class StatementImpl implements Statement {
	private InnerConnection connection;

	public StatementImpl(InnerConnection connection) {
		super();
		this.connection = connection;
	}

	public void executeQuery(final String sql, ResultSetCallback callback)
			throws SQLException {
		connection.query(new Query() {

			public void query(InnerConnection connection) throws SQLException {
				connection.query(sql);

			}

		}, callback);

	}

	public void executeUpdate(final String sql, SuccessCallback callback)
			throws SQLException {
		connection.query(new Query() {

			public void query(InnerConnection connection) throws SQLException {
				connection.update(sql);

			}

		}, callback);

	}

}
