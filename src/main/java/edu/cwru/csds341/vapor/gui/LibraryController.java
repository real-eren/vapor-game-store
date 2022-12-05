package edu.cwru.csds341.vapor.gui;

import java.io.IOException;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import edu.cwru.csds341.vapor.common.Action;
import edu.cwru.csds341.vapor.common.Connections;
import edu.cwru.csds341.vapor.common.Action.Parameter;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class LibraryController implements Initializable {
    private Integer userid;
    @FXML
    private ListView<String> gamelist;
    private ObservableList<String> games = FXCollections.observableArrayList();
    private Map<String, Integer> gamemap= new HashMap<String, Integer>();
    
    LibraryController(Integer userid) {
        this.userid = userid;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        gamelist.setItems(games);
        try (Connection connection = Connections.fromFile(Connections.CREDENTIALS_DIR.resolve("gui.credentials"));) {
            Action action = Action.LIST_GAMES_OWNED;
            CallableStatement cs = action.getCallableStatement(connection);
            Action.applyAll(cs, Map.of(Parameter.VGU_USERID, String.valueOf(this.userid)));
            ResultSet resultSet = cs.executeQuery();
            while (resultSet.next()) {
                gamemap.put(resultSet.getString("game_name"), resultSet.getInt("game_id"));
                games.add(resultSet.getString("game_name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        gamelist.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GamePage.fxml"));
                GamePageController gamePageController = new GamePageController(gamemap.get(newValue), userid);
                loader.setController(gamePageController);
                try {
                    Parent root = loader.load();
                    Stage window = (Stage) gamelist.getScene().getWindow();
                    window.setScene(new Scene(root));
                } catch (IOException e) {
        
                    e.printStackTrace();
                }
            }
        });
        
    }
    
    public void libraryClick() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/library.fxml"));
        LibraryController controller = new LibraryController(userid);
        loader.setController(controller);
        try {
            Parent root = loader.load();
            Stage window = (Stage) gamelist.getScene().getWindow();
            window.setScene(new Scene(root));
        } catch (IOException e) {

            e.printStackTrace();
        }
    }
    
    public void profileClick() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProfilePage.fxml"));
        ProfilePageController controller = new ProfilePageController(userid);
        loader.setController(controller);
        try {
            Parent root = loader.load();
            Stage window = (Stage) gamelist.getScene().getWindow();
            window.setScene(new Scene(root));
        } catch (IOException e) {

            e.printStackTrace();
        }
    }
    
    public void storeClick() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/store.fxml"));
        StoreController controller = new StoreController(userid);
        loader.setController(controller);
        try {
            Parent root = loader.load();
            Stage window = (Stage) gamelist.getScene().getWindow();
            window.setScene(new Scene(root));
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

}
