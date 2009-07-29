package org.mule.transport.jpa.strategy;

import javax.persistence.EntityManager;

public abstract class AbstractEntityManagerStrategy implements EntityManagerStrategy
{
	protected EntityManager em;
	
	public EntityManager getEntityManager()
	{
		return em;
	}

	public void lease()
	{
    	if( em != null && em.isOpen() )
    	{
    		em.close();
    		em = null;
    	}
	}

}
