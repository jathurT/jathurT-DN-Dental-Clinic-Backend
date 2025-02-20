//package com.uor.eng;
//
//import lombok.RequiredArgsConstructor;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
//import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.context.ApplicationContext;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.junit4.SpringRunner;
//
//import static org.junit.jupiter.api.Assertions.assertNotNull;
//import static org.junit.jupiter.api.Assertions.assertTrue;
//
//@ActiveProfiles("test")
//@RunWith(SpringRunner.class)
//@RequiredArgsConstructor
//@SpringBootTest(classes = )
//@MockBean(ElasticSearchIndexService.class)
//public class AppStartTest {
//
//  @Test
//  public void contextLoads() {
//    final var app = new App();
//    assertNotNull("the application context should have loaded.", app);
//  }
//}