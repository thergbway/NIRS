package client.utils;

import com.caucho.hessian.client.HessianProxyFactory;
import nirs.api.MainService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public final class MainServiceAPIFinder {

    private static MainService mainService;

    static {

        Properties properties = new Properties();

        try {
            properties
                    .load(Files.newInputStream(Paths.get("C:/Users/Zetro/IdeaProjects/NIRS/client/src/main/resources/main.properties")));

            mainService = (MainService) new HessianProxyFactory()
                    .create(MainService.class, properties.getProperty("api.url"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private MainServiceAPIFinder() {}

    public static MainService findProxy() {
        return mainService;
    }
}
