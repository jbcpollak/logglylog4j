# Usage.

Usage is relatively straight forward.

    <appender name="loggly"
      class="com.spidertracks.loggly.LogglyAppender">
      <param name="dirName" value="A working directory to store the HSQL queue buffer" />
      <param name="logglyUrl" value="Your loggly url goes here" /> 
      <param name="proxyHost" value="A dns name or an ip address"/> <!-- Optional value -->
      <param name="proxyPort" value="The port number for the proxy"/> <!-- Optional value -->
      <!-- The maximum number of messages to upload in a single http POST -->
      <param name="batchSize" value="50"/>
      <layout class="org.apache.log4j.EnhancedPatternLayout">
        <!-- Pattern to upload.  Will use the pattern layout specified when uploading -->
        <param name="ConversionPattern" value="%d{ISO8601}{GMT}Z %5p [%t]  %m%n" />
      </layout>
    </appender>


# Architecture.

1. Log4j appender write an entry to a queue in an embedded HSQL db
2. An asynchronous reader thread reads the oldest entry from the HSQL db and
uploads it to the configured url.

This supports guaranteed delivery.  If the logger cannot contact Loggly,
     a log4j internal message will be logged, and message will queue locally.
     The sending thread will continue to attempt to connect until it succeeds.  



