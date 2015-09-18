package com.opentok.android.sample;

/**
 * Created by mserrano on 01/02/15.
 */
import java.net.MalformedURLException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.opentok.android.AudioDeviceManager;
import com.opentok.android.BaseVideoRenderer;
import com.opentok.android.Connection;
import com.opentok.android.OpenTokConfig;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.BaseAudioDevice.OutputMode;
import com.opentok.android.PublisherKit.PublisherListener;
import com.opentok.android.Session;
import com.opentok.android.Session.ArchiveListener;
import com.opentok.android.Session.SessionListener;
import com.opentok.android.Stream;
import com.opentok.android.Stream.StreamVideoType;
import com.opentok.android.Subscriber;
import com.opentok.android.SubscriberKit;
import com.opentok.android.SubscriberKit.SubscriberListener;
import com.opentok.android.VideoUtils;
import com.opentok.android.sample.video.CustomVideoRenderer;

public class MainActivity extends Activity implements SessionListener, SubscriberListener,  Session.StreamPropertiesListener {

  @Override
  protected void onDestroy() {

    super.onDestroy();

  }

  public static final String SESSION_ID = "1_MX4xMDB-fjE0NDI1NzA4ODA2MzZ-Tjg5RkhxOUY4ekdDcUZwOXhjTUVlbEFYfn4";
  // Replace with a generated token (from the dashboard or using an OpenTok
  // server SDK)
  public static final String TOKEN = "T1==cGFydG5lcl9pZD0xMDAmc2RrX3ZlcnNpb249dGJwaHAtdjAuOTEuMjAxMS0wNy0wNSZzaWc9M2IyMjVjODVlOWQxOTA2OTU4YjQwNDc0OTAzNTViNmY4ZDY0MGU5NTpzZXNzaW9uX2lkPTFfTVg0eE1EQi1makUwTkRJMU56QTRPREEyTXpaLVRqZzVSa2h4T1VZNGVrZERjVVp3T1hoalRVVmxiRUZZZm40JmNyZWF0ZV90aW1lPTE0NDI1Njk1ODImcm9sZT1tb2RlcmF0b3Imbm9uY2U9MTQ0MjU2OTU4Mi43NzI5MTQxODcyMjM0MCZleHBpcmVfdGltZT0xNDQ1MTYxNTgy";
  // Replace with your OpenTok API key
  public static final String APIKEY = "100";

  private static final boolean AUTO_PUBLISH = false;

  private Room mRoom;
  private Button connectButton;

  private ArrayList<Subscriber> mSubscriber;
  private Stream lastStream;
  private static final String LOGTAG = "sdk-sample";

