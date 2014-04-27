package drfoliberg.common.network.messages;

import drfoliberg.common.network.ClusterProtocol;

public class TaskReport extends AuthMessage {

    private static final long serialVersionUID = -2146895423858055901L;

    private double progress;
    private int taskId;
    private String jobId;
//	private long timeElapsed;
//	private long timeEstimated;
//	private double fps;

    public TaskReport(String unid) {
        super(ClusterProtocol.TASK_REPORT, unid);
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }


}