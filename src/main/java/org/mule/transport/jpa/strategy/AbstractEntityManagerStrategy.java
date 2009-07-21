package org.mule.transport.jpa.strategy;

import javax.persistence.EntityManager;

public abstract class AbstractEntityManagerStrategy implements EntityManagerStrategy
{
	protected EntityManager em;
	
	@Override
	public EntityManager getEntityManager()
	{
		return em;
	}

	@Override
	public void lease()
	{
    	if( em != null && em.isOpen() )
    	{
    		em.close();
    		em = null;
    	}
	}

}
