package edu.cwru.csds341.vapor.gui;

import java.io.IOException;
import java.net.URL;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import edu.cwru.csds341.vapor.common.Action;
import edu.cwru.csds341.vapor.common.Connections;
import edu.cwru.csds341.vapor.common.Action.Parameter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

public class GamePageController implements Initializable{
    private Integer gameid;

    private Integer userid;

    @FXML
    Button purchasebutton;
    @FXML
    Label genre;
    @FXML
    Label price;
    @FXML
    Label esrbrating;
    @FXML
    Label reviewrating;
    @FXML
    Label gamename;
    @FXML
    Label releasedate;
    @FXML
    ListView<String> friendslist;
    ObservableList<String> friends = FXCollections.observableArrayList();

    GamePageController(Integer gameid, Integer userid) {
        this.gameid = gameid;
        this.userid = userid;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        friends.clear();
        friendslist.setItems(friends);
        try (Connection connection = Connections.fromFile(Connections.CREDENTIALS_DIR.resolve("gui.credentials"));) {
            Action action = Action.LIST_ESRB_RATINGS;
            CallableStatement cs = action.getCallableStatement(connection);
            ResultSet resultset = cs.executeQuery();
            Map<String, String> esrbmap = new HashMap<String, String>();
            while(resultset.next()) {
                esrbmap.put(resultset.getString("rating_id"), resultset.getString("name").replace("_", " "));
            }
            action = Action.VIEW_GAME_DETAILS;
            cs = action.getCallableStatement(connection);
            Action.applyAll(cs, Map.of(Parameter.VGD_GAMEID, String.valueOf(this.gameid)));
            resultset = cs.executeQuery();
            resultset.next();
            gamename.setText(resultset.getString(2));
            reviewrating.setText("Average Review Rating: " + resultset.getString(3));
            esrbrating.setText("ESRB Rating: " + esrbmap.get(resultset.getString(4)));
            releasedate.setText("Release Date: " + resultset.getString(5));
            price.setText("Price: " + resultset.getString(6));
            action = Action.LIST_GAME_GENRES;
            cs = action.getCallableStatement(connection);
            Action.applyAll(cs, Map.of(Parameter.LGG_GAMEID, String.valueOf(this.gameid)));
            resultset = cs.executeQuery();
            ArrayList<String> genres = new ArrayList<String>();
            while(resultset.next()) {
                genres.add(resultset.getString(1));
            }
            genre.setText((genres.size() == 1 ? "Genre: " : "Genres: ") + String.join(", ", genres));
            action = Action.LIST_FOLLOWED_THAT_OWN_GAME;
            cs = action.getCallableStatement(connection);
            Action.applyAll(cs, Map.of(Parameter.VFUOG_USERID, String.valueOf(this.userid), Parameter.VFUOG_GAMEID, String.valueOf(this.gameid)));
            resultset = cs.executeQuery();
            while(resultset.next()) {
                friends.add(resultset.getString("username"));
            }
            action = Action.LIST_GAMES_OWNED;
            cs = action.getCallableStatement(connection);
            Action.applyAll(cs, Map.of(Parameter.VGU_USERID, this.userid.toString()));
            resultset = cs.executeQuery();
            while(resultset.next()) {
                if (this.gameid == resultset.getInt("game_id")) {
                    purchasebutton.setVisible(false);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        
    }

    public void onButtonPress() {
        try (Connection connection = Connections.fromFile(Connections.CREDENTIALS_DIR.resolve("gui.credentials"));) {
            Action action = Action.LIST_GAMES_OWNED;
            CallableStatement cs = action.getCallableStatement(connection);
            Action.applyAll(cs, Map.of(Parameter.VGU_USERID, String.valueOf(this.userid)));
            ResultSet resultset = cs.executeQuery();
            ArrayList<String> gamesowned = new ArrayList<String>();
            while(resultset.next()) {
                gamesowned.add(resultset.getString("game_id"));
            }
            if (!gamesowned.contains(String.valueOf(this.gameid))) {
                action = Action.GRANT_GAME;
                cs = action.getCallableStatement(connection);
                Action.applyAll(cs, Map.of(Parameter.GG_USERID, String.valueOf(this.userid), 
                Parameter.GG_GAMEID, String.valueOf(this.gameid),
                Parameter.GG_DATE, java.time.LocalDate.now().toString()));
                cs.executeUpdate();
                initialize(null, null);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
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
            Stage window = (Stage) friendslist.getScene().getWindow();
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
            Stage window = (Stage) friendslist.getScene().getWindow();
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
            Stage window = (Stage) friendslist.getScene().getWindow();
            window.setScene(new Scene(root));
        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}
