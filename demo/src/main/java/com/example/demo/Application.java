package com.example.demo;

import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.servlet.CamelHttpTransportServlet;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.web.embedded.EmbeddedWebServerFactoryCustomizerAutoConfiguration;
import org.springframework.boot.autoconfigure.websocket.servlet.WebSocketServletAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import javax.ws.rs.core.MediaType;
import java.awt.print.Book;

@SpringBootApplication(exclude = {WebSocketServletAutoConfiguration.class, AopAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class, EmbeddedWebServerFactoryCustomizerAutoConfiguration.class, DataSourceAutoConfiguration.class})
@MapperScan(value = "com.example.demo.mapper")
public class Application {

    private final static String DATASOURCE_PREFIX = "spring.datasource";

    @Bean
    @ConfigurationProperties(prefix = DATASOURCE_PREFIX)
    public DataSource dataSource() {
        return new org.apache.tomcat.jdbc.pool.DataSource();
    }

    @Bean
    public SqlSessionFactory sqlSessionFactoryBean() throws Exception {

        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource());
        sqlSessionFactoryBean.setTypeAliasesPackage("com.example.demo.entity");
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

        sqlSessionFactoryBean.setMapperLocations(resolver.getResources("classpath:/mapping/*.xml"));

        return sqlSessionFactoryBean.getObject();
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Value("${server.port}")
    String serverPort;

    @Value("${baeldung.api.path}")
    String contextPath;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    ServletRegistrationBean servletRegistrationBean() {
        ServletRegistrationBean servlet = new ServletRegistrationBean(new CamelHttpTransportServlet(), contextPath + "/*");
        servlet.setName("CamelServlet");
        return servlet;
    }

    @Component
    class RestApi extends RouteBuilder {

        @Autowired
        private UserMapper userMapper;

        @Override
        public void configure() {

            CamelContext context = new DefaultCamelContext();

            // http://localhost:8080/camel/api-doc
            restConfiguration().contextPath(contextPath) //
                    .port(serverPort)
                    .enableCORS(true)
                    .apiContextPath("/api-doc")
                    .apiProperty("api.title", "Test REST API")
                    .apiProperty("api.version", "v1")
                    .apiProperty("cors", "true") // cross-site
                    .apiContextRouteId("doc-api")
                    .component("servlet")
                    .bindingMode(RestBindingMode.json)
                    .dataFormatProperty("prettyPrint", "true");
            /**
             The Rest DSL supports automatic binding json/xml contents to/from
             POJOs using Camels Data Format.
             By default the binding mode is off, meaning there is no automatic
             binding happening for incoming and outgoing messages.
             You may want to use binding if you develop POJOs that maps to
             your REST services request and response types.
             */

            rest("/api/").description("Test REST Service")
                    .id("api-route")
                    .post("/bean")
                    .produces(MediaType.APPLICATION_JSON)
                    .consumes(MediaType.APPLICATION_JSON)
                    // .get("/hello/{place}")
                    .bindingMode(RestBindingMode.auto)
                    .type(MyBean.class)
                    .enableCORS(true)
                    // .outType(OutBean.class)

                    .to("direct:remoteService");

            from("direct:remoteService").routeId("direct-route")
                    .tracing()
                    .log(">>> ${body.id}")
                    .log(">>> ${body.name}")
                    // .transform().simple("blue ${in.body.name}")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            MyBean bodyIn = (MyBean) exchange.getIn()
                                    .getBody();

                            ExampleServices.example(bodyIn);

                            exchange.getIn()
                                    .setBody(bodyIn);
                        }
                    })
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201));

            // get user by id
            rest("/user")
                    .get("{id}").description("Details of an user by id").outType(User.class)
                    .produces(MediaType.APPLICATION_JSON).route()
                    .bean("userMapper", "getById(${header.id})")
                    .endRest()
                    .get("/").description("Get all users").outType(Object.class)
                    .produces(MediaType.APPLICATION_JSON).route()
                    .bean("userMapper", "list()");

            rest("/user/").description("Add new User")
                    .id("post-user")
                    .post("/")
                    .produces(MediaType.APPLICATION_JSON)
                    .consumes(MediaType.APPLICATION_JSON)
                    .bindingMode(RestBindingMode.auto)
                    .type(User.class)
                    .enableCORS(true)
                    .to("direct:userPostService");

            from("direct:userPostService")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            User bodyIn = (User) exchange.getIn()
                                    .getBody();

                            userMapper.insert(bodyIn);

                        }
                    })
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201));

            rest("/user/").description("Update User")
                    .id("put-user")
                    .put("/{id}")
                    .produces(MediaType.APPLICATION_JSON)
                    .consumes(MediaType.APPLICATION_JSON)
                    .bindingMode(RestBindingMode.auto)
                    .type(User.class)
                    .enableCORS(true)
                    .to("direct:userPutService");

            from("direct:userPutService")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            User bodyIn = (User) exchange.getIn()
                                    .getBody();
                            bodyIn.setId(Integer.parseInt(exchange.getIn().getHeader("id").toString()));

                            userMapper.update(bodyIn);

                        }
                    })
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201));

            rest("/user/").description("Delete User")
                    .id("delete-user")
                    .delete("/{id}")
                    .produces(MediaType.APPLICATION_JSON)
                    .consumes(MediaType.APPLICATION_JSON)
                    .bindingMode(RestBindingMode.auto)
                    .type(User.class)
                    .enableCORS(true)
                    .to("direct:userDeleteService");

            from("direct:userDeleteService")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {

                            userMapper.delete(Integer.parseInt(exchange.getIn().getHeader("id").toString()));

                        }
                    })
                    .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201));

        }
    }
}