import org.mule.MuleServer;

public class RunMuleServer {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// String [] strs = {"-config", "mule-config-hello.xml"};
		// String [] strs = {"-config", "mule-config-json.xml"};
		// String [] strs = {"-config", "mule-config-test.xml"};

        System.out.println("arg length = "+args.length);

        
        String [] strs = new String[2];
        strs[0] = "-config";
		try {
            if (args.length == 0) {
                strs[1] = "mule-config.xml";
            } else if (args.length == 1) {
                strs[1] = args[0];
            } else {
                System.err.println("invalid parameter length");
                System.exit(1);
            }

            System.out.println("mule config file name = "+strs[1]);
            
            MuleServer.main(strs);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}
