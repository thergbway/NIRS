package client.view;

import client.model.TableFile;
import client.utils.MainServiceAPIFinder;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import nirs.api.MainService;
import nirs.api.exceptions.InvalidCredentialsException;
import nirs.api.exceptions.InvalidTokenException;
import nirs.api.model.UserInfo;

import java.net.URL;
import java.util.ResourceBundle;

public class ViewController implements Initializable {

    @FXML
    private Label userInfoLabel;
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
        resetToken();
        resetTableViewControls();
        setLoginPaneVisible(true);
    }

    public void onLoginRequest() {
        try {
            token = mainService
                    .getToken(loginTextField.getText().trim(), passwordTextField.getText().trim());

            UserInfo userInfo = mainService
                    .getUserInfo(token);

            StringBuilder userInfoLabelTextBuilder = new StringBuilder(userInfo.getFirstName().concat(" ").concat(userInfo.getLastName()));

            userInfoLabelTextBuilder
                    .append(" (")
                    .append(userInfo.getUsername())
                    .append(", ")
                    .append(userInfo.getEmail())
                    .append(")");

            userInfoLabel
                    .setText(userInfoLabelTextBuilder.toString());

            setLoginPaneVisible(false);
        } catch (InvalidCredentialsException | InvalidTokenException e) {
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
                .setOnKeyReleased(this::checkLoginButtonAvailability);
        passwordTextField
                .setOnKeyReleased(this::checkLoginButtonAvailability);

        setColumnCellFactory();
        checkLoginButtonAvailability(null);
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
        if (isVisible)
            resetLoginPaneControls();
    }

    private void showInvalidCredentialAlert(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle(e.getMessage());
        alert.setHeaderText("Invalid password or login ");
        alert.setContentText("Please, try again");

        alert.showAndWait();
    }

    private void checkLoginButtonAvailability(KeyEvent event) {
        if (loginTextField.getText().isEmpty() || passwordTextField.getText().isEmpty())
            loginButton
                    .setDisable(true);
        else
            loginButton
                    .setDisable(false);

        if (event != null && event.getCode().equals(KeyCode.ENTER))
            loginButton.fire();
    }

    private void resetToken() {
        token = null;
    }

    private void resetLoginPaneControls() {
        loginTextField
                .clear();
        passwordTextField
                .clear();
        loginButton
                .setDisable(true);
    }

    private void resetTableViewControls() {
        userInfoLabel
                .setText("");
    }
}
