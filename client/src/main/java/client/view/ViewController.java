package client.view;

import client.model.TableFile;
import client.utils.MainServiceAPIFinder;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import nirs.api.MainService;
import nirs.api.exceptions.EmailExistsException;
import nirs.api.exceptions.InvalidCredentialsException;
import nirs.api.exceptions.InvalidTokenException;
import nirs.api.exceptions.UserExistsException;
import nirs.api.model.UserInfo;

import java.net.URL;
import java.util.Optional;
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
            showErrorAlert(e);
        }
    }

    public void onSignInRequest() {
        showSignInAlert();
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

    private void showErrorAlert(Exception e) {
        Alert alert = new Alert(Alert.AlertType.ERROR);

        alert.setTitle("Error!");
        alert.setHeaderText(e.getMessage());
        alert.setContentText("Please, try again");

        ((Stage) alert
                .getDialogPane()
                .getScene()
                .getWindow())
                .getIcons()
                .add(new Image(getClass().getResource("/error.png").toString()));

        alert.showAndWait();
    }

    private void checkLoginButtonAvailability(KeyEvent event) {
        if (loginTextField.getText().isEmpty() || passwordTextField.getText().isEmpty()) {
            loginButton
                    .setDisable(true);
        } else
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

    private void showSignInAlert() {
        // Create the custom dialog.
        Dialog<Boolean> dialog = new Dialog<>();

        dialog.setTitle("Sign in");

        ((Stage) dialog
                .getDialogPane()
                .getScene()
                .getWindow())
                .getIcons()
                .add(new Image(getClass().getResource("/new_account.png").toString()));

        dialog.setHeaderText("Fill the forms below to sign in");

        ButtonType createAccountButtonType = new ButtonType("Create account", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(createAccountButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();

        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 45, 10, 10));

        TextField firstNameTextField = new TextField();
        firstNameTextField
                .setPromptText("first name");

        TextField lastNameTextField = new TextField();
        lastNameTextField
                .setPromptText("last name");

        TextField userNameTextField = new TextField();
        userNameTextField
                .setPromptText("user name");

        TextField emailTextField = new TextField();
        emailTextField
                .setPromptText("email");

        PasswordField passwordField = new PasswordField();
        passwordField
                .setPromptText("password");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField
                .setPromptText("confirm password");

        // x, y
        grid.add(firstNameTextField, 1, 0);
        grid.add(new Label("First name: "), 0, 0);

        grid.add(lastNameTextField, 1, 1);
        grid.add(new Label("Last name: "), 0, 1);

        grid.add(userNameTextField, 1, 2);
        grid.add(new Label("Username: "), 0, 2);

        grid.add(emailTextField, 1, 3);
        grid.add(new Label("Email: "), 0, 3);

        grid.add(passwordField, 1, 4);
        grid.add(new Label("Password: "), 0, 4);

        grid.add(confirmPasswordField, 1, 5);
        grid.add(new Label("Confirm password: "), 0, 5);

        Node createAccountButton = dialog
                .getDialogPane()
                .lookupButton(createAccountButtonType);

        createAccountButton
                .setDisable(true);

        {
            firstNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                createAccountButton
                        .setDisable(newValue.trim()
                                // Short set check!
                                .isEmpty() || lastNameTextField.getText().trim()
                                .isEmpty() || userNameTextField.getText().trim()
                                .isEmpty() || emailTextField.getText().trim()
                                .isEmpty() || passwordField.getText().trim()
                                .isEmpty() || confirmPasswordField.getText().trim()
                                .isEmpty() || !passwordField.getText().trim()
                                .equals(confirmPasswordField.getText().trim()));
            });

            lastNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                createAccountButton
                        .setDisable(newValue.trim()
                                .isEmpty() || firstNameTextField.getText().trim()
                                .isEmpty() || userNameTextField.getText().trim()
                                .isEmpty() || emailTextField.getText().trim()
                                .isEmpty() || passwordField.getText().trim()
                                .isEmpty() || confirmPasswordField.getText().trim()
                                .isEmpty() || !passwordField.getText().trim()
                                .equals(confirmPasswordField.getText().trim()));
            });

            userNameTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                createAccountButton
                        .setDisable(newValue.trim()
                                .isEmpty() || lastNameTextField.getText().trim()
                                .isEmpty() || firstNameTextField.getText().trim()
                                .isEmpty() || emailTextField.getText().trim()
                                .isEmpty() || passwordField.getText().trim()
                                .isEmpty() || confirmPasswordField.getText().trim()
                                .isEmpty() || !passwordField.getText().trim()
                                .equals(confirmPasswordField.getText().trim()));
            });

            passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
                createAccountButton
                        .setDisable(newValue.trim()
                                .isEmpty() || lastNameTextField.getText().trim()
                                .isEmpty() || userNameTextField.getText().trim()
                                .isEmpty() || firstNameTextField.getText().trim()
                                .isEmpty() || emailTextField.getText().trim()
                                .isEmpty() || confirmPasswordField.getText().trim()
                                .isEmpty() || !passwordField.getText().trim()
                                .equals(confirmPasswordField.getText().trim()));
            });

            confirmPasswordField.textProperty().addListener((observable, oldValue, newValue) -> {
                createAccountButton
                        .setDisable(newValue.trim()
                                .isEmpty() || lastNameTextField.getText().trim()
                                .isEmpty() || userNameTextField.getText().trim()
                                .isEmpty() || firstNameTextField.getText().trim()
                                .isEmpty() || emailTextField.getText().trim()
                                .isEmpty() || passwordField.getText().trim()
                                .isEmpty() || !passwordField.getText().trim()
                                .equals(confirmPasswordField.getText().trim()));
            });
        }

        dialog.getDialogPane()
                .setContent(grid);

        Platform.runLater(firstNameTextField::requestFocus);

        dialog.setResultConverter(dialogButton ->
                dialogButton == createAccountButtonType);

        Optional<Boolean> result = dialog.showAndWait();

        result.ifPresent(isCreateAccountButtonClicked -> {
            if (isCreateAccountButtonClicked) {
                try {
                    mainService
                            .addNewUser(userNameTextField.getText().trim(), passwordField.getText(), firstNameTextField.getText().trim(), lastNameTextField.getText().trim(), emailTextField.getText().trim());
                    loginTextField
                            .setText(userNameTextField.getText().trim());
                    passwordTextField
                            .setText(passwordField.getText().trim());
                    checkLoginButtonAvailability(null);
                } catch (UserExistsException | EmailExistsException e) {
                    showErrorAlert(e);
                }
            }
        });
    }
}
