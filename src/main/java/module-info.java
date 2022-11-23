module edu.cwru.csds341.vapor {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;
    
    opens edu.cwru.csds341.vapor.gui to javafx.fxml;
    exports edu.cwru.csds341.vapor.gui;
    exports edu.cwru.csds341.vapor.cli;
}
