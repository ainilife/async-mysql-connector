package org.async.mysql.protocol.assembler;

import java.nio.ByteBuffer;

import org.async.mysql.jdbc.impl.AbstractResultSet;
import org.async.mysql.protocol.PacketAssembler;
import org.async.mysql.protocol.packets.RowData;

public class RowDataAssembler implements PacketAssembler<RowData> {


	public RowData process(int step, ByteBuffer buffer, RowData packet,
			Object message) {
		if(packet==null) {
			AbstractResultSet<?> rs=(AbstractResultSet<?>) message;
			packet=new RowData(rs.getFields().length);
		}
		//TODO NULL Support
		packet.getData()[step]=new String(buffer.array(),0,buffer.limit());
		return packet;
	}

}
