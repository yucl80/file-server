package yucl.learn.demo.fs.domain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yucl.learn.demo.fs.cfg.AppProperties;

import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author chunlei.yu
 */
public class FileId implements Comparable<FileId>, java.io.Serializable {

    private static final long serialVersionUID = 1L;

    static final Logger logger = LoggerFactory.getLogger(FileId.class);

    final int _time;
    final int _machine;
    final int _inc;
    final String _schema;
    final String _clusterId;
    boolean _new;

    private static AtomicInteger _nextInc = new AtomicInteger((new Random()).nextInt());

    public static int getCurrentCounter() {
        return _nextInc.get();
    }

    public FileId(String clusterId, String schema, boolean isNew) {
        _time = (int) (System.currentTimeMillis() / 1000);
        _machine = _genmachine;
        _inc = _nextInc.getAndIncrement();
        _schema = schema;
        _clusterId = clusterId;
        _new = true;
    }

    public FileId(String fileId) {
        if (!isValid(fileId))
            throw new IllegalArgumentException("invalid ObjectId [" + fileId + "]");
        _clusterId = fileId.substring(0, 2);
        _schema = fileId.substring(2, 4);
        String s = fileId.substring(4);
        byte b[] = new byte[12];
        for (int i = 0; i < b.length; i++) {
            b[i] = (byte) Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16);
        }
        ByteBuffer bb = ByteBuffer.wrap(b);
        _time = bb.getInt();
        _machine = bb.getInt();
        _inc = bb.getInt();
        _new = false;
    }

    public int getTime() {
        return _time;
    }

    public Date getCreatedTime() {
        return new Date((long) _time * 1000);
    }

    public static boolean isValid(String fileId) {
        if (fileId == null)
            return false;
        String s = fileId.substring(4);
        final int len = s.length();
        if (len != 24)
            return false;

        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);
            if (c >= '0' && c <= '9')
                continue;
            if (c >= 'a' && c <= 'f')
                continue;
            if (c >= 'A' && c <= 'F')
                continue;

            return false;
        }

        return true;
    }

    public static FileId get(String clusterId, String schema) {
        if (clusterId.length() != 2) {
            throw new IllegalArgumentException("invalid cluster Id  [" + clusterId + "]");
        }
        if (schema.length() != 2) {
            throw new IllegalArgumentException("invalid schema name [" + schema + "]");
        }
        return new FileId(clusterId, schema, true);
    }

    public String toString() {
        final StringBuilder buf = new StringBuilder(24);
        for (final byte b : toByteArray()) {
            buf.append(String.format("%02x", b & 0xff));
        }
        return _clusterId + _schema + buf.toString();
    }

    public String getId() {
        final StringBuilder buf = new StringBuilder(24);
        for (final byte b : toByteArray()) {
            buf.append(String.format("%02x", b & 0xff));
        }
        return _clusterId + _schema + buf.toString();
    }

    private byte[] toByteArray() {
        byte b[] = new byte[12];
        ByteBuffer bb = ByteBuffer.wrap(b);
        // by default BB is big endian like we need
        bb.putInt(_time);
        bb.putInt(_machine);
        bb.putInt(_inc);
        return b;
    }

    @Override
    public int hashCode() {
        // int x = _time;
        // x += (_machine * 111);
        // x += (_inc * 17);
        return toString().hashCode();
    }

    int _compareUnsigned(int i, int j) {
        long li = 0xFFFFFFFFL;
        li = i & li;
        long lj = 0xFFFFFFFFL;
        lj = j & lj;
        long diff = li - lj;
        if (diff < Integer.MIN_VALUE)
            return Integer.MIN_VALUE;
        if (diff > Integer.MAX_VALUE)
            return Integer.MAX_VALUE;
        return (int) diff;
    }

    public int compareTo(FileId id) {
        if (id == null)
            return -1;
        int x = _clusterId.compareTo(id._clusterId);
        if (x != 0)
            return x;
        x = _schema.compareTo(id._schema);
        if (x != 0)
            return x;
        x = _compareUnsigned(_time, id._time);
        if (x != 0)
            return x;
        x = _compareUnsigned(_machine, id._machine);
        if (x != 0)
            return x;
        return _compareUnsigned(_inc, id._inc);
    }

    private static final int _genmachine;

    static {
        try {
            // build a 2-byte machine piece based on NICs info
            int machinePiece;
            {
                try {
                    StringBuilder sb = new StringBuilder();
                    Enumeration<NetworkInterface> e = NetworkInterface.getNetworkInterfaces();
                    while (e.hasMoreElements()) {
                        NetworkInterface ni = e.nextElement();
                        sb.append(ni.toString());
                    }
                    machinePiece = sb.toString().hashCode() << 16;
                } catch (Throwable e) {
                    // exception sometimes happens with IBM JVM, use random
                    logger.warn(e.getMessage(), e);
                    machinePiece = (new Random().nextInt()) << 16;
                }
                logger.debug("machine piece post: " + Integer.toHexString(machinePiece));
            }

            // add a 2 byte process piece. It must represent not only the JVM
            // but the class loader.
            // Since static var belong to class loader there could be collisions
            // otherwise
            final int processPiece;
            {
                int processId = new Random().nextInt();
                try {
                    processId = java.lang.management.ManagementFactory.getRuntimeMXBean().getName().hashCode();
                } catch (Throwable t) {
                    logger.error(t.getMessage(), t);
                }

                ClassLoader loader = FileId.class.getClassLoader();
                int loaderId = loader != null ? System.identityHashCode(loader) : 0;

                StringBuilder sb = new StringBuilder();
                sb.append(Integer.toHexString(processId));
                sb.append(Integer.toHexString(loaderId));
                processPiece = sb.toString().hashCode() & 0xFFFF;
                logger.debug("process piece: " + Integer.toHexString(processPiece));
            }

            _genmachine = machinePiece | processPiece;
            logger.debug("machine : " + Integer.toHexString(_genmachine));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public String getschema() {
        return _schema;
    }


}
