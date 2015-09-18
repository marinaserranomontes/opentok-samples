package com.opentok.android.sample;

import android.content.Context;
import com.opentok.android.Session;

/**
 * Created by mserrano on 01/02/15.
 */

public class Room extends Session {

  protected String roomName;

  public Room(Context context, String apiKey, String sessionId) {
    super(context, apiKey, sessionId);
  }

  public String getRoomName() {
    return roomName;
  }

  public void setRoomName(String roomName) {
    this.roomName = roomName;
  }


}

