package dev.andrewbailey.encore.provider

import dev.andrewbailey.encore.model.MediaMetadata
import dev.andrewbailey.encore.model.MediaObject
import dev.andrewbailey.encore.model.MediaSearchArguments

public open class MergedMediaProvider<M : MergedMediaObject> constructor(
    protected val providers: List<WrappedMediaProvider<*, *, M>>
) : MediaProvider<M> {

    final override suspend fun getMediaItemById(id: String): M? {
        return getProviderByMediaId(id).getMediaItemById(id)
    }

    final override suspend fun getMediaItemsByIds(ids: List<String>): List<M> {
        return ids.groupBy { getProviderByMediaId(it) }
            .flatMap { (provider, ids) -> provider.getMediaItemsByIds(ids) }
            .toSet()
            .let { mediaItems ->
                ids.mapNotNull { id -> mediaItems.firstOrNull { it.id == id } }
            }
    }

    override suspend fun searchForMediaItems(
        query: String,
        arguments: MediaSearchArguments
    ): MediaSearchResults<M> {
        return providers.fold(MediaSearchResults(emptyList(), emptyList())) { acc, mergedProvider ->
            val (searchResults, continuation) = mergedProvider.provider.searchForMediaItems(
                query,
                arguments
            )

            MediaSearchResults(
                searchResults = acc.searchResults + mergedProvider.unsafeWrapResult(searchResults),
                playbackContinuation = acc.playbackContinuation + mergedProvider.unsafeWrapResult(
                    continuation
                )
            )
        }
    }

    protected fun getProviderByMediaId(id: String): WrappedMediaProvider<*, *, M> {
        return providers.first { it.doesMediaBelongToProvider(id) }
    }

    protected inline fun <reified T : MediaProvider<*>> query(
        query: (T) -> List<MediaObject>
    ): List<M> {
        return withMediaProvider { mergedProvider: WrappedMediaProvider<T, *, M> ->
            mergedProvider.unsafeWrapResult(query(mergedProvider.provider))
        }
    }

    protected inline fun <reified T : MediaProvider<*>> querySingleItem(
        query: (T) -> MediaObject
    ): M {
        return withMediaProvider { mergedProvider: WrappedMediaProvider<T, *, M> ->
            mergedProvider.unsafeWrapResult(query(mergedProvider.provider))
        }
    }

    protected inline fun <reified T : MediaProvider<U>, U : MediaObject, R> withMediaProvider(
        action: (WrappedMediaProvider<T, *, M>) -> R
    ): R {
        val provider = providers.asSequence()
            .filter { it.provider is T }
            .firstOrNull()

        requireNotNull(provider) {
            "Cannot locate media provider of type ${T::class.java.simpleName}"
        }

        @Suppress("UNCHECKED_CAST")
        return action(provider as WrappedMediaProvider<T, U, M>)
    }

}

public class WrappedMediaProvider<T : MediaProvider<M>, M : MediaObject, R : MergedMediaObject>(
    public val originId: String,
    public val provider: T,
    public val mergeConverter: (M) -> R
) : MediaProvider<R> {

    public fun doesMediaBelongToProvider(id: String): Boolean {
        return id.startsWith("$originId/")
    }

    public fun wrapResult(result: M): R {
        return mergeConverter(result)
    }

    public fun wrapResult(result: List<M>): List<R> {
        return result.map { mergeConverter(it) }
    }

    @Suppress("UNCHECKED_CAST")
    public fun unsafeWrapResult(result: MediaObject): R {
        return wrapResult(result as M)
    }

    @Suppress("UNCHECKED_CAST")
    public fun unsafeWrapResult(result: List<MediaObject>): List<R> {
        return wrapResult(result as List<M>)
    }

    override suspend fun getMediaItemById(id: String): R? {
        return provider.getMediaItemById(unwrapId(id))?.let { wrapResult(it) }
    }

    override suspend fun getMediaItemsByIds(ids: List<String>): List<R> {
        return wrapResult(provider.getMediaItemsByIds(ids.map { unwrapId(it) }))
    }

    override suspend fun searchForMediaItems(
        query: String,
        arguments: MediaSearchArguments
    ): MediaSearchResults<R> {
        return provider.searchForMediaItems(query, arguments).let { results ->
            MediaSearchResults(
                searchResults = wrapResult(results.searchResults),
                playbackContinuation = wrapResult(results.playbackContinuation)
            )
        }
    }

    private fun unwrapId(id: String): String {
        val idPrefix = "$originId/"
        return if (id.startsWith(idPrefix)) { id.drop(idPrefix.length) } else { id }
    }
}

public abstract class MergedMediaObject(
    private val originId: String,
    public val delegate: MediaObject
) : MediaObject {

    final override val id: String
        get() = "$originId/${delegate.id}"

    final override val playbackUri: String
        get() = delegate.playbackUri

    final override fun toMediaMetadata(): MediaMetadata =
        delegate.toMediaMetadata()

}
