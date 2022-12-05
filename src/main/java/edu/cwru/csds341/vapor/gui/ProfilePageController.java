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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class ProfilePageController implements Initializable{

    @FXML
    private TextField commentbar;
    @FXML
    private Label usernamelabel;
    @FXML
    private Label joindatelabel;
    @FXML
    private ListView<String> commentlist;
    @FXML
    private Button followbutton;
    @FXML
    private ListView<String> friendslist;
    private ObservableList<String> friends = FXCollections.observableArrayList();
    private ObservableList<String> comments = FXCollections.observableArrayList();

    private Integer userid;
    private Integer profileuserid;
    private Map<String, Integer> friendsmap= new HashMap<>();

    ProfilePageController(Integer userid, Integer profileuserid) {
        this.userid = userid;
        this.profileuserid = profileuserid;
    }

    ProfilePageController(Integer userid) {
        this.userid = userid;
        this.profileuserid = userid;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        friends.clear();
        comments.clear();
        commentlist.setItems(comments);
        friendslist.setItems(friends);
        followbutton.setText("Follow user");
        try (Connection connection = Connections.fromFile(Connections.CREDENTIALS_DIR.resolve("gui.credentials"));) {
            Action action = Action.VIEW_USER_INFO;
            CallableStatement cs = action.getCallableStatement(connection);
            Action.applyAll(cs, Map.of(Parameter.VU_USER_ID, String.valueOf(this.profileuserid)));
            ResultSet resultset = cs.executeQuery();
            resultset.next();
            joindatelabel.setText("Join Date: " + resultset.getString("join_date"));
            usernamelabel.setText(resultset.getString("username"));
            action = Action.LIST_PROFILE_COMMENTS;
            cs = action.getCallableStatement(connection);
            Action.applyAll(cs, Map.of(Parameter.GPC_USERID, String.valueOf(this.profileuserid)));
            resultset = cs.executeQuery();
            while(resultset.next()) {
                comments.add(resultset.getString("message") + "\n" + resultset.getString(2) + "\t\t" + resultset.getString("datetime"));
            }
            action = Action.LIST_FOLLOWED;
            cs = action.getCallableStatement(connection);
            Action.applyAll(cs, Map.of(Parameter.LFD_USER_ID, this.userid.toString()));
            resultset = cs.executeQuery();
            while(resultset.next()) {
                if (this.profileuserid == resultset.getInt("followed_id")) {
                    followbutton.setText("Unfollow user");
                } 
            }
            action = Action.LIST_FOLLOWED;
            cs = action.getCallableStatement(connection);
            Action.applyAll(cs, Map.of(Parameter.LFD_USER_ID, String.valueOf(this.profileuserid)));
            ResultSet resultSet = cs.executeQuery();
            while (resultSet.next()) {
                friendsmap.put(resultSet.getString("username"), resultSet.getInt("followed_id"));
                friends.add(resultSet.getString("username"));
            }
            friendslist.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ProfilePage.fxml"));
                    ProfilePageController profilepagecontroller= new ProfilePageController(userid, friendsmap.get(newValue));
                    loader.setController(profilepagecontroller);
                    try {
                        Parent root = loader.load();
                        Stage window = (Stage) friendslist.getScene().getWindow();
                        window.setScene(new Scene(root));
                    } catch (IOException e) {
            
                        e.printStackTrace();
                    }
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }   
    }

    public void submitComment() {
        for (var rec: Parameter.MC_MESSAGE.requirements) {
            if (!rec.accepts(commentbar.getText())) {
                Alert alert = new Alert(AlertType.INFORMATION);
                alert.setHeaderText(rec.getMessage());
                alert.showAndWait();
                return;
            }     
        }
        try (Connection connection = Connections.fromFile(Connections.CREDENTIALS_DIR.resolve("gui.credentials"))) {
            Action action = Action.MAKE_COMMENT;
            CallableStatement cs = action.getCallableStatement(connection);
            Action.applyAll(cs, Map.of(Parameter.MC_PROFILE_ID, this.profileuserid.toString()
            , Parameter.MC_COMMENTER_ID, String.valueOf(this.userid)
            , Parameter.MC_MESSAGE, commentbar.getText()
            , Parameter.MC_DATETIME, MainApp.getDateTime()));
            cs.executeUpdate();
            commentbar.setText("");
            initialize(null, null);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }     
    }

    public void followUser() {
        if (this.userid == this.profileuserid) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setHeaderText("You can't follow yourself weirdo!");
            alert.showAndWait();
            return;
        }
        try (Connection connection = Connections.fromFile(Connections.CREDENTIALS_DIR.resolve("gui.credentials"))) {
            Action action = Action.LIST_FOLLOWED;
            CallableStatement cs = action.getCallableStatement(connection);
            Action.applyAll(cs, Map.of(Parameter.LFD_USER_ID, this.userid.toString()));
            ResultSet resultset = cs.executeQuery();
            while(resultset.next()) {
                if (this.profileuserid == resultset.getInt("followed_id")) {
                    action = Action.UNFOLLOW_USER;
                    cs = action.getCallableStatement(connection);
                    Action.applyAll(cs, Map.of(Parameter.UFU_FOLLOWED_ID, this.profileuserid.toString(), Parameter.UFU_FOLLOWER_ID, this.userid.toString()));
                    cs.executeUpdate();
                    return;
                } 
            }
            action = Action.FOLLOW_USER;
            cs = action.getCallableStatement(connection);
            Action.applyAll(cs, Map.of(Parameter.FU_FOLLOWER_ID, this.userid.toString()
            , Parameter.FU_FOLLOWED_ID, String.valueOf(this.profileuserid)
            , Parameter.FU_DATE, MainApp.getDateTime()));
            cs.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            initialize(null, null);
        }
    }

    
    public void libraryClick() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/library.fxml"));
        LibraryController controller = new LibraryController(userid);
        loader.setController(controller);
        try {
            Parent root = loader.load();
            Stage window = (Stage) commentlist.getScene().getWindow();
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
            Stage window = (Stage) commentlist.getScene().getWindow();
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
            Stage window = (Stage) commentlist.getScene().getWindow();
            window.setScene(new Scene(root));
        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    

}

