package org.async.mysql.in.packets;

import org.async.mysql.in.Packet;
import org.async.mysql.in.Parser;

public class Parameter implements Packet {
	//TODO seems like not used
	private int type;
	private int flags;
	private int decimals;
	private int length;

	@Override
	public String toString() {
		return "type="+type+" flags="+" decimals="+decimals+" length="+length;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public int getDecimals() {
		return decimals;
	}

	public void setDecimals(int decimals) {
		this.decimals = decimals;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public void onSuccess(Parser parser) {

	}
}
