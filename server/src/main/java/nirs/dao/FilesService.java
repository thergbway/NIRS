package nirs.dao;

import com.mongodb.gridfs.GridFS;
import nirs.api.model.FileInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FilesService {
    @Autowired
    private GridFS mongoGridFS;

    @Autowired
    private UserService userService;

    public List<FileInfo> getFiles(String token) {
//        String username = userService.getUsername(token);
//
//        DBObject query = new BasicDBObject("owner", username);
//        DBObject sort = new BasicDBObject("uploadDate", -1).append("filename", 1);
//
//        List<GridFSDBFile> files = mongoGridFS.find(query, sort);
//
//        files
//            .stream()
//            .map(file -> FileInfo
//                    .builder()
//                    .id(file.getId().toString())
//                    .filename(file.getFilename())
//                    .size(file.getLength())
//                    .createdTimestamp(file.getUploadDate().toInstant().getEpochSecond())
//                .cipher()
//            )

        throw new RuntimeException();
    }
}
