package org.mule.transport.jpa.strategy;

import javax.persistence.EntityManager;

public class EntityManagerDeleteStrategy extends AbstractEntityManagerStrategy
{

	public EntityManagerDeleteStrategy(EntityManager em)
	{
		this.em = em;
	}
	
	public <T> T execute(T entity)
	{
		em.remove(entity);
		return null;
	}

}
