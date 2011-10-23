/*
 *  $Id$
 */
package com.spidertracks.loggly;

import java.util.Properties;

import org.apache.log4j.*;

/**
 * @author jpollak
 *
 */
public class TestConfigHelper {
    public final static String LOGGER_NAME = "logglyTest";
    
    private static final Level defaultConsoleLevel = Level.INFO; //Level.WARN;
    private static final String defaultConversionPattern = "%d [%t] %-5p %c{4} - %m%n";

    /**
     * Configure basic console-only logging
     */
    public static void setupLogging(String apiKey) {
        Properties loggingProperties = new Properties();

        loggingProperties.setProperty("log4j.rootLogger", defaultConsoleLevel + ",console");
        loggingProperties.setProperty("log4j.appender.console", "org.apache.log4j.ConsoleAppender");
        loggingProperties.setProperty("log4j.appender.console.layout", "org.apache.log4j.PatternLayout");
        loggingProperties.setProperty("log4j.appender.console.layout.ConversionPattern", defaultConversionPattern);

        loggingProperties.setProperty("log4j.appender.loggly","com.spidertracks.loggly.LogglyAppender");
        loggingProperties.setProperty("log4j.appender.loggly.dirName","logs/");
        loggingProperties.setProperty("log4j.appender.loggly.logglyUrl","https://logs.loggly.com/inputs/" + apiKey);
        loggingProperties.setProperty("log4j.appender.loggly.batchSize","50");
        loggingProperties.setProperty("log4j.appender.loggly.layout","org.apache.log4j.EnhancedPatternLayout");
        loggingProperties.setProperty("log4j.appender.loggly.layout.ConversionPattern","%d{ISO8601}{GMT}Z %5p [%t]  %m%n");
        
        loggingProperties.setProperty("log4j.logger." + LOGGER_NAME,"TRACE,loggly");

        LogManager.resetConfiguration();
        PropertyConfigurator.configure(loggingProperties);
    }
}
