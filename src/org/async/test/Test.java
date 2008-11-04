package org.async.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.sql.Date;
import java.sql.SQLException;

import org.async.mysql.MysqlConnection;
import org.async.mysql.facade.AsyncConnection;
import org.async.mysql.facade.PreparedQuery;
import org.async.mysql.facade.PreparedStatement;
import org.async.mysql.facade.ResultSet;
import org.async.mysql.facade.ResultSetCallback;
import org.async.mysql.facade.Statement;
import org.async.mysql.facade.SuccessCallback;
import org.async.mysql.in.packets.OK;
import org.async.net.Multiplexer;

public class Test {

	public static void main(String[] args) throws IOException, SQLException {
		// TODO NULL bitmap
		// TODO NULL values
		// TODO check Date
		SocketChannel channel = SocketChannel.open();
		channel.configureBlocking(false);
		channel.connect(new InetSocketAddress("localhost", 3306));
		Multiplexer mpx = new Multiplexer();
		SelectionKey k = channel.register(mpx.getSelector(),
				SelectionKey.OP_CONNECT);
		AsyncConnection connection = new MysqlConnection(k, "root", "",
				"");
		k.attach(connection);
		Statement st = connection.createStatement();
		SuccessCallback successCallback = new SuccessCallback() {

			@Override
			public void onSuccess(OK ok) {
				System.out.println("OK");
			}

			@Override
			public void onError(SQLException e) {
				e.printStackTrace();

			}

		};
		st
				.executeUpdate(
						"CREATE DATABASE IF NOT EXISTS async_mysql_test collate utf8_general_ci",
						successCallback);
		st.executeUpdate("USE async_mysql_test",successCallback);
		st
				.executeUpdate(
						"CREATE TABLE IF NOT EXISTS test  ("
								+ "id int(11) NOT NULL auto_increment,"
								+ "text0 TEXT collate utf8_general_ci,"
								+ "varchar0 VARCHAR(255) collate utf8_general_ci NOT NULL,"
								+ "date0 DATETIME,"
								+ "PRIMARY KEY  (id)"
								+ ") ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_general_ci;",
						successCallback);
		st.executeUpdate("TRUNCATE test",successCallback);
		PreparedStatement insert = connection
		.prepareStatement("INSERT INTO test SET text0=?,varchar0=?,date0=?");
		insert.executeUpdate(new PreparedQuery() {

			@Override
			public void query(PreparedStatement pstmt) throws SQLException {
				pstmt.setString(1,"dummy text filler");
				pstmt.setString(2,"dummy varchar filler");
				pstmt.setDate(3,new Date(System.currentTimeMillis()));
			}

		},successCallback);
		PreparedStatement ps = connection
				.prepareStatement("select * from test where id=?");
		ResultSetCallback rsCallback = new ResultSetCallback() {
			public void onResultSet(ResultSet rs) {
				while (rs.hasNext()) {
					rs.next();
					System.out.println(rs.getLong(1) + " " + rs.getString(2)
							+ " " + rs.getString(3) + " " + rs.getTimestamp(4));
				}
			}

			public void onError(SQLException e) {
				e.printStackTrace();
			}

		};
		insert.close();
		st.executeQuery("select * from test", rsCallback);
		ps.executeQuery(new PreparedQuery() {
			public void query(PreparedStatement pstmt) throws SQLException {
				pstmt.setInteger(1, 1);
			}

		}, rsCallback);
		ps.close();

		while (true) {
			mpx.select();
		}
	}
}
