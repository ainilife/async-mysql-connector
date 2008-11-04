package org.async.mysql.facade;

import java.sql.SQLException;

public interface Callback {
	void onError(SQLException e);
}
