package net.onest.time;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.lxj.xpopup.XPopup;
import com.mut_jaeryo.circletimer.CircleTimer;


import net.onest.time.api.RandomWordApi;
import net.onest.time.api.TaskApi;
import net.onest.time.api.TomatoClockApi;
import net.onest.time.components.StopClockDialog;
import net.onest.time.navigation.activity.NavigationActivity;
import net.onest.time.utils.DrawableUtil;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class TimerActivity extends AppCompatActivity {
    //倒计时器：
    private CircleTimer circleTimer;
    //正向计时：
    private TextView timeTxt;
    private CountDownTimer mCountDownTimer;
    private long mTimeLeftInMillis = 0; // 初始计时时间为0秒

    private Button interruptBtn,circleBtn,alterBtn,stopBtn;
    private LinearLayout draLin;
    private Intent intent;
    private TextView text,taskName;

    private OkHttpClient httpClient;//可以复用，定义全局
    private Request request;
    private Response response;
    private Call call;
    private Gson gson = new Gson();

    private RelativeLayout timerEntire;

    /** 获取屏幕坐标点 **/
    Point startPoint;// 起始点
    Point endPoint;// 终点
    /** 记录按下的坐标点（起始点）**/
    private float mPosX = 0;
    private float mPosY = 0;
    /** 记录移动后抬起坐标点（终点）**/
    private float mCurPosX = 0;
    private float mCurPosY = 0;



    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_timer);
        findViews();
        draLin.setBackground(DrawableUtil.randomDrawableBack(getApplicationContext()));

        //每日一句:
        text.setText("”"+RandomWordApi.getRandomWord()+"“");
//        btn.setVisibility(View.GONE);
        //调转屏幕
//        alterBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(getRequestedOrientation()==ActivityInfo.SCREEN_ORIENTATION_PORTRAIT){
//                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//                }else{
//                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//                }
//            }
//        });

        //手势滑动:
        timerEntire.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                switch (event.getAction()) {

                    case MotionEvent.ACTION_DOWN:
                        mPosX = event.getX();
                        mPosY = event.getY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        mCurPosX = event.getX();
                        mCurPosY = event.getY();
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mCurPosX - mPosX > 45
                                && (Math.abs(mCurPosX - mPosX) > 45)) {
                            //设置停止弹窗:
                            new StopClockDialog(TimerActivity.this);
                            Toast.makeText(TimerActivity.this, "向右滑动😊", Toast.LENGTH_SHORT).show();
                        }else if (mCurPosX - mPosX < -45
                                && (Math.abs(mCurPosX - mPosX) > 45)) {
                            //设置停止弹窗:
                            new StopClockDialog(TimerActivity.this);
                            Toast.makeText(TimerActivity.this, "向左滑动😊", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                return true;
            }
        });


        intent = getIntent();
        taskName.setText(intent.getStringExtra("name"));
        String timeStr = intent.getStringExtra("time");
        String str = intent.getStringExtra("start");

//设置倒计时:
        if("countDown".equals(intent.getStringExtra("method"))){
            timeTxt.setVisibility(View.GONE);

//        circleTimer.setInitPosition(60);
            int time = Integer.parseInt(timeStr);
            circleTimer.setMaximumTime(time*60+1);
            circleTimer.setInitPosition(time*60+1);

            if("go".equals(str)){
                circleTimer.start();
                // 开始任务:
//                long taskId = intent.getLongExtra("taskId",0L);
//                if(taskId!=0L){
//                    TomatoClockApi.startTomatoClock(taskId);
//                }
            }

            //外部打断
            interruptBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    circleTimer.stop();
                    //设置弹窗
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(TimerActivity.this);
                    LayoutInflater inflater = LayoutInflater.from(TimerActivity.this);
                    View dialogView = inflater.inflate(R.layout.timer_activity_pause_pop, null);
                    final Dialog dialog = builder.create();
                    dialog.show();
                    dialog.getWindow().setContentView(dialogView);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


                    TextView pausedTime = dialogView.findViewById(R.id.pause_time);
                    Button conBtn = dialogView.findViewById(R.id.continue_btn);

                    CountDownTimer countDownTimer = new CountDownTimer(180000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            long day = millisUntilFinished / (1000 * 24 * 60 * 60); //单位天
                            long hour = (millisUntilFinished - day * (1000 * 24 * 60 * 60)) / (1000 * 60 * 60); //单位时
                            long minute = (millisUntilFinished - day * (1000 * 24 * 60 * 60) - hour * (1000 * 60 * 60)) / (1000 * 60); //单位分
                            long second = (millisUntilFinished - day * (1000 * 24 * 60 * 60) - hour * (1000 * 60 * 60) - minute * (1000 * 60)) / 1000;//单位秒

                            NumberFormat f = new DecimalFormat("00");
                            pausedTime.setText(minute + ":" + f.format(second));
                        }

                        public void onFinish() {
//                            pausedTime.setText("done!");
                            dialog.dismiss();
                            circleTimer.start();
                        }
                    };
                    countDownTimer.start();

                    conBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            circleTimer.start();
                            dialog.dismiss();
                            countDownTimer.cancel();
                        }
                    });
                }
            });

            circleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(TimerActivity.this, "现在的时间是"+circleTimer.getValue(), Toast.LENGTH_SHORT).show();
