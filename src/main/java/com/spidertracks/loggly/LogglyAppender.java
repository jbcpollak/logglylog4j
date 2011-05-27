package com.spidertracks.loggly;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.*;
import java.util.List;

/**
 * Currently uses an asynchronous blocking queue to write messages. Messages are
 * written to files with sequential identifiers, these sequential files are then
 * read by the reader thread. When a file is fully consumed, it is removed.
 *
 * @author Todd Nine
 */
public class LogglyAppender extends AppenderSkeleton {

    private final HttpPost poster = new HttpPost();

    private EmbeddedDb db;

//	private LogglyMessageQueue messageQ;

    private String dirName;

    private String logglyUrl;

    private int batchSize = 50;


    private String proxyHost = null;

    private int proxyPort = -1;

    public LogglyAppender() {
        super();
    }

    public LogglyAppender(boolean isActive) {
        super(isActive);
    }

    public void close() {
        poster.stop();

    }

    public boolean requiresLayout() {
        return true;
    }

    @Override
    protected void append(LoggingEvent event) {

        /**
         * We always only produce to the current file. So there's no need for
         * locking
         */

        String output = this.layout.format(event);

        db.writeEntry(output, System.nanoTime());
    }

    /**
     * Reads the output file directory and puts all existing files into the
     * queue.
     */
    @Override
    public void activateOptions() {

        if (dirName == null) {
            LogLog.warn("directory for log queue was not set.  Please set the \"dirName\" property");
        }

        if (logglyUrl == null) {
            LogLog.warn("loggy url for log queue was not set.  Please set the \"logglyUrl\" property");
        }

        db = new EmbeddedDb(dirName, getName(), errorHandler);

//		messageQ = new LogglyMessageQueue(dirName, getName(), errorHandler);
        Thread posterThread = new Thread(poster);
        posterThread.setDaemon(true);
        posterThread.start();

    }

    private class HttpPost implements Runnable {

        boolean running = true;

        public void run() {

            while (running) {
                List<Entry> messages = db.getNext(batchSize);

                if (messages == null || messages.size() == 0) {
                    // nothing to consume,sleep for 1 second
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        // swallow
                        errorHandler
                                .error("Unable to sleep for 1 second in queue consumer",
                                        e, 1);
                    }

                    continue;
                }

                try {
                    //attempt to send and consume the data
                    if (sendData(messages)) {
                        db.deleteEntries(messages);
                    }
                } catch (IOException e) {
                    errorHandler.error(String.format(
                            "Unable to send data to loggly at URL %s",
                            logglyUrl), e, 2);

                }

            }

        }

        /**
         * Send the data via http post
         *
         * @param message
         * @throws IOException
         */
        private boolean sendData(List<Entry> messages) throws IOException {
            URL url = new URL(logglyUrl);
            Proxy proxy = Proxy.NO_PROXY;
            if (proxyHost != null) {
                SocketAddress addr = new InetSocketAddress(proxyHost, proxyPort);
                proxy = new Proxy(Proxy.Type.HTTP, addr);
            }

            URLConnection conn = url.openConnection(proxy);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setUseCaches(false);
            conn.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            OutputStreamWriter wr = new OutputStreamWriter(
                    conn.getOutputStream());

            for (Entry message : messages) {
                wr.write(message.getMessage());
            }

            wr.flush();
            wr.close();

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));

            StringBuffer response = new StringBuffer();
            int value = -1;

            while ((value = in.read()) != -1) {
                response.append((char) value);
            }

            in.close();

            return response.indexOf("ok") > -1;

        }

        /**
         * Stop this thread sending data and write the last read position
         */
        public void stop() {
            running = false;
        }

    }

    /**
     * ProxyHost a valid dns name or ip adresse for a proxy.
     * @param proxyHost
     */
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    /**
     * The proxy port for a proxy
     * @param proxyPort
     */
    public void setProxyPort(int proxyPort) {
        this.proxyPort = proxyPort;
    }

    /**
     * @param dirName the dirName to set
     */
    public void setDirName(String dirName) {
        this.dirName = dirName;
    }

    /**
     * @param logglyUrl the logglyUrl to set
     */
    public void setLogglyUrl(String logglyUrl) {
        this.logglyUrl = logglyUrl;
    }

    /**
     * Set the maximum batch size for uploads.  Defaults to 50.
     *
     * @param batchSize
     */
    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

}
