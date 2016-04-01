package com.maps.mhealth;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewSeries.GraphViewSeriesStyle;
import com.jjoe64.graphview.LineGraphView;

import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.commons.lang3.ArrayUtils;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class HeartRateMonitor extends Activity
{
    public static final String TAG = "HeartRateMonitor";
    private final AtomicBoolean processing = new AtomicBoolean(false);

    private SurfaceView preview = null;
    private SurfaceHolder previewHolder = null;
    private Camera camera = null;

    private TextView text = null;

    private WakeLock wakeLock = null;

    private int averageIndex = 0;
    private final int averageArraySize = 100;
    private final int[] averageArray = new int[averageArraySize];

    public enum TYPE {
        GREEN, RED
    };

    private static TYPE currentType = TYPE.GREEN;

    public static TYPE getCurrent() {
        return currentType;
    }
    public static int bpm;
    private int beatsIndex = 0;
    private final int beatsArraySize = 14;
    private final int[] beatsArray = new int[beatsArraySize];
    private final long[] timesArray = new long[beatsArraySize];
    private double beats = 0;
    private long startTime = 0;
    private long endTime = 0;
    private long diffTime = 0;

    private GraphView graphView;
    private GraphViewSeries exampleSeries;

    int counter = 0;

    private final int sampleSize = 256;
    private final CircularFifoQueue sampleQueue = new CircularFifoQueue(sampleSize);
    private final CircularFifoQueue timeQueue = new CircularFifoQueue(sampleSize);

    public static final CircularFifoQueue bpmQueue = new CircularFifoQueue(40);

    private final FFT fft = new FFT(sampleSize);
    private Metronome metronome;
    private boolean streamData = false;
    public DatagramSocket mSocket = null;
    public DatagramPacket mPacket = null;
    TextView mIP_Adress;
    TextView mPort;
    final Context context = this;
    //public String activityStatus;
    String start;
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.heart_rate);
        preview = (SurfaceView) findViewById(R.id.preview);
        previewHolder = preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        text = (TextView) findViewById(R.id.text);
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK, "DoNotDimScreen");
        this.graphView = new LineGraphView(this, "Heart rate");
        graphView.setScrollable(true);
        this.exampleSeries = new GraphViewSeries("Heart rate",
                new GraphViewSeries.GraphViewSeriesStyle(Color.RED, 8),
                new GraphView.GraphViewData[] {});
        this.graphView.addSeries(this.exampleSeries);
        graphView.setViewPort(0, 60);
        graphView.setVerticalLabels(new String[] { "" });
        graphView.setHorizontalLabels(new String[] { "" });
        graphView.getGraphViewStyle().setVerticalLabelsWidth(1);
        graphView.setBackgroundColor(Color.WHITE);
        LinearLayout layout = (LinearLayout) findViewById(R.id.graph1);
        layout.addView(graphView);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        wakeLock.acquire();
        camera = Camera.open();
        startTime = System.currentTimeMillis();
        metronome = new Metronome(this);
    }

