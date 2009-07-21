package org.mule.transport.jpa.strategy;

import javax.persistence.EntityManager;

public interface EntityManagerStrategy
{
	public <T> T execute(T entity);
	
	public EntityManager getEntityManager();
	
	public void lease();
}
