# Theta Protocol Delivery Android SDK SRC 


## Prerequisites

* Android minimum SDK version : 23 or above
* Video player : ExoPlayer
* Video format : HLS


## Importing the library

Import the delivery-sdk.aar file into your project

Add the imported library to your app project gradle file

## Using the library

### Lifecycle events

Override the onCreate and onDestroy methods of your video's Activity to respectively initialize and destroy the Theta Delivery Library


```
@override fun onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState);
    ...
    ThetaDelivery.init(this);
}
```

```
@override fun onDestroy() {
    ThetaDelivery.destroy(this);
    super.onDestroy();
}
```

### Data Source Factory creation

Instead of using an HlsDataSourceFactory to create your data source, use ThetaDataSourceFactory

```
val dataSourceFactory = ThetaDataSourceFactory(
  this, 
  Util.getUserAgent(this, "DeliverySDK"),
  DefaultBandwidthMeter(),
  streamConfig,
  thetaDataSourceListener); //Optionnal
```

See Theta Data Source Configuration for streamConfig variable

### Data Source Factory usage

Create your ExoPlayer in the same way you usually and use the data source factory previously created to setup your media source (more informations in the ExoPlayer doc https://google.github.io/ExoPlayer/guide.html)

```
val player = ExoPlayerFactory.newSimpleInstance(
  DefaultRenderersFactory(this),
  DefaultTrackSelector(), DefaultLoadControl());
playerView.setPlayer(player);
player.setPlayerWhenReady(true);
val mediaSource = HlsMediaSource.Factory(dataSourceFactory).createMediaSource(myHlsVideoUrl);
player.prepare(mediaSource, true, false);
```


### Implement onTimelineChanged in Player.EventListener

The following implementation will bring the player back on track if it falls behind live. 

```
player.addListener(new Player.EventListener() {
       @override fun onTimelineChanged(Timeline timeline, Object manifest, int reason) {
           if (player != null && player.getBufferedPosition() <= -2000) {
               player.seekToDefaultPosition();
           }
       }
       
       @Override
       ....
})
```


## Theta Data Source Configuration

```
val streamConfig = new ThetaConfig(
  "video_url_example", // Video URL
  "user_id_example", // Unique User Id
);
```


## Theta Data Source Listener

You can subscribe to 4 types of events:

* Info Event: Triggered when the library is initialized and when a new fragment is read
* Traffic Event: Triggered when a fragment is received or sent with peers or CDN
* Peers Changed Event: Triggered when the amount of peers connected changes
* Account Updated Event: Triggered when the user account changes (TFuel earned)
* Error Event: One error event is supported for now : ThetaErrorEvent.WEB_RTC_NOT_SUPPORTED

```
ThetaDataSourceListener() {
  @override fun onInfoEvent(ThetaInfoEvent thetaInfoEvent) {
    Log.d("ThetaDelivery", "onInfoEvent: " + thetaInfoEvent.message);
    Log.d("ThetaDelivery", "onInfoEvent: " + thetaInfoEvent.code);
  }

  @override fun onTrafficEvent(ThetaTrafficEvent trafficEvent) {
    Log.d("ThetaDelivery", "onTrafficEvent: " + trafficEvent.name);
  }
  
  @override fun onPeersChangedEvent(ThetaPeersChangedEvent peersEvent) {
    Log.d("ThetaDelivery", "onPeersChangedEvent");
  }

  @override fun onAccountUpdatedEvent(ThetaUserWalletEvent userWalletEvent) {
    Log.d("ThetaDelivery", "onAccountUpdated: " + userWalletEvent.address);
    Log.d("ThetaDelivery", "onAccountUpdated: " + userWalletEvent.thetaWei);
    Log.d("ThetaDelivery", "onAccountUpdated: " + userWalletEvent.tfuelWei);
  }
  
  @override fun onErrorEvent(ThetaErrorEvent thetaErrorEvent) {
      Log.d("ThetaDelivery", "onErrorEvent: " + thetaErrorEvent.name);
  }
}
```


## What's next?

* TFuel payments
* Performance optimizations