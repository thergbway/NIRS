package nirs.api;

import java.io.IOException;
import java.io.InputStream;

public interface MainService {

    String getGreeting(String name);

    String upload(String filename, InputStream is) throws IOException;
}