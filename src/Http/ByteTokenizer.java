package Http;

/**
 * straight out copy-n-paste from
 * https://github.com/ebarlas/microhttp/blob/main/src/main/java/org/microhttp/ByteTokenizer.java
 */

import java.nio.ByteBuffer;
import java.util.Arrays;
class ByteTokenizer {
    private byte[] array = new byte[0];
    private int position;
    private int size;

    int size() {
        return size;
    }

    int capacity() {
        return array.length;
    }

    int remaining() {
        return size - position;
    }

    void compact() {
        array = Arrays.copyOfRange(array, position, size);
        size = size - position;
        position = 0;
    }

    void add(ByteBuffer buffer) {
        int bufferLen = buffer.remaining();
        if (array.length - size < bufferLen) {
            array = Arrays.copyOf(array, Math.max(size + bufferLen, array.length * 2));
        }
        buffer.get(array, size, bufferLen);
        size += bufferLen;
    }

    byte[] next(int length) {
        if (size - position < length) {
            return null;
        }
        byte[] result = Arrays.copyOfRange(array, position, position + length);
        position += length;
        return result;
    }

    byte[] next(byte[] delimiter) {
        int index = indexOf(delimiter);
        if (index < 0) {
            return null;
        }
        byte[] result = Arrays.copyOfRange(array, position, index);
        position = index + delimiter.length;
        return result;
    }

    private int indexOf(byte[] delimiter) {
        for (int i = position; i <= size - delimiter.length; i++) {
            if (Arrays.equals(delimiter, 0, delimiter.length, array, i, i + delimiter.length)) {
                return i;
            }
        }
        return -1;
    }

}