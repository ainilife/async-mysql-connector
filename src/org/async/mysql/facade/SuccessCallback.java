package org.async.mysql.facade;

import org.async.mysql.in.packets.OK;

public interface SuccessCallback extends Callback {
	void onSuccess(OK ok);
}
