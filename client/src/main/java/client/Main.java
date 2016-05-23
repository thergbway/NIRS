package client;

import nirs.api.GreetingService;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext cx = new AnnotationConfigApplicationContext(Config.class);

        GreetingService service = cx.getBean("greetingService", GreetingService.class);

        System.out.println(service.getGreeting("some guy"));
    }
}
