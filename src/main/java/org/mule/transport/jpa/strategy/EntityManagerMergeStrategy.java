package org.mule.transport.jpa.strategy;

import javax.persistence.EntityManager;

public class EntityManagerMergeStrategy extends AbstractEntityManagerStrategy
{
	
	public EntityManagerMergeStrategy(EntityManager em)
	{
		this.em = em;
	}

	public <T> T execute(T entity)
	{
		return em.merge(entity);
	}

}
