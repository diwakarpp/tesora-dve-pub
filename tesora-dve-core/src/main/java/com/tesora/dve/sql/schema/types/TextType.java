// OS_STATUS: public
package com.tesora.dve.sql.schema.types;

import com.tesora.dve.common.catalog.UserColumn;
import com.tesora.dve.db.NativeType;
import com.tesora.dve.db.mysql.MysqlNativeType;
import com.tesora.dve.exceptions.PEException;
import com.tesora.dve.sql.infoschema.persist.CatalogColumnEntity;
import com.tesora.dve.sql.schema.UnqualifiedName;

public class TextType extends SizedType {

	protected UnqualifiedName charset;
	protected UnqualifiedName collation;
	
	public TextType(NativeType nt, short flags, int size, UnqualifiedName charset, UnqualifiedName collation) {
		super(nt,flags,size);
		this.charset = charset;
		this.collation = collation;
	}
	
	@Override
	public UnqualifiedName getCharset() {
		return charset;
	}
	
	@Override
	public UnqualifiedName getCollation() {
		return collation;
	}

	public void setCollation(UnqualifiedName cs) {
		collation = cs;
	}
	
	public void setCharset(UnqualifiedName cs) {
		charset = cs;
	}
	
	@Override
	public Integer getIndexSize() {
		MysqlNativeType mnt = (MysqlNativeType) getBaseType();
		if (mnt == null) return null;
		switch(mnt.getMysqlType()) {
		case CHAR:
			// assuming utf8
			return getSize() * 3;
		case BINARY:
			return getSize();
		case VARCHAR:
		case VARBINARY:
			return getSize() + (getSize() > 255 ? 2 : 1);
		case TINYBLOB:
		case TINYTEXT:
		case BLOB:
		case TEXT:
		case MEDIUMBLOB:
		case MEDIUMTEXT:
		case LONGBLOB:
		case LONGTEXT:
			// note: this overestimates for all except longX
			return (getSize() + 4);
		default:
			return super.getIndexSize();
		}
	}

	public boolean hasSize() {
		// relying on the fact that text/blob has no default support
		return base.supportsDefaultValue();
	}

	@Override
	public void addColumnTypeModifiers(UserColumn uc) {
		super.addColumnTypeModifiers(uc);
		if (charset != null)
			uc.setCharset(charset.getSQL());
		if (collation != null)
			uc.setCollation(collation.getSQL());
	}
		
	@Override
	public void addColumnTypeModifiers(CatalogColumnEntity cce) throws PEException {
		super.addColumnTypeModifiers(cce);
		if (charset != null)
			cce.setCharset(charset.getSQL());
		if (collation != null)
			cce.setCollation(collation.getSQL());
	}
	
	public void makeBinaryText() {
		flags |= BINARY;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((charset == null) ? 0 : charset.hashCode());
		result = prime * result
				+ ((collation == null) ? 0 : collation.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TextType other = (TextType) obj;
		if (charset == null) {
			if (other.charset != null)
				return false;
		} else if (!charset.equals(other.charset))
			return false;
		if (collation == null) {
			if (other.collation != null)
				return false;
		} else if (!collation.equals(other.collation))
			return false;
		return true;
	}
	
}
