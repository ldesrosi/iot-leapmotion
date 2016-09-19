package com.ibm.iot.comms;

public class IoTException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -76142328669471091L;

	public IoTException(String message) {
		super(message);
	}
	
	public IoTException(String message, Throwable t) {
		super(message, t);
	}
}
