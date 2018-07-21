package com.yc.intelligentlightingassistant.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.view.menu.MenuPopupHelper;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.yc.intelligentlightingassistant.R;
import com.yc.intelligentlightingassistant.SocketThread;
import com.yc.intelligentlightingassistant.adapter.RecyclerAdapter;
import com.yc.intelligentlightingassistant.bean.DeviceInfo;
import com.yc.intelligentlightingassistant.db.DeviceInfoImpl;
import com.yc.intelligentlightingassistant.db.IDeviceInfoDao;
import com.yc.intelligentlightingassistant.util.FormatUtil;
import com.yc.intelligentlightingassistant.util.ToastUtil;
import com.yc.intelligentlightingassistant.util.WifiAPUtil;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    // 权限回调的标识
    private static final int RC = 0x0100;
    // WIFI名称
    public static final String WIFI_NAME = "AndroidAP";
    // WIFI密码
    public static final String WIFI_PASSWORD = "12345678";
    //端口号
    public static final int PORT = 8090;
    //设置1路信息
    private static final int SINGLE_SET = 1;
    //设置16路信息
    private static final int DEVICE_SET = 2;
    //设置全部设备信息
    private static final int ALL_SET = 3;

    @BindView(R.id.drawer_layout)
    DrawerLayout mDrawerLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.recycler_ip_list)
    RecyclerView mIpListRecycler;
    @BindView(R.id.recycler_device_info)
    RecyclerView mDeviceInfoRecycler;
    @BindView(R.id.txt_title)
    TextView mTitleTv;

    // 设备列表的适配器, 用于侧滑菜单栏显示
    private RecyclerAdapter<DeviceInfo> mIpListAdapter;
    // 设备列表 用于侧滑菜单栏显示
    private List<DeviceInfo> mInfoList;
    // 设备列表的适配器，用于主界面显示一个设备的16路信息
    private RecyclerAdapter<DeviceInfo> mDeviceAdapter;
    // 设备列表，用于主界面显示一个设备的16路信息
    private List<DeviceInfo> mDeviceList;
    // 获取wifi信息的工具类
    private WifiAPUtil mWifiUtil;
    // 数据库操作
    private IDeviceInfoDao mDao;
    // ip列表
    private List<String> mIpList;
    // 当前显示的设备信息
    private DeviceInfo mInfo;

    private Map<String, Socket> socketMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initBefore();
        initData();
        initWidget();
    }

    // 初始化绑定ButterKnife和检查权限
    private void initBefore() {
        ButterKnife.bind(this);
        checkPermission();
        EventBus.getDefault().register(this);
    }

    // 初始化数据和Wifi状态
    private void initData() {
        mInfoList = new ArrayList<DeviceInfo>();
        mDeviceList = new ArrayList<>();
        mWifiUtil = WifiAPUtil.getInstance(this);
        mDao = new DeviceInfoImpl(this);
        socketMap = new HashMap<>();
        mInfo = new DeviceInfo();
        if (mWifiUtil.getWifiAPState() != WifiAPUtil.WIFI_AP_STATE_ENABLED) {
            mWifiUtil.turnOnWifiAp(WIFI_NAME, WIFI_PASSWORD, WifiAPUtil.WifiSecurityType.WIFICIPHER_WPA2);
        }
        mIpList = new ArrayList<>();
        ipToDeviceInfo();
        if (mIpList.size() != 0) {
            // 默认显示第一个设备的信息
            mDeviceList = mDao.querySingleInfo(mIpList.get(0));
            setTitle(mDeviceList.get(0));
            mInfo = mDeviceList.get(0);
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                socketListener();
            }
        }).start();
    }

    // 将ip信息转为为设备信息
    private void ipToDeviceInfo() {
        mInfoList.clear();
        // 查询设备是否已经在数据库中存在
        if (mIpList.size() != 0) {
            for (String ip : mIpList) {
                DeviceInfo info = mDao.querySingleAisleInfo(ip);
                // 若数据库中无此设备信息，则创建并插入数据库中
                if (info == null) {
                    mDao.insertInfo(ip);
                }
                info = mDao.querySingleAisleInfo(ip);
                if (info != null) {
                    mInfoList.add(info);
                }
            }
        }
    }

    // 初始化控件
    private void initWidget() {
        setSupportActionBar(mToolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawerLayout.setDrawerListener(toggle);
        toggle.syncState();
        mIpListAdapter = new RecyclerAdapter<DeviceInfo>(mInfoList, new RecyclerAdapter.AdapterListener<DeviceInfo>() {
            @Override
            public void onItemClick(RecyclerAdapter.ViewHolder holder, final DeviceInfo info) {
                // 点击后显示该设备十六路信息
                setTitle(info);
                mDeviceList = mDao.querySingleInfo(info.getIp());
                mDeviceAdapter.replace(mDeviceList);
                mDrawerLayout.closeDrawer(GravityCompat.START);
                mInfo = info;
            }

            @Override
            public void onItemLongClick(RecyclerAdapter.ViewHolder holder, final DeviceInfo info) {
                // 长按修改设备名称
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                String[] items = new String[]{"修改设备名称"};
                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                updateDeviceName(info);
                                break;
                            default:
                                break;
                        }
                    }
                }).create().show();
            }
        }) {
            @Override
            protected int getItemViewType(int position, DeviceInfo info) {
                return R.layout.item_ip;
            }

            @Override
            protected ViewHolder<DeviceInfo> onCreateViewHolder(View root, int viewType) {
                return new IpViewHolder(root);
            }
        };
        mIpListRecycler.setAdapter(mIpListAdapter);
        mIpListRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mIpListRecycler.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mDeviceAdapter = new RecyclerAdapter<DeviceInfo>(mDeviceList, new RecyclerAdapter.AdapterListener<DeviceInfo>() {
            @Override
            public void onItemClick(RecyclerAdapter.ViewHolder holder, DeviceInfo info) {
                // 修改设备一路参数
                showDialog(MainActivity.this, "修改设备一路参数", info, SINGLE_SET);
            }

            @Override
            public void onItemLongClick(RecyclerAdapter.ViewHolder holder, DeviceInfo info) {
            }
        }) {
            @Override
            protected int getItemViewType(int position, DeviceInfo info) {
                return R.layout.item_device;
            }

            @Override
            protected ViewHolder<DeviceInfo> onCreateViewHolder(View root, int viewType) {
                return new DeviceHolder(root);
            }
        };
        mDeviceInfoRecycler.setAdapter(mDeviceAdapter);
        mDeviceInfoRecycler.setLayoutManager(new GridLayoutManager(this, 4));
    }

    // 刷新按钮点击事件
    @OnClick(R.id.im_refresh)
    void onRefreshClick() {
        ipToDeviceInfo();
        mIpListAdapter.notifyDataSetChanged();
        Log.d("TAG", "IPList: " + mInfoList.toString());
    }

    // 更多 点击事件
    @SuppressLint("RestrictedApi")
    @OnClick(R.id.im_more)
    void onMoreClick() {
        PopupMenu popupMenu = new PopupMenu(this, findViewById(R.id.im_more));
        popupMenu.getMenuInflater().inflate(R.menu.menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_setting_single:
                        // 修改设备16路参数
                        showDialog(MainActivity.this, "修改设备16路参数", mInfo, DEVICE_SET);
                        break;
                    case R.id.menu_setting_all:
                        // 修改全部设备16路参数
                        showDialog(MainActivity.this, "修改全部设备16路参数", mInfo, ALL_SET);
                        break;
                    case R.id.menu_exit:
                        finish();
                        break;
                    default:
                        break;
                }
                return false;
            }
        });
        // 利用反射显示出图标
        try {
            Field field = popupMenu.getClass().getDeclaredField("mPopup");
            field.setAccessible(true);
            MenuPopupHelper mHelper = (MenuPopupHelper) field.get(popupMenu);
            mHelper.setForceShowIcon(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        popupMenu.show();
    }

    // Socket监听客户端接入
    private void socketListener() {
        try {
            ServerSocket ss = new ServerSocket(PORT);
            while (true) {
                final Socket socket = ss.accept();
                socket.setKeepAlive(true);
                final String inetAdress = socket.getInetAddress().toString().replace("/", "");
                Log.d("TAG", "socket ip:" + inetAdress);
                if (!mIpList.contains(inetAdress)) {
                    mIpList.add(inetAdress);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ipToDeviceInfo();
                            mIpListAdapter.notifyDataSetChanged();
                            if (mDeviceList.size() == 0) {
                                // 如果当前没有设备，则显示第一个连接的设备
                                mDeviceList = mDao.querySingleInfo(inetAdress);
                                mDeviceAdapter.replace(mDeviceList);
                                mDeviceAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
                socketMap.put(inetAdress, socket);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        while (true) {
                            if (!isClientAlive(socket)) {
                                final String ip = socket.getInetAddress().toString().replace("/", "");
                                mIpList.remove(ip);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        ipToDeviceInfo();
                                        mIpListAdapter.notifyDataSetChanged();
                                        //  如果当前显示的是断开连接的设备，则移除
                                        if (mDeviceList.get(0).getIp().equals(ip)) {
                                            mDeviceList.clear();
                                            mDeviceAdapter.replace(mDeviceList);
                                            mDeviceAdapter.notifyDataSetChanged();
                                        }
                                    }
                                });
                                return;
                            }
                            try {
                                Thread.sleep(5000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                        }
                    }
                }).start();
                new SocketThread(MainActivity.this, SocketThread.SEND_SINGLE, socket, new DeviceInfo("192.168.1.1"
                        , 100, 50, 1, 1)).start();


            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isClientAlive(Socket socket) {
        try {
            socket.sendUrgentData(0);
            Log.e("heat", "向客户端发送心跳包了");
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 修改设备名称
    private void updateDeviceName(final DeviceInfo info) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText inputEt = new EditText(this);
        inputEt.setHint("请输入设备名称");
        builder.setView(inputEt)
                .setTitle("修改设备名称")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = inputEt.getText().toString();
                        if (TextUtils.isEmpty(name)) {
                            ToastUtil.showToast(R.string.error_input_null);
                            return;
                        }
                        mDao.updateDeviceName(info.getIp(), name);
                        ipToDeviceInfo();
                        mIpListAdapter.replace(mInfoList);
                        info.setName(name);
                        setTitle(info);
                        onRefreshClick();
                    }
                })
                .setNegativeButton("取消", null)
                .create()
                .show();
    }

    // 设置标题显示内容
    private void setTitle(DeviceInfo info) {
        if (info.getName() != null) {
            mTitleTv.setText(info.getName() + "(" + info.getIp() + ")");
        } else {
            mTitleTv.setText(info.getIp());
        }
    }

    // 对话框 修改参数
    private void showDialog(final Context context, String title, final DeviceInfo info, final int type) {
        final LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.alert_dialog_layout, null);
        final EditText frequencyEt = view.findViewById(R.id.et_frequency);
        final EditText dutyEt = view.findViewById(R.id.et_duty);
        final SeekBar freSb = view.findViewById(R.id.sb_frequency);
        final SeekBar dutySb = view.findViewById(R.id.sb_duty);
        //seekbar的监听事件
        freSb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    frequencyEt.setText(i + 10 + "");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        dutySb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    dutyEt.setText(i + "");
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        new AlertDialog.Builder(context)
                .setTitle(title)
                .setView(view)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //对数值输入范围的判断
                        double frequency;
                        double duty;
                        if (frequencyEt.getText().toString().equals("")) {
                            frequency = 0;
                        } else {
                            if (Double.parseDouble(frequencyEt.getText().toString()) > 20000 ||
                                    Double.parseDouble(frequencyEt.getText().toString()) < 10) {
                                Toast.makeText(MainActivity.this, "频率数值超出范围！", Toast.LENGTH_SHORT).show();
                                frequency = 0;
                            } else {
                                frequency = Double.parseDouble(frequencyEt.getText().toString());
                            }
                        }
                        if (dutyEt.getText().toString().equals("")) {
                            duty = 0;

                        } else {
                            if (Double.parseDouble(dutyEt.getText().toString()) > 100) {
                                Toast.makeText(MainActivity.this, "占空比数值超出范围！", Toast.LENGTH_SHORT).show();
                                duty = 0;
                            } else {
                                duty = Double.parseDouble(dutyEt.getText().toString());
                            }
                        }
                        info.setFrequency(frequency);
                        info.setDuty(duty);
                        //不同情况的dialog
                        switch (type) {
                            case SINGLE_SET:
                                //更新1路信息
                                mDao.updateAisleInfo(info);
                                Socket s1 = socketMap.get(info.getIp());
                                new SocketThread(MainActivity.this, SocketThread.SEND_SINGLE, s1, info).start();
                                mDeviceList = mDao.querySingleInfo(info.getIp());
                                mDeviceAdapter.replace(mDeviceList);
                                break;
                            case DEVICE_SET:
                                //更新一个设备的信息
                                info.setAisle(0);
                                mDao.updateDeviceInfo(info);
                                Socket s2 = socketMap.get(info.getIp());
                                new SocketThread(MainActivity.this, SocketThread.SEND_DEVICE, s2, info).start();
                                mDeviceList = mDao.querySingleInfo(info.getIp());
                                mDeviceAdapter.replace(mDeviceList);
                                Log.d("TAG", "更新设备16路信息");
                                break;
                            case ALL_SET:
                                //更新全部设备的信息
                                info.setAisle(0);
                                mDao.updateAllInfo(info);
                                mDeviceList = mDao.querySingleInfo(info.getIp());
                                mDeviceAdapter.replace(mDeviceList);
                                setTitle(info);
                                for (int j = 0; j < mIpList.size(); j++) {
                                    Socket s3 = socketMap.get(mIpList.get(j));
                                    new SocketThread(MainActivity.this, SocketThread.SEND_DEVICE, s3, info).start();
                                }
                                break;
                            default:
                                break;
                        }
                    }
                })
                .setNegativeButton("取消", null)
                .setCancelable(false)
                .show();
    }

    @Subscribe
    public void updateInfo(DeviceInfo info) {
        if(info.getAisle() == 0) {
            mDeviceList = mDao.querySingleInfo(info.getIp());
            mDeviceAdapter.replace(mDeviceList);
        } else {
            DeviceInfo deviceInfo = mDeviceList.get(info.getAisle() - 1);
            deviceInfo.setFrequency(info.getFrequency());
            deviceInfo.setDuty(info.getDuty());
            deviceInfo.setVoltage(info.getVoltage());
            mDeviceAdapter.notifyItemChanged(info.getAisle() - 1);
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * 启动应用后，先检查当前手机版本是否需要动态获取权限，若需要，则获取相应的权限
     */
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 判断是否有WRITE_SETTINGS权限
            if (!Settings.System.canWrite(this)) {
                // 申请WRITE_SETTINGS权限
                Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, RC);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 退出的时候关闭Wifi热点
        mWifiUtil.closeWifiAp();
        EventBus.getDefault().unregister(this);
    }

    /**
     * 当没有权限时，提醒用户为软件授权
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 判断是否有WRITE_SETTINGS权限
                if (Settings.System.canWrite(this)) {
                    ToastUtil.showToast(R.string.label_error_without_permission);
                    finish();
                }
            }
        }
    }

    /**
     * Ip列表的ViewHolder
     */
    class IpViewHolder extends RecyclerAdapter.ViewHolder<DeviceInfo> {

        @BindView(R.id.txt_ip)
        TextView mIpTxt;

        public IpViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void onBind(DeviceInfo info) {
            if (!TextUtils.isEmpty(info.getName())) {
                mIpTxt.setText(info.getName() + " ");
            } else {
                mIpTxt.setText(info.getIp());
            }
        }
    }

    /**
     * 设备16路信息列表的ViewHolder
     */
    class DeviceHolder extends RecyclerAdapter.ViewHolder<DeviceInfo> {

        @BindView(R.id.txt_freq)
        TextView freqTxt;
        @BindView(R.id.txt_duty)
        TextView dutyTxt;
        @BindView(R.id.txt_voltage)
        TextView voltageTxt;
        @BindView(R.id.txt_aisle)
        TextView aisleTxt;

        public DeviceHolder(View itemView) {
            super(itemView);
        }

        @Override
        protected void onBind(DeviceInfo info) {
            freqTxt.setText(FormatUtil.formatFrequency(info.getFrequency()));
            dutyTxt.setText(FormatUtil.formatDuty(info.getDuty()));
            voltageTxt.setText(FormatUtil.formatVoltage(info.getVoltage()));
            aisleTxt.setText(FormatUtil.formatChannel(info.getAisle()));
        }
    }
}

