package com.example.clockify.Controller;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.example.clockify.MainApplication;
import com.example.clockify.Model.Model;
import com.example.clockify.Model.Person;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import com.example.clockify.Model.Utils;

/*import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;
import static org.opencv.core.CvType.CV_32SC1;
import static org.opencv.imgproc.Imgproc.resize;*/



import static com.example.clockify.Controller.PairController.getTOTPCode;

import org.bytedeco.javacpp.BytePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.javacpp.DoublePointer;

import org.bytedeco.opencv.global.opencv_imgproc;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.*;
import org.bytedeco.opencv.opencv_objdetect.CascadeClassifier;
import org.bytedeco.opencv.opencv_videoio.VideoCapture;

import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_face.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_imgproc.resize;
import static org.opencv.imgproc.Imgproc.COLOR_BGR2GRAY;

public class MainController extends Utils {
    private ScheduledExecutorService timer;
    private VideoCapture capture = new VideoCapture();
    private boolean cameraActive = false;
    private static int cameraId = 0;
    String xmlFile3 = "xml/haarcascade_frontalface_alt.xml"; //todo: try to only implement detectAndDisplay in utils
    private CascadeClassifier faceCascade = new CascadeClassifier(xmlFile3); // face cascade classifier
    public final static ObservableList<Person> people = FXCollections.observableArrayList();
    @FXML
    public ImageView currentFrame;
    @FXML
    public Label name;
    @FXML
    public VBox video;
    @FXML
    public TextField username, password;

    public static Model model = new Model();
    public static int numberOfPpl = 0;
    public static MatVector images;
    public static ArrayList<String> names = new ArrayList<>();
    public static Mat labels;
    StringProperty n = new SimpleStringProperty("no balls");
    public boolean trained = false;



    @FXML
    public void initialize() throws IOException { //todo: add about me page
        startCamera();

        File directory = new File(System.getProperty("user.dir") + "/people");
        if (directory.isDirectory() && directory.listFiles().length > 0) {
            System.out.println("not empty");
            List<File> fileList = Arrays.asList(directory.listFiles());
            images = new MatVector(fileList.size());
            labels = new Mat(fileList.size(), 1, CV_32SC1);
            IntBuffer labelsBuf = labels.createBuffer();
            System.out.println(fileList);
            int index = 0;
            for (int i = 0; i < fileList.size(); i++) {
                String name = fileList.get(i).getName();
                names.add(name);
                if (fileList.get(i).isDirectory() && fileList.get(i).length() >= 0) {
                    File[] folder = fileList.get(i).listFiles();
                    String secretkey = "";
                    List<File> files = new ArrayList<>();
                    for (int j = 0; j < folder.length; j++) {
                        if (folder[j].getName().equals("secretkey.txt")) {
                            BufferedReader reader = new BufferedReader(new FileReader(folder[j]));
                            secretkey = reader.readLine();
                            reader.close();
                        } else {
                            files.add(folder[j]);
                            Mat img = imread(folder[j].getPath(), IMREAD_GRAYSCALE);
                            resize(img, img, new Size(200, 200));
                            images.put(index, img); //todo: make sure all images are of same size
                            labelsBuf.put(index, i);
                            index++;
                        }
                    }
                    people.add(new Person(name, files, secretkey));
                }
            }
        }

        numberOfPpl = people.size();

        File xml = new File("eigenfacerecognizer.xml");
        if (xml.exists()) {
            model.read(xml.getPath());
        } else if (images.size() > 0) {
            model.update(images, labels);
            trained = true;
        }

        name.textProperty().bind(n);
    }