//                    circleTimer.reset();
                }
            });

            circleTimer.setBaseTimerEndedListener(new CircleTimer.baseTimerEndedListener() {
                @Override
                public void OnEnded() {

                }
            });

            //暂停，停止
            stopBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int time = Integer.parseInt(timeStr);
                    if(time*60 - circleTimer.getValue()<5){
                        Toast toast = Toast.makeText(TimerActivity.this, "不记录5秒以下的专注记录!", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP,0,0);
                        toast.show();
                        Intent intent2 = new Intent();
                        intent2.setClass(TimerActivity.this, NavigationActivity.class);
                        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//它可以关掉所要到的界面中间的activity
                        startActivity(intent2);
                        overridePendingTransition(R.anim.slide_left,R.anim.slide_right);
//                        finish();
//                        NavController navController = Navigation.findNavController(TimerActivity.this, R.id.nav_host_fragments);
//                        navController.navigate(R.id.action_todo_fragment_to_list_fragment);
                    }else{
                        new StopClockDialog(TimerActivity.this);

                    }
                }
            });
        }else if("forWard".equals(intent.getStringExtra("method"))){
            //正向计时：
            circleTimer.setVisibility(View.GONE);
            timeTxt.setText("开始");

            startTimer();
            //开始任务
//            long taskId = intent.getLongExtra("taskId",0L);
//            if(taskId!=0L){
//                TomatoClockApi.startTomatoClock(taskId);
//            }

            //打断:
            interruptBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(TimerActivity.this, "外部打断", Toast.LENGTH_SHORT).show();
                    stopTimer();
                    //设置弹窗
                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(TimerActivity.this);
                    LayoutInflater inflater = LayoutInflater.from(TimerActivity.this);
                    View dialogView = inflater.inflate(R.layout.timer_activity_pause_pop, null);
                    final Dialog dialog = builder.create();
                    dialog.show();
                    dialog.getWindow().setContentView(dialogView);
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));


                    TextView pausedTime = dialogView.findViewById(R.id.pause_time);
                    Button conBtn = dialogView.findViewById(R.id.continue_btn);

                    CountDownTimer countDownTimer = new CountDownTimer(180000, 1000) {
                        public void onTick(long millisUntilFinished) {
                            long day = millisUntilFinished / (1000 * 24 * 60 * 60); //单位天
                            long hour = (millisUntilFinished - day * (1000 * 24 * 60 * 60)) / (1000 * 60 * 60); //单位时
                            long minute = (millisUntilFinished - day * (1000 * 24 * 60 * 60) - hour * (1000 * 60 * 60)) / (1000 * 60); //单位分
                            long second = (millisUntilFinished - day * (1000 * 24 * 60 * 60) - hour * (1000 * 60 * 60) - minute * (1000 * 60)) / 1000;//单位秒

                            NumberFormat f = new DecimalFormat("00");
                            pausedTime.setText(minute + ":" + f.format(second));
                        }

                        public void onFinish() {
//                            pausedTime.setText("done!");
                            dialog.dismiss();
                            circleTimer.start();
                        }
                    };
                    countDownTimer.start();

                    conBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            circleTimer.start();
                            dialog.dismiss();
                            countDownTimer.cancel();
                        }
                    });
                }
            });

            //停止计时/放弃原因:
            stopBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mTimeLeftInMillis/1000<5){
                        Toast toast = Toast.makeText(TimerActivity.this, "不记录5秒以下的专注记录!", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP,0,0);
                        toast.show();
                        Intent intent2 = new Intent();
                        intent2.setClass(TimerActivity.this, NavigationActivity.class);
                        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//它可以关掉所要到的界面中间的activity
                        startActivity(intent2);
                        overridePendingTransition(R.anim.slide_left,R.anim.slide_right);
                        mCountDownTimer.cancel();
                    }else{
                        new StopClockDialog(TimerActivity.this);
                    }
                }
            });

            circleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast toast = Toast.makeText(TimerActivity.this, "正向计时不允许中途开启循环!", Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.TOP, 0, 0);
                    toast.show();
                }
            });
        }

    }

    private void setPieChartData(PieChart abandonReasonChart, List<PieEntry> yVals, List<Integer> colors) {
        PieDataSet pieDataSet = new PieDataSet(yVals, "");
        pieDataSet.setColors(colors);
        PieData pieData = new PieData(pieDataSet);
        abandonReasonChart.setEntryLabelColor(Color.RED);//描述文字的颜色
        pieDataSet.setValueTextSize(15);//数字大小
        pieDataSet.setValueTextColor(Color.BLACK);//数字颜色

        //设置描述的位置
        pieDataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        pieDataSet.setValueLinePart1Length(0.6f);//设置描述连接线长度
        //设置数据的位置
        pieDataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        pieDataSet.setValueLinePart2Length(0.6f);//设置数据连接线长度
        //设置两根连接线的颜色
        pieDataSet.setValueLineColor(Color.BLUE);

        abandonReasonChart.setData(pieData);
        abandonReasonChart.setExtraOffsets(0f,32f,0f,32f);
        //动画（如果使用了动画可以则省去更新数据的那一步）
//        pieChart.animateY(1000); //在Y轴的动画  参数是动画执行时间 毫秒为单位
//        pieChart.animateX(1000); //X轴动画
        abandonReasonChart.animateXY(1000,1000);//XY两轴混合动画
    }


    private void stopTimer() {
        mCountDownTimer.cancel();
    }

    private void startTimer() {
        mCountDownTimer = new CountDownTimer(Long.MAX_VALUE, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis +=1000;
                updateCountdownText();
            }
            @Override
            public void onFinish() {
                // 不需要处理倒计时结束的事件
                mCountDownTimer.cancel();
            }
        }.start();
    }



    private void updateCountdownText() {
        int minutes = (int) (mTimeLeftInMillis / 1000) / 60;
        int seconds = (int) (mTimeLeftInMillis / 1000) % 60;

        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        timeTxt.setText(timeLeftFormatted);
    }

    private void findViews() {
        //计时器：
        circleTimer = findViewById(R.id.circle_timer);
//        startBtn = findViewById(R.id.timer_start);
        interruptBtn = findViewById(R.id.timer_interrupt);
        stopBtn = findViewById(R.id.timer_stop_btn);
        circleBtn = findViewById(R.id.timer_set_circle);
        draLin = findViewById(R.id.timer_background_lin);
        timeTxt = findViewById(R.id.timer_forward);
//        alterBtn = findViewById(R.id.alter_btn);
        text = findViewById(R.id.timer_text);
        taskName = findViewById(R.id.timer_name);
        timerEntire = findViewById(R.id.timer_entirely);
    }
}


//一言——快一点：
//    private void getOneWordTwo() {
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                httpClient = new OkHttpClient();
//                request = new Request.Builder().url("https://uapis.cn/api/say").build();
//                call = httpClient.newCall(request);
//
//                try {
//                    response = call.execute();
//                    String result = response.body().string();
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            text.setText("“ "+result+" ”");
//                        }
//                    });
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            }
//        }).start();
//    }