package org.async.mysql.facade.impl;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

import org.async.mysql.MysqlConnection;
import org.async.mysql.MysqlDefs;
import org.async.mysql.facade.Callback;
import org.async.mysql.facade.HasState;
import org.async.mysql.facade.InnerConnection;
import org.async.mysql.facade.PreparedQuery;
import org.async.mysql.facade.PreparedStatement;
import org.async.mysql.facade.Query;
import org.async.mysql.facade.ResultSetCallback;
import org.async.mysql.facade.SilentQuery;
import org.async.mysql.facade.SuccessCallback;
import org.async.mysql.in.packets.Field;
import org.async.mysql.in.packets.OK;

public class PreparedStatementImpl implements Query, PreparedStatement,
		HasState, SuccessCallback {
	public static int FIELDS = 0;
	public static int PARAMS = 1;
	public static int OVER = 2;
	private Field[] fields;
	private Field[] params;
	private int[] types;
	private Object[] data;
	private int statementId;
	private InnerConnection connection;
	private String sql;
	private int state = 0;
	private boolean closed;

	public PreparedStatementImpl(String sql, InnerConnection connection)
			throws SQLException {
		super();
		this.connection = connection;
		this.sql = sql;
		connection.query(this, this);

	}

	public void executeQuery(final PreparedQuery query,
			ResultSetCallback callback) throws SQLException {
		if (isClosed())
			throw new SQLException(
					" No operations allowed after statement closed.");
		executeInternal(query, callback);
	}

	@Override
	public void executeUpdate(PreparedQuery query, SuccessCallback callback)
			throws SQLException {
		if (isClosed())
			throw new SQLException(
					" No operations allowed after statement closed.");
		executeInternal(query, callback);
	}

	private boolean isClosed() {
		return closed;
	}

	public void close() throws SQLException {
		closed = true;
		SilentQuery query = new SilentQuery() {
			public void query(InnerConnection connection) throws SQLException {
				connection.close(statementId);
			}
		};
		connection.query(query);

	}

	private void executeInternal(final PreparedQuery query, Callback callback)
			throws SQLException {
		connection.query(new Query() {
			public void query(InnerConnection connection) throws SQLException {
				query.query(PreparedStatementImpl.this);
				if (fields.length == 0)
					connection.executeUpdate(statementId, types, data);
				else
					connection.executeQuery(statementId, types, data);

			}

		}, callback);
	}

	public void query(InnerConnection connection) throws SQLException {
		connection.prepare(sql);
		((MysqlConnection) connection).getParser().setMessage(this);
	}

	public void setByte(int idx, Byte b) {
		types[idx - 1] = MysqlDefs.FIELD_TYPE_TINY;
		data[idx - 1] = b;

	}

	public void setBytes(int idx, byte[] bytes) {
		types[idx - 1] = MysqlDefs.FIELD_TYPE_BLOB;
		data[idx - 1] = bytes;

	}

	public void setDate(int idx, Date date) {
		types[idx - 1] = MysqlDefs.FIELD_TYPE_DATETIME;
		data[idx - 1] = date;

	}

	public void setDouble(int idx, Double d) {
		types[idx - 1] = MysqlDefs.FIELD_TYPE_DOUBLE;
		data[idx - 1] = d;

	}

	public void setFloat(int idx, Float f) {
		types[idx - 1] = MysqlDefs.FIELD_TYPE_FLOAT;
		data[idx - 1] = f;

	}

	public void setInteger(int idx, Integer i) {
		types[idx - 1] = MysqlDefs.FIELD_TYPE_LONG;
		data[idx - 1] = i;

	}

	public void setLong(int idx, Long l) {
		types[idx - 1] = MysqlDefs.FIELD_TYPE_LONGLONG;
		data[idx - 1] = l;

	}

	public void setShort(int idx, Short s) {
		types[idx - 1] = MysqlDefs.FIELD_TYPE_SHORT;
		data[idx - 1] = s;

	}

	public void setString(int idx, String string) {
		types[idx - 1] = MysqlDefs.FIELD_TYPE_STRING;
		data[idx - 1] = string;

	}

	public void setTime(int idx, Time time) {
		types[idx - 1] = MysqlDefs.FIELD_TYPE_TIME;
		data[idx - 1] = time;
	}

	public void setTimestamp(int idx, Timestamp timestamp) {
		types[idx - 1] = MysqlDefs.FIELD_TYPE_TIMESTAMP;
		data[idx - 1] = timestamp;
	}

	public Field[] getFields() {
		return fields;
	}

	public void setFields(Field[] fields) {
		this.fields = fields;
	}

	public Object[] getData() {
		return data;
	}

	public void setData(Object[] data) {
		this.data = data;
	}

	public Field[] getParams() {
		return params;
	}

	public void setParams(Field[] params) {
		this.params = params;
	}

	public int[] getTypes() {
		return types;
	}

	public void setTypes(int[] types) {
		this.types = types;
	}

	public int getStatementId() {
		return statementId;
	}

	public void setStatementId(int statementId) {
		this.statementId = statementId;
	}

	public void init(int statementId, int fields, int params) {
		this.statementId = statementId;
		this.fields = new Field[fields];
		this.params = new Field[params];
		this.types = new int[params];
		this.data = new Object[params];
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public void nextState() {
		state++;
	}

	public boolean isOver() {
		return state == OVER;
	}

	public void onSuccess(OK ok) {
	}

	public void onError(SQLException e) {
		e.printStackTrace();

	}
}
