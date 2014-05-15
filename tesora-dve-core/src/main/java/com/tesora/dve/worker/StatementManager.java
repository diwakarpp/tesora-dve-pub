// OS_STATUS: public
package com.tesora.dve.worker;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.tesora.dve.exceptions.PESQLException;

public enum StatementManager {
	
	INSTANCE;

	// use double locking so that it works at higher conn counts
	final ConcurrentHashMap<Long, Set<WorkerStatement>> active = new ConcurrentHashMap<Long, Set<WorkerStatement>>();

	private Set<WorkerStatement> getSet(Long connID) {
//		synchronized(this) {
			Set<WorkerStatement> e = active.get(connID);
			if (e == null) {
				e = Collections.newSetFromMap(new ConcurrentHashMap<WorkerStatement, Boolean>());
				Set<WorkerStatement> realSet = active.putIfAbsent(connID, e);
				if (realSet != null)
					e = realSet;
//				active.put(connID, e);
			}
			return e;
//		}
	}
	
	public void registerStatement(long connectionId, WorkerStatement stmt) {
		Long connID = Long.valueOf(connectionId);
		Set<WorkerStatement> s = getSet(connID);
//		synchronized(s) {
			s.add(stmt);
//		}
	}
	
	public void unregisterStatement(long connectionId, WorkerStatement stmt) {
		Long connID = Long.valueOf(connectionId);
		Set<WorkerStatement> s = getSet(connID);
		boolean removeKey = false;
//		synchronized(s) {
			s.remove(stmt);
			removeKey = s.isEmpty();
//		}
		if (removeKey) {
//			synchronized(this) {
//				Set<WorkerStatement> e = active.get(connID);
//				if (e == null) return;
//				synchronized(e) {
//					if (!e.isEmpty()) return;
					active.remove(connID);
//				}
//			}
		}
	}
	
	public void cancelAllStatements(long connectionId) throws PESQLException {
		Long connID = Long.valueOf(connectionId);
		// use a copy of in case the thing gets modified under us
		Set<WorkerStatement> s = getSet(connID);
		if (s == null) return;
		Set<WorkerStatement> copy = null;
		synchronized(s) {
			copy = new HashSet<WorkerStatement>(s);
		}		
		for (WorkerStatement stmt : copy) {
			stmt.cancel();
		}
	}

	public void cancellAllConnections() throws PESQLException {
		HashSet<WorkerStatement> copy = new HashSet<WorkerStatement>();
		synchronized(this) {
			for(Set<WorkerStatement> v : active.values())
				copy.addAll(v);
		}
		for (WorkerStatement stmt : copy)
			stmt.cancel();
	}

	public void clear() {
		active.clear();
	}
	
}
