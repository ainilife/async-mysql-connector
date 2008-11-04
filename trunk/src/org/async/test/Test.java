package org.async.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.sql.SQLException;

import org.async.mysql.facade.AsyncConnection;
import org.async.mysql.facade.PreparedQuery;
import org.async.mysql.facade.PreparedStatement;
import org.async.mysql.facade.ResultSet;
import org.async.mysql.facade.ResultSetCallback;
import org.async.mysql.facade.Statement;
import org.async.mysql.out.MysqlConnection;
import org.async.net.Multiplexer;

public class Test {

	public static void main(String[] args) throws IOException, SQLException {
		//TODO NULL bitmap
		//TODO NULL values
		//TODO check Date
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(false);
		channel.connect(new InetSocketAddress("localhost", 3306));
		Multiplexer mpx = new Multiplexer();
		SelectionKey k = channel.register(mpx.getSelector(),
				SelectionKey.OP_CONNECT);
		AsyncConnection connection = new MysqlConnection(k, "root", "",
				"baby_speak");
		k.attach(connection);
		Statement st = connection.createStatement();
		PreparedStatement ps = connection
				.prepareStatement("select * from answers where id=?");
		ResultSetCallback rsCallback = new ResultSetCallback() {
			public void onResultSet(ResultSet rs) {
				while (rs.hasNext()) {
					rs.next();
					System.out.println(rs.getLong(1) + " " + rs.getString(2)
							+ " " + rs.getString(3) + " " + rs.getString(4));
				}
			}

			public void onError(SQLException e) {
				e.printStackTrace();
			}

		};
		st.executeQuery("select * from answers", rsCallback);
		ps.execute(new PreparedQuery() {

			public void query(PreparedStatement pstmt) throws SQLException {
				pstmt.setLong(1, 1l);
			}

		}, rsCallback);

		ps.close();

		st.executeQuery("select * from answers", rsCallback);
		while (true) {
			mpx.select();
		}
	}
}
