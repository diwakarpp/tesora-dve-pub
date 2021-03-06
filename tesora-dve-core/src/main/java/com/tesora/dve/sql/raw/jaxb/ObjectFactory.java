//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.11.23 at 05:12:12 PM EST 
//


package com.tesora.dve.sql.raw.jaxb;

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

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.tesora.dve.sql.raw.jaxb package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.tesora.dve.sql.raw.jaxb
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Rawplan }
     * 
     */
    public Rawplan createRawplan() {
        return new Rawplan();
    }

    /**
     * Create an instance of {@link ParameterType }
     * 
     */
    public ParameterType createParameterType() {
        return new ParameterType();
    }

    /**
     * Create an instance of {@link DynamicGroupType }
     * 
     */
    public DynamicGroupType createDynamicGroupType() {
        return new DynamicGroupType();
    }

    /**
     * Create an instance of {@link StepType }
     * 
     */
    public StepType createStepType() {
        return new StepType();
    }

    /**
     * Create an instance of {@link ProjectingStepType }
     * 
     */
    public ProjectingStepType createProjectingStepType() {
        return new ProjectingStepType();
    }

    /**
     * Create an instance of {@link TransactionStepType }
     * 
     */
    public TransactionStepType createTransactionStepType() {
        return new TransactionStepType();
    }

    /**
     * Create an instance of {@link DMLStepType }
     * 
     */
    public DMLStepType createDMLStepType() {
        return new DMLStepType();
    }

    /**
     * Create an instance of {@link TargetTableType }
     * 
     */
    public TargetTableType createTargetTableType() {
        return new TargetTableType();
    }

    /**
     * Create an instance of {@link DistKeyColumnValue }
     * 
     */
    public DistKeyColumnValue createDistKeyColumnValue() {
        return new DistKeyColumnValue();
    }

    /**
     * Create an instance of {@link KeyType }
     * 
     */
    public KeyType createKeyType() {
        return new KeyType();
    }

    /**
     * Create an instance of {@link UpdateStepType }
     * 
     */
    public UpdateStepType createUpdateStepType() {
        return new UpdateStepType();
    }

    /**
     * Create an instance of {@link DistVectColumn }
     * 
     */
    public DistVectColumn createDistVectColumn() {
        return new DistVectColumn();
    }

    /**
     * Create an instance of {@link DistributionType }
     * 
     */
    public DistributionType createDistributionType() {
        return new DistributionType();
    }

    /**
     * Create an instance of {@link DistKeyValue }
     * 
     */
    public DistKeyValue createDistKeyValue() {
        return new DistKeyValue();
    }

}
