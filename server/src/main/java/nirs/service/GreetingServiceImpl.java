package nirs.service;

import nirs.api.GreetingService;

public class GreetingServiceImpl implements GreetingService {

    @Override
    public String getGreeting(String name) {
        return "Hello " + name + "!";
    }
}
