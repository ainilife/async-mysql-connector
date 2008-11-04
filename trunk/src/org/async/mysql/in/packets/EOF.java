package org.async.mysql.in.packets;

import org.async.mysql.facade.HasState;
import org.async.mysql.in.Packet;
import org.async.mysql.in.Parser;

public class EOF implements Packet {
	private int warningCount;
	private int statusFlags;

	public int getWarningCount() {
		return warningCount;
	}

	public void setWarningCount(int warningCount) {
		this.warningCount = warningCount;
	}

	public int getStatusFlags() {
		return statusFlags;
	}

	public void setStatusFlags(int statusFlags) {
		this.statusFlags = statusFlags;
	}

	public void onSuccess(Parser parser) {
		HasState st = (HasState) parser.getMessage();
		st.nextState();
	}

}
