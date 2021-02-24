package dev.andrewbailey.music.ui.navigation

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import dev.andrewbailey.music.R

enum class LibraryPage(
    @DrawableRes
    val iconRes: Int,
    @StringRes
    val labelRes: Int
) {
    Playlists(
        iconRes = R.drawable.ic_library_playlists,
        labelRes = R.string.library_tab_playlists
    ),
    Songs(
        iconRes = R.drawable.ic_library_songs,
        labelRes = R.string.library_tab_songs
    ),
    Albums(
        iconRes = R.drawable.ic_library_albums,
        labelRes = R.string.library_tab_albums
    ),
    Artists(
        iconRes = R.drawable.ic_library_artists,
        labelRes = R.string.library_tab_artists
    ),
    Folders(
        iconRes = R.drawable.ic_library_folder,
        labelRes = R.string.library_tab_folders
    ),
}