    @FXML
    void addperson(ActionEvent event) throws IOException {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("View/addperson.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        scene.getStylesheets().add(MainApplication.class.getResource("View/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Add a person");
        stage.show();
    }

    @FXML
    void removeperson(ActionEvent event) throws IOException{
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("View/removeperson.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        scene.getStylesheets().add(MainApplication.class.getResource("View/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Remove a person");
        stage.show();
    }

    @FXML
    void submit(ActionEvent event) {
        for (int i = 0; i < people.size(); i++) {
            if (names.get(i).equals(username.getText()) && password.getText().equals(getTOTPCode(people.get(i).getSecretkey()))) {
                System.out.println("success");
            }
        }
    }

    @FXML
    void pair(ActionEvent event) throws IOException {
        Stage stage = new Stage();
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("View/pair.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        scene.getStylesheets().add(MainApplication.class.getResource("View/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Pair using google authenticator");
        stage.show();
    }


    public void startCamera() {
        if (!this.cameraActive) {
            this.capture.open(cameraId);
            if (this.capture.isOpened()) {
                this.cameraActive = true;
                Runnable frameGrabber = new Runnable() {
                    @Override
                    public void run() {
                        Mat frame = grabFrame();
                        Image imageToShow = null;
                        try {
                            imageToShow = Utils.mat2Image(frame);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                        updateImageView(currentFrame, imageToShow);
                    }
                };
                this.timer = Executors.newSingleThreadScheduledExecutor();
                this.timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
            }
            else {
                System.err.println("Impossible to open the camera connection...");
            }
        }
        else {
            this.cameraActive = false;
            this.stopAcquisition();
        }
    }

    private Mat grabFrame() {
        Mat frame = new Mat();

        if (this.capture.isOpened()) {
            try {
                this.capture.read(frame);
                if (!frame.empty()) {
                    this.detectAndDisplay(frame);
                    Mat f = new Mat();
                    this.capture.read(f);
                    BufferedImage image = detect(f);
                    if (image != null) {
                        Size sz = new Size(200, 200);
                        if (!f.empty() && !sz.empty()) {
                            Mat img = bufferedImageToMat(image);
                            Mat grayFrame = new Mat();
                            opencv_imgproc.cvtColor(img, grayFrame, COLOR_BGR2GRAY);
                            resize(grayFrame, grayFrame, sz);
                            try {
                                String str = "";
                                if (trained) str = model.predict(grayFrame, names);
                                String finalStr = str;
                                Platform.runLater(() -> {
                                    if (!name.getText().equals(finalStr)) n.setValue(finalStr);
                                });
                            } catch (Exception e) {
                                //System.out.println("not trained yet");
                                System.out.println(e);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Exception during the image elaboration: " + e);
            }
        }

        return frame;
    }

    public void detectAndDisplay(Mat frame) {
        int absoluteFaceSize = 0;
        MatOfRect faces = new MatOfRect();
        Mat grayFrame = new Mat();

        Imgproc.cvtColor(frame, grayFrame, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(grayFrame, grayFrame);

        if (absoluteFaceSize == 0) {
            int height = grayFrame.rows();
            if (Math.round(height * 0.2f) > 0) {
                absoluteFaceSize = Math.round(height * 0.2f);
            }
        }

        this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(absoluteFaceSize, absoluteFaceSize), new Size());

        Rect[] facesArray = faces.toArray();
        for (int i = 0; i < facesArray.length; i++) { Imgproc.rectangle(frame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 0, 0), 3); }


    }

    public BufferedImage detect(Mat f) {
        int absoluteFaceSize = 0;
        MatOfRect faces = new MatOfRect();
        Mat grayFrame = new Mat();

        Imgproc.cvtColor(f, grayFrame, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(grayFrame, grayFrame);

        if (absoluteFaceSize == 0) {
            int height = grayFrame.rows();
            if (Math.round(height * 0.2f) > 0) {
                absoluteFaceSize = Math.round(height * 0.2f);
            }
        }

        this.faceCascade.detectMultiScale(grayFrame, faces, 1.1, 2, 0 | Objdetect.CASCADE_SCALE_IMAGE, new Size(absoluteFaceSize, absoluteFaceSize), new Size());

        if (faces.toArray().length == 0) { return null; }

        Rect face = faces.toArray()[0];
        Rect rectCrop = new Rect(face.x, face.y, face.width, face.height);

        Mat image_roi = new Mat(f, rectCrop);
        return Utils.matToBufferedImage(image_roi);
    }

    private void stopAcquisition() {
        if (this.timer!=null && !this.timer.isShutdown()) {
            try {
                this.timer.shutdown();
                this.timer.awaitTermination(33, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                System.err.println("Exception in stopping the frame capture, trying to release the camera now... " + e);
            }
        }

        if (this.capture.isOpened()) {
            this.capture.release();
        }
    }

    private void updateImageView(ImageView view, Image image) {
        Utils.onFXThread(view.imageProperty(), image);
    }
}
