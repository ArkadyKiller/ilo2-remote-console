import java.io.IOException;
import java.io.OutputStream;

public class TeeOutputStream extends OutputStream {
    private final OutputStream a;
    private final OutputStream b;

    public TeeOutputStream(OutputStream a, OutputStream b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public void write(int x) throws IOException {
        a.write(x);
        b.write(x);
    }

    @Override
    public void write(byte[] buf, int off, int len) throws IOException {
        a.write(buf, off, len);
        b.write(buf, off, len);
    }

    @Override
    public void flush() throws IOException {
        a.flush();
        b.flush();
    }
}
