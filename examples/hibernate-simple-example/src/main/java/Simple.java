import org.mule.api.MuleEventContext;
import org.mule.api.lifecycle.Callable;

import org.mule.application.jpa.entity.Item;

public class Simple implements Callable {
	
	public Object onCall(MuleEventContext eventContext) throws Exception {
		try{
			Item item = (Item)eventContext.getMessage().getPayload();
			
			System.out.println("id: " + item.getId());
			System.out.print("price: " + item.getPrice());
			
			item.setPrice(10000);
			
			System.out.println(" -> " + item.getPrice());

			return item;
		}catch(Exception e){
			throw e;
		}
	}
}
