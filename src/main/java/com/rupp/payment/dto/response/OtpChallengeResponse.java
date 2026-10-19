package com.rupp.payment.dto.response;

import java.time.Instant;
import java.util.UUID;

public record OtpChallengeResponse(UUID challengeId, Long paymentId, Instant expiresAt) {
}
