//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.08.21 at 05:55:21 PM EDT 
//


package com.tesora.dve.sql.raw.jaxb;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for LiteralType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="LiteralType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="string"/>
 *     &lt;enumeration value="integral"/>
 *     &lt;enumeration value="decimal"/>
 *     &lt;enumeration value="hex"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "LiteralType")
@XmlEnum
public enum LiteralType {

    @XmlEnumValue("string")
    STRING("string"),
    @XmlEnumValue("integral")
    INTEGRAL("integral"),
    @XmlEnumValue("decimal")
    DECIMAL("decimal"),
    @XmlEnumValue("hex")
    HEX("hex");
    private final String value;

    LiteralType(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static LiteralType fromValue(String v) {
        for (LiteralType c: LiteralType.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
