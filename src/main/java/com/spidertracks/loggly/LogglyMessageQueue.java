package com.spidertracks.loggly;

import java.util.List;

import org.apache.log4j.spi.ErrorHandler;

/**
 * Simple message queue to abstract the queueing between send and receive. This
 * class is not thread save on receive only a single thread should access the
 * class between the read and consume
 * 
 * @author Todd Nine
 * 
 */
public class LogglyMessageQueue {

	private EmbeddedDb db;

	/**
	 * The file name of the queue
	 * 
	 * @param dirName
	 */
	public LogglyMessageQueue(String dirName, String logName,
			ErrorHandler errorHandler) {

		db = new EmbeddedDb(dirName, logName, errorHandler);

	}

	/**
	 * Push the message into the queue
	 * 
	 * @param message
	 *            . The string message to log
	 */
	public void push(String message) {
		db.writeEntry(message, System.nanoTime());
	}

	/**
	 * Get the last message from the queue, will return null if one is not
	 * available
	 * 
	 * @return
	 */
	public List<Entry> next(int size) {
		return db.getNext(size);
		
	}

	/**
	 * Mark the last read message as consumed
	 * 
	 * @return
	 */
	public boolean consume(List<Entry> entries) {
		
		int count =  db.deleteEntries(entries);
		
		return count == entries.size();
		
	}

}
