package org.async.mysql.in;

import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import org.async.mysql.Utils;
import org.async.mysql.facade.Callback;

public class Parser {
	private Protocol protocol = new Protocol41();
	private List<Integer> waitFor = new LinkedList<Integer>();
	private int dataIdx = 0;
	private ByteBuffer buffer = ByteBuffer.allocate(65536);
	private int packetSize = -1;
	private int itemSize = 0;
	private int skip = 0;
	// private int packetNum;// TODO check this
	private Byte firstByte = null;
	private Packet packet = null;
	private Object message = null;
	private Callback callback;

	public Packet parse(ByteBuffer in) {
		// TODO packet size and read until end
		while (in.remaining() > 0) {
			if (skip > 0) {
				// TODO do this in on step
				in.get();
				skip--;
			} else {
				if (packetSize > 0 && firstByte == null) {
					firstByte = in.array()[in.position()];
				}

				if (buffer.position() == 0) {
					itemSize = packetSize < 0 ? 4 : protocol.getPacketMap(
							waitFor.get(0), firstByte)
							.getSize(dataIdx, message);
					if (itemSize != 0 && itemSize != Integer.MAX_VALUE
							&& itemSize != Integer.MIN_VALUE) {
						buffer.limit(itemSize);
					}
				}
				if (itemSize == 0) {
					for (int i = 0, e = in.remaining(); i < e; i++) {
						byte b = in.get();
						packetSize--;
						if (b != 0) {
							buffer.put(b);
						}
						if (b == 0 || packetSize == 0) {
							buffer.limit(buffer.position());
							break;
						}

					}
				} else if (itemSize == Protocol.LENGTH_CODED_STRING
						&& buffer.limit() == buffer.capacity()) {
					readLengthCodedString(in);
				} else if (itemSize == Protocol.LENGTH_CODED_BINARY
						&& buffer.limit() == buffer.capacity()) {
					readLengthCodedBinary(in);
				} else {
					int length = buffer.remaining();
					if (length > in.remaining()) {
						length = in.remaining();
					}
					buffer.put(in.array(), in.position(), length);
					packetSize -= length;
					in.position(in.position() + length);

				}
				if (buffer.limit() == buffer.position()) {
					try {
						if (packetSize < 0) {
							packetSize = (int) Utils.readLong(buffer.array(),
									0, 3);
							// System.out.println("size="+packetSize);
							// byte[] bb=new byte[packetSize];
							// System.arraycopy(in.array(),in.position(),bb , 0,
							// bb.length);
							// System.out.println(Arrays.toString(bb));
							// packetNum = (int) buffer.array()[3] & 0xFF;

						} else {
							PacketMap<Packet> map = protocol.getPacketMap(
									waitFor.get(0), firstByte);
							packet = map.getAssembler().process(dataIdx,
									buffer, packet, message);
							dataIdx++;

							if (dataIdx == map.size() || packetSize == 0) {
								if (packetSize != 0) {
									skip = packetSize;
								}
								dataIdx = 0;
								firstByte = null;
								packetSize = -1;
								waitFor.remove(0);
								Packet rs = packet;
								packet.onSuccess(this);
								packet = null;
								return rs;
							} else {
								// System.out.println("remaining="+packetSize+"
								// l="+buffer.limit());
							}

						}
					} finally {
						buffer.clear();
					}
				}

			}
		}
		return null;
	}

	private void readLengthCodedBinary(ByteBuffer in) {
		packetSize--;
		buffer.put(in.get());
		byte[] ar = buffer.array();
		int f = ar[0] & 0xFF;
		int limit = -1;
		if (f == 251) {
			buffer.limit(buffer.position());
		} else if (f > 251) {
			// TODO TEST
			if (f == 252 && buffer.position() > 2) {
				limit = (int) Utils.readLong(ar, 1, 2);
				in.position(in.position() + 2);
				packetSize -= 2;
			} else if (f == 253 && buffer.position() > 3) {
				limit = (int) Utils.readLong(ar, 1, 3);
				packetSize -= 3;
				in.position(in.position() + 3);
			} else if (f == 254 && buffer.position() > 8) {
				limit = (int) Utils.readLong(ar, 1, 8);
				packetSize -= 8;
				in.position(in.position() + 8);
			}
			if (limit > -1) {
				buffer.clear();
				buffer.limit(limit);
			}
		} else {
			buffer.clear();
			buffer.limit(1);
			buffer.put((byte) f);
		}
	}

	private void readLengthCodedString(ByteBuffer in) {
		packetSize--;
		buffer.put(in.get());
		byte[] ar = buffer.array();
		int f = ar[0] & 0xFF;
		// System.out.println("F="+f);
		if (f == 251) {
			buffer.clear();
			buffer.limit(buffer.position());
		} else if (f > 251) {
			// TODO TEST this
			if (buffer.position() > f - 250) {
				int limit = (int) Utils.readLong(ar, 1, f - 250);
				buffer.clear();
				packetSize -= f - 250;
				in.position(in.position() + (f - 250));
				buffer.limit(limit);
			}
		} else {
			buffer.clear();
			buffer.limit(f);
		}
	}

	public List<Integer> getWaitFor() {
		return waitFor;
	}

	public void setWaitFor(List<Integer> waitFor) {
		this.waitFor = waitFor;
	}

	public Object getMessage() {
		return message;
	}

	public void setMessage(Object message) {
		this.message = message;
	}

	public Callback getCallback() {
		return callback;
	}

	public void setCallback(Callback callback) {
		this.callback = callback;
	}

}