//    @Override
//    public void onPause()
//    {
//        super.onPause();
//        wakeLock.release();
//        camera.setPreviewCallback(null);
//        camera.stopPreview();
//        camera.release();
//        camera = null;
//        bpm = -1;
//    }

    private SurfaceHolder.Callback surfaceCallback = new SurfaceHolder.Callback()
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceCreated(SurfaceHolder holder)
        {
            try
            {
                camera.setPreviewDisplay(previewHolder);
                camera.setPreviewCallback(previewCallback);
            }
            catch (Throwable t)
            {
                //Log.e("PreviewDemo-surfaceCallback","Exception in setPreviewDisplay()", t);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,int height)
        {
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            Camera.Size size = getSmallestPreviewSize(width, height, parameters);
            if (size != null)
            {
                parameters.setPreviewSize(size.width, size.height);
                Log.d(TAG, "Using width=" + size.width + " height=" + size.height);
            }
            camera.setParameters(parameters);
            camera.startPreview();
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void surfaceDestroyed(SurfaceHolder holder)
        {
            // Ignore
        }
    };

    private PreviewCallback previewCallback = new PreviewCallback()
    {
        /**
         * {@inheritDoc}
         */
        @Override
        public void onPreviewFrame(byte[] data, Camera cam)
        {
            if (data == null)
                throw new NullPointerException();
            Camera.Size size = cam.getParameters().getPreviewSize();
            if (size == null)
                throw new NullPointerException();

            if (!processing.compareAndSet(false, true))
                return;

            int width = size.width;
            int height = size.height;

            int imgAvg = ImageProcessing.decodeYUV420SPtoRedAvg(data.clone(),height, width);

            sampleQueue.add((double) imgAvg);
            timeQueue.add(System.currentTimeMillis());

            double[] y = new double[sampleSize];
            double[] x = ArrayUtils.toPrimitive((Double[]) sampleQueue.toArray(new Double[0]));
            long[] time = ArrayUtils.toPrimitive((Long[]) timeQueue.toArray(new Long[0]));

            if (timeQueue.size() < sampleSize)
            {
                processing.set(false);
                return;
            }

            double Fs = ((double) timeQueue.size())/ (double) (time[timeQueue.size() - 1] - time[0]) * 1000;
            fft.fft(x, y);
            int low = Math.round((float) (sampleSize * 40 / 60 / Fs));
            int high = Math.round((float) (sampleSize * 160 / 60 / Fs));
            int bestI = 0;
            double bestV = 0;
            for (int i = low; i < high; i++)
            {
                double value = Math.sqrt(x[i] * x[i] + y[i] * y[i]);
                if (value > bestV)
                {
                    bestV = value;
                    bestI = i;
                }
            }
            bpm = Math.round((float) (bestI * Fs * 60 / sampleSize));
            bpmQueue.add(bpm);
            endTime = System.currentTimeMillis();
            if(endTime-startTime>40000)
            {
                camera.stopPreview();
                camera.release();
                String activityStatus = getIntent().getStringExtra("ActivityStatus");
                String message = "";
                String thresholdMessage = "";
                if(bpm > Integer.parseInt(getString(R.string.threshold))+Integer.parseInt(getString(R.string.thresholdDifference)))
                {
                    thresholdMessage = getString(R.string.aboveThreshold);
                }
                else if(bpm < Integer.parseInt(getString(R.string.threshold))-Integer.parseInt(getString(R.string.thresholdDifference)))
                {
                    thresholdMessage = getString(R.string.belowThreshold);
                }
                else
                {
                    thresholdMessage = getString(R.string.normalThreshold);
                }
                if(activityStatus.equalsIgnoreCase("Start"))
                {
                    message = String.valueOf(bpm)+ "\n" + thresholdMessage + "\n Click Ok to Continue Tracking";
                }
                else if(activityStatus.equalsIgnoreCase("Stop") )
                {
                    message = String.valueOf(bpm)+ "\n" + thresholdMessage + "\n Click Ok to Stop Tracking and Click Stop to see the Activity Data";
                }
                new AlertDialog.Builder(preview.getContext())
                        .setTitle("Your Heart Rate "+activityStatus)
                        .setMessage(message)
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                String activityStatus = getIntent().getStringExtra("ActivityStatus");
                                Intent i = new Intent(HeartRateMonitor.this, MapsActivity.class);
                                if (activityStatus.equalsIgnoreCase("Start")) {
                                    i.putExtra("HeartBeatStart", "" + bpm);
                                } else if (activityStatus.equalsIgnoreCase("Stop")) {
                                    i.putExtra("HeartBeatStop", "" + bpm);
                                }
                                startActivity(i);
                            }
                        }).show();
            }
            text.setText(String.valueOf(bpm));
            counter++;
            exampleSeries.appendData(new GraphView.GraphViewData(counter,imgAvg), true, 1000);
            processing.set(false);
        }
    };

    private Camera.Size getSmallestPreviewSize(int width, int height,Camera.Parameters parameters)
    {
        Camera.Size result = null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes())
        {
            if (size.width <= width && size.height <= height)
            {
                if (result == null)
                {
                    result = size;
                }
                else
                {
                    int resultArea = result.width * result.height;
                    int newArea = size.width * size.height;

                    if (newArea < resultArea)
                        result = size;
                }
            }
        }
        return result;
    }
}
