package com.swivel.cc.auth.exception;

/**
 * SendEmailFailedException
 */
public class SendEmailFailedException extends AuthServiceException {

    /**
     * SendEmailFailedException Exception with error message.
     *
     * @param errorMessage error message
     */
    public SendEmailFailedException(String errorMessage) {
        super(errorMessage);
    }

    /**
     * SendEmailFailedException Exception with error message.
     *
     * @param errorMessage error message
     */
    public SendEmailFailedException(String errorMessage, Throwable error) {
        super(errorMessage, error);
    }

}
