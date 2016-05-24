package nirs.service;

import com.github.javafaker.Faker;
import com.mongodb.DB;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSInputFile;
import nirs.api.Cipher;
import nirs.api.MainService;
import nirs.api.exceptions.EmailExistsException;
import nirs.api.exceptions.InvalidCredentialsException;
import nirs.api.exceptions.InvalidTokenException;
import nirs.api.exceptions.UserExistsException;
import nirs.api.model.FileInfo;
import nirs.api.model.UserInfo;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@Service
public class MainServiceImpl implements MainService {

    @Autowired
    private DB nirsDB;

    private Storage storage = new Storage();

    public String uploadToMongo(String filename, InputStream is) throws IOException {
        GridFS fs = new GridFS(nirsDB);

        try {

            GridFSInputFile file = fs.createFile(is, filename);
            file.save();

            return file.toString();
        } finally {
            is.close();
        }
    }

    @Override
    public void addNewUser(String username, String password, String firstName, String lastName, String email) throws UserExistsException, EmailExistsException {
        if (storage.username.equals(username))
            throw new UserExistsException(username);
        if (storage.email.equals(email))
            throw new EmailExistsException(email);

        storage.username = username;
        storage.password = password;
        storage.firstName = firstName;
        storage.lastName = lastName;
        storage.email = email;

        storage.generateNewToken();
        storage.generateFiles();
    }

    @Override
    public String getToken(String username, String password) throws InvalidCredentialsException {
        if (!storage.username.equals(username) || !storage.password.equals(password))
            throw new InvalidCredentialsException();

        return storage.token;
    }

    @Override
    public UserInfo getUserInfo(String token) throws InvalidTokenException {
        if (!storage.token.equals(token))
            throw new InvalidTokenException();
        return UserInfo.builder()
            .firstName(storage.firstName)
            .lastName(storage.lastName)
            .username(storage.username)
            .email(storage.email)
            .build();
    }

    @Override
    public List<FileInfo> getFiles(String token) throws InvalidTokenException {
        if (!storage.token.equals(token))
            throw new InvalidTokenException();

        return storage.fileInfos;
    }

    @Override
    public void deleteFile(String token, String id) throws InvalidTokenException {
        if (!storage.token.equals(token))
            throw new InvalidTokenException();

        int indexOfFileToDelete = -1;
        for (int i = 0; i < indexOfFileToDelete; i++) {
            if (storage.fileInfos.get(i).getId().equals(id)) {
                indexOfFileToDelete = i;
                break;
            }
        }

        if (indexOfFileToDelete == -1)
            return;

        storage.fileInfos.remove(indexOfFileToDelete);
        storage.fileContents.remove(indexOfFileToDelete);
    }

    @Override
    public InputStream downloadFile(String token, String id) throws InvalidTokenException {
        if (!storage.token.equals(token))
            throw new InvalidTokenException();

        int indexOfFileToDownload = -1;
        for (int i = 0; i < indexOfFileToDownload; i++) {
            if (storage.fileInfos.get(i).getId().equals(id)) {
                indexOfFileToDownload = i;
                break;
            }
        }

        byte[] content = storage.fileContents.get(indexOfFileToDownload);

        return new ByteArrayInputStream(content);
    }

    @Override
    public void uploadFile(String token, String filename, Cipher cipher, InputStream in) throws InvalidTokenException {
        if (!storage.token.equals(token))
            throw new InvalidTokenException();

        Random rand = new Random();

        storage.fileInfos
            .add(FileInfo.builder()
                .id(String.valueOf(storage.fileInfos.size()))
                .filename(filename)
                .createdTimestamp(Instant.now().getEpochSecond())
                .size((long) rand.nextInt(1024 * 1024 * 200))
                .cipher(cipher)
                .build());

        try {
            storage.fileContents.add(IOUtils.toByteArray(in));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private class Storage {
        public String username = "Zetro";
        public String password = "123";
        public String firstName = "Dmitry";
        public String lastName = "Korobov";
        public String email = "zps@gmail.com";
        public String token;
        public List<FileInfo> fileInfos = new LinkedList<>();
        public List<byte[]> fileContents = new LinkedList<>();

        public Storage() {
            generateNewToken();
            generateFiles();
        }

        public void generateFiles() {
            fileInfos.clear();
            fileContents.clear();

            Random rand = new Random();
            int filesCount = rand.nextInt(40);
            Faker faker = new Faker();

            for (int i = 0; i < filesCount; i++) {
                FileInfo fileInfo = FileInfo.builder()
                    .id(String.valueOf(i))
                    .filename(faker.app().name())
                    .createdTimestamp(Instant.now().minus(rand.nextInt(1500), ChronoUnit.DAYS).getEpochSecond())
                    .size((long) rand.nextInt(1024 * 1024 * 200))
                    .cipher(Cipher.values()[rand.nextInt(Cipher.values().length)])
                    .build();

                int textFactor = rand.nextInt(100);
                String content = faker.chuckNorris().fact();

                StringBuilder sb = new StringBuilder();
                for (int j = 0; j < textFactor; j++) {
                    sb.append("\n\n--------------------\n\n");
                    sb.append(content);
                }

                byte[] bytes = sb.toString().getBytes();

                fileInfos.add(fileInfo);
                fileContents.add(bytes);
            }
        }

        public void generateNewToken() {
            token = String.valueOf(((Double) (new Random().nextDouble())).hashCode());
        }
    }
}