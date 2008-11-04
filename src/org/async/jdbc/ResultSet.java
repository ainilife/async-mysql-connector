package org.async.jdbc;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

public interface ResultSet {
	Byte getByte(int idx);

	Short getShort(int idx);

	Integer getInteger(int idx);

	Long getLong(int idx);

	Float getFloat(int idx);

	Double getDouble(int idx);

	Time getTime(int idx);

	Date getDate(int idx);

	Timestamp getTimestamp(int idx);

	String getString(int idx);

	byte[] getBytes(int idx);

	boolean hasNext();

	void next();

}
