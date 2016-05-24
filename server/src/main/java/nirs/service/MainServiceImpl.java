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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.function.Consumer;

@Service
public class MainServiceImpl implements MainService {

    @Autowired
    private UserService userService;

    @Autowired
    private FilesService filesService;

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

        String username = userService.getUsername(token);

        return filesService.getFiles(username);
    }

    @Override
    public FileInfo getFile(String token, String id) throws InvalidTokenException {
        if (!userService.isValidToken(token))
            throw new InvalidTokenException();

        return filesService.getFile(id);
    }

    @Override
    public void deleteFile(String token, String id) throws InvalidTokenException {
        if (!userService.isValidToken(token))
            throw new InvalidTokenException();

        filesService.deleteFile(id);
    }

    @Override
    public InputStream downloadFile(String token, String id) throws InvalidTokenException {
        if (!userService.isValidToken(token))
            throw new InvalidTokenException();

        return filesService.getFileInputStream(id);
    }

    @Override
    public FileInfo uploadFile(String token, String filename, Cipher cipher, InputStream in) throws InvalidTokenException {
        if (!userService.isValidToken(token))
            throw new InvalidTokenException();

        String username = userService.getUsername(token);

        return filesService.uploadFile(username, filename, cipher, in);
    }

    @Override
    public void test(Consumer<InputStream> consumer) {
        consumer.accept(filesService.getFileInputStream("5743ef6d296c0e1cac1a3b83"));
    }
}