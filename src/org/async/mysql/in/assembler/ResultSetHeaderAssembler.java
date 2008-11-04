package org.async.mysql.in.assembler;

import java.nio.ByteBuffer;

import org.async.mysql.in.PacketAssembler;
import org.async.mysql.in.Utils;
import org.async.mysql.in.packets.ResultSetHeader;

public class ResultSetHeaderAssembler implements PacketAssembler<ResultSetHeader> {
	private boolean binary;
	public ResultSetHeaderAssembler(boolean binary) {
		super();
		this.binary=binary;
	}

	public ResultSetHeader process(int step, ByteBuffer buffer,
			ResultSetHeader packet, Object message) {
		if(packet==null) {
			packet=new ResultSetHeader(binary);
		}
		if(step==0) {
			packet.setFieldCount(Utils.readLong(buffer.array(),0,buffer.limit()));
		} else if(step==1) {
			packet.setExtra(Utils.readLong(buffer.array(),0,buffer.limit()));
		}
		return packet;
	}

}
