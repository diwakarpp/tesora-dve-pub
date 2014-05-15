//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.11.01 at 06:28:31 PM EDT 
//


package com.tesora.dve.tools.analyzer.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for IndexesType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="IndexesType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="index" maxOccurs="unbounded" minOccurs="0">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="sequence" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *                 &lt;attribute name="column" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="nonUnique" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *                 &lt;attribute name="ascending" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *                 &lt;attribute name="type" use="required" type="{}IndexType" />
 *                 &lt;attribute name="cardinality" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
 *               &lt;/restriction>
 *             &lt;/complexContent>
 *           &lt;/complexType>
 *         &lt;/element>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IndexesType", propOrder = {
    "index"
})
public class IndexesType {

    protected List<IndexesType.Index> index;

    /**
     * Gets the value of the index property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the index property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getIndex().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link IndexesType.Index }
     * 
     * 
     */
    public List<IndexesType.Index> getIndex() {
        if (index == null) {
            index = new ArrayList<IndexesType.Index>();
        }
        return this.index;
    }


    /**
     * <p>Java class for anonymous complex type.
     * 
     * <p>The following schema fragment specifies the expected content contained within this class.
     * 
     * <pre>
     * &lt;complexType>
     *   &lt;complexContent>
     *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="sequence" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
     *       &lt;attribute name="column" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="nonUnique" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
     *       &lt;attribute name="ascending" use="required" type="{http://www.w3.org/2001/XMLSchema}boolean" />
     *       &lt;attribute name="type" use="required" type="{}IndexType" />
     *       &lt;attribute name="cardinality" use="required" type="{http://www.w3.org/2001/XMLSchema}int" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "")
    public static class Index {

        @XmlAttribute(name = "name", required = true)
        protected String name;
        @XmlAttribute(name = "sequence", required = true)
        protected int sequence;
        @XmlAttribute(name = "column", required = true)
        protected String column;
        @XmlAttribute(name = "nonUnique", required = true)
        protected boolean nonUnique;
        @XmlAttribute(name = "ascending", required = true)
        protected boolean ascending;
        @XmlAttribute(name = "type", required = true)
        protected IndexType type;
        @XmlAttribute(name = "cardinality", required = true)
        protected int cardinality;

        /**
         * Gets the value of the name property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getName() {
            return name;
        }

        /**
         * Sets the value of the name property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setName(String value) {
            this.name = value;
        }

        /**
         * Gets the value of the sequence property.
         * 
         */
        public int getSequence() {
            return sequence;
        }

        /**
         * Sets the value of the sequence property.
         * 
         */
        public void setSequence(int value) {
            this.sequence = value;
        }

        /**
         * Gets the value of the column property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getColumn() {
            return column;
        }

        /**
         * Sets the value of the column property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setColumn(String value) {
            this.column = value;
        }

        /**
         * Gets the value of the nonUnique property.
         * 
         */
        public boolean isNonUnique() {
            return nonUnique;
        }

        /**
         * Sets the value of the nonUnique property.
         * 
         */
        public void setNonUnique(boolean value) {
            this.nonUnique = value;
        }

        /**
         * Gets the value of the ascending property.
         * 
         */
        public boolean isAscending() {
            return ascending;
        }

        /**
         * Sets the value of the ascending property.
         * 
         */
        public void setAscending(boolean value) {
            this.ascending = value;
        }

        /**
         * Gets the value of the type property.
         * 
         * @return
         *     possible object is
         *     {@link IndexType }
         *     
         */
        public IndexType getType() {
            return type;
        }

        /**
         * Sets the value of the type property.
         * 
         * @param value
         *     allowed object is
         *     {@link IndexType }
         *     
         */
        public void setType(IndexType value) {
            this.type = value;
        }

        /**
         * Gets the value of the cardinality property.
         * 
         */
        public int getCardinality() {
            return cardinality;
        }

        /**
         * Sets the value of the cardinality property.
         * 
         */
        public void setCardinality(int value) {
            this.cardinality = value;
        }

    }

}
