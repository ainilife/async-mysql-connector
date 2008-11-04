package org.async.mysql.protocol.assembler;

import java.nio.ByteBuffer;

import org.async.mysql.jdbc.impl.AbstractResultSet;
import org.async.mysql.protocol.PacketAssembler;
import org.async.mysql.protocol.packets.RowDataBinary;

public class RowDataBinaryAssembler implements PacketAssembler<RowDataBinary> {

	public RowDataBinary process(int step, ByteBuffer buffer,
			RowDataBinary packet, Object message) {
		AbstractResultSet<?> rs=(AbstractResultSet<?>) message;
		if (packet == null) {
			packet = new RowDataBinary(rs.getFields().length);
		}
		// TODO NULL Support
		if (step > 1) {
			//TODO process null bit map
			byte[] ar = new byte[buffer.limit()];
			System.arraycopy(buffer.array(), 0, ar, 0, buffer.limit());
			packet.getData()[step - 2] = ar;
		} else if(step==1) {
			System.arraycopy(buffer.array(),0, packet.getNullBitMap(),0, packet.getNullBitMap().length);
		} else {
			//first byte is always 0??
		}
		return packet;
	}

}
