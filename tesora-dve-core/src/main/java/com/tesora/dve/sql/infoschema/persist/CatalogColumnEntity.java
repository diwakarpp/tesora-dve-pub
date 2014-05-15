// OS_STATUS: public
package com.tesora.dve.sql.infoschema.persist;

import com.tesora.dve.exceptions.PEException;
import com.tesora.dve.persist.SimplePersistedEntity;
import com.tesora.dve.sql.schema.types.Type;

public class CatalogColumnEntity extends SimplePersistedEntity {

	public CatalogColumnEntity(CatalogSchema cs, CatalogTableEntity cte) throws PEException {
		super(cs.getColumn());
		addRequires(cte);
		preValue("has_default_value",0);
		preValue("auto_generated",0);
		preValue("default_value_is_constant",1);
		preValue("on_update",0);
		preValue("cdv",0);
		preValue("hash_position",0);
	}
	
	public void setName(String n) throws PEException {
		preValue("name",n);
	}
	
	public void setNullable(boolean v) throws PEException {
		preValue("nullable",(v ? 1 : 0));
	}
	
	public void setPosition(int v) throws PEException {
		preValue("order_in_table",v);
	}
	
	public void setNativeTypeModifiers(String v) throws PEException {
		preValue("native_type_modifiers",v);		
	}
	
	public String getNativeTypeModifiers() {
		return (String)getValue("native_type_modifiers"); 
	}
	
	public void setCharset(String v) throws PEException {
		preValue("charset",v);
	}
	
	public void setCollation(String v) throws PEException {
		preValue("collation",v);
	}
	
	public String getCharset() { 
		return (String)getValue("charset");
	}
	
	public String getCollation() {
		return (String)getValue("collation");
	}
	
	public void setType(Type t) throws PEException {
		preValue("native_type_name",t.getTypeName());
		preValue("data_type",t.getDataType());
		if (t.hasSize()) {
			preValue("size",t.getSize());
			if (t.hasPrecisionAndScale()) {
				preValue("prec",t.getPrecision());
				preValue("scale",t.getScale());
			} else {
				preValue("prec",0);
				preValue("scale",0);
			}
		} else {
			preValue("size",0);
			preValue("prec",0);
			preValue("scale",0);
		}
		t.addColumnTypeModifiers(this);
	}
}
