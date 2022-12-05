package edu.cwru.csds341.vapor.gui;

import java.io.IOException;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class StoreController implements Initializable {
    @FXML
    private ListView<String> topsoldlist;

    private Map<String, Integer> gamemap = new HashMap<String, Integer>();

    @FXML
    private ListView<String> gamelist;

    private Integer userid;

    private ObservableList<String> observablelist = FXCollections.observableArrayList();

    private ObservableList<String> obslist = FXCollections.observableArrayList();
    private ArrayList<String> allgames = new ArrayList<String>();

    @FXML
    private TextField searchbar;

    StoreController(Integer userid) {
        this.userid = userid;
    }

    


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        searchbar.textProperty().addListener(new ChangeListener<String>() {
            @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    gamelist.setItems(FXCollections.observableArrayList(allgames.stream()
                    .filter(str -> str.toLowerCase().contains(searchbar.getText().toLowerCase()))
                    .collect(Collectors.toList())));
                }
        });
        topsoldlist.setItems(observablelist);
        gamelist.setItems(obslist);
        try (Connection connection = Connections.fromFile(Connections.CREDENTIALS_DIR.resolve("gui.credentials"));) {
            Action action = Action.LIST_N_BEST_SELLING;
            CallableStatement cs = action.getCallableStatement(connection);
            Action.applyAll(cs, Map.of(Parameter.VTSG_LIMIT, "10"));
            ResultSet resultSet = cs.executeQuery();
            while (resultSet.next()) {
                gamemap.put(resultSet.getString(2), resultSet.getInt(1));
                observablelist.add(resultSet.getString(2));
            }
            Statement statement = connection.createStatement();
            String select = "SELECT game_id, game_name from dbo.game";
            ResultSet resultset2 = statement.executeQuery(select);
            while (resultset2.next()) {
                gamemap.put(resultset2.getString(2), resultset2.getInt(1));
                obslist.add(resultset2.getString(2));
                allgames.add(resultset2.getString(2));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        gamelist.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                switchToGamePage(newValue);
            }
        });

        topsoldlist.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                switchToGamePage(newValue);
            }
        });
    }

    private void switchToGamePage(String game_name) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/GamePage.fxml"));
        GamePageController gamePageController = new GamePageController(gamemap.get(game_name), this.userid);
        loader.setController(gamePageController);
        try {
            Parent root = loader.load();
            Stage window = (Stage) topsoldlist.getScene().getWindow();
            window.setScene(new Scene(root));
        } catch (IOException e) {

            e.printStackTrace();
        }
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
