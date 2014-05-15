// OS_STATUS: public
package com.tesora.dve.sql.util;

public enum TestName {
	NATIVE("nativeTest","native",true,false,false),
	SINGLE("singleTest","single",false,true,false),
	MULTI("multiTest","multi",false,false,false),
	SINGLEPORTAL("singlePortalTest","singleportal",false,true,false),
	MULTIPORTAL("multiPortalTest","multiportal",false,false,false),
	NATIVEMULTI("nativeMultiTest","nativemulti",true,false,false),
	NATIVESINGLE("nativeSingleTest","nativesingle",true,true,false),
	NATIVEMULTIPORTAL("nativeMultiPortalTest","nativemultiportal",true,false,false),
	NATIVESINGLEPORTAL("nativeSinglePortalTest","nativesingleportal",true,true,false),
	SINGLEMT("singleMTTest","singlemt",false,true,true),
	MULTIMT("multiMTTest","multimt",false,false,true),
	SINGLEMTPORTAL("singleMTPortalTest","singlemtportal",false,true,true),
	MULTIMTPORTAL("multiMTPortalTest","multimtportal",false,false,true),
	NATIVESINGLEMT("nativeSingleMTTest","nativesinglemt",true,true,true),
	NATIVEMULTIMT("nativeMultiMTTest","nativemultimt",true,false,true),
	NATIVESINGLEMTPORTAL("nativeSingleMTPortalTest","nativesinglemtportal",true,true,true),
	NATIVEMULTIMTPORTAL("nativeMultiMTPortalTest","nativemultimtportal",true,false,true);
	
	private final String testName;
	private final String sysvarName;
	// true for any variant that has native ddl
	private final boolean nativeCombo;
	// true for any variant that is single pe site
	private final boolean pesingle;
	private final boolean mt;
	private TestName(String legacyName, String newName, boolean isNativeCombo, boolean isPESingle, boolean isMT) {
		testName = legacyName;
		sysvarName = newName;
		mt = isMT;
		nativeCombo = isNativeCombo;
		pesingle = isPESingle;
	}
	
	public String getMethodName() {
		return testName;
	}
	
	public String getNewName(){
		return sysvarName;
	}
	
	public boolean isMT() {
		return mt;
	}
	
	public boolean isPESingle() {
		return pesingle;
	}
	
	public boolean isNativeCombo() {
		return nativeCombo;
	}
	public static TestName getMatching(String name) {
		for(TestName tn : TestName.values()) {
			if (tn.getNewName().equals(name))
				return tn;
		}
		return null;
	}
}