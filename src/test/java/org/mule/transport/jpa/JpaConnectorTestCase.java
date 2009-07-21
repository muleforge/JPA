/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSource, Inc.  All rights reserved.  http://www.mulesource.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */

package org.mule.transport.jpa;

import org.mule.DefaultMuleMessage;
import org.mule.api.transport.Connector;
import org.mule.transport.AbstractConnectorTestCase;
import org.mule.transport.jpa.JpaConnector;

public class JpaConnectorTestCase extends AbstractConnectorTestCase
{

    /* For general guidelines on writing transports see
       http://mule.mulesource.org/display/MULE/Writing+Transports */

    public Connector createConnector() throws Exception
    {
        /* IMPLEMENTATION NOTE: Create and initialise an instance of your
           connector here. Do not actually call the connect method. */

        JpaConnector c = new JpaConnector();
        c.setName("Test");
        // TODO Set any additional properties on the connector here
        return c;
    }

    public String getTestEndpointURI()
    {
    	return "jpa://k1";
    }

    public Object getValidMessage() throws Exception
    {
        JpaConnector c = new JpaConnector();
        c.setName("Test");
        
        return new DefaultMuleMessage("abc");
    }

    public void testProperties() throws Exception
    {
    }

}
