package org.async.jdbc;

import org.async.mysql.protocol.packets.OK;

public interface SuccessCallback extends Callback {
	void onSuccess(OK ok);
}
