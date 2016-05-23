package client.utils;

import com.caucho.hessian.client.HessianProxyFactory;
import nirs.api.MainService;

import java.io.IOException;
import java.util.Properties;

public final class MainServiceAPIFinder {

    private static MainService mainService;

    static {

        Properties properties = new Properties();

        try {
            properties
                    .load(MainServiceAPIFinder.class.getResourceAsStream("/main.properties"));

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
