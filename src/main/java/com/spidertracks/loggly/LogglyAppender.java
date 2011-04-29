package com.spidertracks.loggly;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Currently uses an asynchronous blocking queue to write messages. Messages are
 * written to files with sequential identifiers, these sequential files are then
 * read by the reader thread. When a file is fully consumed, it is removed.
 * 
 * 
 * 
 * @author Todd Nine
 * 
 */
public class LogglyAppender extends AppenderSkeleton {

	private final HttpPost poster = new HttpPost();

	private LogglyMessageQueue messageQ;

	private String dirName;

	private String logName;

	private String logglyUrl;

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

		messageQ.push(output);
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

		if (logName == null) {
			LogLog.warn("log name for log queue was not set.  Please set the \"logName\" property");
		}

		if (logglyUrl == null) {
			LogLog.warn("loggy url for log queue was not set.  Please set the \"logglyUrl\" property");
		}

		messageQ = new LogglyMessageQueue(dirName, logName, errorHandler);
		Thread posterThread = new Thread(poster);
		posterThread.setDaemon(true);
		posterThread.start();

	}

	private class HttpPost implements Runnable {

		boolean running = true;

		public void run() {

			while (running) {
				String message = messageQ.peek();

				if (message == null) {
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
					if (sendData(message)) {
						messageQ.consume();
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
		private boolean sendData(String message) throws IOException {
			URL url = new URL(logglyUrl);
			URLConnection conn = url.openConnection();
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setUseCaches(false);
			conn.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded");
			OutputStreamWriter wr = new OutputStreamWriter(
					conn.getOutputStream());
			wr.write(message);
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
	 * @param dirName
	 *            the dirName to set
	 */
	public void setDirName(String dirName) {
		this.dirName = dirName;
	}

	/**
	 * @param logName
	 *            the logName to set
	 */
	public void setLogName(String logName) {
		this.logName = logName;
	}

	/**
	 * @param logglyUrl
	 *            the logglyUrl to set
	 */
	public void setLogglyUrl(String logglyUrl) {
		this.logglyUrl = logglyUrl;
	}

}
