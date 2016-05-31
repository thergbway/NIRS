package client.view;

import client.model.TableFile;
import client.utils.ListenableFileInputStream;
import client.utils.MainServiceAPIFinder;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
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
import nirs.api.model.FileInfo;
import nirs.api.model.UserInfo;
import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;

public final class ViewController {

    private final MainService mainService = MainServiceAPIFinder.findProxy();
    public Label userInfoLabel;
    public AnchorPane mainPane;
    public AnchorPane welcomePane;
    public TableView<TableFile> tableView;
    public TableColumn<TableFile, String> statusColumn;
    public TableColumn<TableFile, String> cipherColumn;
    public TableColumn<TableFile, String> filenameColumn;
    public TableColumn<TableFile, String> sizeColumn;
    public TableColumn<TableFile, String> createdColumn;

    public Button loginButton;
    public Button downloadButton;

    public TextField loginTextField;
    public PasswordField passwordTextField;

    private String sessionToken;

    @FXML
    public void initialize() {

        Consumer<KeyEvent> credentialVerifier = keyEvent -> {
            if (loginTextField.getText().trim().isEmpty() || passwordTextField.getText().trim().isEmpty())
                loginButton.setDisable(true);
            else
                loginButton.setDisable(false);

            if (keyEvent.getCode().equals(KeyCode.ENTER))
                loginButton.fire();
        };

        loginTextField
            .setOnKeyReleased(credentialVerifier::accept);

        passwordTextField
            .setOnKeyReleased(credentialVerifier::accept);

        tableView.getSelectionModel().getSelectedItems().addListener((ListChangeListener<TableFile>) c -> {
            if (tableView.getSelectionModel().getSelectedItems().size() == 1)
                downloadButton.setDisable(false);
            else downloadButton.setDisable(true);
        });

        filenameColumn
            .setCellValueFactory(param -> param.getValue().fileName);
        createdColumn
            .setCellValueFactory(param -> param.getValue().created);
        sizeColumn
            .setCellValueFactory(param -> param.getValue().size);
        cipherColumn
            .setCellValueFactory(param -> param.getValue().cipher);
        statusColumn
            .setCellValueFactory(param -> param.getValue().status);
    }

    public void onLoginRequest() {
        try {
            sessionToken = mainService
                .getToken(loginTextField.getText().trim(), passwordTextField.getText().trim());

            UserInfo userInfo = mainService.getUserInfo(sessionToken);

            StringBuilder userInfoLabelTextBuilder = new StringBuilder()
                .append(userInfo.getFirstName())
                .append(" ")
                .append(userInfo.getLastName())
                .append(" (")
                .append(userInfo.getUsername())
                .append(", ")
                .append(userInfo.getEmail())
                .append(")");

            userInfoLabel
                .setText(userInfoLabelTextBuilder.toString());

            loadTableViewContent();

            welcomePane.setVisible(false);
            mainPane.setVisible(true);

        } catch (InvalidCredentialsException | InvalidTokenException e) {
            showErrorAlert(e);
        }
    }

    public void onLogoutRequest() {
        mainPane.setVisible(false);

        loginTextField
            .clear();
        passwordTextField
            .clear();
        loginButton
            .setDisable(true);

        welcomePane.setVisible(true);
    }

    public void onSignInRequest() {
        showSignInAlert(null);
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

        dialog.setHeaderText("Fill the form below to sign in");

        ButtonType createAccountButtonType = new ButtonType("Create account", ButtonBar.ButtonData.OK_DONE);

        dialog.getDialogPane()
            .getButtonTypes()
            .addAll(createAccountButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();

        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 45, 10, 10));

        TextField firstNameTextField = new TextField();
        firstNameTextField.setPromptText("first name");

        TextField lastNameTextField = new TextField();
        lastNameTextField.setPromptText("last name");

        TextField usernameTextField = new TextField();
        usernameTextField.setPromptText("user name");

        TextField emailTextField = new TextField();
        emailTextField.setPromptText("email");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("password");

        PasswordField confirmPasswordField = new PasswordField();
        confirmPasswordField.setPromptText("confirm password");

        if (userInfo != null) {
            firstNameTextField
                .setText(userInfo.getFirstName());
            lastNameTextField
                .setText(userInfo.getLastName());
            usernameTextField
                .setText(userInfo.getUsername());
            emailTextField
                .setText(userInfo.getEmail());
        } else {
            usernameTextField
                .setText(loginTextField.getText().trim());
            passwordField
                .setText(passwordTextField.getText().trim());
        }

        // x, y
        grid.add(firstNameTextField, 1, 0);
        grid.add(new Label("First name: "), 0, 0);

