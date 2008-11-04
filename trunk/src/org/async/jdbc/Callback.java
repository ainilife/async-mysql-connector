package org.async.jdbc;

import java.sql.SQLException;

public interface Callback {
	void onError(SQLException e);
}
