package org.async.mysql.in;

public class Utils {

	public static long readLong(byte[] ar, int offset,int length) {
		long rs = 0;
		for (int i = offset; i < length; i++) {
			rs += (ar[i] & 0xFF) << (i * 8);
		}
		return rs;
	}
}
