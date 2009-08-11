/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) Osaka Gas Information System Research Institute Co., Ltd.
 * All rights reserved.  http://www.ogis-ri.co.jp/
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.transport.jpa.config;

import org.mule.config.spring.factories.InboundEndpointFactoryBean;
import org.mule.config.spring.factories.OutboundEndpointFactoryBean;
import org.mule.config.spring.handlers.AbstractMuleNamespaceHandler;
import org.mule.config.spring.parsers.MuleDefinitionParser;
import org.mule.config.spring.parsers.collection.ChildSingletonMapDefinitionParser;
import org.mule.config.spring.parsers.delegate.ParentContextDefinitionParser;
import org.mule.config.spring.parsers.specific.endpoint.TransportEndpointDefinitionParser;
import org.mule.config.spring.parsers.specific.endpoint.TransportGlobalEndpointDefinitionParser;
import org.mule.config.spring.parsers.specific.properties.NestedMapDefinitionParser;
import org.mule.transport.jpa.JpaConnector;
import org.mule.endpoint.URIBuilder;

/**
 * <code>JpaNamespaceHandler</code> Todo Document
 */
public class JpaNamespaceHandler extends AbstractMuleNamespaceHandler
{
	public static final String QUERY_KEY = "queryKey";
	public static final String DISPATCHER_ACTION = "action";
	
    public void init()
    {
        registerConnectorDefinitionParser(JpaConnector.class);
        TransportEndpointDefinitionParser inboundParser =
        	new TransportEndpointDefinitionParser(JpaConnector.JPA, TransportEndpointDefinitionParser.PROTOCOL, InboundEndpointFactoryBean.class,
        		new String[]{ QUERY_KEY },
        		new String[] { JpaConnector.PROPERTY_POLLING_FREQUENCY }
        	);
        inboundParser.addAlias(QUERY_KEY, URIBuilder.HOST);
        registerBeanDefinitionParser("inbound-endpoint", inboundParser);
        
        TransportEndpointDefinitionParser outboundParser = 
        	new TransportEndpointDefinitionParser(JpaConnector.JPA, TransportEndpointDefinitionParser.PROTOCOL, OutboundEndpointFactoryBean.class,
        		new String[] { DISPATCHER_ACTION },
        		new String[] { }
        	);
        outboundParser.addAlias(DISPATCHER_ACTION, URIBuilder.HOST);
        registerBeanDefinitionParser("outbound-endpoint", outboundParser);
        
        TransportGlobalEndpointDefinitionParser endpointParser =
        	new TransportGlobalEndpointDefinitionParser(
        			JpaConnector.JPA, 
        			TransportEndpointDefinitionParser.PROTOCOL, 
        			TransportGlobalEndpointDefinitionParser.RESTRICTED_ENDPOINT_ATTRIBUTES, 
        			new String[][] {new String[]{QUERY_KEY}, new String[]{DISPATCHER_ACTION}},
	        		new String[][] {  }
        	);
        endpointParser.addAlias(QUERY_KEY, URIBuilder.HOST);
        endpointParser.addAlias(DISPATCHER_ACTION, URIBuilder.HOST);
        registerBeanDefinitionParser("endpoint", endpointParser);
        
        MuleDefinitionParser connectorQuery = new ChildSingletonMapDefinitionParser("query");
        MuleDefinitionParser endpointQuery = new NestedMapDefinitionParser("properties", "queries");
        endpointQuery.addCollection("properties");
        registerMuleBeanDefinitionParser("query", new ParentContextDefinitionParser("connector", connectorQuery).otherwise(endpointQuery));
//        registerBeanDefinitionParser("transaction", new TransactionDefinitionParser(JdbcTransactionFactory.class));
    }
}
