package nirs.config;

import nirs.api.GreetingService;
import nirs.service.GreetingServiceImpl;
import org.apache.commons.dbcp2.BasicDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.remoting.caucho.HessianServiceExporter;
import org.sql2o.QuirksMode;
import org.sql2o.Sql2o;

import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = "nirs")
public class AppConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
        pspc.setLocations(new ClassPathResource("main.properties"));
        return pspc;
    }

    @Bean(name = "dataSource")
    public DataSource dataSource(
        @Value("${db.url}") String url,
        @Value("${db.username}") String username,
        @Value("${db.password}") String password,
        @Value("${db.driver_class_name}") String driverClassName
    ) {
        final BasicDataSource dataSource = new BasicDataSource();

        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        dataSource.setUsername(username);
        dataSource.setPassword(password);

        return dataSource;
    }

    @Bean(name = "sql2o")
    @Autowired
    public Sql2o sql2o(DataSource dataSource) {
        return new Sql2o(dataSource, QuirksMode.PostgreSQL);
    }

    @Bean
    public GreetingService greetingService() {
        return new GreetingServiceImpl();
    }

    @Bean(name = "/api")
    @Autowired
    public HessianServiceExporter hessianServiceExporter(GreetingService greetingService) {
        HessianServiceExporter exporter = new HessianServiceExporter();
        exporter.setService(greetingService);
        exporter.setServiceInterface(GreetingService.class);

        return exporter;
    }
}