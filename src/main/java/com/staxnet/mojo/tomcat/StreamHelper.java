package com.staxnet.mojo.tomcat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamHelper {
    public static OutputStream createMergedOutputStream(OutputStream out1, OutputStream out2)
    {
        return new MergedOutputStream(out1, out2);
    }
    
    public static OutputStream createAutoFlushOutputStream(OutputStream out)
    {
        return new AutoFlushOutputStream(out);
    }
    
    public static OutputStream createNoCloseOutputStream(OutputStream out)
    {
        return new NoCloseOutputStream(out);
    }
    
    public static InputStream createNoCloseInputStream(InputStream in)
    {
        return new NoCloseInputStream(in);
    }
    
    public static void close(InputStream in)
    {
        try {
            in.close();
        } catch (IOException e) {
        }
    }
    
    public static void close(OutputStream out)
    {
        try {
            out.close();
        } catch (IOException e) {
        }
    }
    
    public static InputStream copyAndConsumeStream(InputStream in) throws IOException
    {        
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        transferStream(in, bout);
        return new ByteArrayInputStream(bout.toByteArray());
    }
    
    public static void consumeStream(InputStream in) throws IOException
    {        
        byte[] bytes = new byte[1024];
        int numRead = in.read(bytes);
        while(numRead != -1)
        {
            numRead = in.read(bytes);
        }
        in.close();
    }

    public static void transferStream(InputStream in,
            OutputStream out) throws IOException {
        byte[] bytes = new byte[1024];
        int numRead = in.read(bytes);
        while(numRead != -1)
        {
            out.write(bytes, 0, numRead);
            numRead = in.read(bytes);
        }
    }
    
    public static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        transferStream(in, bout);
        byte[] bytes = bout.toByteArray();
        bout.close();
        return bytes;
    }
}

class NoCloseInputStream extends InputStream
{
    private InputStream in;
    NoCloseInputStream(InputStream in)
    {
        this.in = in;
    }
    public int available() throws IOException {
        return in.available();
    }
    public void close() throws IOException {
    }
    public boolean equals(Object obj) {
        return in.equals(obj);
    }
    public int hashCode() {
        return in.hashCode();
    }
    public void mark(int arg0) {
        in.mark(arg0);
    }
    public boolean markSupported() {
        return in.markSupported();
    }
    public int read() throws IOException {
        return in.read();
    }
    public int read(byte[] arg0, int arg1, int arg2) throws IOException {
        return in.read(arg0, arg1, arg2);
    }
    public int read(byte[] arg0) throws IOException {
        return in.read(arg0);
    }
    public void reset() throws IOException {
        in.reset();
    }
    public long skip(long arg0) throws IOException {
        return in.skip(arg0);
    }
    public String toString() {
        return in.toString();
    }
    
    
}

class NoCloseOutputStream extends OutputStream
{
    private OutputStream out;
    NoCloseOutputStream(OutputStream out)
    {
        this.out = out;
    }
    @Override
    public void close() throws IOException {
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        out.write(b);
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }
}

class AutoFlushOutputStream extends OutputStream
{
    private OutputStream out;
    AutoFlushOutputStream(OutputStream out)
    {
        this.out = out;
    }
    @Override
    public void close() throws IOException {
        out.close();
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out.write(b, off, len);
        out.flush();
    }

    @Override
    public void write(byte[] b) throws IOException {
        out.write(b);
        out.flush();
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
        out.flush();
    }
}

class MergedOutputStream extends OutputStream
{
    private OutputStream out1;
    private OutputStream out2;
    
    MergedOutputStream(OutputStream out1, OutputStream out2)
    {
        this.out1 = out1;
        this.out2 = out2;
    }
    
    @Override
    public void close() throws IOException {
        out1.close();
        out2.close();
    }

    @Override
    public void flush() throws IOException {
        out1.flush();
        out2.flush();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        out1.write(b, off, len);
        out2.write(b, off, len);
    }

    @Override
    public void write(byte[] b) throws IOException {
        out1.write(b);
        out2.write(b);
    }

    @Override
    public void write(int b) throws IOException {
        out1.write(b);
        out2.write(b);
    }    
}