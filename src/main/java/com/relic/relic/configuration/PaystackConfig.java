//package com.relic.relic.configuration;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import com.github.chriseteka.paystackclient.PaystackClient;
//
//public class PaystackConfig {
//    @Value("${paystack.apiKey}")
//    private String apiKey;
//
//    @Bean
//    public PaystackClient paystackClient() {
//        return new PaystackClient(apiKey);
//    }
//}
