package com.ibm.leap.main;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.ibm.iot.comms.IoTException;
import com.leapmotion.leap.Controller;
import com.leapmotion.leap.Gesture;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Bounds;
import javafx.scene.DepthTest;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

public class LeapControllerApplication extends Application {
	private static Logger LOGGER = Logger.getLogger(LeapControllerApplication.class.getName());
	private static final double WIDTH = 1024;
	private static final int HEIGTH = 800;
	
	private Group rootGroup = null;
	
	private Timeline timeline = null;
	private FrameProcessor processor = null;
	private Controller  leapController = null;
    
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Text text = new Text("BB8 Demo");
        text.setFont(Font.font ("Arial", 27));
        text.setFill(Color.WHITE);
        text.setTextAlignment(TextAlignment.CENTER);

        Bounds bounds = text.getLayoutBounds();
        text.setX((WIDTH - bounds.getWidth())/2);
        text.setY(-1 * (HEIGTH /2) + 3*bounds.getHeight());

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setTranslateX(WIDTH/2);
        camera.setTranslateY(0);
        camera.setTranslateZ(-1000);
        camera.setFarClip(2000);
        camera.setFieldOfView(40);
		
		rootGroup = new Group();
		rootGroup.setDepthTest(DepthTest.ENABLE);
		rootGroup.getChildren().add(camera);
		rootGroup.getChildren().add(text);
		
		Scene scene = new Scene(rootGroup, 1024, 800, Color.BLACK);		
		scene.setCamera(camera);
		
		primaryStage.setScene(scene);
		primaryStage.show();
		
		leapController = new Controller();
		
		leapController.enableGesture(Gesture.Type.TYPE_SWIPE);
		//leapController.enableGesture(Gesture.Type.TYPE_CIRCLE);
		
		processor = new FrameProcessor(rootGroup, leapController, WIDTH/2);
		try {
			processor.init();
		} catch (IoTException e) {
			LOGGER.log(Level.SEVERE,"Exception initializing processor",e);
		}
		
		synchronizeWithLeapMotion();
    }
    
	private void synchronizeWithLeapMotion() {
		timeline = new Timeline();
		timeline.setCycleCount(Timeline.INDEFINITE);
		timeline.getKeyFrames().add(
				new KeyFrame(Duration.seconds(1.0 / 60.0), ea -> processor.update()));
		timeline.play();
}
    @Override
    public void stop() {
    	timeline.stop();
    	processor.release();
        leapController.delete();
    }

}
