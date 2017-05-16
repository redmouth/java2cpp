package co.deepblue.java2cpp.io;


import java.io.IOException;
import java.io.Writer;

/**
 * Created by levin on 17-5-11.
 */
public class BufferedStringWriter extends Writer {
    StringBuilder builder;

    public BufferedStringWriter() {
        builder = new StringBuilder();
    }

    public BufferedStringWriter(String data) {
        builder = new StringBuilder();
        builder.append(data);
    }

    public void insert(String data, int offset) {
        builder.insert(offset, data);
    }

    public void insertAtBegin(String data) {
        builder.insert(0, data);
    }

    public String getString() {
        return builder.toString();
    }

    public void write(String data) {
        builder.append(data);
    }

    public void writeln(String line) {
        builder.append(line).append("\n");
    }




    public void write(char cbuf[], int off, int len) throws IOException {
    }

    public void flush() throws IOException {

    }

    public void close() throws IOException {

    }
}
