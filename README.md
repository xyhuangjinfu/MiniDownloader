# MiniDownloader
A handy multi task downloader on Android platform, which support htttp and ftp protocol. 

## How to use. ##
#### Initial MiniDownloader. ####
```
MiniDownloader.getInstance().init(contextInstance);
```
#### Set debuggable if you want. In debug mode it will print some log. ####
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
                    public void onStart(Task task) {

                    }

                    @Override
                    public void onProgress(Task task, Progress progress) {

                    }

                    @Override
                    public void onStop(Task task) {

                    }

                    @Override
                    public void onFinish(Task task) {

                    }
                },
                new ErrorListener() {
                    @Override
                    public void onError(Task task, Exception error) {

                    }
                });
```
#### Start task. ####
```
MiniDownloader.getInstance().start(task);
```
#### Stop task. ####
```
MiniDownloader.getInstance().stop(task);
```
#### When you exit your app and restart at next time, you can restart your tasks which not finished in last download. ####
```
List<Task> unfinishedTasks = MiniDownloader.getInstance().getStoppedTaskList();
for (int i = 0; i < unfinishedTasks.size; i++) {
    MiniDownloader.getInstance().start(unfinishedTasks.get(i));
}
```
