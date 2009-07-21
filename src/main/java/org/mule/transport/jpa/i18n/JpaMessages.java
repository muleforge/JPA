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

package org.mule.transport.jpa.i18n;

import org.mule.config.i18n.Message;
import org.mule.config.i18n.MessageFactory;
import org.mule.transport.jpa.JpaConnector;

/**
 * <code>JpaMessages</code> Todo Document
 */
public class JpaMessages extends MessageFactory
{
	private static final JpaMessages factory = new JpaMessages();
	
    private static final String BUNDLE_PATH = getBundlePath(JpaConnector.JPA);
    
    public static Message noReceiverForEndpoint(String name, Object uri)
    {
    	return factory.createMessage(BUNDLE_PATH, 1, name, uri);
    }
    
    public static Message moreThanOneMessageInTransaction(String property1, String property2)
    {
        return factory.createMessage(BUNDLE_PATH, 2, property1, property2);
    }
    
    public static Message forcePropertyNoTransaction(String property, String transction)
    {
        return factory.createMessage(BUNDLE_PATH, 3, property, transction);
    }
    
    public static Message forceProperty(String property1, String property2)
    {
        return factory.createMessage(BUNDLE_PATH, 4, property1, property2);
    }

}
