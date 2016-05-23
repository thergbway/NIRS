//package tester;
//
//import nirs.api.MainService;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.remoting.caucho.HessianProxyFactoryBean;
//
//@Configuration
//@ComponentScan(basePackages = "tester")
//public class Config {
//
//    @Bean
//    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
//        PropertySourcesPlaceholderConfigurer pspc = new PropertySourcesPlaceholderConfigurer();
//        pspc.setLocations(new ClassPathResource("main.properties"));
//        return pspc;
//    }
//
//    @Bean(name = "mainService")
//    public HessianProxyFactoryBean hessianProxyFactoryBean(@Value("${api.url}") String apiUrl) {
//        HessianProxyFactoryBean bean = new HessianProxyFactoryBean();
//
//        bean.setServiceUrl(apiUrl );
//        bean.setServiceInterface(MainService.class);
//
//        return bean;
//    }
//}