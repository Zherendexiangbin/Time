package net.onest.time.navigation.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import net.onest.time.R;
import net.onest.time.api.UserApi;
import net.onest.time.api.dto.UserDto;

import java.util.concurrent.TimeUnit;

public class ResetPasswordActivity extends AppCompatActivity {
    private TextInputEditText resetUser, resetCode, resetPassword, passwordConfirm;
    private Button getCode, btnReset;
    private String resetPasswordKey;

    @SuppressLint("MissingInflateId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        setContentView(R.layout.activity_resetpassword_page);

        findViewById();
        setListeners();
    }

    private void setListeners() {
        //注册
        btnReset.setOnClickListener(view -> {
            String user = resetUser.getText().toString().trim();
            String code = resetCode.getText().toString().trim();
            String password = resetPassword.getText().toString().trim();
            String confirm = passwordConfirm.getText().toString().trim();

            if(user.length()==0 || code.length()==0 || password.length()==0 || confirm.length()==0){
                Toast.makeText(this, "不可为空，请重新输入！", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirm)) {
                Toast.makeText(this, "两次密码输入不同，请重新输入！", Toast.LENGTH_SHORT).show();
            }else {
                UserDto userDto = new UserDto();
                userDto.setEmail(user);
                userDto.setPassword(password);
                userDto.setConfirmPassword(confirm);
                userDto.setEmailCodeKey(resetPasswordKey);
                userDto.setEmailCode(code);
                UserApi.modifyPassword(userDto);
                Toast.makeText(this, "密码修改成功，请登录", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        //获取验证码
        getCode.setOnClickListener(view -> {
            if (resetUser.getText().toString().isEmpty()){
                Toast.makeText(this, "请输入手机号或邮箱！", Toast.LENGTH_SHORT).show();
            }else {
                resetPasswordKey = UserApi.getEmailCodeKey(resetUser.getText().toString().trim());
                startCountdown();
            }
        });
    }

    private void findViewById() {
        resetUser = findViewById(R.id.edt_reset_user);
        resetCode = findViewById(R.id.edt_reset_code);
        resetPassword = findViewById(R.id.edt_reset_password);
        passwordConfirm = findViewById(R.id.edt_reset_password_confirm);
        getCode = findViewById(R.id.btn_getCode);
        btnReset = findViewById(R.id.btn_reset);
    }

    //获取验证码倒计时
    private void startCountdown () {
        new CountDownTimer(60 * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished);
                getCode.setText("已发送(" + seconds + "s)");
                //getCode.setTextColor(Color.BLACK);
                getCode.setEnabled(false);
            }

            @Override
            public void onFinish() {
                getCode.setText("获取验证码");
                getCode.setEnabled(true);
            }
        }.start();
    }
}
