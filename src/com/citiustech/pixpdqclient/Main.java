/*
 * This is a test client for sending SSL message for the PIX and PDQ
 */
package com.citiustech.pixpdqclient;

import com.misyshealthcare.connect.net.SecureConnection;
import com.misyshealthcare.connect.net.SecureConnectionDescription;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author <a href="mailto:abhijeet.limkar@citiustech.com">Abhijeet Limkar</a>
 */
public class Main {

    private static final String STARTOFENCODING = "\013";
    private static final String ENDOFENCODING = "\034";
    private static final Logger log = Logger.getLogger(Main.class.getName());
    private String host;
    private int port;
    private String trustStore;
    private String trustStorePass;
    private String keyStore;
    private String keyStorePass;
    private String file;

    /**
     * Constructor for initialization of parameters from the property file
     */
    private Main() {

        Properties tProps = new Properties();

        InputStream is = null;
        try {
            is = new FileInputStream(new File("conf/client.properties"));
            tProps.load(is);
            is.close();
        } catch (FileNotFoundException ex) {
            log.error("FileNotFoundException: " + ex.getMessage());
        } catch (IOException ex) {
            log.error("IOException: " + ex.getMessage());
        }

        host = tProps.getProperty("host");
        port = Integer.parseInt(tProps.getProperty("port"));
        trustStore = tProps.getProperty("trustStore");
        trustStorePass = tProps.getProperty("trustStorePass");
        keyStore = tProps.getProperty("keyStore");
        keyStorePass = tProps.getProperty("keyStorePass");
        file = tProps.getProperty("file");
    }

    /**
     * This methos is used for getting the descriptor for the connection
     * @return SecureConnectionDescription
     */
    private SecureConnectionDescription getDescription() {

        SecureConnectionDescription scd;
        scd = new SecureConnectionDescription();
        scd.setHostname(host);
        scd.setPort(port);
        scd.setTrustStore(trustStore);
        scd.setKeyStore(keyStore);
        scd.setTrustStorePassword(trustStorePass);
        scd.setKeyStorePassword(keyStorePass);
        scd.complete();

        log.info("Opening Connecion To Host " + host + "[" + port + "]");

        return scd;
    }

    /**
     * This method reads the response from the server and logs to file
     * @param conn
     */
    private void reciveResponse(SecureConnection conn) throws Exception {

        try {
            /* read response */
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));

            String inputLine;
            log.info("Reading Server Response ");
            while ((inputLine = in.readLine()) != null) {
                log.info(inputLine);
                if (inputLine.contains(ENDOFENCODING)) {
                    break;
                }
            }
            log.info("Server Response Finished");

        } catch (Exception ex) {
            log.error("Unable To Read From The Host", ex);
			throw ex;
        }

    }

    /**
     * This method reads the given file and send message to the server
     * @param conn
     */
    private void sendMessage(SecureConnection conn) throws Exception {
        PrintWriter out = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(conn.getOutputStream())));
        out.write(STARTOFENCODING);

        BufferedReader input = null;
        try {
            input = new BufferedReader(new FileReader(file));

            String line = null; //not declared within while loop
            log.info("Sending Message To Server");
            while ((line = input.readLine()) != null) {
                log.info(line);
                out.println(line);
            }
            log.info("Message Sent Completed");
        } catch (Exception ex) {
            log.error("Error While Sending message", ex);
			throw ex;
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException ex) {
                    log.error("Unable To Close File", ex);
                }
            }
        }

        out.println(ENDOFENCODING);
        out.flush();
    }

    private void processMessage() {

		try {
			SecureConnection conn = new SecureConnection(getDescription());

			conn.connect();
			
			sendMessage(conn);
			reciveResponse(conn);
		} catch (Exception ex){
		}

	}

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Main m = new Main();
        if (args.length == 1) {
            m.file = args[0];
        }
        m.processMessage();
    }
}
