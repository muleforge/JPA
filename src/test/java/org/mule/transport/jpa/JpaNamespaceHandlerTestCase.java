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

import org.mule.tck.FunctionalTestCase;
import org.mule.transport.jpa.JpaConnector;

/**
 * TODO
 */
public class JpaNamespaceHandlerTestCase extends FunctionalTestCase
{
    protected String getConfigResources()
    {
        //TODO You'll need to edit this file to configure the properties specific to your transport
        return "jpa-namespace-config.xml";
    }

    public void testHibernatejpatransportConfig() throws Exception
    {
        JpaConnector c = (JpaConnector) muleContext.getRegistry().lookupConnector("jpaConnector");
        assertNotNull(c);
        assertTrue(c.isConnected());
        assertTrue(c.isStarted());

        //TODO Assert specific properties are configured correctly


    }
}
