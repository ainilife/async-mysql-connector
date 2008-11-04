package org.async.mysql.in.packets;

import org.async.mysql.facade.impl.AbstractResultSet;
import org.async.mysql.facade.impl.BinaryResultSet;
import org.async.mysql.facade.impl.StringResultSet;
import org.async.mysql.in.Packet;
import org.async.mysql.in.Parser;
import org.async.mysql.in.Protocol;

public class ResultSetHeader implements Packet {
	private long fieldCount;
	private long extra;
	private boolean _binary;

	public ResultSetHeader(boolean _binary) {
		super();
		this._binary = _binary;
	}

	public long getFieldCount() {
		return fieldCount;
	}

	public void setFieldCount(long fieldCount) {
		this.fieldCount = fieldCount;
	}

	public long getExtra() {
		return extra;
	}

	public void setExtra(long extra) {
		this.extra = extra;
	}

	public void onSuccess(Parser parser) {
		AbstractResultSet<?> rs = _binary?new BinaryResultSet(fieldCount):new StringResultSet(fieldCount);
		parser.setMessage(rs);
		for (int i = 0; i < fieldCount; i++) {
			parser.getWaitFor().add(i,Protocol.FIELD_PACKET);
		}
		int p = _binary ? Protocol.ROW_DATA_BINARY : Protocol.ROW_DATA;
		parser.getWaitFor().add((int)fieldCount,p);// EOF
		parser.getWaitFor().add((int)fieldCount+1,p);// DATA EOF
	}
}
