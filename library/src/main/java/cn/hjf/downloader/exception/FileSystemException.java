package cn.hjf.downloader.exception;

/**
 * Created by huangjinfu on 2017/8/3.
 */

public class FileSystemException extends Exception {

    public FileSystemException() {
    }

    public FileSystemException(String message) {
        super(message);
    }

    public FileSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public FileSystemException(Throwable cause) {
        super(cause);
    }
}
