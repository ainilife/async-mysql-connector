package org.async.mysql.in.packets;

import org.async.mysql.facade.impl.BinaryResultSet;
import org.async.mysql.in.Packet;
import org.async.mysql.in.Parser;
import org.async.mysql.in.Protocol;

public class RowDataBinary implements Packet {
	private byte[] nullBitMap;
	private byte[][] data;

	public RowDataBinary(int size) {
		super();
		data = new byte[size][];
		nullBitMap = new byte[(size + 9) / 8];
	}

	public byte[] getNullBitMap() {
		return nullBitMap;
	}

	public void setNullBitMap(byte[] nullBitMap) {
		this.nullBitMap = nullBitMap;
	}

	public byte[][] getData() {
		return data;
	}

	public void setData(byte[][] data) {
		this.data = data;
	}

	public void onSuccess(Parser parser) {
		BinaryResultSet rs=(BinaryResultSet) parser.getMessage();
		rs.getData().add(data);
		parser.getWaitFor().add(0,Protocol.ROW_DATA_BINARY);
	}

}
