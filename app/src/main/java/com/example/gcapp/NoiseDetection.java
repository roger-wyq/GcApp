package com.example.gcapp;


import android.media.MediaRecorder;
import android.util.Log;
import android.media.AudioFormat;
import android.media.AudioRecord;


public class NoiseDetection {
 
    private static final String TAG = "AudioRecord";
    static final int SAMPLE_RATE_IN_HZ = 8000;
    static final int BUFFER_SIZE = AudioRecord.getMinBufferSize(SAMPLE_RATE_IN_HZ,
                                AudioFormat.CHANNEL_IN_DEFAULT, AudioFormat.ENCODING_PCM_16BIT);
    AudioRecord mAudioRecord;
    boolean isGetVoiceRun;
    Object mLock;
 
    public NoiseDetection() {
        mLock = new Object();
    }
 
    public double getNoiseLevel() {
        if (isGetVoiceRun) {
            Log.e(TAG, "还在录着呢");
            return 0;
        }
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                                SAMPLE_RATE_IN_HZ, AudioFormat.CHANNEL_IN_DEFAULT,
                                AudioFormat.ENCODING_PCM_16BIT, BUFFER_SIZE);
        if (mAudioRecord == null) {
            Log.e("sound", "mAudioRecord初始化失败");
        }
        isGetVoiceRun = true;

        mAudioRecord.startRecording();
        short[] buffer = new short[BUFFER_SIZE];
        int num = 0;
        double avg = 0;
        while (isGetVoiceRun) {
            //r是实际读取的数据长度，一般而言r会小于buffersize
            int r = mAudioRecord.read(buffer, 0, BUFFER_SIZE);
            long v = 0;
            // 将 buffer 内容取出，进行平方和运算
            for (int i = 0; i < buffer.length; i++) {
                v += buffer[i] * buffer[i];
            }
            // 平方和除以数据总长度，得到音量大小。
            double mean = v / (double) r;

            double volume = 10 * Math.log10(mean);
            Log.d(TAG, num +": 分贝值:" + volume);
            avg += volume;
            num ++;
            if(num == 50) {
                avg /= 50;
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
                return avg;
            }
            synchronized (mLock) {
                try {
                    mLock.wait(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        mAudioRecord.stop();
        mAudioRecord.release();
        mAudioRecord = null;
        avg /= num;
        return avg;
    }
}