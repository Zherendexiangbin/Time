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
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Vibrator;
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
import android.widget.EditText;
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
import net.onest.time.api.dto.TaskDto;
import net.onest.time.api.vo.TaskVo;
import net.onest.time.api.vo.TomatoClockVo;
import net.onest.time.components.StopClockDialog;
import net.onest.time.navigation.activity.NavigationActivity;
import net.onest.time.utils.DrawableUtil;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.format.TextStyle;
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
    private long mTimeLeftInMillis = 0; // 记录正向计时的时间【初始0秒】
    private long pauseHave; // 记录暂停的时间
    private long pauseTime;//设置暂停初始时间3分钟

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

    private StopClockDialog stopClockDialog;

    private long taskId;
    private String timeStr;//倒计时时间
    private String str ;//是否开始
    private String name;//任务名

    //震动提醒
    private Vibrator mVibrator;
    //记录时钟数:
    private int num=0;//点击开始，即占用一个番茄钟
    //要循环的番茄钟数:
    private List<TomatoClockVo> tomatoClockVos = new ArrayList<>();
    private int rest;//休息时间
    private int loopTimes;//循环次数
    
    private TaskVo taskVo;


    /** 获取屏幕坐标点 **/
    Point startPoint;// 起始点
    Point endPoint;// 终点
    /** 记录按下的坐标点（起始点）**/
    private float mPosX = 0;
    private float mPosY = 0;
    /** 记录移动后抬起坐标点（终点）**/
    private float mCurPosX = 0;
    private float mCurPosY = 0;

    @Override
    public void onBackPressed() {

        if("forWard".equals(intent.getStringExtra("method"))){

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
                stopClockDialog = new StopClockDialog(TimerActivity.this,taskVo);
            }
        }else{
            int time = Integer.parseInt(intent.getStringExtra("time"));

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
                stopClockDialog = new StopClockDialog(TimerActivity.this,taskVo);

            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.slide_left,R.anim.slide_right);
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_timer);
        findViews();
        draLin.setBackground(DrawableUtil.randomDrawableBack(getApplicationContext()));

        //每日一句:
        text.setText("”"+RandomWordApi.getRandomWord()+"“");


        pauseTime = 180000;
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
                        if (mCurPosX - mPosX > 70
                                && (Math.abs(mCurPosX - mPosX) > 70)) {
                            //设置停止弹窗:
                            new StopClockDialog(TimerActivity.this,taskVo);
                            Toast.makeText(TimerActivity.this, "向右滑动😊", Toast.LENGTH_SHORT).show();
                        }else if (mCurPosX - mPosX < -70
                                && (Math.abs(mCurPosX - mPosX) > 70)) {
                            //设置停止弹窗:
                            new StopClockDialog(TimerActivity.this,taskVo);
                            Toast.makeText(TimerActivity.this, "向左滑动😊", Toast.LENGTH_SHORT).show();
                        }
                        break;
                }
                return true;
            }
        });


        intent = getIntent();
        name = intent.getStringExtra("name");
        timeStr = intent.getStringExtra("time");
        str = intent.getStringExtra("start");
        taskId = intent.getLongExtra("taskId",0);
        taskVo = (TaskVo) intent.getSerializableExtra("task");

        rest = taskVo.getRestTime();//休息时间  min
        loopTimes = taskVo.getEstimate().get(0);//循环次数

        taskName.setText(name);

