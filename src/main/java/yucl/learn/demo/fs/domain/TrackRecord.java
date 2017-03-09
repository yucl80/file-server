package yucl.learn.demo.fs.domain;

/**
 * Created by yuchunlei on 2017/3/9.
 */
public class TrackRecord {
    private long begin;
    private long end;
    public long getBegin() {
        return begin;
    }

    public void setBegin(long begin) {
        this.begin = begin;
    }


    public long getEnd() {
        return end;
    }

    public void setEnd(long end) {
        this.end = end;
    }



    public TrackRecord(long begin, long end) {
        this.begin = begin;
        this.end = end;
    }

}