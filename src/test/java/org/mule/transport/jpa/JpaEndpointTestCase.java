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

import org.mule.api.endpoint.EndpointURI;
import org.mule.endpoint.MuleEndpointURI;
import org.mule.tck.AbstractMuleTestCase;

public class JpaEndpointTestCase extends AbstractMuleTestCase
{

    /* For general guidelines on writing transports see
       http://mule.mulesource.org/display/MULE/Writing+Transports */

    public void testValidEndpointURI() throws Exception
    {
        // TODO test creating and asserting Endpoint values eg

        EndpointURI url = new MuleEndpointURI("tcp://localhost:7856");
        assertEquals("tcp", url.getScheme());
        //assertEquals("tcp://localhost:7856", url.getAddress());
        //assertNull(url.getEndpointName());
        assertEquals(7856, url.getPort());
        assertEquals("localhost", url.getHost());
        //assertEquals("tcp://localhost:7856", url.getAddress());
        assertEquals(0, url.getParams().size());
        
    }

}
