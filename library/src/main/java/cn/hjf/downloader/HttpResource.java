package cn.hjf.downloader;

/**
 * Created by huangjinfu on 2017/8/5.
 */

class HttpResource extends Resource {

    private static final long serialVersionUID = 1L;

    private final long contentLength;
    private final String acceptRanges;
    private final String eTag;
    private final String lastModified;

    public HttpResource(long contentLength, String acceptRanges, String eTag, String lastModified) {
        this.contentLength = contentLength;
        this.acceptRanges = acceptRanges;
        this.eTag = eTag;
        this.lastModified = lastModified;
    }

    public long getContentLength() {
        return contentLength;
    }

    public String getAcceptRanges() {
        return acceptRanges;
    }

    public String geteTag() {
        return eTag;
    }

    public String getLastModified() {
        return lastModified;
    }
}
