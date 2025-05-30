open module org.example.shootergame {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.google.gson;

    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires java.naming;
    requires java.sql;

    exports org.example.shootergame.client;
    exports org.example.shootergame.server;
    exports org.example.shootergame.common;
    exports org.example.shootergame.model;
    exports org.example.shootergame.network;
    exports org.example.shootergame.db;
}