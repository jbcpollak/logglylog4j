/*
 *  $Id$
 */
package com.spidertracks.loggly;

import java.util.Random;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 * @author jpollak
 *
 */
public class LogglyRandomGenerator {
    
    public static void main(String[] args) throws InterruptedException {
        final String apiKey = args[0];
        if (apiKey == null || apiKey == "") {
            System.out.println("You must specify your loggly api key to test this code");
            System.exit(-1);
        }
        TestConfigHelper.setupLogging(apiKey);
        
        Logger logger = Logger.getLogger(TestConfigHelper.LOGGER_NAME);
        
        Level[] levels = new Level[]{Level.TRACE, Level.DEBUG, Level.INFO, Level.WARN, Level.ERROR};
        
        long id = 0;
        Random rand = new Random();
        while(true) {
            id++;
            int level = rand.nextInt(levels.length);
            logger.log(levels[level], "Id=" + id + " The time is now " + System.currentTimeMillis());
            
            // sleep a random amount up to 30 seconds
            Thread.sleep((long) (30 * 1000 * rand.nextFloat()));
        }
        
        // Gracefully shutdown. This lets the appender shutdown its thread cleanly.
//        LogManager.shutdown();
    }
}
