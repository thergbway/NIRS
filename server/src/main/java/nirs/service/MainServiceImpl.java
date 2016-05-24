package nirs.service;

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
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;

@Service
public class MainServiceImpl implements MainService {
    private static final Logger logger = Logger.getLogger(MainServiceImpl.class);

    @Autowired
    private UserService userService;

    @Autowired
    private FilesService filesService;

    @Override
    public void addNewUser(String username, String password, String firstName, String lastName, String email) throws UserExistsException, EmailExistsException {
        logger.debug(String.format("addNewUser() executing. Params: username = %1s, password = ?, firstName = %2s, " +
            "lastName = %3s, email = %4s", username, firstName, lastName, email));
        if (userService.isUsernamePresented(username)) {
            logger.debug(".....username exists exception");
            throw new UserExistsException(username);
        }
        if (userService.isEmailPresented(email)) {
            logger.debug(".....email exists exception");
            throw new EmailExistsException(email);
        }

        userService.createUser(username, password, firstName, lastName, email);
        logger.debug(".....success");
    }

    @Override
    public String getToken(String username, String password) throws InvalidCredentialsException {
        logger.debug(String.format("getToken() executing. Params: username = %1s, password = ?", username));
        if (!userService.isValidCredentials(username, password)) {
            logger.debug(".....invalid credentials exception");
            throw new InvalidCredentialsException();
        }

        String token = userService.createToken(username);
        logger.debug(".....success");
        return token;
    }

    @Override
    public UserInfo getUserInfo(String token) throws InvalidTokenException {
        logger.debug(String.format("getUserInfo() executing. Params: token = %1s", token));
        if (!userService.isValidToken(token)) {
            logger.debug(".....invalid token exception");
            throw new InvalidTokenException();
        }

        UserInfo userInfo = userService.getUserInfo(token);
        logger.debug(".....success");
        return userInfo;
    }

    @Override
    public List<FileInfo> getFiles(String token) throws InvalidTokenException {
        logger.debug(String.format("getFiles() executing. Params: token = %1s", token));
        if (!userService.isValidToken(token)) {
            logger.debug(".....invalid token exception");
            throw new InvalidTokenException();
        }

        String username = userService.getUsername(token);

        List<FileInfo> files = filesService.getFiles(username);
        logger.debug(String.format(".....success. Files count = %1d", files.size()));
        return files;
    }

    @Override
    public FileInfo getFile(String token, String id) throws InvalidTokenException {
        logger.debug(String.format("getFile() executing. Params: token = %1s, id = %2s", token, id));
        if (!userService.isValidToken(token)) {
            logger.debug(".....invalid token exception");
            throw new InvalidTokenException();
        }

        FileInfo file = filesService.getFile(id);
        logger.debug(".....success");
        return file;
    }

    @Override
    public void deleteFile(String token, String id) throws InvalidTokenException {
        logger.debug(String.format("deleteFile() executing. Params: token = %1s, id = %2s", token, id));
        if (!userService.isValidToken(token)) {
            logger.debug(".....invalid token exception");
            throw new InvalidTokenException();
        }

        filesService.deleteFile(id);
        logger.debug(".....success");
    }

    @Override
    public byte[] downloadFilePart(String token, String id, int offset) throws InvalidTokenException {
        logger.debug(String.format("downloadFilePart() executing. Params: token = %1s, id = %2s, offset = %3s",
            token, id, offset));
        if (!userService.isValidToken(token)) {
            logger.debug(".....invalid token exception");
            throw new InvalidTokenException();
        }

        byte[] filePart = filesService.getFilePart(id, offset);
        logger.debug(String.format(".....success. Returning %1.3f kB", (double) filePart.length / 1024.0));
        return filePart;
    }

    @Override
    public FileInfo uploadFile(String token, String filename, Cipher cipher, InputStream in) throws InvalidTokenException {
        logger.debug(String.format("uploadFile() executing. " +
            "Params: token = %1s, filename = %2s, cipher = %3s", token, filename, cipher));
        if (!userService.isValidToken(token)) {
            logger.debug(".....invalid token exception");
            throw new InvalidTokenException();
        }

        String username = userService.getUsername(token);

        FileInfo fileInfo = filesService.uploadFile(username, filename, cipher, in);
        logger.debug(".....success");
        return fileInfo;
    }
}