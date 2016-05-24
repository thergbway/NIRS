package nirs.service;

import com.github.javafaker.Faker;
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
import nirs.dao.FilesService;
import nirs.dao.UserService;
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
    private GridFS mongoGridFS;

    @Autowired
    private UserService userService;

    @Autowired
    private FilesService filesService;

    private Storage storage = new Storage();

    public String uploadToMongo(String filename, InputStream is) throws IOException {
        try {

            GridFSInputFile file = mongoGridFS.createFile(is, filename);
            file.save();

            return file.toString();
        } finally {
            is.close();
        }
    }

    @Override
    public void addNewUser(String username, String password, String firstName, String lastName, String email) throws UserExistsException, EmailExistsException {
        if(userService.isUsernamePresented(username))
            throw new UserExistsException(username);
        if(userService.isEmailPresented(email))
            throw new EmailExistsException(email);

        userService.createUser(username, password, firstName, lastName, email);
    }

    @Override
    public String getToken(String username, String password) throws InvalidCredentialsException {
        if (!userService.isValidCredentials(username, password))
            throw new InvalidCredentialsException();

        return userService.createToken(username);
    }

    @Override
    public UserInfo getUserInfo(String token) throws InvalidTokenException {
        if (!userService.isValidToken(token))
            throw new InvalidTokenException();

        return userService.getUserInfo(token);
    }

    @Override
    public List<FileInfo> getFiles(String token) throws InvalidTokenException {
        if (!userService.isValidToken(token))
            throw new InvalidTokenException();

        return storage.fileInfos;
    }

    @Override
    public FileInfo getFile(String token, String id) throws InvalidTokenException {
        return FileInfo.builder()
            .id("as")
            .filename("sdf.txt")
            .size(122L)
            .cipher(Cipher.AES128)
            .createdTimestamp(1324234534L)
            .build();
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
    public String uploadFile(String token, String filename, Cipher cipher, InputStream in) throws InvalidTokenException {
        if (!storage.token.equals(token))
            throw new InvalidTokenException();

        Random rand = new Random();

        String idToBeAdded = String.valueOf(storage.fileInfos.size());
        storage.fileInfos
            .add(FileInfo.builder()
                .id(idToBeAdded)
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

        return idToBeAdded;
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

            System.out.println("Storage is generated");
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
                        .filename(faker.app().name() + "." + faker.hacker().abbreviation().toLowerCase())
                        .createdTimestamp(Instant.now().minus(rand.nextInt(1500), ChronoUnit.DAYS).getEpochSecond())
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