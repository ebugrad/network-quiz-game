module org.ebugrad.computer_networks_project {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.prefs;
    requires com.google.gson;

    opens org.ebugrad.computer_networks_project to javafx.fxml;
    exports org.ebugrad.computer_networks_project;
}