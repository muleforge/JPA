package org.mule.transport.jpa.strategy;

import javax.persistence.EntityManager;

public class EntityManagerPersistStrategy extends AbstractEntityManagerStrategy
{
	
	public EntityManagerPersistStrategy(EntityManager em)
	{
		this.em = em;
	}

	public <T> T execute(T entity)
	{
		em.persist(entity);
		return null;
	}

}
