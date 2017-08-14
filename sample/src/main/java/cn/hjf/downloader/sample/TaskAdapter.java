package cn.hjf.downloader.sample;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import cn.hjf.downloader.Progress;
import cn.hjf.downloader.Task;

/**
 * Created by huangjinfu on 2017/8/7.
 */

public class TaskAdapter extends BaseAdapter {

    private Context context;
    private List<Task> taskList;
    private OnEventListener onEventListener;

    public interface OnEventListener {
        void onStart(Task task);

        void onStop(Task task);

        void onDelete(Task task);
    }

    public TaskAdapter(Context context, List<Task> taskList) {
        this.context = context;
        this.taskList = taskList;
    }

    @Override
    public int getCount() {
        return taskList == null ? 0 : taskList.size();
    }

    @Override
    public Object getItem(int position) {
        return taskList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.lv_task, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        initValue(holder, position);
        initEvent(holder, position);

        return convertView;
    }

    private void initValue(ViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.infoTv.setText("url:" + task.getUrlStr() + "\npath:" + task.getFilePath());

        Progress progress = task.getProgress();
        if (progress != null) {
            holder.pb.setMax(100);
            holder.pb.setProgress((int) (progress.getDownloaded() * 100.0 / progress.getTotal()));
            holder.speedTv.setText(progress.getNetworkSpeed() + "kb/s");
        } else {
            holder.pb.setMax(0);
            holder.pb.setProgress(0);
            holder.speedTv.setText("0kb/s");
        }

        holder.statusTv.setText(task.getStatus().toString());
        holder.priorityTv.setText(task.getPriority().toString());

        if (task.getStatus() == Task.Status.NEW) {
            holder.startBtn.setEnabled(true);
            holder.stopBtn.setEnabled(false);
        } else if (task.getStatus() == Task.Status.WAITING || task.getStatus() == Task.Status.RUNNING) {
            holder.startBtn.setEnabled(false);
            holder.stopBtn.setEnabled(true);
        } else if (task.getStatus() == Task.Status.STOPPED) {
            holder.startBtn.setEnabled(true);
            holder.stopBtn.setEnabled(false);
        } else if (task.getStatus() == Task.Status.FINISHED) {
            holder.startBtn.setEnabled(false);
            holder.stopBtn.setEnabled(false);
        } else if (task.getStatus() == Task.Status.ERROR) {
            holder.startBtn.setEnabled(true);
            holder.stopBtn.setEnabled(false);
        }
    }

    private void initEvent(ViewHolder holder, final int position) {
        holder.startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onEventListener != null) {
                    onEventListener.onStart(taskList.get(position));
                }
            }
        });
        holder.stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onEventListener != null) {
                    onEventListener.onStop(taskList.get(position));
                }
            }
        });
        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onEventListener != null) {
                    onEventListener.onDelete(taskList.get(position));
                }
            }
        });
    }

    public void setOnEventListener(OnEventListener onEventListener) {
        this.onEventListener = onEventListener;
    }

    private static class ViewHolder {
        TextView infoTv, statusTv, priorityTv, speedTv;
        ProgressBar pb;
        Button startBtn, stopBtn, deleteBtn;

        public ViewHolder(View rootView) {
            infoTv = (TextView) rootView.findViewById(R.id.infoTv);
            statusTv = (TextView) rootView.findViewById(R.id.statusTv);
            priorityTv = (TextView) rootView.findViewById(R.id.priorityTv);
            speedTv = (TextView) rootView.findViewById(R.id.speedTv);
            pb = (ProgressBar) rootView.findViewById(R.id.pb);
            startBtn = (Button) rootView.findViewById(R.id.start);
            stopBtn = (Button) rootView.findViewById(R.id.stop);
            deleteBtn = (Button) rootView.findViewById(R.id.delete);
        }
    }
}
