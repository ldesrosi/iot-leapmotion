package com.ibm.leap.main;

import com.ibm.iot.comms.IoTException;
import com.leapmotion.leap.Arm;
import com.leapmotion.leap.Bone;
import com.leapmotion.leap.Bone.Type;
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Finger;
import com.leapmotion.leap.Frame;
import com.leapmotion.leap.Vector;

import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.shape.Cylinder;
import javafx.scene.text.Text;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class FrameProcessor {
	private Controller controller;
	private GestureListener listener;
	private long lastGesture = 0;
	
	private Group rootGroup = null;
	
	private double xOffset = 0;
	
    
    public FrameProcessor(Group rootGroup, Controller controller, double xOffset) {
    	this.controller = controller;
    	this.listener = new GestureListener();
    	
    	this.rootGroup = rootGroup;
    	
    	this.xOffset = xOffset;
	}
    
    public void init() throws IoTException {
    	listener.init(rootGroup);
    }
    
    public void release() {
    	listener.release();
    }
    
	public void update() {		
		// This needs to be running within the JavaFX thread...
		assert(Platform.isFxApplicationThread());
		
		Frame frame = controller.frame();
		if (!frame.isValid()) return;
		
		Node camera = rootGroup.getChildren().get(0);
		Node text = rootGroup.getChildren().get(1);
		rootGroup.getChildren().clear();
		rootGroup.getChildren().addAll(camera, text);
		
		
        frame.gestures().forEach(gesture->{
        	if (gesture.isValid()) {
        		listener.processGesture(gesture);
        		lastGesture = System.currentTimeMillis();
        	}
        });
        
        if (System.currentTimeMillis() - lastGesture > 5000) {
        	((Text) rootGroup.getChildren().get(1)).setText("BB8 Demo");
        }
        
        frame.hands().forEach(hand->{    
	        Arm arm = hand.arm();
	        
	        if (arm.isValid()) {
	        	renderArm(arm);
	        }
	        
        	hand.fingers().forEach(finger->{
        		if (finger.isValid()) {
        		   renderFinger(finger);
        		}
        	});
        });
	}

	private void renderArm(Arm arm) {
		//length
		Vector displacement = arm.elbowPosition().minus(arm.wristPosition());
		float length = displacement.magnitude();

		renderCylinder(arm.width(), length, arm.elbowPosition(), arm.direction());				
	}
	
	private void renderFinger(Finger finger) {		
	    for(int i = 3; i >= 0; i--) {
	        Bone bone = finger.bone(Type.swigToEnum(i));
	        renderCylinder(bone.width(), bone.length(), bone.prevJoint(), bone.direction());
	        
	    }
	}

	private void renderCylinder(float width, float length, Vector position, Vector direction) {
		direction = convert(direction);
		
		Cylinder cylinder = new Cylinder(width/2,length);
		
		Vector cross = direction.cross(new Vector(0,-1,0));
		double angle = direction.angleTo(new Vector(0,-1,0));
		Point3D rotationAxis = new Point3D(cross.getX(), -cross.getY(), cross.getZ());       
		
		cylinder.getTransforms().addAll(
			new Translate(position.getX() + xOffset, -position.getY() + 100, -position.getZ()),
			new Rotate(
					-Math.toDegrees(angle),
					0,0,0,
					rotationAxis)
		);

		rootGroup.getChildren().add(cylinder);
	}
	
	private Vector convert(Vector v) {
		return new Vector(v.getX(), -v.getY(), -v.getZ());
	}
}
