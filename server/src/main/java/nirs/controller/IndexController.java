package nirs.controller;

import com.mongodb.DB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.sql2o.Connection;
import org.sql2o.Sql2o;

@Controller
public class IndexController {
    @Value("${index.welcome_message}")
    private String message;

    @Autowired
    private Sql2o sql2o;

    @Autowired
    private DB nirsDB;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    @ResponseBody
    public String showIndex() {
        StringBuilder sb = new StringBuilder();

        sb.append(message);
        sb.append("<br>");
        sb.append("database connection exists: ");

        try (Connection con = sql2o.open()) {
            int scalar = con
                .createQuery("SELECT 25")
                .executeAndFetchFirst(Integer.class);

            if (scalar == 25)
                sb.append("OK");
            else sb.append("FAILED");
        }
        sb.append("<br>");

        sb.append("mongo connection exists: ");

        try{
            nirsDB.createCollection("test_collection", null);
            nirsDB.getCollection("test_collection").drop();

            sb.append("OK");
        } catch (Exception ex){
            sb.append("FAILED");
        }

        return sb.toString();
    }
}