package nirs.service;

import nirs.api.MainService;

import java.io.IOException;
import java.io.InputStream;

public class MainServiceImpl implements MainService {

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
}