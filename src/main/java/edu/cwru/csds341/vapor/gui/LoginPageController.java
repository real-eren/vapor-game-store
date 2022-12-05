package edu.cwru.csds341.vapor.gui;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import edu.cwru.csds341.vapor.common.Action;
import edu.cwru.csds341.vapor.common.Connections;
import edu.cwru.csds341.vapor.common.Action.Parameter;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class LoginPageController {

    @FXML
    private AnchorPane backdrop;

    @FXML
    private Button closeBtn;

    @FXML
    private Button submitBtn;

    @FXML
    private Pane topBar;

    @FXML
    private TextField userNameField;

    @FXML
    void onClose(ActionEvent event) {
        Stage stage = (Stage) closeBtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    void onSubmit() {
        Action action = Action.VIEW_USER_INFO;
        for (var req : Parameter.VU_USER_ID.requirements) {
            if (!req.accepts(userNameField.getText())) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setHeaderText(req.getMessage());
                alert.showAndWait();
                return;
            }
        }
        try (Connection connection = Connections
                .fromFile(Connections.CREDENTIALS_DIR.resolve("gui.credentials"));) {
            CallableStatement cs = action.getCallableStatement(connection);
            Action.applyAll(cs, Map.of(Parameter.VU_USER_ID, userNameField.getText()));
            ResultSet resultset = cs.executeQuery();
            if (resultset.next()) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProfilePage.fxml"));
                ProfilePageController profilepagecontroller = new ProfilePageController(
                        Integer.valueOf(userNameField.getText()));
                loader.setController(profilepagecontroller);
                try {
                    Parent root = loader.load();
                    Stage window = (Stage) userNameField.getScene().getWindow();
                    window.setScene(new Scene(root));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setHeaderText("userid not found");
                alert.showAndWait();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
