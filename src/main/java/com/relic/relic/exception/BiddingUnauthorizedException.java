package com.relic.relic.exception;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class BiddingUnauthorizedException extends RuntimeException {
    public BiddingUnauthorizedException(String message) {
        super(message);
    }
}