  Handler mHandler = new Handler();


  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_main);

    connectButton = (Button) findViewById(R.id.connectButton);

    mSubscriber = new ArrayList<Subscriber>();

  }

  @Override
  public void onPause() {
    super.onPause();
    Log.i(LOGTAG, "onPause activity");

    if (mRoom != null) {
      mRoom.onPause();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.i(LOGTAG, "onResume activity");

    if (mRoom != null) {
      mRoom.onResume();
    }
  }

  public void onClickConnect(View v) {

    if (mRoom == null) {

			/*
			 * OpenTokConfig methods
			 */
      try {
        // set environment
        OpenTokConfig.setAPIRootURL("https://anvil-tbdev.opentok.com",
            false);

      } catch (MalformedURLException e) {
        e.printStackTrace();
      }

      // enable OTKit logs
      OpenTokConfig.setOTKitLogs(true);
      // enable bindings logs
      OpenTokConfig.setJNILogs(true);
      //OpenTokConfig.setWebRTCLogs(true);


      mRoom = new Room(MainActivity.this, APIKEY, SESSION_ID);
      mRoom.setRoomName("test");
      mRoom.setSessionListener(this);
      mRoom.connect(TOKEN);

    } else {
      connectButton.setEnabled(false);
      Log.i(LOGTAG, "onClickDisconnect");
      mRoom.disconnect();
    }
  }

  public void onClickSetPreferredValues(View v) {

    if (mSubscriber != null && mSubscriber.size() != 0 ) {
      mSubscriber.get(0).setPreferredResolution(new VideoUtils.Size(320, 180));
      mSubscriber.get(0).setPreferredFrameRate(15);
    }
  }

  public void onClickDisablePreferredValues(View v) {
    if (mSubscriber != null && mSubscriber.size() != 0 ) {
      mSubscriber.get(0).setPreferredResolution(new VideoUtils.Size(SubscriberKit.NO_PREFERRED_RESOLUTION));
      mSubscriber.get(0).setPreferredFrameRate(SubscriberKit.NO_PREFERRED_FRAMERATE);
    }
  }

  @Override
  public void onConnected(Session session) {
    Log.i(LOGTAG, "connected session: "
        + session.getConnection().getConnectionId());
    Log.i(LOGTAG, "connected session data: "
            + session.getConnection().getData());
    connectButton.setEnabled(true);
    connectButton.setText(R.string.disconnectStr);

  }

  @Override
  public void onDisconnected(Session session) {

    Log.i(LOGTAG, "disconnected session: "
        + (session.getConnection() != null ? session.getConnection()
        .getConnectionId() : "no connection"));
    connectButton.setEnabled(true);
    connectButton.setText(R.string.connectStr);

    LinearLayout ln = (LinearLayout) this.findViewById(R.id.optionscolumn);

    for (Subscriber s : mSubscriber) {
      ln.removeView(s.getView());
    }

    mSubscriber.clear();
    mRoom = null;
  }

  @Override
  public void onError(Session session, OpentokError error) {
    Log.i(LOGTAG, "*** SESSION error  code:" + error.getErrorCode());
    Log.i(LOGTAG, "*** SESSION error:" + error.getMessage());

    Toast.makeText(getApplicationContext(), error.getMessage(),
        Toast.LENGTH_SHORT).show();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    Log.i(LOGTAG, "onConfiguration changed!!");
    super.onConfigurationChanged(newConfig);
  }

  @Override
  public void onStreamReceived(Session session, final Stream stream) {
    Log.i(LOGTAG, "on session received stream " + stream.getStreamId());

    if ((!mRoom.getConnection().equals(stream.getConnection()))) {

      Subscriber s = new Subscriber(MainActivity.this, stream);
      s.setSubscribeToAudio(false);
      s.setSubscriberListener(this);

      s.setRenderer(new CustomVideoRenderer(this));
      s.setStyle(BaseVideoRenderer.STYLE_VIDEO_SCALE,
              BaseVideoRenderer.STYLE_VIDEO_FILL);

      mRoom.subscribe(s);

      final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
          320, 240);

      final LinearLayout ln = (LinearLayout) this
          .findViewById(R.id.optionscolumn);
      ln.addView(s.getView(), lp);

      mSubscriber.add(s);

      lastStream = stream;
    }
  }

  @Override
  public void onStreamDropped(Session session, Stream stream) {
    Log.i(LOGTAG, "on session dropped stream " + stream.getStreamId());
    for (int i = 0; i < mSubscriber.size(); i++) {
      Subscriber s = mSubscriber.get(i);
      if (s.getStream().getStreamId().equals(stream.getStreamId())) {
        LinearLayout ln = (LinearLayout) this
            .findViewById(R.id.optionscolumn);
        ln.removeView(s.getView());
        mRoom.unsubscribe(s);
        mSubscriber.remove(i);
        break;
      }
    }

  }

  @Override
  public void onConnected(SubscriberKit subscriber) {
    Log.i(LOGTAG, "subscriber connected");

  }

  @Override
  public void onDisconnected(SubscriberKit subscriber) {
    Log.i(LOGTAG, "subscriber destroyed");

  }

  @Override
  public void onError(SubscriberKit subscriber, OpentokError error) {
  }

  @Override
  public void onStreamHasAudioChanged(Session session, Stream stream, boolean b) {

  }

  @Override
  public void onStreamHasVideoChanged(Session session, Stream stream, boolean b) {

  }

  @Override
  public void onStreamVideoDimensionsChanged(Session session, Stream stream, int i, int i1) {

  }

  @Override
  public void onStreamVideoTypeChanged(Session session, Stream stream, StreamVideoType streamVideoType) {

  }
}