        grid.add(lastNameTextField, 1, 1);
        grid.add(new Label("Last name: "), 0, 1);

        grid.add(usernameTextField, 1, 2);
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

        ChangeListener<String> createAccountButtonAvailabilityChecker = (observable, oldValue, newValue) -> createAccountButton.setDisable(
            firstNameTextField.getText().trim().isEmpty()
                || lastNameTextField.getText().trim().isEmpty()
                || usernameTextField.getText().trim().isEmpty()
                || emailTextField.getText().trim().isEmpty()
                || passwordField.getText().trim().isEmpty()
                || confirmPasswordField.getText().trim().isEmpty()
                || !passwordField.getText().trim().equals(confirmPasswordField.getText().trim()));

        firstNameTextField.textProperty().addListener(createAccountButtonAvailabilityChecker::changed);
        lastNameTextField.textProperty().addListener(createAccountButtonAvailabilityChecker::changed);
        usernameTextField.textProperty().addListener(createAccountButtonAvailabilityChecker::changed);
        passwordField.textProperty().addListener(createAccountButtonAvailabilityChecker::changed);
        confirmPasswordField.textProperty().addListener(createAccountButtonAvailabilityChecker::changed);

        dialog.getDialogPane()
            .setContent(grid);

        dialog.setResultConverter(dialogButton ->
            dialogButton == createAccountButtonType);

        Optional<Boolean> result = dialog.showAndWait();

