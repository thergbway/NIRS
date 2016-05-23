package client.view;

import client.model.TableFile;
import client.utils.MainServiceAPIFinder;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import nirs.api.MainService;
import nirs.api.exceptions.InvalidCredentialsException;

import java.net.URL;
import java.util.ResourceBundle;

public class ViewController implements Initializable {

    @FXML
    private Label loginLabel;
    @FXML
    private AnchorPane tableViewPane;
    @FXML
    private AnchorPane loginPane;

    @FXML
    private TableView<TableFile> tableFileView;

    @FXML
    private TableColumn<TableFile, String> statusColumn;
    @FXML
    private TableColumn<TableFile, String> cipherColumn;
    @FXML
    private TableColumn<TableFile, String> fileNameColumn;
    @FXML
    private TableColumn<TableFile, String> fileSizeColumn;

    @FXML
    private TableColumn<TableFile, String> createdColumn;
    @FXML
    private Button loginButton;

    @FXML
    private Button signInButton;
    @FXML
    private TextField loginTextField;

    @FXML
    private PasswordField passwordTextField;

    private MainService mainService;

    private String token;

    public void onLogoutRequest() {

//        setLoginPaneVisible(true);
    }

    public void onLoginRequest() {
        try {
            token = mainService
                    .getToken(loginTextField.getText().trim(), passwordTextField.getText().trim());
            setLoginPaneVisible(false);
        } catch (InvalidCredentialsException e) {
            showInvalidCredentialAlert(e);
        }
    }

    public void onSignInRequest() {

    }

    public void onDownloadFileRequest() {

    }

    public void onAddFileRequest() {

    }

    public void onDeleteFileRequest() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        mainService = MainServiceAPIFinder.findProxy();

        loginTextField
                .setOnKeyReleased(event -> checkLoginButtonAvailability());
        passwordTextField
                .setOnKeyReleased(event -> checkLoginButtonAvailability());

        setColumnCellFactory();
        checkLoginButtonAvailability();
        setLoginPaneVisible(true);
    }

    private void setColumnCellFactory() {
        fileNameColumn
                .setCellValueFactory(param -> param.getValue().fileName);
        createdColumn
                .setCellValueFactory(param -> param.getValue().created);
        fileSizeColumn
                .setCellValueFactory(param -> param.getValue().size);
        cipherColumn
                .setCellValueFactory(param -> param.getValue().cipher);
        statusColumn
                .setCellValueFactory(param -> param.getValue().status);
    }

    private void setLoginPaneVisible(boolean isVisible) {
        loginPane
                .setVisible(isVisible);
        tableViewPane
                .setVisible(!isVisible);
    }

    private void showInvalidCredentialAlert(InvalidCredentialsException e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle(e.getMessage());
        alert.setHeaderText("Invalid password or login ");
        alert.setContentText("Please, try again");

        alert.showAndWait();
    }

    private void checkLoginButtonAvailability() {
        if (loginTextField.getText().isEmpty() | passwordTextField.getText().isEmpty())
            loginButton
                    .setDisable(true);
        else
            loginButton
                    .setDisable(false);
    }
}
