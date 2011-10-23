/*
 *  $Id$
 */
package com.spidertracks.loggly;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

/**
 * @author jpollak
 *
 */
public class LogglyAppenderManualTest {

    
    public static void main(String[] args) throws InterruptedException {
        final String apiKey = args[0];
        if (apiKey == null || apiKey == "") {
            System.out.println("You must specify your loggly api key to test this code");
            System.exit(-1);
        }
        TestConfigHelper.setupLogging(apiKey);
        
        Logger logger = Logger.getLogger(TestConfigHelper.LOGGER_NAME);
        
        long time = System.currentTimeMillis();
        String execId = " Execution Id: " + time;
        
        logger.trace("This is a trace line." + execId);
        logger.debug("This is a debug line." + execId);
        logger.warn("This is a warning message." + execId);
        logger.fatal("This is a fatal error!" + execId);
        
        // Gracefully shutdown. This lets the appender shutdown its thread cleanly.
        LogManager.shutdown();
    }

}
