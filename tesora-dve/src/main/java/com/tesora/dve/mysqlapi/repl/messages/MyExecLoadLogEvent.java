// OS_STATUS: public
package com.tesora.dve.mysqlapi.repl.messages;

import io.netty.buffer.ByteBuf;

import org.apache.log4j.Logger;

import com.tesora.dve.exceptions.PEException;
import com.tesora.dve.mysqlapi.repl.MyReplicationSlaveService;

public class MyExecLoadLogEvent extends MyLogEventPacket {
	private static final Logger logger = Logger
			.getLogger(MyExecLoadLogEvent.class);

	int fileId;
	
	public MyExecLoadLogEvent(MyReplEventCommonHeader ch) {
		super(ch);
	}

	@Override
	public void unmarshallMessage(ByteBuf cb) {
		fileId = cb.readInt();
	}

	@Override
	public void marshallMessage(ByteBuf cb) throws PEException {
		cb.writeInt(fileId);
	}

	@Override
	public void processEvent(MyReplicationSlaveService plugin) {
		logger.warn("Message is parsed but no handler is implemented for log event type: EXEC_LOAD_EVENT");
	}
}
