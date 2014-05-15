//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2013.11.14 at 11:27:14 AM EST 
//


package com.tesora.dve.tools.jaxb.persistent;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PersistentSiteCfgList complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="PersistentSiteCfgList">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="site" maxOccurs="unbounded">
 *           &lt;complexType>
 *             &lt;complexContent>
 *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                 &lt;sequence>
 *                   &lt;element name="siteInstance" maxOccurs="unbounded">
 *                     &lt;complexType>
 *                       &lt;complexContent>
 *                         &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *                           &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="url" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="user" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="password" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                           &lt;attribute name="enabled" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *                           &lt;attribute name="master" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *                         &lt;/restriction>
 *                       &lt;/complexContent>
 *                     &lt;/complexType>
 *                   &lt;/element>
 *                 &lt;/sequence>
 *                 &lt;attribute name="haMode" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
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
@XmlType(name = "PersistentSiteCfgList", propOrder = {
    "site"
})
public class PersistentSiteCfgList {

    @XmlElement(required = true)
    protected List<PersistentSiteCfgList.Site> site;

    /**
     * Gets the value of the site property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the site property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSite().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link PersistentSiteCfgList.Site }
     * 
     * 
     */
    public List<PersistentSiteCfgList.Site> getSite() {
        if (site == null) {
            site = new ArrayList<PersistentSiteCfgList.Site>();
        }
        return this.site;
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
     *       &lt;sequence>
     *         &lt;element name="siteInstance" maxOccurs="unbounded">
     *           &lt;complexType>
     *             &lt;complexContent>
     *               &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
     *                 &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="url" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="user" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="password" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *                 &lt;attribute name="enabled" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
     *                 &lt;attribute name="master" type="{http://www.w3.org/2001/XMLSchema}boolean" />
     *               &lt;/restriction>
     *             &lt;/complexContent>
     *           &lt;/complexType>
     *         &lt;/element>
     *       &lt;/sequence>
     *       &lt;attribute name="haMode" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
     *     &lt;/restriction>
     *   &lt;/complexContent>
     * &lt;/complexType>
     * </pre>
     * 
     * 
     */
    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = {
        "siteInstance"
    })
    public static class Site {

        @XmlElement(required = true)
        protected List<PersistentSiteCfgList.Site.SiteInstance> siteInstance;
        @XmlAttribute(name = "haMode", required = true)
        protected String haMode;
        @XmlAttribute(name = "name", required = true)
        protected String name;

        /**
         * Gets the value of the siteInstance property.
         * 
         * <p>
         * This accessor method returns a reference to the live list,
         * not a snapshot. Therefore any modification you make to the
         * returned list will be present inside the JAXB object.
         * This is why there is not a <CODE>set</CODE> method for the siteInstance property.
         * 
         * <p>
         * For example, to add a new item, do as follows:
         * <pre>
         *    getSiteInstance().add(newItem);
         * </pre>
         * 
         * 
         * <p>
         * Objects of the following type(s) are allowed in the list
         * {@link PersistentSiteCfgList.Site.SiteInstance }
         * 
         * 
         */
        public List<PersistentSiteCfgList.Site.SiteInstance> getSiteInstance() {
            if (siteInstance == null) {
                siteInstance = new ArrayList<PersistentSiteCfgList.Site.SiteInstance>();
            }
            return this.siteInstance;
        }

        /**
         * Gets the value of the haMode property.
         * 
         * @return
         *     possible object is
         *     {@link String }
         *     
         */
        public String getHaMode() {
            return haMode;
        }

        /**
         * Sets the value of the haMode property.
         * 
         * @param value
         *     allowed object is
         *     {@link String }
         *     
         */
        public void setHaMode(String value) {
            this.haMode = value;
        }

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
         * <p>Java class for anonymous complex type.
         * 
         * <p>The following schema fragment specifies the expected content contained within this class.
         * 
         * <pre>
         * &lt;complexType>
         *   &lt;complexContent>
         *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
         *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="url" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="user" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="password" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
         *       &lt;attribute name="enabled" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
         *       &lt;attribute name="master" type="{http://www.w3.org/2001/XMLSchema}boolean" />
         *     &lt;/restriction>
         *   &lt;/complexContent>
         * &lt;/complexType>
         * </pre>
         * 
         * 
         */
        @XmlAccessorType(XmlAccessType.FIELD)
        @XmlType(name = "")
        public static class SiteInstance {

            @XmlAttribute(name = "name", required = true)
            protected String name;
            @XmlAttribute(name = "url", required = true)
            protected String url;
            @XmlAttribute(name = "user", required = true)
            protected String user;
            @XmlAttribute(name = "password", required = true)
            protected String password;
            @XmlAttribute(name = "enabled")
            protected Boolean enabled;
            @XmlAttribute(name = "master")
            protected Boolean master;

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
             * Gets the value of the url property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getUrl() {
                return url;
            }

            /**
             * Sets the value of the url property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setUrl(String value) {
                this.url = value;
            }

            /**
             * Gets the value of the user property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getUser() {
                return user;
            }

            /**
             * Sets the value of the user property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setUser(String value) {
                this.user = value;
            }

            /**
             * Gets the value of the password property.
             * 
             * @return
             *     possible object is
             *     {@link String }
             *     
             */
            public String getPassword() {
                return password;
            }

            /**
             * Sets the value of the password property.
             * 
             * @param value
             *     allowed object is
             *     {@link String }
             *     
             */
            public void setPassword(String value) {
                this.password = value;
            }

            /**
             * Gets the value of the enabled property.
             * 
             * @return
             *     possible object is
             *     {@link Boolean }
             *     
             */
            public boolean isEnabled() {
                if (enabled == null) {
                    return true;
                } else {
                    return enabled;
                }
            }

            /**
             * Sets the value of the enabled property.
             * 
             * @param value
             *     allowed object is
             *     {@link Boolean }
             *     
             */
            public void setEnabled(Boolean value) {
                this.enabled = value;
            }

            /**
             * Gets the value of the master property.
             * 
             * @return
             *     possible object is
             *     {@link Boolean }
             *     
             */
            public Boolean isMaster() {
                return master;
            }

            /**
             * Sets the value of the master property.
             * 
             * @param value
             *     allowed object is
             *     {@link Boolean }
             *     
             */
            public void setMaster(Boolean value) {
                this.master = value;
            }

        }

    }

}
