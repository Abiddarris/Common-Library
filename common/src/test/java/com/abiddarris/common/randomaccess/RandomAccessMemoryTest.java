package com.abiddarris.common.randomaccess;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

public class RandomAccessMemoryTest {

    private RandomAccessMemory memory = new RandomAccessMemory();

    @Test
    public void writeAndRead() throws IOException {
        memory.write('h');
        memory.seek(0);

        assertEquals('h', memory.read());
        assertEquals(1, memory.getLength());
        assertEquals(1, memory.getPointer());
    }

    @Test
    public void seek() throws IOException {
        memory.write('h');
        memory.seek(0);

        assertEquals(1, memory.getLength());
        assertEquals(0, memory.getPointer());
    }

    @Test
    public void read_end_of_buf() throws IOException {
        assertEquals(-1, memory.read());
        assertEquals(0, memory.getLength());
        assertEquals(0, memory.getPointer());
    }

    @Test
    public void write_and_read_bytes() throws IOException {
        memory.write("hi my name is Dave".getBytes());

        assertEquals(18, memory.getLength());
        assertEquals(18, memory.getPointer());

        memory.seek(0);

        byte[] buf = new byte[18];

        assertEquals(18, memory.read(buf));
        assertArrayEquals("hi my name is Dave".getBytes(), buf);;
    }

    @Test
    public void write_bytes_pass_null() throws IOException {
        assertThrows(NullPointerException.class, () -> memory.write(null));
    }

    @Test
    public void read_bytes_the_end_of_file() throws IOException {
        byte[] buf = new byte[18];

        assertEquals(-1, memory.read(buf));
        assertArrayEquals(new byte[18], buf);
    }

    @Test
    public void read_overgrown_bytes() throws IOException {
        memory.write("Lorem ipsum".getBytes());
        memory.seek(0);

        byte[] buf = new byte[13];
        buf[11] = 'a';
        buf[12] = 'c';

        assertEquals(11, memory.read(buf));

        byte[] result = new byte[13];
        result[11] = 'a';
        result[12] = 'c';

        System.arraycopy("Lorem ipsum".getBytes(), 0, result, 0, 11);

        assertArrayEquals(result, buf);

        assertEquals(11, memory.getLength());
        assertEquals(11, memory.getPointer());
    }

    @Test
    public void read_bytes_pass_null() {
        assertThrows(NullPointerException.class, () -> memory.read(null));
    }

    @Test
    public void write_bytes_extra_pass_null() {
        assertThrows(NullPointerException.class, () -> memory.write(null, 0, 1));
    }

    @Test
    public void write_bytes_extra_pass_negative_offset() {
        assertThrows(IndexOutOfBoundsException.class, () -> memory.write(new byte[16], -1, 1));
    }

    @Test
    public void write_bytes_extra_pass_negative_len() {
        assertThrows(IndexOutOfBoundsException.class, () -> memory.write(new byte[16], 1, -2));
    }

    @Test
    public void write_bytes_extra_pass_len_cannot_be_satisfied() {
        assertThrows(IndexOutOfBoundsException.class, () -> memory.write(new byte[16], 1, 16));
    }

    @Test
    public void write_and_read_bytes_extra() throws IOException {
        byte[] data = ". I live in USA".getBytes();

        memory.write(data, 0, data.length);

        assertEquals(15, memory.getLength());
        assertEquals(15, memory.getPointer());

        memory.seek(0);

        byte[] readBuf = new byte[15];

        assertEquals(15, memory.read(readBuf, 0, readBuf.length));
        assertArrayEquals(readBuf, data);
    }

    @Test
    public void write_and_read_bytes_extra_with_offset() throws IOException {
        byte[] data = "BHJS. I was".getBytes();

        memory.write(data, 4, data.length - 4);

        assertEquals(7, memory.getLength());
        assertEquals(7, memory.getPointer());

        memory.seek(0);

        byte[] readBuf = new byte[11];
        data[0] = 'A';
        data[1] = 'B';
        data[2] = 'C';
        data[3] = 'D';

        byte[] expected = new byte[11];

        System.arraycopy(readBuf, 0, expected, 0, 4);
        System.arraycopy(". I was".getBytes(), 0, expected, 4, 7);

        assertEquals(7, memory.read(readBuf, 4, 7));
        assertArrayEquals(expected, readBuf);
    }

    @Test
    public void write_and_read_bytes_extra_with_len() throws IOException {
        byte[] data = " writing th[><*(]".getBytes();

        memory.write(data, 0, 11);

        assertEquals(11, memory.getLength());
        assertEquals(11, memory.getPointer());

        byte[] readBytes = new byte[17];

        System.arraycopy("sigma]".getBytes(), 0, readBytes, 11, 6);

        byte[] expected = new byte[17];
        System.arraycopy(readBytes, 0, expected, 0, readBytes.length);
        System.arraycopy(" writing th".getBytes(), 0, expected, 0, 11);

        memory.seek(0);

        assertEquals(11, memory.read(readBytes, 0, 11));
        assertArrayEquals(expected, readBytes);
    }

    @Test
    public void read_bytes_extra_pass_null() {
        assertThrows(NullPointerException.class, () -> memory.read(null, 0, 1));
    }

    @Test
    public void read_bytes_extra_negative_offset() {
        assertThrows(IndexOutOfBoundsException.class, () -> memory.read(new byte[16], -1, 1));
    }

    @Test
    public void read_bytes_extra_negative_len() {
        assertThrows(IndexOutOfBoundsException.class, () -> memory.read(new byte[16], 0, -1));
    }

    @Test
    public void read_bytes_extra_unsatisfied_len() {
        assertThrows(IndexOutOfBoundsException.class, () -> memory.read(new byte[16], 1, 16));
    }

