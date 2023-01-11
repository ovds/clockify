package com.example.clockify.Model;

import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.opencv.imgproc.Imgproc.resize;

public class Person implements accessfile {
    public String name;
    private String secretkey; //todo: ensure security while getting secret key to create qr codes
    private ArrayList<BufferedImage> images = new ArrayList<>();
    String xmlFile3 = "xml/haarcascade_frontalface_alt.xml";
    private CascadeClassifier faceCascade = new CascadeClassifier(xmlFile3); // face cascade classifier
    public boolean paired = false;

    public Person(String name, List<File> list, String secretkey) throws IOException {
        this.name = name;
        openfile(name);
        for (int i = 0; i < list.size(); i++) {
            try {
                File f = new File(System.getProperty("user.dir") + "/people/" + name + "/" + i + ".jpg");
                if (!f.exists()) {
                    Mat img = Imgcodecs.imread(list.get(i).getPath());
                    BufferedImage image = detectAndDisplay(img);
                    if (image != null) {
                        img = Utils.bufferedImageToMat(image);
                        Size sz = new Size(200, 200);
                        resize(img, img, sz);
                        images.add(image);
                        ImageIO.write(image, "jpg", f);
                        f.mkdir();
                    }
                }
            } catch (Exception e) {
                System.out.println(e);
            }
        }
        File secret = new File(System.getProperty("user.dir") + "/people/" + name + "/secretkey.txt");
        BufferedWriter file = new BufferedWriter(new FileWriter(secret));
        file.write(secretkey);
        file.flush();
        file.close();
        this.secretkey = secretkey;
    }

    @Override
    public void openfile(String name) {
        File folder = new File(System.getProperty("user.dir") + "/people/" + name);
        boolean b = folder.mkdirs();
        if (b) System.out.println("folder created succesfully");
    }

    @Override //todo: find use for this method
    public void removefile(String name) {}

    public String getName() { return name; }

    public String getSecretkey() { return secretkey; }

    public ArrayList<BufferedImage> getImages() { return images; }

    public BufferedImage detectAndDisplay(Mat frame) {
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

        Rect face = new Rect(0, 0, frame.cols(), frame.rows());
        if (faces.toArray().length > 0) {
            face = faces.toArray()[0];
        } else {
            return null;
        }
        Rect rectCrop = new Rect(face.x, face.y, face.width, face.height);

        Mat image_roi = new Mat(frame, rectCrop);
        return Utils.matToBufferedImage(image_roi);
    }
}
