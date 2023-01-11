package com.example.clockify;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.WindowEvent;
import org.bytedeco.javacpp.Loader;
import org.bytedeco.opencv.opencv_java;

import java.io.IOException;

public class MainApplication extends Application { //todo: train the Model using the files given and implement database where login times are recorded

    public static void main(String[] args) {
        Loader.load(opencv_java.class);
        launch(args);
    }

    public static final String SPLASH_IMAGE = "splash.png";
    private AnchorPane splashLayout;
    private Label progressText;
    private static final int SPLASH_WIDTH = 676;
    private static final int SPLASH_HEIGHT = 210;

    @Override
    public void init() {
        ImageView splash = new ImageView(new Image(SPLASH_IMAGE));
        progressText = new Label("I feel like giving up on cs, jk");
        splashLayout = new AnchorPane();
        splashLayout.getChildren().addAll(splash, progressText);
        progressText.setLayoutX(2);
        progressText.setLayoutY(SPLASH_HEIGHT + 70);
        splashLayout.setEffect(new DropShadow());
    }

    @Override
    public void start(Stage stage) throws IOException {
        final Task<ObservableList<String>> w = new Task<ObservableList<String>>() {
            @Override
            protected ObservableList<String> call() throws InterruptedException {
                ObservableList<String> done = FXCollections.<String>observableArrayList();
                ObservableList<String> words = FXCollections.observableArrayList("Loading images", "Retrieving faces", "Selling data to Mark Zuckerburg", "Finding bugs", "Removing bugs", "");

                for (int i = 0; i < words.size(); i++) {
                    Thread.sleep(400);
                    updateProgress(i + 1, words.size());
                    String word = words.get(i);
                    done.add(word);
                    updateMessage(word + "...");
                }

                return done;
            }
        };

        showSplash(
                stage, w,
                () -> {
                    try {
                        showMainStage();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        new Thread(w).start();
    }

    private void showMainStage() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("View/main.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1280, 720);
        Stage stage = new Stage();
        stage.setTitle("Clockify");
        scene.getStylesheets().add(MainApplication.class.getResource("View/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.getIcons().add(new Image(MainApplication.class.getResourceAsStream("View/icon.png"))); //todo: better font thanks (sans-fransisco bold)
        stage.show();

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                Platform.exit();
                System.exit(0);
            }
        });
    }

    private void showSplash(final Stage initStage, Task<?> task, InitCompletionHandler initCompletionHandler) {
        progressText.textProperty().bind(task.messageProperty());
        task.stateProperty().addListener((observableValue, oldState, newState) -> {
            if (newState == Worker.State.SUCCEEDED) {
                initStage.toFront();
                initCompletionHandler.complete();
                try {
                    Thread.sleep(250);
                } catch (Exception e) {}
                initStage.hide();
            }
        });

        Scene splashScene = new Scene(splashLayout, Color.TRANSPARENT);
        final Rectangle2D bounds = Screen.getPrimary().getBounds();
        initStage.setScene(splashScene);
        initStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - SPLASH_WIDTH / 2);
        initStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - SPLASH_HEIGHT / 2);
        initStage.initStyle(StageStyle.TRANSPARENT);
        initStage.setAlwaysOnTop(true);
        initStage.show(); //todo: try to add animation to the splash page where the text dissappears and the icon enlarges and
    }

    public interface InitCompletionHandler {
        void complete();
    }

}

//todo: add the feature where you can enter your name if your face doesnt register with password (regex)
//todo: add more comments describing the code and also start on documentation