// SPDX-FileCopyrightText: 2023-2025 The ReReSo Authors, see AUTHORS.md
//
// SPDX-License-Identifier: Apache-2.0

package tools.aqua.rereso.util

import com.github.luben.zstd.Zstd.defaultCompressionLevel as zstdDefaultCompressionLevel
import java.io.InputStream
import java.io.OutputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream.MAX_BLOCKSIZE as BZ2_MAX_BLOCKSIZE
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream
import org.apache.commons.compress.compressors.gzip.GzipParameters
import org.apache.commons.compress.compressors.xz.XZCompressorInputStream
import org.apache.commons.compress.compressors.xz.XZCompressorOutputStream
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream
import org.tukaani.xz.LZMA2Options.PRESET_DEFAULT as XZ_PRESET_DEFAULT

/** Wrap `this` into an [GzipCompressorInputStream]. */
fun InputStream.gunzip(): GzipCompressorInputStream = GzipCompressorInputStream(this)

/**
 * Wrap `this` into an [GzipCompressorOutputStream] with the given [parameters], defaulting to the
 * default [GzipParameters].
 */
fun OutputStream.gzip(parameters: GzipParameters = GzipParameters()): GzipCompressorOutputStream =
    GzipCompressorOutputStream(this, parameters)

/** Wrap `this` into an [BZip2CompressorInputStream]. */
fun InputStream.bunzip2(): BZip2CompressorInputStream = BZip2CompressorInputStream(this)

/**
 * Wrap `this` into an [BZip2CompressorOutputStream] using the given [blocksize], defaulting to the
 * maximum block size.
 */
fun OutputStream.bzip2(blocksize: Int = BZ2_MAX_BLOCKSIZE): BZip2CompressorOutputStream =
    BZip2CompressorOutputStream(this, blocksize)

/** Wrap `this` into an [XZCompressorInputStream]. */
fun InputStream.unxz(): XZCompressorInputStream = XZCompressorInputStream(this)

/**
 * Wrap `this` into an [XZCompressorOutputStream] using the given [preset], defaulting to the XZ
 * default.
 */
fun OutputStream.xz(preset: Int = XZ_PRESET_DEFAULT): XZCompressorOutputStream =
    XZCompressorOutputStream(this, preset)

/** Wrap `this` into an [ZstdCompressorInputStream]. */
fun InputStream.unzstd(): ZstdCompressorInputStream = ZstdCompressorInputStream(this)

/**
 * Wrap `this` into an [ZstdCompressorOutputStream] using the given [preset] and [useChecksum]
 * setting, defaulting to the zstd default and no checksum.
 */
fun OutputStream.zstd(
    preset: Int = zstdDefaultCompressionLevel(),
    useChecksum: Boolean = false,
): ZstdCompressorOutputStream = ZstdCompressorOutputStream(this, preset, useChecksum)
