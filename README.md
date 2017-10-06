# MiniDownloader
A handy multi task downloader on Android platform, which support http and ftp protocol. Also support continue download from the break point of last stopped.

## How to use. ##
#### Initial MiniDownloader. ####
```
MiniDownloader.getInstance().init(contextInstance);
```
#### Set debuggable if you want. ####
In debug mode it will print some log.
```
MiniDownloader.getInstance().setDebuggable(true);
```
#### Create a task to download. ####
```
        Task task = new Task(
                "http://haixi.jb51.net:8080/201506/books/sfdl3.rar",
                Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MiniDownloader" + File.separator + "sfdl3.rar",
                new Listener() {
                    @Override
                    public void onWait(Task task) {
                    }

                    @Override
                    public void onStart(Task task) {
                    }

                    @Override
                    public void onProgressUpdate(Task task) {
                    }

                    @Override
                    public void onStop(Task task) {
                    }

                    @Override
                    public void onFinish(Task task) {
                    }

                    @Override
                    public void onDelete(Task task) {
                    }
                },
                new ErrorListener() {
                    @Override
                    public void onError(Task task, Exception error) {
                    }
                },
                Task.Priority.HIGH);
```
#### If some special charaters in your url or user or password, like @, #, you can use a TaskUrl instead to plain url string. ####
```
        Task task = new Task(
                new FtpTaskUrl(
                                "192.168.1.7",
                                21,
                                "h/&^%$#@?:jf",
                                "h/&^%$#@?:jf666",
                                "te&^%$#@st.txt"
                        ),
                Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "MiniDownloader" + File.separator + "a.txt",
                listener,
                errorListener);
```
#### Start task. ####
```
MiniDownloader.getInstance().start(task);
```
#### Listen task progress update. ####
```
@Override
public void onProgressUpdate(Task task) {
    Progress progress = task.getProgress();
    double progressPercentage = progress.getDownloaded() * 1.0 / progress.getTotal();
    String speedInfo = progress.getNetworkSpeed() + "kb/s";
}
```
#### Stop task. ####
If you are downloading a large file, but can not downloading util it finish, so you can stop it and continue download at next start up.
```
MiniDownloader.getInstance().stop(task);
```
#### Delete task. ####
If you are stop a task, and never want to continue it, and not want to leave the data downloaded before, you can delete it.
```
MiniDownloader.getInstance().delete(task);
```
#### When you exit your app and restart at next time, you can restart your tasks which not finished in last download. ####
```
List<Task> unfinishedTasks = MiniDownloader.getInstance().getStoppedTaskList();
for (int i = 0; i < unfinishedTasks.size; i++) {
    MiniDownloader.getInstance().start(unfinishedTasks.get(i));
}
```
