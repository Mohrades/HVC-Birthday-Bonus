package log4j;


import java.io.File;

/*import org.apache.log4j.PropertyConfigurator;*/
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

public class Log4jInitServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4163392871937850682L;

    public void init() {
    	String cheminWebApp = getServletContext().getRealPath("/");
        String cheminLogConfig = (cheminWebApp + getInitParameter("log4j-config-location")).replace("\\", File.separator).replace("/", File.separator);
        String cheminLog = (cheminWebApp + getInitParameter("log4j-output-log")).replace("\\", File.separator).replace("/", File.separator);
        // File logPathDir = new File( cheminLog );

        // définir une variable d'environnement qui sera utilisée par Log4J dans son fichier de configuration pour établir le
        // chemin du fichier journal utilisé
        System.setProperty("hvcLog4jFileOutputStream", cheminLog);

        if (cheminLogConfig != null) {
        	/*From Log4J 2's documentation*/
        	// import org.apache.logging.log4j.core.LoggerContext;
        	LoggerContext context = (org.apache.logging.log4j.core.LoggerContext) LogManager.getContext(false);
        	File file = new File(cheminLogConfig);
        	// this will force a reconfiguration
        	context.setConfigLocation(file.toURI());

        	/*From Log4J 1's documentation*/
        	// initialiser Log4J en utilisant un fichier de configuration
        	// PropertyConfigurator.configure(cheminLogConfig);
        	// Configurator.initialize("name", ConfigurationFactory.getInstance()., cheminLogConfig);
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) {
    	// String chemin = request.getSession().getServletContext().getRealPath("/") + "uploads\\" + nomDocument;
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) {
    	// String chemin = request.getServletContext().getRealPath("/") + "database\\journaux\\succes\\" + nomDocument;
    }

}
