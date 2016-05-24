package nirs.dao;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFS;
import com.mongodb.gridfs.GridFSDBFile;
import com.mongodb.gridfs.GridFSInputFile;
import nirs.api.Cipher;
import nirs.api.model.FileInfo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FilesService {
    @Autowired
    private GridFS mongoGridFS;

    public List<FileInfo> getFiles(String username) {

        DBObject query = new BasicDBObject("owner", username);
        DBObject sort = new BasicDBObject("uploadDate", -1).append("filename", 1);

        List<GridFSDBFile> files = mongoGridFS.find(query, sort);

        return files
            .stream()
            .map(file -> FileInfo
                    .builder()
                    .id(file.getId().toString())
                    .filename(file.getFilename())
                    .size(file.getLength())
                    .createdTimestamp(file.getUploadDate().toInstant().getEpochSecond())
                    .cipher(Cipher.valueOf((String) file.get("cipher")))
                    .build()
            )
            .collect(Collectors.toList());
    }

    public FileInfo getFile(String id) {
        GridFSDBFile file = mongoGridFS.findOne(new ObjectId(id));

        return FileInfo
            .builder()
            .id(file.getId().toString())
            .filename(file.getFilename())
            .size(file.getLength())
            .createdTimestamp(file.getUploadDate().toInstant().getEpochSecond())
            .cipher(Cipher.valueOf((String) file.get("cipher")))
            .build();
    }

    public void deleteFile(String id) {
        mongoGridFS.remove(new ObjectId(id));
    }

    public byte[] getFilePart(String id, int offset) {
        InputStream in = mongoGridFS.find(new ObjectId(id)).getInputStream();
        byte[] bytes = new byte[1024 * 128];
        int bytesRead;
        try {
            in.skip(offset);
            bytesRead = in.read(bytes);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        if (bytesRead == -1)
            return new byte[0];

        byte[] bytesToReturn = new byte[bytesRead];

        System.arraycopy(bytes, 0, bytesToReturn, 0, bytesRead);

        return bytesToReturn;
    }

    public FileInfo uploadFile(String username, String filename, Cipher cipher, InputStream in) {
        GridFSInputFile file = mongoGridFS.createFile(in, true);

        file.setChunkSize(1024L * 128L);
        file.setFilename(filename);
        file.put("owner", username);
        file.put("cipher", cipher.name());

        file.save();

        return getFile(file.getId().toString());
    }
}
