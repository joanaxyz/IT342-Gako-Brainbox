package edu.cit.gako.brainbox.audio

object BrainBoxAudioContract {
    const val ACTION_LOAD = "edu.cit.gako.brainbox.audio.action.LOAD"
    const val ACTION_LOAD_AND_PLAY = "edu.cit.gako.brainbox.audio.action.LOAD_AND_PLAY"
    const val ACTION_PLAY = "edu.cit.gako.brainbox.audio.action.PLAY"
    const val ACTION_PAUSE = "edu.cit.gako.brainbox.audio.action.PAUSE"
    const val ACTION_STOP = "edu.cit.gako.brainbox.audio.action.STOP"
    const val ACTION_SEEK_TO_CHUNK = "edu.cit.gako.brainbox.audio.action.SEEK_TO_CHUNK"
    const val ACTION_SET_SPEECH_RATE = "edu.cit.gako.brainbox.audio.action.SET_SPEECH_RATE"
    const val ACTION_CLEAR_SESSION = "edu.cit.gako.brainbox.audio.action.CLEAR_SESSION"

    const val EXTRA_REQUEST_WIRE = "edu.cit.gako.brainbox.audio.extra.REQUEST_WIRE"
    const val EXTRA_CHUNK_INDEX = "edu.cit.gako.brainbox.audio.extra.CHUNK_INDEX"
    const val EXTRA_SPEECH_RATE = "edu.cit.gako.brainbox.audio.extra.SPEECH_RATE"
}
