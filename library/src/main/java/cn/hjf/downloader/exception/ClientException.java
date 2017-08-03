package cn.hjf.downloader.exception;

/**
 * Created by huangjinfu on 2017/8/3.
 */

public class ClientException extends Exception {

    public ClientException() {
    }

    public ClientException(String message) {
        super(message);
    }

    public ClientException(String message, Throwable cause) {
        super(message, cause);
    }

    public ClientException(Throwable cause) {
        super(cause);
    }
}
