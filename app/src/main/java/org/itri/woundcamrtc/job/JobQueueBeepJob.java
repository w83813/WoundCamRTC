package org.itri.woundcamrtc.job;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.birbit.android.jobqueue.Job;
import com.birbit.android.jobqueue.JobManager;
import com.birbit.android.jobqueue.Params;
import com.birbit.android.jobqueue.RetryConstraint;

public class JobQueueBeepJob extends Job {
    private final String TAG = getClass().getSimpleName();
    private JobManager jobQueueManager = null;
    private static BeepManager beepManager = null;
    private int soundNo = -1;

    public JobQueueBeepJob(JobManager _jobQueueManager, String tag, BeepManager param, int _soundNo) {
        //super(new Params(PRIORITY).requireNetwork().persist().groupBy(tag).singleInstanceBy(tag));
        super(new Params(Thread.NORM_PRIORITY).groupBy("beepJob"));
        jobQueueManager = _jobQueueManager;
        beepManager = param;
        soundNo = _soundNo;
    }

    @Override
    public void onAdded() {
    }

    @Override
    public void onRun() throws Throwable {
        beepManager.playBeepSoundAndVibrate(soundNo);
//        try {
//            SystemClock.sleep(10);
//        } catch (Exception e) {
//
//        }
    }

    @Override
    protected RetryConstraint shouldReRunOnThrowable(@NonNull Throwable throwable, int runCount, int maxRunCount) {
        //如果重试了n次仍未成功，那么就放弃执行任务，也进入onCancel
        if (runCount == 3)
            return RetryConstraint.CANCEL;
        return RetryConstraint.RETRY;
    }

    //如果重试超过限定次数，将onCancel.
    //如果用户主动放弃删掉这个任务，也一样进入onCancel
    @Override
    protected void onCancel(int cancelReason, @Nullable Throwable throwable) {
    }
}
