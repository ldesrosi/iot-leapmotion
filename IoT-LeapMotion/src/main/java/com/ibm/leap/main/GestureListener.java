package com.ibm.leap.main;

import java.util.logging.Logger;

import com.google.gson.JsonObject;
import com.ibm.iot.comms.IoTException;
import com.ibm.iot.comms.IoTManager;
import com.leapmotion.leap.CircleGesture;
import com.leapmotion.leap.Gesture;
import com.leapmotion.leap.KeyTapGesture;
import com.leapmotion.leap.ScreenTapGesture;
import com.leapmotion.leap.SwipeGesture;

import javafx.scene.Group;
import javafx.scene.text.Text;

public class GestureListener {
	private static Logger LOGGER = Logger.getLogger(GestureListener.class.getName());
	private IoTManager manager = null;
	
	private Group rootGroup = null;
	
	private boolean invertLeftRight = true;
	private boolean invertUpDown = true;

	public void init(Group rootGroup) throws IoTException {
		this.rootGroup = rootGroup;
		
		manager = IoTManager.getManager();

		manager.activate();
	}
	
	public void release() {
		manager.deactivate();
	}

	public void processGesture(Gesture gesture) {
		if (!gesture.isValid())
			return;

		LOGGER.finest("id:" + gesture.id());
		LOGGER.finest("duration:" + gesture.durationSeconds());
		LOGGER.finest("state:" + gesture.state());
		LOGGER.finest("type:" + gesture.type());

		if (gesture.type().equals(Gesture.Type.TYPE_CIRCLE))
			onCircle(new CircleGesture(gesture));
		else if (gesture.type().equals(Gesture.Type.TYPE_SWIPE))
			onSwipe(new SwipeGesture(gesture));
		else if (gesture.type().equals(Gesture.Type.TYPE_KEY_TAP))
			onKeyTap(new KeyTapGesture(gesture));
		else if (gesture.type().equals(Gesture.Type.TYPE_SCREEN_TAP))
			onScreenTap(new ScreenTapGesture(gesture));
		else
			LOGGER.info("Gesture type is invalid:" + gesture.type());
	}

	public void onSwipe(SwipeGesture gesture) {		
		if (gesture.state() == Gesture.State.STATE_STOP) {
			SwipeDirection swipeDirection = direction(gesture);
			updateStatusText(swipeDirection, gesture);
			
			JsonObject direction = new JsonObject();
			direction.addProperty("x", gesture.direction().getX());
			direction.addProperty("y", gesture.direction().getY());
			direction.addProperty("z", gesture.direction().getZ());
	
			JsonObject startPosition = new JsonObject();
			startPosition.addProperty("x", gesture.startPosition().getX());
			startPosition.addProperty("y", gesture.startPosition().getY());
			startPosition.addProperty("z", gesture.startPosition().getZ());
	
			JsonObject position = new JsonObject();
			position.addProperty("x", gesture.position().getX());
			position.addProperty("y", gesture.position().getY());
			position.addProperty("z", gesture.position().getZ());
	
			JsonObject event = new JsonObject();
			event.add("directionVector", direction);
			event.add("startPosition", startPosition);
			event.add("position", position);
			event.addProperty("direction", swipeDirection.toString());
			event.addProperty("speed", gesture.speed());
			event.addProperty("duration", gesture.duration());
	
			manager.sendEvent("Swipe", event);			
		}
	}

	private SwipeDirection direction(SwipeGesture gesture) {
		//We ignore z
		//The biggest absolute value between x and y determine if the direction is up/down or left/right
		//The sign (positive or negative determines the subsequent direction
		float x = gesture.direction().getX();
		float y = gesture.direction().getY();
		
		if (Math.abs(x) > Math.abs(y)) {
			if (invertLeftRight) {
				return (x >= 0)?SwipeDirection.LEFT:SwipeDirection.RIGHT;
			} else {
				return (x >= 0)?SwipeDirection.RIGHT:SwipeDirection.LEFT;
			}
		} else {
			if (invertUpDown) {
				return (y >= 0)?SwipeDirection.DOWN:SwipeDirection.UP;
			} else {
				return (y >= 0)?SwipeDirection.UP:SwipeDirection.DOWN;
			}
		}
	}

	public void onCircle(CircleGesture gesture) {
		if (gesture.state() == Gesture.State.STATE_STOP) {
			JsonObject normal = new JsonObject();
			normal.addProperty("x", gesture.normal().getX());
			normal.addProperty("y", gesture.normal().getY());
			normal.addProperty("z", gesture.normal().getZ());

			JsonObject center = new JsonObject();
			center.addProperty("x", gesture.center().getX());
			center.addProperty("y", gesture.center().getY());
			center.addProperty("z", gesture.center().getZ());

			JsonObject event = new JsonObject();
			event.add("normal", normal);
			event.add("center", center);

			event.addProperty("radius", gesture.radius());
			event.addProperty("progress", gesture.progress());
			event.addProperty("duration", gesture.duration());

			manager.sendEvent("Circle", event);
		}
	}

	public void onKeyTap(KeyTapGesture gesture) {

		JsonObject direction = new JsonObject();
		direction.addProperty("x", gesture.direction().getX());
		direction.addProperty("y", gesture.direction().getY());
		direction.addProperty("z", gesture.direction().getZ());

		JsonObject position = new JsonObject();
		position.addProperty("x", gesture.position().getX());
		position.addProperty("y", gesture.position().getY());
		position.addProperty("z", gesture.position().getZ());

		JsonObject event = new JsonObject();
		event.add("direction", direction);
		event.add("position", position);
		event.addProperty("duration", gesture.duration());

		manager.sendEvent("KeyTap", event);
	}

	public void onScreenTap(ScreenTapGesture gesture) {
		JsonObject direction = new JsonObject();
		direction.addProperty("x", gesture.direction().getX());
		direction.addProperty("y", gesture.direction().getY());
		direction.addProperty("z", gesture.direction().getZ());

		JsonObject position = new JsonObject();
		position.addProperty("x", gesture.position().getX());
		position.addProperty("y", gesture.position().getY());
		position.addProperty("z", gesture.position().getZ());

		JsonObject event = new JsonObject();
		event.add("direction", direction);
		event.add("position", position);
		event.addProperty("duration", gesture.duration());

		manager.sendEvent("KeyTap", event);
	}
	
	private void updateStatusText(SwipeDirection swipeDirection, SwipeGesture gesture) {
		LOGGER.info("SWIPE DIRECTION: " + swipeDirection.toString());
		
		if (invertLeftRight && (swipeDirection.equals(SwipeDirection.RIGHT) || swipeDirection.equals(SwipeDirection.LEFT))) {
			swipeDirection = swipeDirection.equals(SwipeDirection.RIGHT)?SwipeDirection.LEFT:SwipeDirection.RIGHT;
		} else if (invertUpDown && (swipeDirection.equals(SwipeDirection.UP) || swipeDirection.equals(SwipeDirection.DOWN))) {
			swipeDirection = swipeDirection.equals(SwipeDirection.UP)?SwipeDirection.DOWN:SwipeDirection.UP;
		}
		
		((Text) rootGroup.getChildren().get(1)).setText(swipeDirection.toString());
	}
}
