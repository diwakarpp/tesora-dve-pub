// OS_STATUS: public
package com.tesora.dve.mysqlapi.repl;

import org.apache.commons.lang.StringUtils;

public class MyBinLogPosition {
	private String masterHost;
	private String fileName;
	private long position;

	public MyBinLogPosition() {
	};

	public MyBinLogPosition(String masterHost, String fileName, long position) {
		this.masterHost = masterHost;
		this.fileName = fileName;
		this.position = position;
	}

	public String getMasterHost() {
		return masterHost;
	}

	public void setMasterHost(String masterHost) {
		this.masterHost = masterHost;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getPosition() {
		return position;
	}

	public void setPosition(long position) {
		this.position = position;
	}
	
	@Override
	public String toString() {
		return masterHost + "/" + fileName + "/" + position;
	}
	
	@Override
	public boolean equals(Object arg0) {
		if (arg0 instanceof MyBinLogPosition) {
			MyBinLogPosition other = (MyBinLogPosition)arg0;
			boolean ret = StringUtils.equals(this.masterHost, other.masterHost) && 
					StringUtils.equals(this.fileName, other.fileName) && 
					(this.position == other.position);
			return ret;
		} else {
			return super.equals(arg0);
		}
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (int) (prime * result + (this.position^(this.position>>>32)));
		result = prime * result + ((masterHost == null) ? 0 : masterHost.hashCode());
		result = prime * result + ((fileName == null) ? 0 : fileName.hashCode());
		return result;
	}
}
