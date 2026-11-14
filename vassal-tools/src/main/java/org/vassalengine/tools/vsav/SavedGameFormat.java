package org.vassalengine.tools.vsav;

import VASSAL.build.module.GameState;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Random;

/**
 * How the savedGame entry of a .vsav / .vlog is encoded on write.
 * Reading needs no choice: VASSAL's DeobfuscatingInputStream detects both.
 */
public enum SavedGameFormat {
    /** XZ-compressed, as VASSAL 3.8.0+ writes. Not readable by 3.7. */
    XZ,
    /** Hex-obfuscated ("!VCSK" header), as VASSAL 3.7 and earlier wrote. Readable by every version. */
    LEGACY;

    /**
     * Wrap {@code out} so what is written to it is stored in this format.
     * Closing the returned stream finishes the encoding but leaves {@code out} open.
     */
    public OutputStream wrap(OutputStream out) throws IOException {
        OutputStream target = new NonClosingOutputStream(out);
        return this == XZ ? GameState.compressSavedGame(target) : new LegacyObfuscatingOutputStream(target);
    }

    /**
     * The pre-3.8 encoding, ported from VASSAL 3.7 tools/io/ObfuscatingOutputStream
     * (3.8.0 kept only the reading side): header, random key as two hex digits,
     * then each byte XORed with the key as two hex digits.
     */
    private static final class LegacyObfuscatingOutputStream extends FilterOutputStream {
        private static final String HEADER = "!VCSK";
        private static final byte[] HEX = "0123456789abcdef".getBytes(StandardCharsets.US_ASCII);
        private static final Random RANDOM = new Random();

        private final byte key;
        private final byte[] pair = new byte[2];

        LegacyObfuscatingOutputStream(OutputStream out) throws IOException {
            super(out);
            key = (byte) RANDOM.nextInt(256);
            out.write(HEADER.getBytes(StandardCharsets.US_ASCII));
            writeHex(key);
        }

        @Override
        public void write(int b) throws IOException {
            writeHex((byte) (b ^ key));
        }

        @Override
        public void write(byte[] bytes, int off, int len) throws IOException {
            for (int i = 0; i < len; i++) {
                write(bytes[off + i]);
            }
        }

        private void writeHex(byte b) throws IOException {
            pair[0] = HEX[(b & 0xF0) >>> 4];
            pair[1] = HEX[b & 0x0F];
            out.write(pair);
        }
    }

    /** Keeps the enclosing ZipOutputStream open when the format stream is closed. */
    private static final class NonClosingOutputStream extends FilterOutputStream {
        NonClosingOutputStream(OutputStream out) {
            super(out);
        }

        @Override
        public void write(byte[] bytes, int off, int len) throws IOException {
            out.write(bytes, off, len);
        }

        @Override
        public void close() throws IOException {
            flush();
        }
    }
}
