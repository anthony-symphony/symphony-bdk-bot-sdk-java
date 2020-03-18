package com.symphony.bdk.bot.sdk.symphony.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Extension app authentication response
 *
 * @author Marcus Secato
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthenticateResponse {
  private String appId;
  private String appToken;
}
