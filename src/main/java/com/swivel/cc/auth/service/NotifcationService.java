package com.swivel.cc.auth.service;

import com.swivel.cc.auth.domain.request.EmailRequestDto;
import com.swivel.cc.auth.domain.request.MobileNoRequestDto;
import com.swivel.cc.auth.domain.request.SmsRequestDto;
import com.swivel.cc.auth.exception.AuthServiceException;
import com.swivel.cc.auth.wrapper.SendEmailResponseWrapper;
import com.swivel.cc.auth.wrapper.SendSmsResponseWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
@Slf4j
public class NotifcationService {
    private static final String TIME_ZONE_HEADER = "Time-Zone";
    private static final String APP_KEY = "app-key";
    private final String utilServiceAppKey;
    private final String mailSendUri;
    private final String smsSendUri;
    private final RestTemplate restTemplate;

    @Autowired
    public NotifcationService(@Value("${util.baseUrl}") String baseUrl,
                              @Value("${util.uri.sendMail}") String mailPath,
                              @Value("${util.uri.sendSms}") String smsPath,
                              @Value("${util.utilServiceAppKey}") String appKey,
                              RestTemplate restTemplate) {
        this.mailSendUri = baseUrl + mailPath;
        this.smsSendUri = baseUrl + smsPath;
        this.utilServiceAppKey = appKey;
        this.restTemplate = restTemplate;
    }


    /**
     * This method allows sending email.
     *
     * @param receiver receiver email
     * @param subject  email subject
     * @param body     email body
     * @param timeZone time zone
     */
    @Async
    public void sendMail(String receiver, String subject, String body, String timeZone) {
        var headers = getAuthHeaders(timeZone);
        var mailRequestDto = new EmailRequestDto(receiver, subject, body);
        HttpEntity<EmailRequestDto> entity = new HttpEntity<>(mailRequestDto, headers);
        try {
            log.debug("Calling util service to send the email to: {}", receiver);
            restTemplate.exchange(mailSendUri, HttpMethod.POST, entity, SendEmailResponseWrapper.class);
            log.debug("Sending email was success to: {}", receiver);
        } catch (HttpClientErrorException e) {
            log.error("Sending email was failed to: {}", receiver, e);
            throw new AuthServiceException("Sending email was failed.", e);
        }

    }


    /**
     * This method allows sending email.
     *
     * @param recipientNo recipient number
     * @param sms         sms
     * @param timeZone    time zone
     */
    @Async
    public void sendSms(MobileNoRequestDto recipientNo, String sms, String timeZone) {
        var headers = getAuthHeaders(timeZone);
        var smsRequestDto = new SmsRequestDto(sms, recipientNo);
        HttpEntity<SmsRequestDto> entity = new HttpEntity<>(smsRequestDto, headers);

        try {
            log.debug("Calling util service to send the sms to: {}", recipientNo.toLogJson());
            restTemplate.exchange(smsSendUri, HttpMethod.POST, entity, SendSmsResponseWrapper.class);
            log.debug("Sending sms was success to: {}", recipientNo.toLogJson());
        } catch (HttpClientErrorException e) {
            log.error("Sending sms was failed to: {}", recipientNo.toLogJson(), e);
            throw new AuthServiceException("Sending sms was failed.", e);
        }
    }

    /**
     * This method generate headers for util service urls.
     *
     * @param timeZone time zone
     * @return
     */
    private HttpHeaders getAuthHeaders(String timeZone) {
        var headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(TIME_ZONE_HEADER, timeZone);
        headers.set(APP_KEY, utilServiceAppKey);
        return headers;
    }
}
