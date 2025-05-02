// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso.util

import java.io.InputStream
import java.io.OutputStream
import java.nio.file.Path
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipFile

/** An iterator wrapping the [SevenZArchiveEntry] of the [archive], a [SevenZFile]. */
class SevenZEntryIterator(private val archive: SevenZFile) :
    AbstractIterator<SevenZArchiveEntry>() {
  override fun computeNext() = archive.nextEntry?.let { setNext(it) } ?: done()
}

/** Iterate over the entries in this 7z file. */
fun SevenZFile.entriesIterator(): SevenZEntryIterator = SevenZEntryIterator(this)

/**
 * Read the given [entry] from this 7z file. The file *must* be positioned at the start of the
 * file's contents!
 */
fun SevenZFile.read(entry: SevenZArchiveEntry): ByteArray {
  require(entry.size <= Int.MAX_VALUE)
  return ByteArray(entry.size.toInt()).also { check(read(it).toLong() == entry.size) }
}

/** Wrap this [InputStream] into a [TarArchiveInputStream]. */
fun InputStream.untar(): TarArchiveInputStream = TarArchiveInputStream(this)

/** Wrap this [OutputStream] into a [TarArchiveOutputStream]. */
fun OutputStream.tar(): TarArchiveOutputStream = TarArchiveOutputStream(this)

/** Open this [Path] as a [ZipFile]. */
fun Path.asZip(): ZipFile = ZipFile.Builder().setPath(this).get()

/** Create an [InputStream] for reading the entry [name] in this zip file. */
fun ZipFile.getInputStream(name: String): InputStream = getInputStream(getEntry(name))!!
