package com.relic.relic.service;

public interface MailingService {
    void sendEmail(String to, String subject, String text);
}
