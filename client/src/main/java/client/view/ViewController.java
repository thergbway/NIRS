package client.view;

import client.model.TableFile;
import client.utils.ListenableFileInputStream;
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
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import nirs.api.Cipher;
import nirs.api.MainService;
import nirs.api.exceptions.EmailExistsException;
import nirs.api.exceptions.InvalidCredentialsException;
import nirs.api.exceptions.InvalidTokenException;
import nirs.api.exceptions.UserExistsException;
import nirs.api.model.UserInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static client.model.TableFileStatus.*;

public class ViewController implements Initializable {

    @FXML
    private Label userInfoLabel;
    @FXML
    private AnchorPane tableViewPane;
    @FXML
    private AnchorPane loginPane;

    @FXML
    private TableView<TableFile> fileTableView;

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
    private TextField loginTextField;
    @FXML
    private PasswordField passwordTextField;

    private ExecutorService executorService;

    private MainService mainService;

    private String token;

    public void onLogoutRequest() {
        resetToken();
        resetFileTableViewControls();
        setLoginPaneVisible(true);
    }

    public void onLoginRequest() {
        try {
            token = mainService
                    .getToken(loginTextField.getText().trim(), passwordTextField.getText().trim());

            UserInfo userInfo = mainService
                    .getUserInfo(token);

            StringBuilder userInfoLabelTextBuilder = new StringBuilder(userInfo
                    .getFirstName()
                    .concat(" ")
                    .concat(userInfo.getLastName()));

            userInfoLabelTextBuilder
                    .append(" (")
                    .append(userInfo.getUsername())
                    .append(", ")
                    .append(userInfo.getEmail())
                    .append(")");

            userInfoLabel
                    .setText(userInfoLabelTextBuilder.toString());

            setColumnCellValueFactory();

            loadFileTableViewContent();

            setLoginPaneVisible(false);

        } catch (InvalidCredentialsException | InvalidTokenException e) {
            showErrorAlert(e);
        }
    }

    public void onSignInRequest() {
        showSignInAlert(null);
    }

    public void onDownloadFileRequest() {

        TableFile selectedFile = fileTableView
                .getSelectionModel()
                .getSelectedItem();

        if (selectedFile != null) downloadFile(selectedFile);

    }

    public void onAddFileRequest() {
        uploadFile();
    }

    public void onDeleteFileRequest() {
        if (fileTableView.getSelectionModel() != null)
            deleteSelectedTableFile();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        mainService = MainServiceAPIFinder
                .findProxy();

        executorService = Executors
                .newFixedThreadPool(100);

        loginTextField
                .setOnKeyReleased(this::checkLoginButtonAvailability);
        passwordTextField
                .setOnKeyReleased(this::checkLoginButtonAvailability);

        checkLoginButtonAvailability(null);

        setLoginPaneVisible(true);
    }

    public void shutdownExecutorService() {
        executorService
                .shutdown();
    }

