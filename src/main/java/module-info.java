module com.office.frontend {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires lombok;
    requires spring.boot;
    requires spring.core;
    requires spring.context;
    requires spring.beans;
    requires spring.web;
    requires org.apache.pdfbox;
    opens com.office.frontend to javafx.fxml;
    exports com.office.frontend;
}