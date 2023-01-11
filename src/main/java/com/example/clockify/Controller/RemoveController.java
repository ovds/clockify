package com.example.clockify.Controller;

import com.example.clockify.Model.Person;
import com.example.clockify.Model.accessfile;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;

public class RemoveController implements accessfile {
    @FXML
    private Label name;

    @FXML
    private TableColumn<Person, String> names;

    @FXML //todo: if no other instances of event handling is used then use remove button handling
    private Button remove;

    @FXML
    private TextField search; //todo: implement search function

    @FXML
    private TableView<Person> table;

    @FXML
    public void removeperson() { //todo: make this work
        for (int i = 0; i < MainController.people.size(); i++) {
            if (MainController.people.get(i).getName().equals(name.getText())) {
                MainController.people.remove(i);
                return;
            }
        }
    }

    @FXML
    public void initialize() {
        names.setCellValueFactory(new PropertyValueFactory<>("name"));
        table.setItems(MainController.people);
        StringProperty nametxt = new SimpleStringProperty("hehehehaw");
        table.getStyleClass().add("noheader");
        table.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                nametxt.setValue("Do you want to remove " + table.getSelectionModel().getSelectedItem().getName() + "?");
            }
        });
        name.textProperty().bind(nametxt);
    }

    @Override //todo: find use for these 2 methods
    public void openfile(String name) {

    }

    @Override
    public void removefile(String name) {

    }
}
