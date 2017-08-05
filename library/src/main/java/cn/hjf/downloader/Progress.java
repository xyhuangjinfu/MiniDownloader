package cn.hjf.downloader;

import java.io.Serializable;
import java.util.List;

/**
 * Created by huangjinfu on 2017/8/5.
 */

public class Progress implements Serializable {

    private static final long serialVersionUID = 1L;

    private long total;
    private long download;
    private List<Range> downloadedRanges;
}
