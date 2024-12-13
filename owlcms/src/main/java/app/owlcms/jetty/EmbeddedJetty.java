package app.owlcms.jetty;

import java.util.concurrent.CountDownLatch;

import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.slf4j.LoggerFactory;

import com.vaadin.open.Open;
import com.vaadin.open.Options;

import app.owlcms.apputils.LogbackConfigReloader;
import ch.qos.logback.classic.Logger;

public class EmbeddedJetty extends com.github.mvysny.vaadinboot.VaadinBoot {

	private static Logger startLogger = (Logger) LoggerFactory.getLogger(EmbeddedJetty.class);
	private static EmbeddedJetty server;
	private Runnable initConfig;
	private Runnable initData;
	private CountDownLatch latch;
	
	Logger logger = (Logger) LoggerFactory.getLogger(EmbeddedJetty.class);

	public EmbeddedJetty(CountDownLatch countDownLatch, String appName) {
		this.setLatch(countDownLatch);
		this.setAppName(appName);
	}

	public CountDownLatch getLatch() {
		return latch;
	}

	public void run(Integer serverPort, String string) throws Exception {
		this.setPort(serverPort);
		this.run();
		initConfig.run();
		initData.run();
	}

	public EmbeddedJetty setInitConfig(Runnable initConfig) {
		this.initConfig = initConfig;
		return this;
	}

	public EmbeddedJetty setInitData(Runnable initData) {
		this.initData = initData;
		return this;
	}

	public void setLatch(CountDownLatch latch) {
		this.latch = latch;
	}

	public EmbeddedJetty setStartLogger(Logger startLogger) {
		EmbeddedJetty.startLogger = startLogger;
		return this;
	}

	@Override
	public void onStarted(WebAppContext c) {
		startLogger.info("started on port {}", this.getPort());
	}

    @Override
	public void run() throws Exception {
    	server = this;
        start();        

        // this gets called both when CTRL+C is pressed, and when main() terminates.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> stop("Shutdown hook called, shutting down")));
        startLogger.info("Press CTRL+C to shutdown");

        //Open.open(getServerURL());
        
		new Thread(() -> {
			logger.info("Starting browser");
			Options openOptions = new Options();
			openOptions.setNewInstance(true);
			openOptions.setBackground(true);
			openOptions.setWait(false);
			Open.open(getServerURL(), openOptions);
			logger.info("Browser started");
		}).start();

    }
    
    public static void stop(boolean restart) {
    	if (server != null) {
    		server.stop(restart ? "stopping prior to restart." : "intentional stop.");
    	}
    }
    
    public static void restart() {
    	if (server != null) {
    		server.stop("stopping for restart");
    	}
    	try {
    		LogbackConfigReloader.reloadLogbackConfiguration();
    		startLogger.info("restarting.");
    		server.run();
		} catch (Exception e) {
			e.printStackTrace();
		}
    }

}
