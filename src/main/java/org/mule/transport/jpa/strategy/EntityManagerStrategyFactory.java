package org.mule.transport.jpa.strategy;

import java.util.EnumSet;

import javax.persistence.EntityManager;

public class EntityManagerStrategyFactory
{
	private enum ReceiveAction
	{
		DELETE,
		MERGE,
		PERSIST;
	}
	
	private static EnumSet<ReceiveAction> receiveActions = EnumSet.of(ReceiveAction.DELETE, ReceiveAction.MERGE, ReceiveAction.PERSIST);
	
	public static boolean isValidReceiveAction(String action)
	{
		boolean isMatch = false;
		for(ReceiveAction receiveAction : receiveActions) {
			if( receiveAction.name().equalsIgnoreCase(action) ) {
				isMatch = true;
				break;
			}
		}
		return isMatch;
	}
	
	private EntityManagerStrategyFactory()
	{
	}
	
	public static EntityManagerStrategy createEntityManagerCommand(EntityManager em, String queryType) throws Exception
	{
		EntityManagerStrategy emcmd = null;
		if(queryType.equalsIgnoreCase(ReceiveAction.DELETE.name()))
		{
			emcmd = new EntityManagerDeleteStrategy(em);
		}
		else if(queryType.equalsIgnoreCase(ReceiveAction.MERGE.name()))
		{
			emcmd = new EntityManagerMergeStrategy(em);
		}
		else if(queryType.equalsIgnoreCase(ReceiveAction.PERSIST.name()))
		{
			emcmd = new EntityManagerPersistStrategy(em);
		}
		else
		{
			throw new Exception("invalid request to EntityManager. ");
		}
		return emcmd;
	}
}
