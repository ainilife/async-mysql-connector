package org.async.mysql.in.packets;

import org.async.mysql.facade.impl.PreparedStatementImpl;
import org.async.mysql.in.Packet;
import org.async.mysql.in.Parser;
import org.async.mysql.in.Protocol;

public class PSOK implements Packet {
	private int statementId;
	private int columns;
	private int parameters;


	public int getStatementId() {
		return statementId;
	}


	public void setStatementId(int statementId) {
		this.statementId = statementId;
	}


	public int getColumns() {
		return columns;
	}


	public void setColumns(int columns) {
		this.columns = columns;
	}


	public int getParameters() {
		return parameters;
	}


	public void setParameters(int parameters) {
		this.parameters = parameters;
	}


	public void onSuccess(Parser parser) {

		for(int i=0;i<parameters;i++) {
			parser.getWaitFor().add(i,Protocol.FIELD_PACKET);
		}
		parser.getWaitFor().add(parameters,Protocol.ROW_DATA_BINARY);
		for(int i=0;i<columns;i++) {
			parser.getWaitFor().add(parameters+i+1,Protocol.FIELD_PACKET);
		}
		parser.getWaitFor().add(parameters+columns+1,Protocol.ROW_DATA_BINARY);
		PreparedStatementImpl pstmt=(PreparedStatementImpl)parser.getMessage();
		pstmt.init(statementId, columns, parameters);
	}
}
