package log4j;

import java.io.File;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

public class Log4j2Initializer implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		// TODO Auto-generated method stub
        // initialize log4j here
        ServletContext context = event.getServletContext();

        String log4jConfigFile = context.getInitParameter("log4j-config-location");
        // String log4jConfigFullPath = (context.getRealPath("") + File.separator + log4jConfigFile).replace("\\", File.separator).replace("/", File.separator);
        String log4jConfigFullPath = (context.getRealPath("") + log4jConfigFile).replace("\\", File.separator).replace("/", File.separator);

        // String contextPath = context.getContextPath();
        // System.setProperty("contextPath", contextPath);
        /*String log4jOutputFullPath = (context.getRealPath("") + File.separator + context.getInitParameter("log4j-output-log")).replace("\\", File.separator).replace("/", File.separator);*/
        String log4jOutputFullPath = (context.getRealPath("") + context.getInitParameter("log4j-output-log")).replace("\\", File.separator).replace("/", File.separator);
        System.setProperty("hvcLog4jFileOutputStream", log4jOutputFullPath);

        if (log4jConfigFullPath != null) {
        	/*From Log4J 2's documentation*/
        	// import org.apache.logging.log4j.core.LoggerContext;
        	LoggerContext loggerContext = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
        	File file = new File(log4jConfigFullPath);
        	// this will force a reconfiguration
        	loggerContext.setConfigLocation(file.toURI());        

            /*From Log4J 1's documentation*/
            // PropertyConfigurator.configure(fullPath);       	
        }

	}

}
