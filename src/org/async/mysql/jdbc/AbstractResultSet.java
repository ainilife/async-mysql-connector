package org.async.mysql.jdbc;

import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.async.jdbc.ResultSet;
import org.async.mysql.protocol.HasState;
import org.async.mysql.protocol.packets.Field;

public abstract class AbstractResultSet<T> implements ResultSet, HasState {
	public static int FIELDS = 0;
	public static int DATA = 1;
	public static int OVER = 2;
	protected Field[] fields;
	protected List<T> data = new LinkedList<T>();
	protected Iterator<T> it;
	protected Object[] unpackedRow;
	protected int state = 0;

	public AbstractResultSet(long fieldCount) {
		fields = new Field[(int) fieldCount];
	}

	public boolean hasNext() {
		if (it == null) {
			it = data.iterator();
		}
		return it.hasNext();
	}

	public void next() {
		if (it == null) {
			it = data.iterator();
		}
		T next = it.next();
		unpackedRow = unpack(next);
	}

	protected abstract Object[] unpack(T next);

	public Field[] getFields() {
		return fields;
	}

	public void setFields(Field[] fields) {
		this.fields = fields;
	}

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

	public Byte getByte(int idx) {
		return ((Number) unpackedRow[idx-1]).byteValue();
	}

	public Short getShort(int idx) {
		return ((Number) unpackedRow[idx-1]).shortValue();
	}

	public Integer getInteger(int idx) {
		return ((Number) unpackedRow[idx-1]).intValue();
	}

	public Long getLong(int idx) {
		return ((Number) unpackedRow[idx-1]).longValue();
	}

	public Float getFloat(int idx) {
		return ((Number) unpackedRow[idx-1]).floatValue();
	}

	public Double getDouble(int idx) {
		return ((Number) unpackedRow[idx-1]).doubleValue();
	}

	public Time getTime(int idx) {
		return (Time) unpackedRow[idx-1];
	}

	public Date getDate(int idx) {
		return (Date) unpackedRow[idx-1];
	}

	public Timestamp getTimestamp(int idx) {
		return (Timestamp) unpackedRow[idx-1];
	}

	public String getString(int idx) {
		return (String) unpackedRow[idx-1];
	}

	public byte[] getBytes(int idx) {
		return (byte[]) unpackedRow[idx-1];
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
		return state==OVER;
	}

}
