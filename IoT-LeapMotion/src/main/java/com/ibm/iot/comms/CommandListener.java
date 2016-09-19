package com.ibm.iot.comms;

import com.google.gson.JsonObject;

public interface CommandListener {
	public void processCommand(String command, JsonObject payload);
}
