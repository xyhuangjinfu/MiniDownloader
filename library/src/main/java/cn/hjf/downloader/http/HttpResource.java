package cn.hjf.downloader.http;

import java.io.Serializable;

/**
 * Created by huangjinfu on 2017/8/3.
 */

class HttpResource implements Serializable {

    private static final long serialVersionUID = 1L;

    private long contentLength;
    private String acceptRanges;
    private String eTag;
    private String lastModified;

    public HttpResource(long contentLength, String acceptRanges, String eTag, String lastModified) {
        this.contentLength = contentLength;
        this.acceptRanges = acceptRanges;
        this.eTag = eTag;
        this.lastModified = lastModified;
    }

    public long getContentLength() {
        return contentLength;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
    }

    public String getAcceptRanges() {
        return acceptRanges;
    }

    public void setAcceptRanges(String acceptRanges) {
        this.acceptRanges = acceptRanges;
    }

    public String geteTag() {
        return eTag;
    }

    public void seteTag(String eTag) {
        this.eTag = eTag;
    }

    public String getLastModified() {
        return lastModified;
    }

    public void setLastModified(String lastModified) {
        this.lastModified = lastModified;
    }
}