    @Test
    public void read_bytes_extra_end_of_file() throws IOException {
        byte[] data = new byte[16];

        assertEquals(-1, memory.read(data, 0, data.length));
        assertArrayEquals(new byte[16], data);

        assertEquals(0, memory.getLength());
        assertEquals(0, memory.getPointer());
    }

    @Test
    public void read_bytes_extra_overgrown_to_the_end_of_file() throws IOException {
        memory.write("Hi there".getBytes());
        memory.seek(0);

        byte[] data = new byte[10];
        byte[] expected = new byte[10];

        System.arraycopy("Hi there".getBytes(), 0, expected, 0, 8);

        assertEquals(8, memory.read(data, 0, data.length));

        assertArrayEquals(expected, data);

        assertEquals(8, memory.getLength());
        assertEquals(8, memory.getPointer());
    }

    @Test
    public void grow_and_shrink_then_regrow() throws IOException {
        memory.setLength(5);

        assertEquals(5, memory.getLength());
        assertEquals(0, memory.getPointer());

        byte[] data = new byte[4];

        assertEquals(data.length, memory.read(data, 0, data.length));
        assertArrayEquals(new byte[4], data);
        assertEquals(5, memory.getLength());
        assertEquals(4, memory.getPointer());

        data = "DATA".getBytes();
        byte[] expected = Arrays.copyOf(data, data.length);

        memory.seek(0);
        memory.write(data, 0, data.length);

        assertEquals(5, memory.getLength());
        assertEquals(4, memory.getPointer());

        memory.seek(0);

        data = new byte[4];
        assertEquals(data.length, memory.read(data, 0, data.length));

        assertArrayEquals(expected, data);
        assertEquals(5, memory.getLength());
        assertEquals(4, memory.getPointer());

        memory.setLength(0);

        assertEquals(0, memory.getLength());
        assertEquals(0, memory.getPointer());

        data = new byte[4];

        assertEquals(-1, memory.read(data));
        assertArrayEquals(new byte[4], data);
        assertEquals(0, memory.getLength());
        assertEquals(0, memory.getPointer());

        memory.setLength(5);

        assertEquals(5, memory.getLength());
        assertEquals(0, memory.getPointer());

        data = new byte[4];
        assertEquals(4, memory.read(data));
        assertArrayEquals(new byte[4], data);
        assertEquals(5, memory.getLength());
        assertEquals(4, memory.getPointer());

    }

    @Test
    public void shrink_without_pointer_overflowing() throws IOException {
        memory.write("Hi there".getBytes());
        memory.seek(3);

        assertEquals(8, memory.getLength());
        assertEquals(3, memory.getPointer());

        memory.setLength(5);

        assertEquals(5, memory.getLength());
        assertEquals(3, memory.getPointer());
    }

    @Test
    public void grow_to_beyond_max_integer() {
        assertThrows(IndexOutOfBoundsException.class, () -> memory.setLength(Integer.MAX_VALUE + 2));
    }

    @Test
    public void shrink_to_negative() throws IOException {
        assertThrows(IndexOutOfBoundsException.class, () -> memory.setLength(-1));
    }

    @Test
    public void seek_to_negative() throws IOException {
        assertThrows(IndexOutOfBoundsException.class, () -> memory.seek(-1));
    }

    @Test
    public void seek_beyond_max_int() throws IOException {
        assertThrows(IndexOutOfBoundsException.class, () -> memory.seek(Integer.MAX_VALUE + 1));
    }

    @Test
    public void seek_beyond_length() throws IOException {
        memory.seek(64);

        assertEquals(0, memory.getLength());
        assertEquals(64, memory.getPointer());
    }

    @Test
    public void write_after_seek_beyond_length() throws IOException {
        memory.seek(5);
        memory.write('b');

        assertEquals(6, memory.getLength());
        assertEquals(6, memory.getPointer());

        memory.seek(5);

        assertEquals('b', memory.read());
    }

    @Test
    public void read_after_seek_beyond_length() throws IOException {
        memory.seek(5);

        assertEquals(0, memory.read());
        assertEquals(6, memory.getLength());
        assertEquals(6, memory.getPointer());
    }

    @Test
    public void write_bytes_after_seek() throws IOException {
        memory.seek(5);

        byte[] data = "HAI".getBytes();

        memory.write(data);

        assertEquals(8, memory.getLength());
        assertEquals(8, memory.getPointer());

        memory.seek(5);

        data = new byte[3];

        assertEquals(3, memory.read(data));
        assertArrayEquals("HAI".getBytes(), data);
        assertEquals(8, memory.getLength());
        assertEquals(8, memory.getPointer());
    }

    @Test
    public void read_bytes_after_seek_beyond_length() throws IOException {
        memory.seek(5);

        byte[] b = new byte[4];

        assertEquals(1, memory.read(b));
        assertArrayEquals(new byte[4], b);
        assertEquals(6, memory.getLength());
        assertEquals(6, memory.getPointer());
    }

    @Test
    public void replace_byte() throws IOException {
        memory.write('A');
        memory.seek(0);
        memory.write('H');

        assertEquals('H', memory.readAt(0));
        assertEquals(1, memory.getLength());
        assertEquals(1, memory.getPointer());
    }

    @Test
    public void replace_bytes() throws IOException {
        memory.seek(0);
        memory.write("JI".getBytes());

        byte[] b = new byte[2];

        assertEquals(2, memory.getLength());
        assertEquals(2, memory.readAt(b, 0));
        assertArrayEquals("JI".getBytes(), b);
        assertEquals(2, memory.getPointer());
    }

}
