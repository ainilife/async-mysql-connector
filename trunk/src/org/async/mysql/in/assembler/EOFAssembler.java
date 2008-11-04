package org.async.mysql.in.assembler;

import java.nio.ByteBuffer;

import org.async.mysql.in.PacketAssembler;
import org.async.mysql.in.Utils;
import org.async.mysql.in.packets.EOF;

public class EOFAssembler implements PacketAssembler<EOF> {

	public EOF process(int step, ByteBuffer buffer, EOF packet, Object message) {
		if(packet==null) {
			packet=new EOF();
		}
		if(step==1) {
			packet.setWarningCount((int) Utils.readLong(buffer.array(),0,2));
		} else if(step==2) {
			packet.setStatusFlags((int) Utils.readLong(buffer.array(),0,2));
		}
		return packet;
	}

}
