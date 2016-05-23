package client;

import nirs.api.GreetingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.remoting.caucho.HessianProxyFactoryBean;

@Configuration
@ComponentScan(basePackages = "client")
public class Config {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
        pspc.setLocations(new ClassPathResource("main.properties"));
        return pspc;
    }

    @Bean(name = "greetingService")
    public HessianProxyFactoryBean hessianProxyFactoryBean() {
        HessianProxyFactoryBean bean = new HessianProxyFactoryBean();

        bean.setServiceUrl("http://localhost:8080/api");
        bean.setServiceInterface(GreetingService.class);

        return bean;
    }
}