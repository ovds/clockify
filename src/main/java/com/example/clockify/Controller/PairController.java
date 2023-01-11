package com.example.clockify.Controller;

import java.io.*;
import java.net.URLEncoder;

import com.example.clockify.Model.Person;
import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import de.taimos.totp.TOTP;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import org.apache.commons.codec.binary.*;

public class PairController {

    @FXML
    private TableView<Person> table;
    @FXML
    private TableColumn<Person, String> names;
    @FXML
    private TextField search; //todo: implement search function
    @FXML
    private ImageView qrcode;

    public static String getTOTPCode(String secretKey) {
        Base32 base32 = new Base32();
        byte[] bytes = base32.decode(secretKey);
        String hexKey = Hex.encodeHexString(bytes);
        return TOTP.getOTP(hexKey);
    }

    public static String getGoogleAuthenticatorBarCode(String secretKey, String account, String issuer) {
        try {
            return "otpauth://totp/"
                    + URLEncoder.encode(issuer + ":" + account, "UTF-8").replace("+", "%20")
                    + "?secret=" + URLEncoder.encode(secretKey, "UTF-8").replace("+", "%20")
                    + "&issuer=" + URLEncoder.encode(issuer, "UTF-8").replace("+", "%20");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void createQRCode(String barCodeData, FileOutputStream out, int height, int width) throws WriterException, IOException {
        BitMatrix matrix = new MultiFormatWriter().encode(barCodeData, BarcodeFormat.QR_CODE, width, height);
        MatrixToImageWriter.writeToStream(matrix, "png", out);
    }

    @FXML
    public void initialize() {
        names.setCellValueFactory(new PropertyValueFactory<>("name"));
        table.setItems(MainController.people);

        table.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                Person p = table.getSelectionModel().getSelectedItem();
                String barCode = getGoogleAuthenticatorBarCode(p.getSecretkey(), p.getName(), "Clockify");
                try {
                    FileOutputStream image = new FileOutputStream(System.getProperty("user.dir") + "/images/" + p.getName() + ".jpg");
                    createQRCode(barCode, image, 250, 250);
                    Image qrimg = new Image(System.getProperty("user.dir") + "/images/" + p.getName() + ".jpg");
                    qrcode.setImage(qrimg);
                } catch (Exception e) { System.out.println("qrcode writing didnt work"); }
            }
        });
    }
}
