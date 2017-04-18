package com.tencent.newilivedemo;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ConfigurationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.tencent.TIMMessage;
import com.tencent.av.sdk.AVRoomMulti;
import com.tencent.av.sdk.AVVideoCtrl;
import com.tencent.av.sdk.AVView;
import com.tencent.ilivesdk.ILiveCallBack;
import com.tencent.ilivesdk.ILiveConstants;
import com.tencent.ilivesdk.ILiveSDK;
import com.tencent.ilivesdk.core.ILiveLog;
import com.tencent.ilivesdk.core.ILiveLoginManager;
import com.tencent.ilivesdk.core.ILiveRoomManager;
import com.tencent.ilivesdk.core.ILiveRoomOption;
import com.tencent.ilivesdk.view.AVRootView;
import com.tencent.livesdk.ILVCustomCmd;
import com.tencent.livesdk.ILVLiveConfig;
import com.tencent.livesdk.ILVLiveConstants;
import com.tencent.livesdk.ILVLiveManager;
import com.tencent.livesdk.ILVText;
import com.tencent.ilivefilter.TILFilter;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.io.FileNotFoundException;

public class LiveActivity extends Activity implements View.OnClickListener , SeekBar.OnSeekBarChangeListener{
    Button createBtn, joinbtn, switchBtn,backBtn, sendBtn, inviteBtn, closeMemBtn;
    AVRootView mAvRootView;          // 视频播放画面
    Button logoutBtn,loginLive,registLive;

    Button mBtSwitchFilter = null;
    Button mBtCancelFilter = null;

    // 日志抓取
    private Button mBtLogGrab = null;

    EditText roomNum, roomNumJoin, textInput, memId, hostIdInput,myId,myPwd;
    TextView myLoginId;
    FrameLayout loginView;
    private static final String TAG = LiveActivity.class.getSimpleName();
    private final int REQUEST_PHONE_PERMISSIONS = 0;
    private int mCurCameraId;

    private boolean bLogin = false, bEnterRoom = false;
    // GPUImage 打开配置
    // 控制参数
    private boolean mbShowRoomParam = true;
    private boolean mbSetPrCallback = false;
    private int mFilterNumber = 1;
    private int mPreFilterNumber = 1;

    Button mBtShowRoomInfo = null;
    // 美颜参数调节
    private LinearLayout mLinearBeauty = null;
    private LinearLayout mLinearRoomInfo = null;
    private SeekBar mSeekBlurLevel = null;
    private SeekBar mSeekWhiteLevel = null;
    private TextView mTextBlurLevel = null;
    private TextView mTextWhiteLevel = null;
    int m_iBlurLevel = 6;
    int m_iWhiteLevel = 6;

    private final String FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();

    // ptu
    private Dialog mEffectFilterSettingDialog = null;
    private Dialog mEffectPendantSettingDialog = null;

