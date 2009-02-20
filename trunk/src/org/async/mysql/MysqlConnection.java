package org.async.mysql;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

import org.async.jdbc.AsyncConnection;
import org.async.jdbc.Callback;
import org.async.jdbc.Connection;
import org.async.jdbc.PreparedStatement;
import org.async.jdbc.Query;
import org.async.jdbc.ResultSetCallback;
import org.async.jdbc.Statement;
import org.async.jdbc.SuccessCallback;
import org.async.mysql.jdbc.AbstractResultSet;
import org.async.mysql.jdbc.InnerConnection;
import org.async.mysql.jdbc.PreparedStatementImpl;
import org.async.mysql.jdbc.StatementImpl;
import org.async.mysql.protocol.HasState;
import org.async.mysql.protocol.Packet;
import org.async.mysql.protocol.Parser;
import org.async.mysql.protocol.Protocol;
import org.async.mysql.protocol.packets.EOF;
import org.async.mysql.protocol.packets.Error;
import org.async.mysql.protocol.packets.Handshake;
import org.async.mysql.protocol.packets.OK;
import org.async.mysql.protocol.packets.PSOK;
import org.async.net.ChannelProcessor;

public class MysqlConnection implements ChannelProcessor, AsyncConnection,
		InnerConnection {
	private Handshake handshake;
	private final ByteBuffer out = ByteBuffer.allocate(65536);
	private final ByteBuffer in = ByteBuffer.allocate(65536);
	private SocketChannel channel;
	private Parser parser = new Parser();
	private List<Query> queries = new LinkedList<Query>();
	private List<Callback> callbacks = new LinkedList<Callback>();
	private String user;
	private String password;
	private String database;
	private SelectionKey key;
	private boolean closed = false;

	// TODO on connect callback

	public MysqlConnection(String host, int port, String user, String password,
			String database, Selector selector, SuccessCallback onConnect)
			throws IOException {
		super();
		channel = SocketChannel.open();
		channel.configureBlocking(false);
		channel.connect(new InetSocketAddress(host, port));
		key = channel.register(selector, SelectionKey.OP_CONNECT);
		key.attach(this);
		this.user = user;
		this.password = password;
		this.database = database;
		callbacks.add(onConnect);
		out.position(4);

	}

	public void auth(String user, String password, String database)
			throws SQLException {

		Utils.writeLong(out, MysqlDefs.CLIENT_LONG_PASSWORD
				| MysqlDefs.CLIENT_CONNECT_WITH_DB
				| MysqlDefs.CLIENT_PROTOCOL_41
				| MysqlDefs.CLIENT_SECURE_CONNECTION, 4);
		Utils.writeLong(out, 65536, 4);
		Utils.writeLong(out, 8, 1);
		Utils.filler(out, 23);
		Utils.nullTerminated(out, user);
		if (password.length() != 0) {
			Utils.lengthEncodedString(out, scramble411(password, handshake
					.getSeed()));
		} else {
			Utils.filler(out, 1);
		}
		// Utils.filler(out, 1);
		Utils.nullTerminated(out, database);
		send(1);
		parser.getWaitFor().add(Protocol.SUCCESS_PACKET);
	}

	// public void sendLongData(int statementId, int paramNum, int type,
	// Object data) throws SQLException {
	// out.put((byte) MysqlDefs.COM_STMT_SEND_LONG_DATA);
	// Utils.writeLong(out, statementId, 4);
	// Utils.writeLong(out, paramNum - 1, 2);
	// Utils.writeLong(out, type, 2);
	// Utils.write(out, type, data);
	// send(0);
	// }

	public void executeQuery(int statementId, int[] types, Object[] params)
			throws SQLException {
		execute(statementId, types, params);
		parser.getWaitFor().add(Protocol.RESULT_SET_PACKET_BINARY);
	}

	private void execute(int statementId, int[] types, Object[] params)
			throws SQLException {
		out.put((byte) MysqlDefs.COM_STMT_EXECUTE);
		Utils.writeLong(out, statementId, 4);
		Utils.writeLong(out, 0, 1);
		Utils.writeLong(out, 1, 4);
		int p = out.position();
		int paramCount = params.length;
		int nullCount = ((paramCount + 7) / 8);
		byte[] nullBitsBuffer = new byte[nullCount];
		Utils.filler(out, nullCount);
		// Send data to server
		Utils.writeLong(out, 1, 1);
		for (int t : types) {
			Utils.writeLong(out, t, 2);
		}
		for (int i = 0; i < params.length; i++) {
			if (params[i] == null) {
				nullBitsBuffer[i / 8] |= (1 << (i & 7));
			} else {
				Utils.write(out, types[i], params[i]);
			}
		}
		System.arraycopy(nullBitsBuffer, 0, out.array(), p,
				nullBitsBuffer.length);
		send(0);
	}

	@Override
	public void executeUpdate(int statementId, int[] types, Object[] data)
			throws SQLException {
		execute(statementId, types, data);
		parser.getWaitFor().add(Protocol.SUCCESS_PACKET);
	}

	public void close(int statementId) throws SQLException {
		out.put((byte) MysqlDefs.COM_STMT_CLOSE);
		Utils.writeLong(out, statementId, 4);
		// parser.getWaitFor().add(Protocol.SUCCESS_PACKET);
		send(0);

	}

	public void close() throws SQLException {
		query(new SilentQuery() {
			@Override
			public void query(Connection connection) throws SQLException {
				out.put((byte) MysqlDefs.COM_QUIT);
				send(0);
				close(key);
				key = null;
			}

		});
		closed = true;
	}

	private void send(int num) throws SQLException {
		try {
			out.flip();
			Utils.writeLong(out, out.limit() - 4, 3);
			Utils.writeLong(out, num, 1);
			out.position(0);
			while (out.remaining() > 0) {
				channel.write(out);
			}

			// byte[] b=new byte[out.limit()];
			// System.out.println("SEND");
			// System.arraycopy(out.array(), 0, b, 0,out.limit());
			// System.out.println(Arrays.toString(b));
			out.clear();
			out.position(4);
		} catch (Exception e) {
			throw new SQLException(e.getMessage());
		}
	}

	public void query(String sql) throws SQLException {
		out.put((byte) MysqlDefs.COM_QUERY);
		out.put(sql.getBytes());
		send(0);
		parser.getWaitFor().add(Protocol.RESULT_SET_PACKET);

	}

	@Override
	public void update(String sql) throws SQLException {
		out.put((byte) MysqlDefs.COM_QUERY);
		out.put(sql.getBytes());
		send(0);
		parser.getWaitFor().add(Protocol.SUCCESS_PACKET);

	}

	public void prepare(String sql) throws SQLException {
		out.put((byte) MysqlDefs.COM_STMT_PREPARE);
		out.put(sql.getBytes());
		send(0);
		parser.getWaitFor().add(Protocol.PSOK_PACKET);

	}

	private static byte[] scramble411(String password, String seed) {
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-1");

			byte[] passwordHashStage1 = md.digest(password.getBytes());
			md.reset();

			byte[] passwordHashStage2 = md.digest(passwordHashStage1);
			md.reset();
			md.update(seed.getBytes());
			md.update(passwordHashStage2);
			byte[] toBeXord = md.digest();

			int numToXor = toBeXord.length;

			for (int i = 0; i < numToXor; i++) {
				toBeXord[i] = (byte) (toBeXord[i] ^ passwordHashStage1[i]);
			}
			return toBeXord;
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} //$NON-NLS-1$
		return null;

	}

	public void accept(SelectionKey key) {
		throw new IllegalStateException();

	}

	public void close(SelectionKey key) {
		key.cancel();
		SocketChannel channel = (SocketChannel) key.channel();
		try {
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void connect(SelectionKey key) {
		SocketChannel channel = (SocketChannel) key.channel();
		try {
			channel.finishConnect();
			key.interestOps(SelectionKey.OP_READ);
			parser.getWaitFor().add(Protocol.HAND_SHAKE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void read(SelectionKey key) {
		SocketChannel channel = (SocketChannel) key.channel();
		try {
			int read = -1;
			while ((read = channel.read(in)) > 0) {
				in.limit(read);
				in.position(0);
				while (in.remaining() > 0) {
					Packet result = parser.parse(in);
					if (result != null) {
						if (result instanceof EOF) {
							if (((HasState) parser.getMessage()).isOver()) {
								if (parser.getMessage() instanceof AbstractResultSet) {
									AbstractResultSet<?> rs = (AbstractResultSet<?>) parser
											.getMessage();
									Callback callback = callbacks.remove(0);
									if (callback != null) {
										((ResultSetCallback) callback)
												.onResultSet(rs);
									}
								}
								if (!queries.isEmpty())
									key.interestOps(SelectionKey.OP_WRITE);
							}
						} else if (result instanceof OK) {
							Callback callback = callbacks.remove(0);
							if (callback != null) {
								((SuccessCallback) callback)
										.onSuccess((OK) result);
							}
							if (!queries.isEmpty())
								key.interestOps(SelectionKey.OP_WRITE);
						} else if (result instanceof PSOK) {
							callbacks.remove(0);
							if (!queries.isEmpty())
								key.interestOps(SelectionKey.OP_WRITE);
						} else if (result instanceof Error) {
							Callback callback = callbacks.remove(0);
							if (callback != null) {
								Error e = (Error) result;
								callback.onError(new SQLException(e
										.getMessage(), e.getSqlState()));
							}
							if (!queries.isEmpty())
								key.interestOps(SelectionKey.OP_WRITE);

						} else if (result instanceof Handshake) {
							this.handshake = (Handshake) result;
							auth(user, password, database);
						}

					}
				}
				in.clear();
			}
			if (read == -1) {
				close(key);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void write(SelectionKey key) {
		try {
			Query q = queries.remove(0);
			q.query(this);
			if (!(q instanceof SilentQuery) || queries.isEmpty()) {
				if (this.key != null)
					key.interestOps(SelectionKey.OP_READ);
			}
		} catch (SQLException e) {
			Callback callback = callbacks.remove(0);
			if (callback != null) {
				callback.onError(e);
			}
		}
	}

	public void query(Query q, Callback callback) throws SQLException {
		if (isClosed())
			throw new SQLException(
					" No operations allowed after connection closed.");
		queries.add(q);
		if (callbacks.isEmpty()) {
			key.interestOps(SelectionKey.OP_WRITE);
		}
		callbacks.add(callback);

	}

	public void query(SilentQuery q) throws SQLException {
		if (isClosed())
			throw new SQLException(
					" No operations allowed after connection closed.");
		queries.add(q);
		if (callbacks.isEmpty() && queries.isEmpty()) {
			key.interestOps(SelectionKey.OP_WRITE);
		}

	}

	public Parser getParser() {
		return parser;
	}

	public void setParser(Parser parser) {
		this.parser = parser;
	}

	public Statement createStatement() {
		return new StatementImpl(this);
	}

	public PreparedStatement prepareStatement(String sql) throws SQLException {
		return new PreparedStatementImpl(sql, this);
	}

	public boolean isClosed() {
		return closed;
	}

	@Override
	public void reset(int statementId) throws SQLException {
		out.put((byte) MysqlDefs.COM_STMT_RESET);
		Utils.writeLong(out, statementId, 4);
		//parser.getWaitFor().add(Protocol.SUCCESS_PACKET);
		parser.getWaitFor().add(Protocol.SUCCESS_PACKET);
		send(0);
		
	}
	
	public int load() {
		return Math.max(queries.size(),callbacks.size());
	}


}
