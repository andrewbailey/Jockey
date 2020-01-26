package dev.andrewbailey.encore.player.state

sealed class TransportState {

    abstract val repeatMode: RepeatMode
    abstract val shuffleMode: ShuffleMode

    data class Active(
        val status: PlaybackState,
        val seekPosition: SeekPosition,
        val queue: QueueState,
        override val repeatMode: RepeatMode
    ) : TransportState() {

        override val shuffleMode: ShuffleMode
            get() = if (queue is QueueState.Linear) {
                ShuffleMode.LINEAR
            } else {
                ShuffleMode.SHUFFLED
            }

    }

    data class Idle(
        override val repeatMode: RepeatMode,
        override val shuffleMode: ShuffleMode
    ) : TransportState()

}
