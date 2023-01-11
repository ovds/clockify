module com.example.attendanceapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires commons.codec;
    requires totp;
    requires com.google.zxing;
    requires com.google.zxing.javase;
    requires org.bytedeco.opencv;
    requires org.bytedeco.javacv;
    requires org.bytedeco.javacpp;

    exports com.example.clockify.Controller;
    opens com.example.clockify.Controller to javafx.fxml;
    exports com.example.clockify.Model;
    opens com.example.clockify.Model to javafx.fxml;
}