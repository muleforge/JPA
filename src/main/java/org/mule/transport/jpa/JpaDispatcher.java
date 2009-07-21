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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.LockModeType;
import javax.persistence.Query;

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleEvent;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.OutboundEndpoint;
import org.mule.api.transaction.Transaction;
import org.mule.api.transformer.TransformerException;
import org.mule.transaction.TransactionCoordination;
import org.mule.transport.AbstractMessageDispatcher;
import org.mule.transport.jpa.strategy.EntityManagerStrategy;
import org.mule.transport.jpa.strategy.EntityManagerStrategyFactory;

/**
 * <code>JpaDispatcher</code> Todo Document
 */
public class JpaDispatcher extends AbstractMessageDispatcher
{
	protected JpaConnector jpaConnector;

	/**
	 * @param endpoint
	 */
	public JpaDispatcher(OutboundEndpoint endpoint)
	{
		super(endpoint);
		jpaConnector = (JpaConnector)endpoint.getConnector();
	}

	/* (non-Javadoc)
	 * @see org.mule.transport.AbstractMessageDispatcher#doDispatch(org.mule.api.MuleEvent)
	 */
	protected void doDispatch(MuleEvent event) throws Exception
	{
		logger.debug("Dispatch event: " + event);
		
		doSend(event);
	}

	/* (non-Javadoc)
	 * @see org.mule.transport.AbstractMessageDispatcher#doSend(org.mule.api.MuleEvent)
	 */
	protected MuleMessage doSend(MuleEvent event) throws Exception
	{
		MuleMessage msg = null;
		String address = endpoint.getEndpointURI().getAddress();
		if( EntityManagerStrategyFactory.isValidReceiveAction(address) )
		{
			msg = doExecute(event, EntityManagerStrategyFactory.createEntityManagerCommand(jpaConnector.getEntityManager(), address));
		}
		else
		{
			msg = doQuery(event);
		}
		return msg;
	}

	/**
	 * merge to entitymanager.
	 * 
	 * @param event
	 * @return muleMessage
	 * @throws TransformerException
	 * @throws Exception
	 */
	private MuleMessage doExecute(MuleEvent event, EntityManagerStrategy emStrategy)
			throws TransformerException, Exception
	{
		EntityManager em = null;
		EntityTransaction etx = null;
		
		synchronized (this)
		{
			Object payload = event.transformMessage();
			Transaction tx = TransactionCoordination.getInstance().getTransaction();
			
			try
			{
				em = emStrategy.getEntityManager();
				
				if( tx == null )
					etx = em.getTransaction();
//			em.persist(event.getMessage().getPayload());
				if(!em.isOpen())
				{
					throw new Exception("Cannot open entityManager."); 
				}
				
				if( etx!= null && !etx.isActive() )
				{
					etx.begin();
				}
				
				Object result = null;
				if(payload instanceof List)
				{
					logger.debug("payload type is list.");
					List payloadList = (List)payload;
					List resultList = new ArrayList();
					for(Iterator payloadIter = payloadList.iterator(); payloadIter.hasNext();)
					{
						Object entity = payloadIter.next();
						
						if( etx!= null && !etx.isActive() )
						{
							em.lock(entity, LockModeType.WRITE);
						}
						
						logger.debug("entity is " + entity.getClass() + ".");
						Object iterResult = emStrategy.execute(entity);
						resultList.add(iterResult);
					}
					result = resultList;
				}
				else
				{
					if( etx!= null && !etx.isActive() )
					{
						em.lock(payload, LockModeType.WRITE);
					}
					
					logger.debug("entity is " + payload.getClass() + ".");
					result = emStrategy.execute(payload);
				}
				if( etx!= null && etx.isActive() )
				{
					etx.commit();
				}
				
				event.getMessage().setPayload(result);
				return event.getMessage();
			}
			catch(Exception e)
			{
				if( tx == null && etx!= null && etx.isActive() )
				{
					etx.rollback();
				}
				handleException(e);
			}
			finally
			{
				emStrategy.lease();
			}
		}
		return null;
	}

	/**
	 * query to entityManager.
	 * 
	 * @param event
	 * @return muleMessage
	 * @throws TransformerException
	 */
	private MuleMessage doQuery(MuleEvent event) throws TransformerException
	{
		EntityManager em = null;
		EntityTransaction etx = null;
		Object payload = event.transformMessage();
		try
		{
			Transaction tx = TransactionCoordination.getInstance().getTransaction();
			
			em = jpaConnector.getEntityManager();
			if( tx == null )
			{
				etx = em.getTransaction();
			}
			if(!em.isOpen())
			{
				throw new Exception("Cannot open entityManager.");
			}
			
			List resultSet = null;
		    Query query = em.createQuery(this.jpaConnector.getStatement(getEndpoint()));
		    
		    if(payload instanceof Map)
		    {
		    	Map payloadMap = (Map)payload;
		    	
		    	for( Object key : payloadMap.keySet() )
		    	{
		    		Object value = payloadMap.get(key);
		    		
		    		logger.debug("Param " + key + ": " + value + "(" + value.getClass() + ")");
		    		query.setParameter((String) key, value);
		    	}
		    }
		    
			if( etx!= null && !etx.isActive() )
			{
				etx.begin();
			}

		    resultSet = query.getResultList();
			
			if( etx!= null && etx.isActive() )
			{
				etx.rollback();
			}
			
			return new DefaultMuleMessage(resultSet);
		}
		catch(Exception e)
		{
			handleException(e);
		}
		finally
		{
			jpaConnector.closeEntityManager();
		}
		return null;
	}

}
