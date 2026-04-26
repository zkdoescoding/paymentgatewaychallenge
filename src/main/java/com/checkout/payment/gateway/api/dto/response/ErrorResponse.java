package com.checkout.payment.gateway.api.dto.response;

public record ErrorResponse(String message) {

  @Override
  public String toString() {
    return "ErrorResponse{" +
        "message='" + message + '\'' +
        '}';
  }
}
