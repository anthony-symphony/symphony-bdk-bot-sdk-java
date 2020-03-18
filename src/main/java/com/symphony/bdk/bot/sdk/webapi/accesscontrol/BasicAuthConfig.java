package com.symphony.bdk.bot.sdk.webapi.accesscontrol;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.stereotype.Component;

/**
 * Enables protecting endpoints through basic authentication or/and IP
 * whitelisting.
 *
 * @author Marcus Secato
 *
 */
@Component
@EnableWebSecurity
public class BasicAuthConfig extends WebSecurityConfigurerAdapter {
  private static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthConfig.class);

  private BasicAuthProps authProps;
  private BasicAuthProvider basicAuthenticationProvider;

  public BasicAuthConfig(BasicAuthProps authProps,
      BasicAuthProvider basicAuthenticationProvider) {
    this.basicAuthenticationProvider = basicAuthenticationProvider;
    this.authProps = authProps;
  }

  /**
   * Injects basic authentication on Spring Security
   *
   * @param auth builder of Spring Security
   */
  @Override
  protected void configure(AuthenticationManagerBuilder auth) {
    auth.authenticationProvider(basicAuthenticationProvider);
  }

  /**
   * Ignores any other request that is not configured
   *
   * @param web Configurer of Spring Security
   */
  @Override
  public void configure(WebSecurity web) {
    web.ignoring().antMatchers(HttpMethod.OPTIONS, "/**");
  }

  /**
   * Verifies if exists any kind of authentication (basic or ip whitelist) and
   * inject it into spring security.
   *
   * @param http Configurer of Spring Security
   */
  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http.headers().frameOptions().disable().and().csrf().disable();
    if (authProps.isBasicAuth() || authProps.isIpWhitelist()) {
      String[] urlMapping = authProps.getUrlMapping().toArray(
          new String[authProps.getUrlMapping().size()]);

      ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry expression =
          http.authorizeRequests()
              .antMatchers(urlMapping)
              .access(buildAccess())
              .anyRequest()
              .permitAll();
      if (authProps.isBasicAuth()) {
        expression.and().httpBasic();
      }
      expression.and()
          .sessionManagement()
          .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    } else {
      LOGGER.info("No Auth info!");
      http.csrf().disable()
          .authorizeRequests()
          .anyRequest()
          .permitAll();
    }
  }

  /**
   * Builds access string on spring security format
   *
   * @return string containing all the ip whitelist and basic authentication
   *         (if applicable)
   */
  private String buildAccess() {
    String access = "";
    if (authProps.isIpWhitelist()) {
      LOGGER.info("Authentication by IP Whitelist!");
      access = authProps.getIpWhitelist().stream().reduce(null, (acc, ip) ->
          acc == null ?
              "hasIpAddress('" + ip + "')" :
              acc + " or hasIpAddress('" + ip + "')");
    }
    if (authProps.isBasicAuth()) {
      LOGGER.info("Authentication by Basic!");
      access += access.isEmpty() ?
          "isAuthenticated()" :
          " or isAuthenticated()";
    }
    return access;
  }

}
