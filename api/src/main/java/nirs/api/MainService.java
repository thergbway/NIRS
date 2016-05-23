package nirs.api;

import nirs.api.exceptions.EmailExistsException;
import nirs.api.exceptions.InvalidCredentialsException;
import nirs.api.exceptions.InvalidTokenException;
import nirs.api.exceptions.UserExistsException;
import nirs.api.model.FileInfo;
import nirs.api.model.UserInfo;

import java.io.InputStream;
import java.util.List;

public interface MainService {

    void addNewUser(String username, String password, String firstName, String lastName, String email)
        throws UserExistsException, EmailExistsException;

    String getToken(String username, String password) throws InvalidCredentialsException;

    UserInfo getUserInfo(String token) throws InvalidTokenException;

    List<FileInfo> getFiles(String token) throws InvalidTokenException;

    void deleteFile(String token, String id) throws InvalidTokenException;

    InputStream downloadFile(String token, String id) throws InvalidTokenException;

    void uploadFile(String token, String filename, Cipher cipher, InputStream in) throws InvalidTokenException;
}