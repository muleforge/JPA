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

package org.mule.transport.jpa;

import java.util.Map;
import java.util.regex.Pattern;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.mule.transaction.TransactionCoordination;
import org.mule.transaction.XaTransaction;
import org.mule.transaction.XaTransaction.MuleXaObject;
import org.mule.transport.AbstractConnector;
import org.mule.util.StringUtils;
import org.mule.util.TemplateParser;
import org.mule.api.MuleException;
import org.mule.api.endpoint.ImmutableEndpoint;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.lifecycle.InitialisationException;
import org.mule.api.service.Service;
import org.mule.api.transaction.Transaction;
import org.mule.api.transaction.TransactionException;

/**
 * <code>JpaConnector</code> TODO document
 */
public class JpaConnector extends AbstractConnector
{
    /* This constant defines the main transport protocol identifier */
    public static final String JPA = "jpa";

    public static final String PROPERTY_QUERY_KEY = "queryKey";
    public static final String PROPERTY_POLLING_FREQUENCY = "pollingFrequency";
    public static final String PROPERTY_MAX_FETCH_SIZE = "maxFetchSize";
    public static final long DEFAULT_POLLING_FREQUENCY = 1000;
    public static final long DEFAULT_MAX_FETCH_SIZE = 1000;

    private static final Pattern STATEMENT_ARGS = TemplateParser.WIGGLY_MULE_TEMPLATE_PATTERN;

    protected long pollingFrequency = 0;
    protected long maxFetchSize;
    protected String persistenceUnit = null;
    protected Map queries;

    private EntityManagerFactory emf;
    private EntityManager em;

    public void doInitialise() throws InitialisationException
    {
        createMultipleTransactedReceivers = false;
    	
    	emf = Persistence.createEntityManagerFactory(this.persistenceUnit);
    }
    
    protected org.mule.api.transport.MessageReceiver createReceiver(Service service, InboundEndpoint endpoint) throws Exception 
    {
        Map props = endpoint.getProperties();
        if (props != null)
        {
            String tempPolling = (String) props.get(PROPERTY_POLLING_FREQUENCY);
            if (tempPolling != null)
            {
                pollingFrequency = Long.parseLong(tempPolling);
            }
            String tempMaxFetchSize = (String)props.get(PROPERTY_MAX_FETCH_SIZE);
            if( tempMaxFetchSize != null )
            {
            	maxFetchSize = Long.parseLong(tempMaxFetchSize);
            }
        }

        if (pollingFrequency <= 0)
        {
            pollingFrequency = DEFAULT_POLLING_FREQUENCY;
        }
        if (maxFetchSize <= 0)
        {
        	maxFetchSize = DEFAULT_MAX_FETCH_SIZE;
        }
        
        return getServiceDescriptor().createMessageReceiver(this, service, endpoint, new Object[]{ new Long(pollingFrequency), new Long(maxFetchSize) });
    }

    public void doConnect() throws Exception
    {
    }

    public void doDisconnect() throws Exception
    {
    }

    public void doStart() throws MuleException
    {
    }

    public void doStop() throws MuleException
    {
    }

    public void doDispose()
    {
    	if(em != null)
	    	em.close();
    	if(emf != null)
	    	emf.close();
    }

    public String getProtocol()
    {
        return JPA;
    }
    
    public EntityManager getEntityManager()
    {
        Transaction tx = TransactionCoordination.getInstance().getTransaction();
        if (tx != null)
        {
            if (tx.hasResource(em))
            {
                logger.debug("Retrieving session from current transaction");
                Object r = tx.getResource(em);
                if (r instanceof MuleXaSession) 
                	return ((MuleXaSession) r).impl;
                else
                	return (EntityManager) r;
            }
        }
    	if( em == null || em.isOpen() == false )
    	{
	    	em = emf.createEntityManager();
    	}
        if (tx != null)
        {
            logger.debug("Binding session to current transaction");
            try
            {
            	Object r;
            	if (tx instanceof XaTransaction) 
            		r = new MuleXaSession(em);
            	else
            		r = em;
            	
            	tx.bindResource(em, r);
            }
            catch (TransactionException e)
            {
                throw new RuntimeException("Could not bind connection to current transaction", e);
            }
        }
    	return em;
    }
    
    public void closeEntityManager()
    {
    	if( em != null && em.isOpen() )
    	{
    		em.close();
    		em = null;
    	}
    }
    
    /**
	 * @return the queries
	 */
	public Map getQueries()
	{
		return queries;
	}

	/**
	 * @param queries the queries to set
	 */
	public void setQueries(Map queries)
	{
		this.queries = queries;
	}

	/**
	 * @return the pollingFrequency
	 */
	public long getPollingFrequency()
	{
		return pollingFrequency;
	}

	/**
	 * @param pollingFrequency the pollingFrequency to set
	 */
	public void setPollingFrequency(long pollingFrequency)
	{
		this.pollingFrequency = pollingFrequency;
	}

	/**
	 * @return the maxFetchSize
	 */
	public long getMaxFetchSize()
	{
		return maxFetchSize;
	}

	/**
	 * @param maxFetchSize the maxFetchSize to set
	 */
	public void setMaxFetchSize(long maxFetchSize)
	{
		this.maxFetchSize = maxFetchSize;
	}

	/**
	 * @return the persistenceUnit
	 */
	public String getPersistenceUnit()
	{
		return persistenceUnit;
	}

	/**
	 * @param persistenceUnit the persistenceUnit to set
	 */
	public void setPersistenceUnit(String persistenceUnit)
	{
		this.persistenceUnit = persistenceUnit;
	}

	public String getQuery(ImmutableEndpoint endpoint, String stmtKey)
    {
        Object query = null;
        if (endpoint != null && endpoint.getProperties() != null)
        {
            Object queries = endpoint.getProperties().get("queries");
            if (queries instanceof Map)
            {
                query = ((Map) queries).get(stmtKey);
            }
        }
        if (query == null)
        {
            if (this.queries != null)
            {
                query = this.queries.get(stmtKey);
            }
        }
        return query == null ? null : query.toString();
    }

    public String getStatement(ImmutableEndpoint endpoint)
    {
        String writeStmt = endpoint.getEndpointURI().getAddress();
        String str;
        if ((str = getQuery(endpoint, writeStmt)) != null)
        {
            writeStmt = str;
        }
        writeStmt = StringUtils.trimToEmpty(writeStmt);
        if (StringUtils.isBlank(writeStmt))
        {
            throw new IllegalArgumentException("Missing statement");
        }

        return writeStmt;
    }
    
	private class MuleXaSession implements MuleXaObject
	{
		private EntityManager impl;
		
		MuleXaSession(EntityManager em) { this.impl = em; }

		public void close() throws Exception
		{
	    	if( impl != null && impl.isOpen() )
	    	{
	    		impl.close();
	    	}
		}
		
		public boolean enlist() throws TransactionException
		{
			return true;
		}

		public boolean delist() throws Exception
		{
			return true;
		}

		public Object getTargetObject()
		{
			return impl;
		}

		public boolean isReuseObject()
		{
			return false;
		}

		public void setReuseObject(boolean reuseObject)
		{
		}

	}
}
