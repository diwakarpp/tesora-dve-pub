package com.tesora.dve.db.mysql.libmy;

/*
 * #%L
 * Tesora Inc.
 * Database Virtualization Engine
 * %%
 * Copyright (C) 2011 - 2014 Tesora Inc.
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

import io.netty.buffer.ByteBuf;

public class MyPrepareOKResponse extends MyResponseMessage {

	public static final byte OKPKT_FIELD_COUNT = 0;
	protected long stmtId;
	protected int warningCount=0;
	protected int numColumns=0;
	protected int numParams=0;
	
	public MyPrepareOKResponse() {
	}

    public MyPrepareOKResponse(MyPrepareOKResponse other) {
        this.stmtId = other.stmtId;
        this.warningCount = other.warningCount;
        this.numColumns = other.numColumns;
        this.numParams = other.numParams;
    }
	
	public MyPrepareOKResponse( int numParams, int numColumns ) {
		this.numParams = numParams;
		this.numColumns = numColumns;
	}
	
	@Override
    public void marshallMessage(ByteBuf cb) {
		cb.writeZero(1);				// status
		cb.writeInt((int) stmtId);
		cb.writeShort(numColumns);
		cb.writeShort(numParams);
		cb.writeZero(1);				// filler
		cb.writeShort(warningCount);
	}

	@Override
	public void unmarshallMessage(ByteBuf cb) {
		cb.skipBytes(1);
		stmtId = cb.readInt();
		numColumns = cb.readUnsignedShort();
		numParams = cb.readUnsignedShort();
		cb.skipBytes(1);
		warningCount = cb.readShort();
	}

	public long getStmtId() {
		return stmtId;
	}

	public void setStmtId(long stmtId) {
		this.stmtId = stmtId;
	}

	public int getWarningCount() {
		return warningCount;
	}

	public void setWarningCount(int warningCount) {
		this.warningCount = warningCount;
	}

	public int getNumColumns() {
		return numColumns;
	}

	public void setNumColumns(int numColumns) {
		this.numColumns = numColumns;
	}

	public int getNumParams() {
		return numParams;
	}

	public void setNumParams(int numParams) {
		this.numParams = numParams;
	}

	@Override
	public MyMessageType getMessageType() {
		return MyMessageType.PREPAREOK_RESPONSE;
	}

}
