package com.example.clockify.Model;

/*import org.opencv.core.Mat;
import org.opencv.face.EigenFaceRecognizer;*/

import org.bytedeco.opencv.opencv_core.Mat;
import org.bytedeco.opencv.opencv_core.MatVector;
import org.bytedeco.opencv.opencv_face.EigenFaceRecognizer;

import java.util.ArrayList;
import java.util.List;

public class Model {
    EigenFaceRecognizer faceRecognizer;

    public Model() {
        faceRecognizer = EigenFaceRecognizer.create();
    }

    public Model(MatVector src, Mat labels) {
        this();
        faceRecognizer.train(src, labels);
    }

    public String predict(Mat src, ArrayList<String> labels) {
        return labels.get(faceRecognizer.predict_label(src));
    }

    public void update(MatVector src, Mat labels) { faceRecognizer.train(src, labels); }

    public void read(String filename) {
        try {
            faceRecognizer.read(filename);
        } catch (Exception e) { System.out.println("file not found for xml file"); }
    }

    public void write(String filename) {
        faceRecognizer.write(filename);
    }
}
