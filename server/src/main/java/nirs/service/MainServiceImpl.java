package nirs.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;

@Service
public class MainServiceImpl implements MainService {

    @Autowired
    private DB nirsDB;

    private Storage storage = new Storage();

    public String upload(String filename, InputStream is) throws IOException {
        int bytesRead = 0;
        try {
            byte[] bytes = new byte[1024 * 1024 * 5];
            int currReadBytes;
            while ((currReadBytes = is.read(bytes)) != -1) {
                bytesRead += currReadBytes;
                if ((bytesRead % (1024 * 1024 * 5)) == 0)
                    System.out.println("... " + (double) bytesRead / 1024.0 / 1024.0 + " MB read");
            }
        } finally {
            is.close();
        }

        System.out.println("Read " + bytesRead + " bytes");

        return "Read " + bytesRead + " bytes";
    }

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
        if(storage.username.equals(username))
            throw new UserExistsException(username);
        if(storage.email.equals(email))
            throw new EmailExistsException(email);

        storage.username = username;
        storage.password = password;
        storage.firstName = firstName;
        storage.lastName = lastName;
        storage.email = email;

        storage.generateNewToken();
    }

    @Override
    public String getToken(String username, String password) throws InvalidCredentialsException {
        if(!storage.username.equals(username) || !storage.password.equals(password))
            throw new InvalidCredentialsException();

        return storage.token;
    }

    @Override
    public UserInfo getUserInfo(String token) throws InvalidTokenException {
        if(!storage.token.equals(token))
            throw new InvalidTokenException();

        return new UserInfo(storage.firstName, storage.lastName, storage.username, storage.email);
    }

    @Override
    public List<FileInfo> getFiles(String token) throws InvalidTokenException {
        return null;
    }

    @Override
    public void deleteFile(String token, String id) throws InvalidTokenException {

    }

    @Override
    public InputStream downloadFile(String token, String id) throws InvalidTokenException {
        return null;
    }

    @Override
    public void uploadFile(String token, String filename, Cipher cipher, InputStream in) throws InvalidTokenException {

    }

    private class Storage {
        public String username = "Zetro";
        public String password = "123";
        public String firstName = "Dmitry";
        public String lastName = "Korobov";
        public String email = "zps@gmail.com";
        public String token;
        public List<FileInfo> fileInfos;
        public List<byte[]> fileContents;

        public Storage() {
            generateNewToken();
            generateFiles();
        }

        public void generateFiles() {

        }

        public void generateNewToken() {
            token = String.valueOf(((Double) (new Random().nextDouble())).hashCode());
        }



    }
}