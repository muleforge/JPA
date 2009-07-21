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

import org.mule.DefaultMuleMessage;
import org.mule.api.MuleMessage;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.lifecycle.CreateException;
import org.mule.api.service.Service;
import org.mule.api.transaction.Transaction;
import org.mule.api.transport.Connector;
import org.mule.api.transport.MessageAdapter;
import org.mule.transaction.TransactionCoordination;
import org.mule.transaction.XaTransactionFactory;
import org.mule.transport.ConnectException;
import org.mule.transport.TransactedPollingMessageReceiver;
import org.mule.transport.jpa.i18n.JpaMessages;
import org.mule.util.MapUtils;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

/**
 * <code>JpaTransactedMessageReceiver</code> Todo Document
 *
 */
public class JpaTransactedMessageReceiver extends TransactedPollingMessageReceiver
{
	public static final String RECEIVE_MESSAGE_IN_TRANSACTION = "receiveMessageInTransaction";
	public static final String RECEIVE_MESSAGES_IN_XA_TRANSACTION = "receiveMessagesInXaTransaction";
	
	private JpaConnector jpaConnector;
	
	private long maxFetchSize;

    public boolean receiveMessagesInXaTransaction = false;
    private volatile boolean aggregateResult;
    
    public JpaTransactedMessageReceiver(Connector connector,
                                          Service service,
                                          final InboundEndpoint endpoint,
                                          long frequency,
                                          long maxFetchSize) throws CreateException
    {
        super(connector, service, endpoint);
        
        jpaConnector = (JpaConnector)connector;
        this.setFrequency(frequency);
        this.maxFetchSize = maxFetchSize;

        boolean transactedEndpoint = endpoint.getTransactionConfig().isTransacted();
        boolean xaTransactedEndpoint = (transactedEndpoint &&
            endpoint.getTransactionConfig().getFactory() instanceof XaTransactionFactory);
        
        boolean receiveMessageInTransaction = MapUtils.getBooleanValue(endpoint.getProperties(),
            RECEIVE_MESSAGE_IN_TRANSACTION, false);
        this.setReceiveMessagesInTransaction(receiveMessageInTransaction && transactedEndpoint);
        if (receiveMessageInTransaction && !transactedEndpoint)
        {
            logger.warn(JpaMessages.forcePropertyNoTransaction(RECEIVE_MESSAGE_IN_TRANSACTION, "transaction"));
            receiveMessageInTransaction = false;
        }
        
        receiveMessagesInXaTransaction = MapUtils.getBooleanValue(endpoint.getProperties(),
            RECEIVE_MESSAGES_IN_XA_TRANSACTION, false);
        if (receiveMessagesInXaTransaction && !receiveMessageInTransaction)
        {
            logger.warn(JpaMessages.forceProperty(RECEIVE_MESSAGES_IN_XA_TRANSACTION, RECEIVE_MESSAGE_IN_TRANSACTION));
            receiveMessagesInXaTransaction = false;
        }
        else if (receiveMessagesInXaTransaction && isReceiveMessagesInTransaction() && !xaTransactedEndpoint)
        {
            logger.warn(JpaMessages.forcePropertyNoTransaction(RECEIVE_MESSAGES_IN_XA_TRANSACTION, "XA transaction"));
            receiveMessagesInXaTransaction = false;
        }
    
    }
    
    protected List getMessages() throws Exception
    {
    	EntityManager em = null;
    	List resultSet = null;
    	
    	try
    	{
    		em = jpaConnector.getEntityManager();
	        if( !em.isOpen() )
	        {
	        	throw new Exception("Cannot open entityManager. ");
	        }
	        String queryStatement = this.jpaConnector.getStatement(this.getEndpoint());
	        Query query = em.createQuery(queryStatement);
	        
	        query.setMaxResults((int)this.maxFetchSize);
	        
	        logger.debug("SQL QUERY: " + queryStatement );
	        resultSet = query.getResultList();
	        if(resultSet != null && resultSet.size() > 1 && isReceiveMessagesInTransaction() && !receiveMessagesInXaTransaction)
	        {
                aggregateResult = true;
                logger.warn(JpaMessages.moreThanOneMessageInTransaction(RECEIVE_MESSAGE_IN_TRANSACTION, RECEIVE_MESSAGES_IN_XA_TRANSACTION));
                List singleResultList = new ArrayList(1);
                singleResultList.add(resultSet);
                return singleResultList;
	        }
	        
    	}
    	catch(Exception e)
    	{
    		this.jpaConnector.handleException(e);
    	}
    	finally
    	{
    		jpaConnector.closeEntityManager();
    	}
        return resultSet;
    }
    
    protected void processMessage(Object message) throws Exception
    {
    	Transaction tx = TransactionCoordination.getInstance().getTransaction();
    	try
    	{
	        MessageAdapter msgAdapter = connector.getMessageAdapter(message);
	        MuleMessage muleMessage = new DefaultMuleMessage(msgAdapter);
	        
	        // ack. statement
	        
	        routeMessage(muleMessage, endpoint.isSynchronous());
    	}
    	catch(Exception ex)
    	{
    		if(tx != null)
    		{
    			tx.setRollbackOnly();
    		}
    		throw ex;
    	}
    	finally
    	{
    		if(endpoint.getMuleContext().getTransactionManager() != null || tx == null)
    		{
    			//con close
    		}
    	}
    }

    public void doConnect() throws ConnectException
    {
        /* IMPLEMENTATION NOTE: Should make a connection to the underlying
           transport i.e. connect to a socket or register a soap service. When
           there is no connection to be made this method should be used to
           chack that resources are available. For example the
           FileMessageReceiver checks that the directories it will be using
           are available and readable. The MessageReceiver should remain in a
           'stopped' state even after the doConnect() method is called. This
           means that a connection has been made but no events will be
           received until the start() method is called. Calling start() on the
           MessageReceiver will call doConnect() if the receiver hasn't
           connected. */

        /* IMPLEMENTATION NOTE: If you need to spawn any threads such as
           worker threads for this receiver you can Schedule a worker thread
           with the work manager i.e.

             getWorkManager().scheduleWork(worker, WorkManager.INDEFINITE, null, null);
           Where 'worker' implemments javax.resource.spi.work.Work */

        /* IMPLEMENTATION NOTE: When throwing an exception from this method
           you need to throw a ConnectException that accepts a Message, a
           cause exception and a reference to this MessageReceiver i.e.

             throw new ConnectException(new Message(Messages.FAILED_TO_SCHEDULE_WORK), e, this);
        */

        // TODO the code necessay to Connect to the underlying resource
    }

    public void doDisconnect() throws ConnectException
    {
        /* IMPLEMENTATION NOTE: Disconnects and tidies up any rources allocted
           using the doConnect() method. This method should return the
           MessageReceiver into a disconnected state so that it can be
           connected again using the doConnect() method. */

        // TODO release any resources
    }
    
    public void doDispose()
    {
        // Optional; does not need to be implemented. Delete if not required

        /* IMPLEMENTATION NOTE: Is called when the Conector is being dispoed
           and should clean up any resources. The doStop() and doDisconnect()
           methods will be called implicitly when this method is called. */
    }

}