    private void setColumnCellValueFactory() {
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
        alert.setContentText("Please try again");

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

    private void resetFileTableViewControls() {
        userInfoLabel
                .setText("");
    }

    private void loadFileTableViewContent() {
        try {

            fileTableView
                    .getItems()
                    .clear();

            mainService
                    .getFiles(token)
                    .forEach(fileInfo ->
                            fileTableView
                                    .getItems()
                                    .add(new TableFile(fileInfo.getFilename(), fileInfo.getCreatedTimestamp(), fileInfo.getSize(), fileInfo.getCipher(), fileInfo.getId())));
        } catch (InvalidTokenException e) {
            showErrorAlert(e);
        }
    }

    private void showSignInAlert(UserInfo userInfo) {
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

        if (userInfo != null) {
            firstNameTextField
                    .setText(userInfo.getFirstName());
            lastNameTextField
                    .setText(userInfo.getLastName());
            userNameTextField
                    .setText(userInfo.getUsername());
            emailTextField
                    .setText(userInfo.getEmail());
        } else {
            userNameTextField
                    .setText(loginTextField.getText().trim());
            passwordField
                    .setText(passwordTextField.getText().trim());
        }

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
                } catch (UserExistsException e) {
                    showErrorAlert(e);
                    showSignInAlert(UserInfo.builder()
                            .firstName(firstNameTextField.getText().trim())
                            .lastName(lastNameTextField.getText().trim())
                            .email(emailTextField.getText().trim())
                            .build());
                } catch (EmailExistsException e) {
                    showErrorAlert(e);
                    showSignInAlert(UserInfo.builder()
                            .firstName(firstNameTextField.getText().trim())
                            .lastName(lastNameTextField.getText().trim())
                            .username(userNameTextField.getText().trim())
                            .build());
                }
            }
        });
    }

    private void downloadFile(TableFile selectedFile) {

        FileChooser fileChooser = new FileChooser();

        fileChooser
                .setInitialFileName(selectedFile.fileName.get());

        fileChooser
                .setTitle("Save file");

        File fileToSave = fileChooser
                .showSaveDialog(new Stage());

        if (fileToSave != null)
            showChooseCipherAlert()
                    .ifPresent(cipher ->
                            showCipherKeyAlert()
                                    .ifPresent(key -> {

                                        selectedFile
                                                .status
                                                .set(String.valueOf(DECRYPTING));

                                        Consumer<Integer> progressConsumer = progress ->
                                                selectedFile
                                                        .status
                                                        .set(String.valueOf(DOWNLOADING).concat(" ").concat(String.valueOf(progress).concat("%")));

                                        executorService.execute(() -> {

                                            try (FileOutputStream fileOutputStream = new FileOutputStream(fileToSave)) {

                                                int offset = 0;

                                                double factor = 100.0 / selectedFile
                                                        .getFileSize();

                                                long lastCheckTimeMillis = System
                                                        .currentTimeMillis();

                                                do {

                                                    byte[] filePart = mainService
                                                            .downloadFilePart(token, selectedFile.getId(), offset);

                                                    fileOutputStream
                                                            .write(filePart);

                                                    long currentTimeMillis = System
                                                            .currentTimeMillis();

                                                    if ((currentTimeMillis - lastCheckTimeMillis) > 50L) {
                                                        progressConsumer
                                                                .accept((int)((double) offset * factor));

                                                        lastCheckTimeMillis = currentTimeMillis;
                                                    }

                                                    offset += filePart.length;


                                                } while (offset < selectedFile.getFileSize());

                                                selectedFile
                                                        .status
                                                        .set(String.valueOf(OK));

                                            } catch (IOException | InvalidTokenException e) {
                                                showErrorAlert(e);
                                            }

                                        });

                                    }));

    }

    private void uploadFile() {

        FileChooser fileChooser = new FileChooser();

        fileChooser
                .setTitle("Choose file to upload");

        File fileToUpload = fileChooser
                .showOpenDialog(new Stage());

        if (fileToUpload != null)
            showChooseCipherAlert()
                    .ifPresent(cipher ->
                            showCipherKeyAlert()
                                    .ifPresent(key -> {

                                        TableFile tableFile = new TableFile(fileToUpload.getName(), 0L, 0L, cipher, null);

                                        fileTableView
                                                .getItems()
                                                .add(tableFile);

                                        tableFile
                                                .status
                                                .set(String.valueOf(ENCRYPTING));

                                        Consumer<Integer> progressConsumer = progress ->
                                                tableFile
                                                        .status
                                                        .set(String.valueOf(UPLOADING).concat(" ").concat(String.valueOf(progress)).concat("%"));

                                        executorService
                                                .execute(() -> {

                                                    try {

                                                        tableFile
                                                                .update(mainService
                                                                .uploadFile(token, fileToUpload.getName(), cipher, ListenableFileInputStream.newListenableStream(fileToUpload, progressConsumer)));

                                                        tableFile
                                                                .status
                                                                .set(String.valueOf(OK));

                                                    } catch (InvalidTokenException | IOException e) {
                                                        showErrorAlert(e);
                                                    }

                                                });
                                    }));
    }

    private Optional<Cipher> showChooseCipherAlert() {

        ChoiceDialog<Cipher> dialog = new ChoiceDialog<>(Cipher.AES256, Cipher.values());

        dialog.setTitle("Choose cipher");
        dialog.setHeaderText("Please choose algorithm to encrypt the file");
        dialog.setContentText("Choose cipher:");

        return dialog.showAndWait();
    }

    private Optional<byte[]> showCipherKeyAlert() {
        // Create the custom dialog.
        Dialog<String> dialog = new Dialog<>();

        dialog.setTitle("Login Dialog");
        dialog.setHeaderText("Look, a Custom Login Dialog");

        ButtonType okButtonType = new ButtonType("Ok", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane()
                .getButtonTypes()
                .addAll(okButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 70, 10, 10));

        PasswordField symmetricKeyTextField = new PasswordField();
        symmetricKeyTextField
                .setPromptText("Key");

        grid.add(new Label("Symmetric key:"), 0, 1);
        grid.add(symmetricKeyTextField, 1, 1);

        Node okButton = dialog
                .getDialogPane()
                .lookupButton(okButtonType);

        okButton.setDisable(true);

        symmetricKeyTextField
                .textProperty()
                .addListener((observable, oldValue, newValue) -> {
                    okButton.setDisable(newValue.trim().isEmpty());
                });

        dialog.getDialogPane()
                .setContent(grid);

        Platform.runLater(symmetricKeyTextField::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return symmetricKeyTextField.getText();
            }
            return null;
        });

        return dialog
                .showAndWait()
                .map(String::getBytes);
    }

    private void deleteSelectedTableFile() {
        try {
            TableView.TableViewSelectionModel<TableFile> selectionModel = fileTableView
                    .getSelectionModel();
            mainService
                    .deleteFile(token, selectionModel
                            .getSelectedItem()
                            .getId());
            fileTableView
                    .getItems()
                    .remove(selectionModel.getFocusedIndex());
        } catch (InvalidTokenException e) {
            showErrorAlert(e);
        }
    }
}
