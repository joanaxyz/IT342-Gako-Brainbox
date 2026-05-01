package edu.cit.gako.brainbox.features.playback.audio

object BrainBoxAudioContract {
    const val ACTION_LOAD = "edu.cit.gako.brainbox.features.playback.action.LOAD"
    const val ACTION_LOAD_AND_PLAY = "edu.cit.gako.brainbox.features.playback.action.LOAD_AND_PLAY"
    const val ACTION_PREPARE = "edu.cit.gako.brainbox.features.playback.action.PREPARE"
    const val ACTION_PLAY = "edu.cit.gako.brainbox.features.playback.action.PLAY"
    const val ACTION_PAUSE = "edu.cit.gako.brainbox.features.playback.action.PAUSE"
    const val ACTION_STOP = "edu.cit.gako.brainbox.features.playback.action.STOP"
    const val ACTION_SEEK_TO_CHUNK = "edu.cit.gako.brainbox.features.playback.action.SEEK_TO_CHUNK"
    const val ACTION_SET_SPEECH_RATE = "edu.cit.gako.brainbox.features.playback.action.SET_SPEECH_RATE"
    const val ACTION_CLEAR_SESSION = "edu.cit.gako.brainbox.features.playback.action.CLEAR_SESSION"

    const val EXTRA_REQUEST_WIRE = "edu.cit.gako.brainbox.features.playback.extra.REQUEST_WIRE"
    const val EXTRA_CHUNK_INDEX = "edu.cit.gako.brainbox.features.playback.extra.CHUNK_INDEX"
    const val EXTRA_SPEECH_RATE = "edu.cit.gako.brainbox.features.playback.extra.SPEECH_RATE"
}
