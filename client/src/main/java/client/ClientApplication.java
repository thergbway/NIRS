package client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class ClientApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client.fxml"));

        Parent root = loader.load();

        primaryStage
                .setScene(new Scene(root));

        primaryStage
                .setTitle("File storage");

        primaryStage
                .getIcons()
                .add(new Image("/icon.png"));

        primaryStage
                .sizeToScene();

        primaryStage
                .setResizable(false);

        primaryStage
                .show();

    }

    public static void main(String[] args) {
        launch(args);
    }
}
