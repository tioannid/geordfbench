package gr.uoa.di.rdf.Geographica.strabon;

import gr.uoa.di.rdf.Geographica.systemsundertest.RunSystemUnderTest;

import org.apache.log4j.Logger;

public class RunStrabon extends RunSystemUnderTest {

	private static Logger logger = Logger.getLogger(RunStrabon.class.getSimpleName());
	
	@Override
	protected void addOptions() {
		super.addOptions();

		options.addOption("h", "host", true, "Server");
		options.addOption("db", "database", true, "Database");
		options.addOption("p", "port", true, "Port");
		options.addOption("u", "username", true, "Username");
		options.addOption("P", "password", true, "Password");
	}

	@Override
	protected void logOptions() {
		super.logOptions();
		
		logger.info("Excluded options");
		logger.info("Server:\t"+cmd.getOptionValue("host"));
		logger.info("Database:\t"+cmd.getOptionValue("database"));
		logger.info("Port:\t"+cmd.getOptionValue("port"));
		logger.info("Username:\t"+cmd.getOptionValue("username"));
		logger.info("Password:\t"+cmd.getOptionValue("password"));
	}

	
	protected void initSystemUnderTest() throws Exception {
		String host = (cmd.getOptionValue("host")!=null?cmd.getOptionValue("host"):"localhost");
		String db = cmd.getOptionValue("database");
		int port = (cmd.getOptionValue("port")!=null?Integer.parseInt(cmd.getOptionValue("port")):1521);		
		String user = cmd.getOptionValue("username");
		String password = cmd.getOptionValue("password");

		sut = new StrabonSUT(db, user, password, port, host);		
	}

	public static void main(String[] args) throws Exception {
		RunSystemUnderTest runStrabon = new RunStrabon();
		
		runStrabon.run(args);
	}
}
