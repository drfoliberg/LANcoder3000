package main.java.drfoliberg.master.api.web;

import main.java.drfoliberg.common.Service;
import main.java.drfoliberg.master.Master;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.server.handler.ResourceHandler;

public class ApiServer extends Service {

	private static final String webDir = "main/web";

	private Master master;
	Server server;

	public ApiServer(Master master) {
		this.master = master;
	}

	@Override
	public void run() {
		server = new Server(master.getConfig().getApiServerPort());

		ContextHandler ctxStatic = new ContextHandler("/");
		ContextHandler ctxApi = new ContextHandler("/api");

		// static resources handler
		ResourceHandler staticHandler = new ResourceHandler();
		staticHandler.setResourceBase(this.getClass().getClassLoader().getResource(webDir).toExternalForm());
		staticHandler.setDirectoriesListed(true);
		ctxStatic.setHandler(staticHandler);

		// api handler
		ApiHandler apiHandler = new ApiHandler(master);
		ctxApi.setHandler(apiHandler);

		ContextHandlerCollection contexts = new ContextHandlerCollection();
		contexts.setHandlers(new Handler[] { ctxStatic, ctxApi });

		server.setHandler(contexts);

		try {
			server.start();
			server.join();
		} catch (Exception e) {
			// TODO alert master api server api crashed
			e.printStackTrace();
		}
	}

	@Override
	public void stop() {
		super.stop();
		try {
			server.stop();
			System.out.println("MASTER: Closed api server");
		} catch (Exception e) {
			System.err.println("MASTER: Could not close api server !");
			e.printStackTrace();
		}
	}

}