    TILFilter mUDFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.live_layout);

        createBtn = (Button) findViewById(R.id.create);
        joinbtn = (Button) findViewById(R.id.join);
        backBtn = (Button) findViewById(R.id.back);
        switchBtn =  (Button) findViewById(R.id.switchRoom);
        sendBtn = (Button) findViewById(R.id.text_send);
        inviteBtn = (Button) findViewById(R.id.invite);
        closeMemBtn = (Button) findViewById(R.id.close_mem);

        loginLive = (Button) findViewById(R.id.login_live);
        registLive = (Button) findViewById(R.id.register_live);
        loginView =  (FrameLayout)findViewById(R.id.login_fragment);

        mAvRootView = (AVRootView) findViewById(R.id.avRootView);

        logoutBtn = (Button) findViewById(R.id.btn_logout);
        roomNum = (EditText) findViewById(R.id.room_num);
        roomNumJoin = (EditText) findViewById(R.id.room_num_join);
        textInput = (EditText) findViewById(R.id.text_input);
        hostIdInput = (EditText) findViewById(R.id.host_id);
        memId = (EditText) findViewById(R.id.mem_id);
        myId= (EditText) findViewById(R.id.my_id);
        myPwd =(EditText) findViewById(R.id.my_pwd);
        myLoginId = (TextView) findViewById(R.id.my_login_id);
        // 打开摄像头
        mBtSwitchFilter = (Button)findViewById(R.id.btSwitchFIlter);
        mBtCancelFilter = (Button)findViewById(R.id.btCancelFIlter);
        mBtShowRoomInfo = (Button)findViewById(R.id.btShowRoomParam);

        // 日志抓取
        mBtLogGrab = (Button) findViewById(R.id.btLogCapture);

        // 美颜参数调节
        mLinearBeauty = (LinearLayout) findViewById(R.id.LinearBeautyParam);
        mLinearRoomInfo = (LinearLayout) findViewById(R.id.LinearRoomParam);
        mSeekBlurLevel = (SeekBar) findViewById(R.id.seekBarBlur);
        mSeekWhiteLevel = (SeekBar) findViewById(R.id.seekBarWhite);
        mTextBlurLevel = (TextView) findViewById(R.id.textBlurLevel);
        mTextWhiteLevel = (TextView) findViewById(R.id.textWhiteLevel);

        mTextBlurLevel.setText(String.valueOf(m_iBlurLevel));
        mTextWhiteLevel.setText(String.valueOf(m_iWhiteLevel));

        mSeekBlurLevel.setProgress(m_iBlurLevel);
        mSeekBlurLevel.setOnSeekBarChangeListener(this);

        mSeekWhiteLevel.setProgress(m_iWhiteLevel);
        mSeekWhiteLevel.setOnSeekBarChangeListener(this);

        mBtShowRoomInfo.setOnClickListener(this);
        mBtLogGrab.setOnClickListener(this);
        mBtSwitchFilter.setOnClickListener(this);
        mBtCancelFilter.setOnClickListener(this);
        createBtn.setOnClickListener(this);
        joinbtn.setOnClickListener(this);
        backBtn.setOnClickListener(this);
        logoutBtn.setOnClickListener(this);
        sendBtn.setOnClickListener(this);
        inviteBtn.setOnClickListener(this);
        closeMemBtn.setOnClickListener(this);
        loginLive.setOnClickListener(this);
        registLive.setOnClickListener(this);
        switchBtn.setOnClickListener(this);
        checkPermission();


        Log.i(TAG, "onCreate: initSdk ");
        //初始化SDK
        ILiveSDK.getInstance().initSdk(getApplicationContext(), 1400013700, 7285);

        // 不设置 AVSDK view
        ILVLiveManager.getInstance().setAvVideoView(mAvRootView);

        // 关闭IM群组
        ILVLiveConfig liveConfig = new ILVLiveConfig();

        liveConfig.setLiveMsgListener(new ILVLiveConfig.ILVLiveMsgListener() {
            @Override
            public void onNewTextMsg(ILVText text, String SenderId) {
                Toast.makeText(LiveActivity.this, "onNewTextMsg : " + text, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNewCmdMsg(int cmd, String param, String id) {
                switch (cmd) {
                    case ILVLiveConstants.ILVLIVE_CMD_INVITE:
                        Toast.makeText(LiveActivity.this, "onNewCmdMsg : received a invitation! ", Toast.LENGTH_SHORT).show();
                        ILiveLog.d(TAG, "ILVB-LiveApp|received ");
                        ILVLiveManager.getInstance().upToVideoMember( "LiveGuest", new ILiveCallBack() {
                            @Override
                            public void onSuccess(Object data) {

                            }

                            @Override
                            public void onError(String module, int errCode, String errMsg) {

                            }
                        });
                        break;
                    case  ILVLiveConstants.ILVLIVE_CMD_INVITE_CANCEL:

                        break;
                    case ILVLiveConstants.ILVLIVE_CMD_INVITE_CLOSE:
                        ILVLiveManager.getInstance().downToNorMember("Guest", new ILiveCallBack() {
                            @Override
                            public void onSuccess(Object data) {

                            }

                            @Override
                            public void onError(String module, int errCode, String errMsg) {

                            }
                        });
                        break;
                    case ILVLiveConstants.ILVLIVE_CMD_INTERACT_AGREE:
                        break;
                    case  ILVLiveConstants.ILVLIVE_CMD_INTERACT_REJECT:
                        break;
                }

            }

            @Override
            public void onNewCustomMsg(int cmd, String param, String id) {
                Toast.makeText(LiveActivity.this, "cmd "+ cmd, Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onNewOtherMsg(TIMMessage message) {

            }

        });
        //初始化直播场景
        ILVLiveManager.getInstance().init(liveConfig);
        //设置渲染界面

        //设置小窗口初始位置
        mAvRootView.setGravity(mAvRootView.LAYOUT_GRAVITY_RIGHT);
        mAvRootView.setSubMarginX(12);
        mAvRootView.setSubMarginY(100);
        //配置拖拽
        mAvRootView.setSubCreatedListener(new AVRootView.onSubViewCreatedListener() {
            @Override
            public void onSubViewCreated() {
                for (int i = 1; i < 3; i++) {
                    mAvRootView.getViewByIndex(i).setDragable(true);
                }
            }
        });

        // 判断Android 系统版本号
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            mUDFilter = new TILFilter(LiveActivity.this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ILVLiveManager.getInstance().onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ILVLiveManager.getInstance().onResume();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void logout(boolean quit) {
        if (bLogin) {
            ILiveLoginManager.getInstance().iLiveLogout(null);
        }
        finish();
    }

    @Override
    public void onBackPressed() {
        if (bEnterRoom) {
            ILiveRoomManager.getInstance().quitRoom(new ILiveCallBack() {
                @Override
                public void onSuccess(Object data) {
                    logout(true);
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {
                    logout(true);
                }
            });
        } else {
            logout(true);
        }
    }

    public int setExternalBeautyFilter(int filterID){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1){
            Toast.makeText(LiveActivity.this, "error!! Now Android API is " + Build.VERSION.SDK_INT + " minSupport is " + Build.VERSION_CODES.JELLY_BEAN_MR1, Toast.LENGTH_SHORT).show();
            return -1;
        }
        if (filterID < 0){
            // 取消相机数据前处理回调接口
            mbSetPrCallback = false;
            // 取消滤镜
            mUDFilter.setFilter(-1);
            // 设置回调为空
            boolean bRet = ILiveSDK.getInstance().getAvVideoCtrl().setLocalVideoPreProcessCallback(null);
            Log.i(TAG, "cancel LocalVideoPreProcessCallback " + bRet);
            return 0;
        }else{
            // 添加相机数据前处理回调接口
            if (false == mbSetPrCallback){
                boolean bRet = ILiveSDK.getInstance().getAvVideoCtrl().setLocalVideoPreProcessCallback(new AVVideoCtrl.LocalVideoPreProcessCallback(){
                    @Override
                    public void onFrameReceive(AVVideoCtrl.VideoFrame var1) {

                        mUDFilter.processData(var1.data, var1.dataLen, var1.width, var1.height, var1.srcType);
                    }
                });
                Log.i(TAG, "1 setLocalVideoPreProcessCallback " + bRet);
            }
            mbSetPrCallback = true;
            // 设置滤镜
            return mUDFilter.setFilter(filterID);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_logout) { //登陆房间
            ILiveLoginManager.getInstance().iLiveLogout( new ILiveCallBack() {
                @Override
                public void onSuccess(Object data) {
                    bLogin = false;
                    loginView.setVisibility(View.VISIBLE);
                    Toast.makeText(LiveActivity.this, "logout success !", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {

                    Toast.makeText(LiveActivity.this, module + "|logout fail " + errCode + " " + errMsg, Toast.LENGTH_SHORT).show();
                }
            });
        } else if (view.getId() == R.id.create) { //创建房间
            createRoom();
        } else if (view.getId() == R.id.join) {//加入房间
            int room = Integer.parseInt("" + roomNumJoin.getText());
            String hostId = "" + hostIdInput.getText();
            //加入房间配置项
            ILiveRoomOption memberOption = new ILiveRoomOption(hostId)
                    .autoCamera(false) //是否自动打开摄像头
                    .controlRole("Guest") //角色设置
                    .authBits(AVRoomMulti.AUTH_BITS_JOIN_ROOM | AVRoomMulti.AUTH_BITS_RECV_AUDIO | AVRoomMulti.AUTH_BITS_RECV_CAMERA_VIDEO | AVRoomMulti.AUTH_BITS_RECV_SCREEN_VIDEO) //权限设置
                    .videoRecvMode(AVRoomMulti.VIDEO_RECV_MODE_SEMI_AUTO_RECV_CAMERA_VIDEO) //是否开始半自动接收
                    .autoMic(false);//是否自动打开mic
            //加入房间
            ILVLiveManager.getInstance().joinRoom(room, memberOption, new ILiveCallBack() {
                @Override
                public void onSuccess(Object data) {
                    bEnterRoom = true;
                    Toast.makeText(LiveActivity.this, "join room  ok ", Toast.LENGTH_SHORT).show();
                    logoutBtn.setVisibility(View.INVISIBLE);
                    backBtn.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {
                    Toast.makeText(LiveActivity.this, module + "|join fail " + errMsg + " " + errMsg, Toast.LENGTH_SHORT).show();
                }
            });
        } else if (view.getId() == R.id.back) {
            if (mAvRootView != null)
                ;
                //avRootView.clearUserView();
            //退出房间
            ILVLiveManager.getInstance().quitRoom(new ILiveCallBack() {
                @Override
                public void onSuccess(Object data) {
                    bEnterRoom = false;
                    Toast.makeText(LiveActivity.this, "quit room  ok ", Toast.LENGTH_SHORT).show();
                    logoutBtn.setVisibility(View.VISIBLE);
                    backBtn.setVisibility(View.INVISIBLE);

                    mUDFilter.destroyFilter();
                    mbSetPrCallback = false;       // 退出房间后，回调函数会自动失效
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {
                    Toast.makeText(LiveActivity.this, module + "|join fail " + errCode + " " + errMsg, Toast.LENGTH_SHORT).show();
                }
            });
        } else if (view.getId() == R.id.text_send) {
            //发送消息
            ILVText iliveText = new ILVText(ILVText.ILVTextType.eGroupMsg,ILiveRoomManager.getInstance().getIMGroupId(), "");
            iliveText.setText("" + textInput.getText());
            //发送消息
            ILVLiveManager.getInstance().sendText(iliveText, new ILiveCallBack() {
                @Override
                public void onSuccess(Object data) {
                    Toast.makeText(LiveActivity.this, "send succ!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {

                }

            });
        } else if (view.getId() == R.id.invite) {
            //邀请上麦
            ILVCustomCmd cmd = new ILVCustomCmd();
            cmd.setCmd(ILVLiveConstants.ILVLIVE_CMD_INVITE);
            cmd.setType(ILVText.ILVTextType.eC2CMsg);
            cmd.setDestId("" + memId.getText());
            cmd.setParam("");
            ILVLiveManager.getInstance().sendCustomCmd(cmd, new ILiveCallBack<TIMMessage>() {
                @Override
                public void onSuccess(TIMMessage data) {
                    Toast.makeText(LiveActivity.this, "invite send succ!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {
                    Log.i(TAG, "onError: "+errMsg);
                }

            });
//            mCurCameraId = (ILiveConstants.FRONT_CAMERA==mCurCameraId) ? ILiveConstants.BACK_CAMERA : ILiveConstants.FRONT_CAMERA;
//            ILiveRoomManager.getInstance().switchCamera(mCurCameraId);
        } else if (view.getId() == R.id.close_mem) {
            //关闭上麦
            ILVCustomCmd cmd = new ILVCustomCmd();
            cmd.setCmd(ILVLiveConstants.ILVLIVE_CMD_INVITE_CLOSE);
            cmd.setType(ILVText.ILVTextType.eC2CMsg);
            cmd.setDestId("" + memId.getText());
            cmd.setParam("");
            ILVLiveManager.getInstance().sendCustomCmd(cmd, new ILiveCallBack<TIMMessage>() {
                @Override
                public void onSuccess(TIMMessage data) {
                    Toast.makeText(LiveActivity.this, "invite send succ!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {

                }

            });

        } else if(view.getId()==R.id.register_live){
            Log.i(TAG, "onClick: register ");
            ILiveLoginManager.getInstance().tlsRegister(""+myId.getText(), ""+myPwd.getText(), new ILiveCallBack() {
                @Override
                public void onSuccess(Object data) {
                    Toast.makeText(LiveActivity.this, "register suc !!!", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {
                    Toast.makeText(LiveActivity.this, "register failed : "+errMsg, Toast.LENGTH_SHORT).show();
                }
            });


        } else if(view.getId()==R.id.login_live){
            loginSystem();
        } else if(view.getId()==R.id.switchRoom){
            Log.i(TAG, "onClick: switchRoom ");
            int room = Integer.parseInt("" + roomNumJoin.getText());
            String hostId = "" + hostIdInput.getText();
            //加入房间配置项
            ILiveRoomOption memberOption = new ILiveRoomOption(hostId)
                    .autoCamera(false) //是否自动打开摄像头
                    .controlRole("Guest") //角色设置
                    .authBits(AVRoomMulti.AUTH_BITS_JOIN_ROOM | AVRoomMulti.AUTH_BITS_RECV_AUDIO | AVRoomMulti.AUTH_BITS_RECV_CAMERA_VIDEO | AVRoomMulti.AUTH_BITS_RECV_SCREEN_VIDEO) //权限设置
                    .videoRecvMode(AVRoomMulti.VIDEO_RECV_MODE_SEMI_AUTO_RECV_CAMERA_VIDEO) //是否开始半自动接收
                    .autoMic(false);//是否自动打开mic
            //加入房间
            ILVLiveManager.getInstance().switchRoom(room, memberOption, new ILiveCallBack() {
                @Override
                public void onSuccess(Object data) {
                    bEnterRoom = true;
//                    Toast.makeText(LiveActivity.this, "join room  ok ", Toast.LENGTH_SHORT).show();
                    logoutBtn.setVisibility(View.INVISIBLE);
                    backBtn.setVisibility(View.VISIBLE);
                }

                @Override
                public void onError(String module, int errCode, String errMsg) {
//                    Toast.makeText(LiveActivity.this, module + "|switchRoom fail " + errMsg + " " + errMsg, Toast.LENGTH_SHORT).show();
                }
            });
        } else if (view.getId() == R.id.btSwitchFIlter){
                int ret = setExternalBeautyFilter(mFilterNumber);
                if (ret < 0) {
                    mFilterNumber = 1;
                    setExternalBeautyFilter(mFilterNumber);
                }
                mLinearBeauty.setVisibility(View.VISIBLE);
                mPreFilterNumber = mFilterNumber;
                String sMsg = null;
                switch (mFilterNumber)
                {
                    case 1:
                        sMsg = "美颜";
                        break;
                    case 2:
                        sMsg = "浪漫";
                        break;
                    case 3:
                        sMsg = "清新";
                        break;
                    case 4:
                        sMsg = "唯美";
                        break;
                    case 5:
                        sMsg = "粉嫩";
                        break;
                    case 6:
                        sMsg = "怀旧";
                        break;
                    case 7:
                        sMsg = "蓝调";
                        break;
                    case 8:
                        sMsg = "清凉";
                        break;
                    case 9:
                        sMsg = "日系";
                        break;
                    default:
                        sMsg = "原始图像";
                        break;
                }
                mBtSwitchFilter.setText(sMsg);

                mFilterNumber++;
        } else if (view.getId() == R.id.btCancelFIlter){
            int ret = setExternalBeautyFilter(-1);
            mLinearBeauty.setVisibility(View.GONE);
            mBtSwitchFilter.setText("原始图像");
            mFilterNumber = mPreFilterNumber;
        } else if (view.getId() == R.id.btShowRoomParam){
            if (true == mbShowRoomParam){
                mBtShowRoomInfo.setText("显示房间信息");
                mLinearRoomInfo.setVisibility(View.GONE);
                mbShowRoomParam = false;
            }else{
                mBtShowRoomInfo.setText("隐藏房间信息");
                mLinearRoomInfo.setVisibility(View.VISIBLE);
                mbShowRoomParam = true;
            }
        } else if (view.getId() == R.id.btLogCapture){
                Time t=new Time(); // or Time t=new Time("GMT+8"); 加上Time Zone资料。
                t.setToNow(); // 取得系统时间。

                String sFileName = FILE_PATH + "/filterSDK-" + t.month + "." + t.monthDay + "."  + t.hour + "."  + t.minute + "."  + t.second + ".log";
                String sCommand = "logcat -v time -d -f " + sFileName;

                Log.i(TAG, "click logstartCaptue button,excute " + sCommand);

                try {
                    Process process = Runtime.getRuntime().exec(sCommand);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                AlertDialog dialog = new AlertDialog.Builder(LiveActivity.this)
                        .setTitle("确认")
                        .setMessage("日志保存成功! 路径:" + sFileName)
                        .setPositiveButton("好的", null)
                        .show();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    private void createRoom(){
        int room = Integer.parseInt("" + roomNum.getText());
        //创建房间配置项
        ILiveRoomOption hostOption = new ILiveRoomOption(ILiveLoginManager.getInstance().getMyUserId()).
                controlRole("User2Test")//角色设置
                .autoFocus(true)
                .authBits(AVRoomMulti.AUTH_BITS_DEFAULT)//权限设置
                .cameraId(ILiveConstants.FRONT_CAMERA)//不打开摄像头摄像头
                .autoMic(true)
                .autoCamera(true)
                .videoRecvMode(AVRoomMulti.VIDEO_RECV_MODE_SEMI_AUTO_RECV_CAMERA_VIDEO);//是否开始半自动接收
        mCurCameraId = mCurCameraId = ILiveConstants.FRONT_CAMERA;

        //创建房间
        ILVLiveManager.getInstance().createRoom(room, hostOption, new ILiveCallBack() {
            @Override
            public void onSuccess(Object data) {
                Toast.makeText(LiveActivity.this, "create room  ok", Toast.LENGTH_SHORT).show();
                logoutBtn.setVisibility(View.INVISIBLE);
                backBtn.setVisibility(View.VISIBLE);
            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                Toast.makeText(LiveActivity.this, module + "|create fail " + errMsg + " " + errMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loginSystem(){
        ILiveLoginManager.getInstance().tlsLogin("" + myId.getText(), "" + myPwd.getText(), new ILiveCallBack<String>() {
            @Override
            public void onSuccess(String data) {
                ILiveLoginManager.getInstance().iLiveLogin("" + myId.getText(), data, new ILiveCallBack() {
                    @Override
                    public void onSuccess(Object data) {
                        bLogin = true;
                        Toast.makeText(LiveActivity.this, "login success !", Toast.LENGTH_SHORT).show();
                        myLoginId.setText(""+ILiveLoginManager.getInstance().getMyUserId());
                        loginView.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onError(String module, int errCode, String errMsg) {
                        Toast.makeText(LiveActivity.this, module + "|login fail " + errCode + " " + errMsg, Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onError(String module, int errCode, String errMsg) {
                Toast.makeText(LiveActivity.this, module + "|login fail " + errCode + " " + errMsg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    void checkPermission() {
        final List<String> permissionsList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if ((checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED))
                permissionsList.add(Manifest.permission.CAMERA);
            if ((checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED))
                permissionsList.add(Manifest.permission.RECORD_AUDIO);
            if ((checkSelfPermission(Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED))
                permissionsList.add(Manifest.permission.WAKE_LOCK);
            if ((checkSelfPermission(Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED))
                permissionsList.add(Manifest.permission.MODIFY_AUDIO_SETTINGS);
            if ((checkSelfPermission(Manifest.permission.MODIFY_AUDIO_SETTINGS) != PackageManager.PERMISSION_GRANTED))
                permissionsList.add(Manifest.permission.MODIFY_AUDIO_SETTINGS);
            if (permissionsList.size() != 0) {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]),
                        REQUEST_PHONE_PERMISSIONS);
            }
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int i = seekBar.getId();
        if (i == R.id.seekBarBlur) {
            mTextBlurLevel.setText(String.valueOf(progress));
            m_iBlurLevel = progress;
            mUDFilter.setBeauty(m_iBlurLevel);
        } else if (i == R.id.seekBarWhite) {
            mTextWhiteLevel.setText(String.valueOf(progress));
            m_iWhiteLevel = progress;
            mUDFilter.setWhite(m_iWhiteLevel);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