        result.ifPresent(isCreateAccountButtonClicked -> {
            if (isCreateAccountButtonClicked) {
                try {
                    mainService
                        .addNewUser(usernameTextField.getText().trim(), passwordField.getText(),
                            firstNameTextField.getText().trim(), lastNameTextField.getText().trim(),
                            emailTextField.getText().trim());
                    loginTextField
                        .setText(usernameTextField.getText().trim());
                    passwordTextField
                        .setText(passwordField.getText().trim());
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
                        .username(usernameTextField.getText().trim())
                        .build());
                }
            }
        });
    }

    public void onDownloadFileRequest() {

        TableFile selectedFile = tableView
            .getSelectionModel()
            .getSelectedItem();

        FileChooser fileChooser = new FileChooser();

        fileChooser
            .setInitialFileName(selectedFile.fileName.get());

        fileChooser
            .setTitle("Save file");

        File fileToSave = fileChooser
            .showSaveDialog(new Stage());

        if (fileToSave != null)
            showCipherKeyAlert()
                .ifPresent(key -> {

                    Consumer<Integer> progressConsumer = progress ->
                        selectedFile
                            .status
                            .set("DOWNLOADING " + String.valueOf(progress) + " % ");

                    new Thread(() -> {
                        try {
                            selectedFile.status.set("PREPARING...");

                            javax.crypto.Cipher cipher = null;
                            SecretKeySpec secretKeySpec = null;

                            Cipher fileCipher = Cipher.valueOf(selectedFile.cipher.get());
                            switch (fileCipher) {
                                case AES128:
                                    cipher = javax.crypto.Cipher.getInstance("AES");
                                    secretKeySpec = new SecretKeySpec(Arrays.copyOf(DigestUtils.sha256(key), 16),
                                        "AES");
                                    cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKeySpec);
                                    break;
                                case AES256:
                                    cipher = javax.crypto.Cipher.getInstance("AES");
                                    secretKeySpec = new SecretKeySpec(Arrays.copyOf(DigestUtils.sha256(key), 16),
                                        "AES");
                                    cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKeySpec);
                                    break;
                                case DES:
                                    cipher = javax.crypto.Cipher.getInstance("DES/CBC/PKCS5Padding");
                                    secretKeySpec = new SecretKeySpec(Arrays.copyOf(DigestUtils.sha256(key),8),
                                        "DES");
                                    cipher.init(javax.crypto.Cipher.DECRYPT_MODE, secretKeySpec,
                                        new IvParameterSpec(new byte[]{1,2,3,4,5,6,7,8}));
                                    break;
                            }

                            try (

                                FileOutputStream fos = new FileOutputStream(fileToSave);
                                CipherOutputStream cos = new CipherOutputStream(fos, cipher)
                            ) {
                                int offset = 0;

                                double factor = 100.0 / selectedFile.getFileSize();

                                long lastCheckTimeMillis = System.currentTimeMillis();

                                byte[] filePart;
                                do {

                                    filePart = mainService
                                        .downloadFilePart(sessionToken, selectedFile.getId(), offset);

                                    cos.write(filePart);

                                    long currentTimeMillis = System.currentTimeMillis();

                                    if ((currentTimeMillis - lastCheckTimeMillis) > 50L) {
                                        progressConsumer.accept((int) ((double) offset * factor));

                                        lastCheckTimeMillis = currentTimeMillis;
                                    }

                                    offset += filePart.length;

                                } while (filePart.length != 0);

                                selectedFile
                                    .status
                                    .set("OK");

                            } catch (Exception e) {
                                showErrorAlert(e);
                            }
                        } catch (Exception e) {
                            showErrorAlert(e);
                        }
                    }).start();

                });
    }

    //todo
    public void onAddFileRequest() {

        FileChooser fileChooser = new FileChooser();

        fileChooser
            .setTitle("Choose file to upload");

        File fileToUpload = fileChooser
            .showOpenDialog(new Stage());

        if (fileToUpload != null)
            showChooseCipherAlert()
                .ifPresent(cipherEnum ->
                    showCipherKeyAlert()
                        .ifPresent(key -> {

                            TableFile tableFile = new TableFile(fileToUpload.getName(), 0L, 0L, cipherEnum, null, "N/A");

                            tableView
                                .getItems()
                                .add(tableFile);

                            Consumer<Integer> progressConsumer = progress ->
                                tableFile
                                    .status
                                    .set("UPLOADING " + String.valueOf(progress) + "%");

                            new Thread(() -> {
                                try {
                                    tableFile.status.set("PREPARING...");

                                    javax.crypto.Cipher cipher = null;
                                    SecretKeySpec secretKeySpec = null;

                                    switch (cipherEnum) {
                                        case AES128:
                                            cipher = javax.crypto.Cipher.getInstance("AES");
                                            secretKeySpec = new SecretKeySpec(Arrays.copyOf(DigestUtils.sha256(key), 16),
                                                "AES");
                                            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKeySpec);
                                            break;
                                        case AES256:
                                            cipher = javax.crypto.Cipher.getInstance("AES");
                                            secretKeySpec = new SecretKeySpec(Arrays.copyOf(DigestUtils.sha256(key), 16),
                                                "AES");
                                            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKeySpec);
                                            break;
                                        case DES:
                                            cipher = javax.crypto.Cipher.getInstance("DES/CBC/PKCS5Padding");
                                            secretKeySpec = new SecretKeySpec(Arrays.copyOf(DigestUtils.sha256(key),8),
                                                "DES");
                                            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, secretKeySpec,
                                                new IvParameterSpec(new byte[]{1,2,3,4,5,6,7,8}));
                                            break;
                                    }

                                    InputStream fis = ListenableFileInputStream.newListenableStream(fileToUpload, progressConsumer);
                                    BufferedInputStream bis = new BufferedInputStream(fis);
                                    CipherInputStream cis = new CipherInputStream(bis, cipher);

                                    FileInfo uploadFileInfo = mainService.uploadFile(sessionToken,
                                        fileToUpload.getName(), cipherEnum,
                                        cis);

                                    tableFile.update(uploadFileInfo);

                                    tableFile
                                        .status
                                        .set("OK");

                                } catch (Exception e) {
                                    showErrorAlert(e);
                                }
                            }).start();
                        }));
    }

    public void onDeleteFileRequest() {
        if (tableView.getSelectionModel() != null)
            try {
                TableView.TableViewSelectionModel<TableFile> selectionModel = tableView
                    .getSelectionModel();
                mainService
                    .deleteFile(sessionToken, selectionModel
                        .getSelectedItem()
                        .getId());
                tableView
                    .getItems()
                    .remove(selectionModel.getFocusedIndex());
            } catch (InvalidTokenException e) {
                showErrorAlert(e);
            }
    }

    private void showErrorAlert(Exception e) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);

            alert.setTitle("Error!");
            alert.setHeaderText(e.getMessage());
            alert.setContentText("Please try again");

            Stage alertWindowStage = (Stage) alert
                .getDialogPane()
                .getScene()
                .getWindow();

            alertWindowStage
                .getIcons()
                .add(new Image(getClass().getResource("/error.png").toString()));

            alert.showAndWait();
        });
    }

    private void loadTableViewContent() {
        try {

            tableView
                .getItems()
                .clear();

            mainService
                .getFiles(sessionToken)
                .forEach(fileInfo ->
                    tableView
                        .getItems()
                        .add(new TableFile(fileInfo.getFilename(), fileInfo.getCreatedTimestamp(),
                            fileInfo.getSize(), fileInfo.getCipher(), fileInfo.getId(), "OK")));
        } catch (InvalidTokenException e) {
            showErrorAlert(e);
        }
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

        dialog.setTitle("Key");
        dialog.setHeaderText("Type symmetric key");

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
            return null;//fixme may be NPE
        });

        return dialog
            .showAndWait()
            .map(String::getBytes);
    }
}
