package com.symphony.bdk.bot.sdk.event.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import model.events.UserJoinedRoom;

/**
 * Symphony User joined room event
 *
 * @author Marcus Secato
 */
@Data
@NoArgsConstructor
public class UserJoinedRoomEvent extends BaseEvent {

  private StreamDetails stream;
  private UserDetails user;

  public UserJoinedRoomEvent(UserJoinedRoom event) {
    this.streamId = event.getStream().getStreamId();
    this.userId = event.getAffectedUser().getUserId();
    this.stream = new StreamDetails(event.getStream());
    this.user = new UserDetails(event.getAffectedUser());
  }

}
