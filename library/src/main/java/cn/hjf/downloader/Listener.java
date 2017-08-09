package cn.hjf.downloader;

/**
 * Created by huangjinfu on 2017/8/7.
 */

public interface Listener {

    /**
     * Notify that task be submitted to workers.
     *
     * @param task
     */
    void onWait(Task task);

    /**
     * Notify that task really be executed by workers.
     *
     * @param task
     */
    void onStart(Task task);

    /**
     * Notify that task progress is updated.
     *
     * @param task
     * @param progress
     */
    void onProgress(Task task, Progress progress);

    /**
     * Notify that task be stopped.
     *
     * @param task
     */
    void onStop(Task task);

    /**
     * Notify that task be finished.
     *
     * @param task
     */
    void onFinish(Task task);

    /**
     * notify that task be deleted.
     *
     * @param task
     */
    void onDelete(Task task);
}
