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

@Service
public class MainServiceImpl implements MainService {

    @Autowired
    private DB nirsDB;

    @Override
    public String getGreeting(String name) {
        return "Hello " + name + "!";
    }

    @Override
    public String upload(String filename, InputStream is) throws IOException {
        int bytesRead = 0;
        try {
            byte[] bytes = new byte[1024*1024*5];
            int currReadBytes;
            while ((currReadBytes = is.read(bytes)) != -1) {
                bytesRead += currReadBytes;
                if((bytesRead % (1024*1024*5)) == 0)
                    System.out.println("... " + (double)bytesRead/1024.0/1024.0 + " MB read");
            }
        } finally {
            is.close();
        }

        System.out.println("Read " + bytesRead + " bytes");

        return "Read " + bytesRead + " bytes";
    }

    @Override
    public String uploadToMongo(String filename, InputStream is) throws IOException {
        GridFS fs = new GridFS(nirsDB);

        try {

            GridFSInputFile file = fs.createFile(is, filename);
            file.save();

            return file.toString();
        }finally {
            is.close();
        }
    }

    @Override
    public void addNewUser(String username, String password, String firstName, String lastName, String email) throws UserExistsException, EmailExistsException {

    }

    @Override
    public String getToken(String username, String password) throws InvalidCredentialsException {
        return null;
    }

    @Override
    public UserInfo getUserInfo(String token) throws InvalidTokenException {
        return null;
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
}