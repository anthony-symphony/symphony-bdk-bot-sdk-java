package com.symphony.bdk.bot.sdk.symphony;

import java.util.List;

import com.symphony.bdk.bot.sdk.symphony.exception.SymphonyClientException;
import com.symphony.bdk.bot.sdk.symphony.model.SymphonyUser;
import com.symphony.bdk.bot.sdk.symphony.model.SymphonyUserFilter;
import com.symphony.bdk.bot.sdk.symphony.model.SymphonyUserSearchResult;

/**
 * Retrieves user-related details
 *
 * @author Gabriel Berberian
 *
 */
public interface UsersClient {

  /**
   * @return bot user id
   */
  Long getBotUserId();

  /**
   * @return bot display name
   */
  String getBotDisplayName();

  /**
   * Gets user given an username
   *
   * @param username
   * @return the user, null if user not found
   * @throws SymphonyClientException on error connecting to Symphony
   */
  SymphonyUser getUserFromUsername(String username)
      throws SymphonyClientException;

  /**
   * Gets user given an email
   *
   * @param email
   * @param local
   * @return the user, null if user not found
   * @throws SymphonyClientException on error connecting to Symphony
   */
  SymphonyUser getUserFromEmail(String email, Boolean local)
      throws SymphonyClientException;

  /**
   * Gets user given an user userId
   *
   * @param userId
   * @param local
   * @return the user, null if user not found
   * @throws SymphonyClientException on error connecting to Symphony
   */
  SymphonyUser getUserFromId(Long userId, Boolean local)
      throws SymphonyClientException;

  /**
   * Gets users given some user ids
   *
   * @param userIds
   * @param local
   * @return the users or empty list if no user found
   * @throws SymphonyClientException on error connecting to Symphony
   */
  List<SymphonyUser> getUsersFromIdList(List<Long> userIds, Boolean local)
      throws SymphonyClientException;

  /**
   * Gets users given some emails
   *
   * @param emails
   * @param local
   * @return the users or empty list if no user found
   * @throws SymphonyClientException on error connecting to Symphony
   */
  List<SymphonyUser> getUsersFromEmailList(List<String> emails, Boolean local)
      throws SymphonyClientException;

  /**
   * Searcher for user given an filter
   *
   * @param userFilter
   * @return the user search result
   * @throws SymphonyClientException on error connecting to Symphony
   */
  SymphonyUserSearchResult searchUsers(SymphonyUserFilter userFilter)
      throws SymphonyClientException;

  /**
   * Gets the bot
   *
   * @return the bot
   * @throws SymphonyClientException on error connecting to Symphony
   */
  SymphonyUser getSessionUser() throws SymphonyClientException;

}
