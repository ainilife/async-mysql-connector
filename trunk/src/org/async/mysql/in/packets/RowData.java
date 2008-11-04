package org.async.mysql.in.packets;

import java.util.Arrays;

import org.async.mysql.facade.impl.StringResultSet;
import org.async.mysql.in.Packet;
import org.async.mysql.in.Parser;
import org.async.mysql.in.Protocol;

public class RowData implements Packet {
	private String[] data;

	public RowData(int size) {
		super();
		data = new String[size];
	}

	public void onSuccess(Parser parser) {
		StringResultSet rs=(StringResultSet) parser.getMessage();
		rs.getData().add(data);
		parser.getWaitFor().add(0,Protocol.ROW_DATA);
	}

	public Object[] getData() {
		return data;
	}

	public void setData(String[] data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return Arrays.toString(data);
	}
}
