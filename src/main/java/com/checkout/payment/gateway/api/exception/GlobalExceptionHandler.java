package com.checkout.payment.gateway.api.exception;

import com.checkout.payment.gateway.domain.exception.PaymentNotFoundException;
import com.checkout.payment.gateway.domain.model.PaymentStatus;
import com.checkout.payment.gateway.bank.exception.BankException;
import com.checkout.payment.gateway.bank.exception.BankUnavailableException;
import com.checkout.payment.gateway.domain.exception.PaymentValidationException;
import com.checkout.payment.gateway.api.dto.response.ErrorResponse;
import com.checkout.payment.gateway.api.dto.response.RejectedPaymentResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@ControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(PaymentNotFoundException.class)
  public ResponseEntity<ErrorResponse> handlePaymentNotFoundException(PaymentNotFoundException ex) {
    LOG.error("PaymentNotFoundException: {}", ex.getMessage(), ex);
    return new ResponseEntity<>(new ErrorResponse("Payment not found"), HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(PaymentValidationException.class)
  public ResponseEntity<RejectedPaymentResponse> handlePaymentValidationException(PaymentValidationException ex) {
    LOG.warn("Payment rejected with validation errors: {}", ex.getValidationErrors());
    return ResponseEntity
        .badRequest()
        .body(new RejectedPaymentResponse(PaymentStatus.REJECTED, ex.getValidationErrors()));
  }

  @ExceptionHandler(BankUnavailableException.class)
  public ResponseEntity<ErrorResponse> handleBankUnavailable(BankUnavailableException ex) {
    LOG.warn("Bank unavailable: {}", ex.getMessage());
    return new ResponseEntity<>(new ErrorResponse("Payment processor unavailable"), HttpStatus.SERVICE_UNAVAILABLE);
  }

  // Bank returned bad response
  @ExceptionHandler(BankException.class)
  public ResponseEntity<ErrorResponse> handleBankError(BankException ex) {
    LOG.error("Bank error", ex);
    return new ResponseEntity<>(new ErrorResponse("Payment processing error"), HttpStatus.BAD_GATEWAY);
  }

  @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class})
  public ResponseEntity<ErrorResponse> handleBadRequest(Exception ex) {
    LOG.warn("Bad request: {}", ex.getMessage());
    return new ResponseEntity<>(new ErrorResponse("Invalid request"), HttpStatus.BAD_REQUEST);
  }

  // Catch all safety net
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
    LOG.error("An unexpected error has occurred", ex);
    return new ResponseEntity<>(new ErrorResponse("Internal server error"), HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
