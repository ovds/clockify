package com.example.clockify.Controller;

import com.example.clockify.Model.Person;
import com.example.clockify.Model.accessfile;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.codec.binary.Base32;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

public class AddController implements accessfile {

    @FXML
    private TextField name, files;

    private List<File> images = new ArrayList<>();

    @FXML
    void addperson() throws IOException { //todo: check if the person names has been entered before
        if (name.getText().isBlank()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Warning");
            alert.setHeaderText("Name was not entered");
            alert.setContentText("Please enter a name");
            alert.showAndWait();
        } else if (images.size() < 5) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Warning");
            alert.setHeaderText("Lesser than 5 images were added");
            alert.setContentText("Please add more images");
            alert.showAndWait();
        } else {
            MainController.people.add(new Person(name.getText(), images, generateSecretKey()));

            images.clear();
            files.clear();
            name.clear();
        }
    }

    @FXML
    void addfile() {
        openfile("/Downloads/");
        for (int i = 0; i < images.size(); i++) {
            files.appendText(images.get(i).getName() + "\n"); //todo: either change the ui of the files added or format text better
        }
    }

    @Override
    public void openfile(String name) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Add Pictures");
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + name));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("All Images", "*.jpg", "*.png", "*.jpeg"),
                new FileChooser.ExtensionFilter("JPG", "*.jpg"), //todo: add more extensions and have a better all images
                new FileChooser.ExtensionFilter("PNG", "*.png")
        );
        images.addAll(fileChooser.showOpenMultipleDialog(new Stage()));
    }

    @Override
    public void removefile(String name) {}

    @FXML
    void initialize() {
        files.setEditable(false);
    }

    public static String generateSecretKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[20];
        random.nextBytes(bytes);
        Base32 base32 = new Base32();
        return base32.encodeToString(bytes);
    }
}
