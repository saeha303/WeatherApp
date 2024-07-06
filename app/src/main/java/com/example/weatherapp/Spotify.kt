package com.example.weatherapp

import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
enum class PlayingState {
    PAUSED, PLAYING, STOPPED
}
object SpotifyService {
    private const val CLIENT_ID = "d969724b5d734c0988d254ddd1445bb7"
    private const val  REDIRECT_URI = "weatherapp://callback"
    private val REQUEST_CODE = 1337
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private var isPlay=true

    fun connect(context: AppCompatActivity) {
        println("no baksoo")
        val builder = AuthorizationRequest.Builder(CLIENT_ID, AuthorizationResponse.Type.TOKEN, REDIRECT_URI)
        builder.setScopes(arrayOf("user-read-private","user-read-email"))
        builder.setShowDialog(true)
        val request = builder.build()
        isPlay = !isPlay

        AuthorizationClient.openLoginActivity(context, REQUEST_CODE, request)
        println("i did it")

    }
    fun postOnActivityResult(requestCode: Int, resultCode: Int, intent: Intent?,context: Context){
        println("I beg you")
            // Check if the result comes from the correct request
            if (requestCode == REQUEST_CODE) {
                println("pls")
                val response = AuthorizationClient.getResponse(resultCode, intent)
                when (response.type) {
                    AuthorizationResponse.Type.TOKEN -> {
                        println("no token")
                        // Now connect to the Spotify App Remote
                        var connectionParams: ConnectionParams = ConnectionParams.Builder(CLIENT_ID)
                            .setRedirectUri(REDIRECT_URI)
                            .showAuthView(true)
                            .build()
                        println("after connectionParams")
                        SpotifyAppRemote.connect(
                            context,
                            connectionParams,
                            object : Connector.ConnectionListener {

                                override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                                    this@SpotifyService.spotifyAppRemote = spotifyAppRemote
                                    Log.d("MainActivity", "Connected! Yay!")
                                    // Play a playlist
                                    play("spotify:playlist:5siJFLogU7YAQKXI51z7Mn")
                                }

                                override fun onFailure(throwable: Throwable) {
                                    Log.e("MainActivity", throwable.message, throwable)
                                }
                            })
                    }

                    AuthorizationResponse.Type.ERROR -> {
                        println("here is the response: " + response.state)
                        // Handle error response
                        Log.e("MainActivity", "Auth error: ${response.error}")
                    }

                    else -> {
                        // Handle other cases
                    }
                }

            }
    }
    fun handleSpotifyLoginResponse(intent: Intent?) {

        intent?.data?.let { uri ->
            if (uri.toString().startsWith(REDIRECT_URI)) {
                // Extract the access token from the URI
                val accessToken = uri.getQueryParameter("access_token")
                if (accessToken != null) {
                    Log.d("Authorization", "Access Token: $accessToken")
                    // Handle the access token as needed
                } else {
                    Log.e("Authorization", "Error: Access token not found in URI")
                }
            }
        }
    }
    fun check():Boolean {
        return spotifyAppRemote==null
    }
    fun play(uri: String) {
        spotifyAppRemote?.playerApi?.play(uri)
    }

    fun resume() {
        spotifyAppRemote?.playerApi?.resume()
    }

    fun pause() {
        spotifyAppRemote?.playerApi?.pause()
    }

    fun playingState(handler: (PlayingState) -> Unit) {
        spotifyAppRemote?.playerApi?.playerState?.setResultCallback { result ->
            if (result.track.uri == null) {
                handler(PlayingState.STOPPED)
            } else if (result.isPaused) {
                handler(PlayingState.PAUSED)
            } else {
                handler(PlayingState.PLAYING)
            }
        }
    }
    fun disconnect() {
        spotifyAppRemote?.let {
            println("ummmm")
            SpotifyAppRemote.disconnect(it)
            println("ashchi")
            spotifyAppRemote=null
        }
    }
}

class Spotify {
    fun setupViews () {
        SpotifyService.playingState {
            when(it) {
//                PlayingState.PLAYING -> showPauseButton()
//                PlayingState.STOPPED -> showPlayButton()
//                PlayingState.PAUSED -> showResumeButton()
            }
        }
    }

    fun setupListeners() {
//        playButton.setOnClickListener {
//            SpotifyService.play("spotify:album:5L8VJO457GXReKVVfRhzyM")
//            showPauseButton()
//        }
//
//        pauseButton.setOnClickListener {
//            SpotifyService.pause()
//            showResumeButton()
//        }
//
//        resumeButton.setOnClickListener {
//            SpotifyService.resume()
//            showPauseButton()
//        }
    }
}