//设置倒计时:
        if("countDown".equals(intent.getStringExtra("method"))){
            timeTxt.setVisibility(View.GONE);
//        circleTimer.setInitPosition(60);
            int time = Integer.parseInt(timeStr);
            circleTimer.setMaximumTime(time*60);
            circleTimer.setInitPosition(time*60+1);

            if("go".equals(str)){
                circleTimer.start();
            }

            //对于倒计时:若是超过5秒，添加倒计时的番茄钟
            long taskId = taskVo.getTaskId();
            if(taskId!=0L){
                tomatoClockVos = TomatoClockApi.addTomatoClock(taskId);
                Toast.makeText(this, "开始添加番茄钟", Toast.LENGTH_SHORT).show();
            }
//            if(time*60-circleTimer.getValue()>5){
//
//            }

            if(pauseTime==0){
                Toast toast = Toast.makeText(TimerActivity.this, "本次任务的暂停限制时间已用完!", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP,0,0);
                toast.show();
            }else{
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

                        CountDownTimer countDownTimer = new CountDownTimer(pauseTime, 1000) {
                            public void onTick(long millisUntilFinished) {
                                long day = millisUntilFinished / (1000 * 24 * 60 * 60); //单位天
                                long hour = (millisUntilFinished - day * (1000 * 24 * 60 * 60)) / (1000 * 60 * 60); //单位时
                                long minute = (millisUntilFinished - day * (1000 * 24 * 60 * 60) - hour * (1000 * 60 * 60)) / (1000 * 60); //单位分
                                long second = (millisUntilFinished - day * (1000 * 24 * 60 * 60) - hour * (1000 * 60 * 60) - minute * (1000 * 60)) / 1000;//单位秒
                                pauseHave=millisUntilFinished;
                                NumberFormat f = new DecimalFormat("00");
                                pausedTime.setText(minute + ":" + f.format(second));
                            }

                            public void onFinish() {
//                            pausedTime.setText("done!");
                                dialog.dismiss();
                                circleTimer.start();
                                pauseTime=0;
                            }
                        };
                        countDownTimer.start();

                        conBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                circleTimer.start();
                                dialog.dismiss();
                                countDownTimer.cancel();
                                pauseTime = pauseHave;
                            }
                        });
                    }
                });
            }

            //循环次数:
            circleBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(taskName.getText().toString().contains("无限循环中")){
                        Toast toast = Toast.makeText(TimerActivity.this, "关闭无限循环模式!", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.TOP,0,0);
                        toast.show();
                        taskName.setText(name);
                    }else{
//                        Toast.makeText(TimerActivity.this, "现在的时间是"+circleTimer.getValue(), Toast.LENGTH_SHORT).show();
//                        circleTimer.reset();
                        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(TimerActivity.this);
                        LayoutInflater inflater = LayoutInflater.from(TimerActivity.this);
                        View dialogView = inflater.inflate(R.layout.timer_activity_circle_times, null);
                        final Dialog dialog = builder.create();
                        dialog.show();
                        dialog.getWindow().setContentView(dialogView);
                        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

                        EditText timersEdit = dialogView.findViewById(R.id.circle_timers);
                        EditText restEdit = dialogView.findViewById(R.id.per_circle_rest);
                        Button forever = dialogView.findViewById(R.id.circle_forever);
                        Button circleYes = dialogView.findViewById(R.id.circle_yes);
                        Button circleNo = dialogView.findViewById(R.id.circle_no);

                        forever.setOnClickListener(view->{
                            Toast.makeText(TimerActivity.this, "你选择了无限循环模式!", Toast.LENGTH_SHORT).show();
                            taskName.setText(name+ "    无限循环中");
                            dialog.dismiss();
                        });

                        circleYes.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //获取文本框的值
                                rest = Integer.valueOf(restEdit.getText().toString().trim());
                                loopTimes = Integer.valueOf(timersEdit.getText().toString().trim());

                                taskVo.setRestTime(rest);
                                taskVo.getEstimate().add(loopTimes);

                                TaskDto taskDto = new TaskDto().withTaskVo(taskVo);
                                TaskApi.updateTask(taskDto);

                                dialog.dismiss();
                            }
                        });

                        circleNo.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.dismiss();
                            }
                        });
                    }
                }
            });

            //时钟结束时调用:
            circleTimer.setBaseTimerEndedListener(new CircleTimer.baseTimerEndedListener() {
                @Override
                public void OnEnded() {
                    ++num;
                    Log.e("目前num:",num +"");
                    Log.e("目前任务名:",name +"实际上:" + taskName.getText().toString());

                    // 震动效果的系统服务
                    mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
                    long[] pattern = {200, 200 };
                    mVibrator.vibrate(pattern, -1);

                    //执行完休息时间，即可退出！！！
                    if(num == loopTimes * 2 + 2){
                        circleTimer.setInitPosition(0);
                        circleTimer.setMaximumTime(0);

                        TaskApi.complete(taskVo.getTaskId());

//                            circleTimer.setValue(10);//设置时钟的值
                        Toast.makeText(TimerActivity.this, "任务完成☺", Toast.LENGTH_SHORT).show();
                        Intent intent2 = new Intent();
                        intent2.setClass(TimerActivity.this, NavigationActivity.class);
                        intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//它可以关掉所要到的界面中间的activity
                        startActivity(intent2);
                        overridePendingTransition(R.anim.slide_left,R.anim.slide_right);
                    }

                    //休息时间:
//                    int rest = 5*60;//五分钟
                    //番茄钟数:
                    int clock = tomatoClockVos.get(0).getClockDuration()*60;

                    //无限循环状态:
                    if(taskName.getText().toString().contains("无限循环中")){
                        if(num % 2==0 || num == loopTimes){
                            taskName.setText(taskVo.getTaskName());
                            circleTimer.setMaximumTime(clock);
                            circleTimer.setInitPosition(clock + 1);
                            circleTimer.setTextFont(Typeface.SERIF);
                            circleTimer.start();

                        }else {
                            taskName.setText("休息中~");
                            circleTimer.setMaximumTime(rest * 60);
                            circleTimer.setInitPosition(rest * 60 + 1);
                            circleTimer.start();
                            circleTimer.setTextFont(Typeface.SERIF);
                        }
                    }

                    //普通状态:
                    if(num % 2 == 0 || num == loopTimes*2){
                        taskName.setText(name);
                        circleTimer.setMaximumTime(clock);
                        circleTimer.setInitPosition(clock + 1);
                        circleTimer.start();
                        circleTimer.setTextFont(Typeface.SERIF);
                    }else {
                        taskName.setText("休息中~");
                        circleTimer.setMaximumTime(rest * 60);
                        circleTimer.setInitPosition(rest * 60 + 1);
                        circleTimer.start();

                        circleTimer.setTextFont(Typeface.SERIF);
                    }
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
                        stopClockDialog = new StopClockDialog(TimerActivity.this,taskVo,circleTimer);
                    }
                }
            });
        }else if("forWard".equals(intent.getStringExtra("method"))){
            //正向计时：
            circleTimer.setVisibility(View.GONE);
            timeTxt.setText("开始");

            startTimer();//开始计时

            //打断:
            if(pauseTime==0){
                Toast toast = Toast.makeText(TimerActivity.this, "本次任务的暂停限制时间已用完!", Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.TOP,0,0);
                toast.show();
            }else{
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

                        CountDownTimer countDownTimer = new CountDownTimer(pauseTime, 1000) {
                            public void onTick(long millisUntilFinished) {
                                long day = millisUntilFinished / (1000 * 24 * 60 * 60); //单位天
                                long hour = (millisUntilFinished - day * (1000 * 24 * 60 * 60)) / (1000 * 60 * 60); //单位时
                                long minute = (millisUntilFinished - day * (1000 * 24 * 60 * 60) - hour * (1000 * 60 * 60)) / (1000 * 60); //单位分
                                long second = (millisUntilFinished - day * (1000 * 24 * 60 * 60) - hour * (1000 * 60 * 60) - minute * (1000 * 60)) / 1000;//单位秒

                                pauseHave = millisUntilFinished;
                                NumberFormat f = new DecimalFormat("00");
                                pausedTime.setText(minute + ":" + f.format(second));
                            }

                            public void onFinish() {
//                            pausedTime.setText("done!");
                                dialog.dismiss();
                                startTimer();
                                pauseTime=0;
                            }
                        };
                        countDownTimer.start();

                        conBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                startTimer();
                                dialog.dismiss();
                                countDownTimer.cancel();
                                pauseTime = pauseHave;
                            }
                        });
                    }
                });
            }

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
                        stopClockDialog = new StopClockDialog(TimerActivity.this,taskVo);
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
                //对于正向计时:若是超过5秒，添加正向计时的番茄钟
                if(mTimeLeftInMillis/1000 == 5){
                    if(taskId != 0){
                        TomatoClockApi.addTomatoClock(taskId);
                        Log.e("番茄钟","添加");
                        Toast.makeText(TimerActivity.this, "开始添加番茄钟"+mTimeLeftInMillis, Toast.LENGTH_SHORT).show();
                    }
                }
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

    @Override
    protected void onDestroy() {
        if(stopClockDialog != null) {
            stopClockDialog.dismiss();
        }
        super.onDestroy();
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