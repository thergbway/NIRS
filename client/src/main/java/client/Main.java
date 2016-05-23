package client;

import nirs.api.MainService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.FileInputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        AnnotationConfigApplicationContext cx = new AnnotationConfigApplicationContext(Config.class);

        MainService service = cx.getBean("mainService", MainService.class);

        System.out.println(service.getGreeting("some guy"));
        System.out.println(service.uploadToMongo("sdf.txt", new FileInputStream("C:\\Users\\AND\\Desktop\\AngularJS для новичка- вчера, сегодня, завтра.mp4")));
    }
}
