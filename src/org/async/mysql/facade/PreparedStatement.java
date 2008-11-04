package org.async.mysql.facade;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;

public interface PreparedStatement {
	void executeQuery(PreparedQuery query,ResultSetCallback callback) throws SQLException;

	void executeUpdate(PreparedQuery query,SuccessCallback callback) throws SQLException;

	void setByte(int idx, Byte b);

	void setShort(int idx, Short s);

	void setInteger(int idx, Integer i);

	void setLong(int idx, Long l);

	void setFloat(int idx, Float f);

	void setDouble(int idx, Double d);

	void setTime(int idx, Time time);

	void setDate(int idx, Date date);

	void setTimestamp(int idx, Timestamp timestamp);

	void setString(int idx, String string);

	void setBytes(int idx, byte[] bytes);

	void close() throws SQLException;

}
