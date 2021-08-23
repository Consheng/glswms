package ykk.xc.com.glswms.entrance


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Message
import android.os.Process.killProcess
import android.provider.Settings
import android.support.v4.content.FileProvider
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import butterknife.OnClick
import okhttp3.*
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.bean.AppInfo
import ykk.xc.com.glswms.comm.ActivityCollector
import ykk.xc.com.glswms.comm.BaseFragment
import ykk.xc.com.glswms.comm.Comm
import ykk.xc.com.glswms.entrance.page5.ServiceSetActivity
import ykk.xc.com.glswms.set.BindDefaultStockActivity
import ykk.xc.com.glswms.set.ModifyPWDActivity
import ykk.xc.com.glswms.set.Set_NetworkErrorData_MainActivity
import ykk.xc.com.glswms.util.IDownloadContract
import ykk.xc.com.glswms.util.IDownloadPresenter
import ykk.xc.com.glswms.util.JsonUtil
import ykk.xc.com.glswms.util.LogUtil
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*


/**
 * 设置
 */
class MainTabFragment6 : BaseFragment(), IDownloadContract.View {

    companion object {
        private val SUCC1 = 200
        private val UNSUCC1 = 500
        private val TEST = 201
        private val UNTEST = 501
        private val UPDATE_PLAN = 1
        private val REQUESTCODE = 101

        private val LOGOUT = 123
    }
    private val context = this
    private var mPresenter: IDownloadPresenter? = null
    private val okHttpClient = OkHttpClient()
    private var mContext: Activity? = null


    // 消息处理
    private val mHandler = MyHandler(this)

    /**
     * 显示下载的进度
     */
    private var downloadDialog: Dialog? = null
    private var progressBar: ProgressBar? = null
    private var tvDownPlan: TextView? = null
    private var progress: Int = 0

    private class MyHandler(activity: MainTabFragment6) : Handler() {
        private val mActivity: WeakReference<MainTabFragment6>

        init {
            mActivity = WeakReference(activity)
        }

        override fun handleMessage(msg: Message) {
            val m = mActivity.get()
            if (m != null) {
                m.hideLoadDialog()
                when (msg.what) {
                    SUCC1 // 得到更新信息
                    -> {
                        val appInfo = JsonUtil.strToObject(msg.obj as String, AppInfo::class.java)
                        if (m.getAppVersionCode(m.mContext) != appInfo!!.appVersion) {
                            m.showNoticeDialog(appInfo.appRemark)
                        } else {
                            m.toasts("已经是最新版本了！")
                        }
                    }
                    UNSUCC1 // 得到更新信息失败
                    -> m.toasts("已经是最新版本了！！！")
                    TEST // 网络ok
                    -> m.toasts("网络通畅")
                    UNTEST // 网络异常
                    -> m.toasts("网络异常，请检查ip和端口！")
                    UPDATE_PLAN // 更新进度
                    -> {
                        m.progressBar!!.progress = m.progress
                        m.tvDownPlan!!.text = String.format(Locale.CHINESE, "%d%%", m.progress)
                    }
                    LOGOUT -> { // 退出返回信息 进入
                        // 移出记录的sessioin
                        val spfOther = m.spf(m.getResStr(R.string.saveOther))
                        spfOther.edit().remove("session").commit()

                        m.closeHandler(m.mHandler)
                        ActivityCollector.finishAll()
                        System.exit(0) //凡是非零都表示异常退出!0表示正常退出!
                    }
                }
            }
        }
    }

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.aa_main_item6, container, false)
    }

    override fun initData() {
        mContext = activity
        mPresenter = IDownloadPresenter(this)
        requestPermission()
    }

    @OnClick(R.id.lin_item1, R.id.lin_item2, R.id.lin_item3, R.id.lin_item4, R.id.lin_item5, R.id.lin_item6, R.id.lin_item7, R.id.lin_item8, R.id.lin_item100)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.lin_item1 -> { // wifi设置
                startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
            R.id.lin_item2 -> { // 服务器设置
                show(ServiceSetActivity::class.java, null)
            }
            R.id.lin_item3 ->{// 网络测试
                toasts("网络通畅！！！")
            }
            R.id.lin_item4 ->{// 更新版本
                run_findAppInfo()
            }
            R.id.lin_item5 ->{ // 打印
//                show(PrintMainActivity::class.java, null)
            }
            R.id.lin_item6 ->{ // 网络异常数据
                show(Set_NetworkErrorData_MainActivity::class.java, null)
            }
            R.id.lin_item7 ->{ // 绑定默认仓库
                show(BindDefaultStockActivity::class.java, null)
            }
            R.id.lin_item8 ->{ // 修改密码
                show(ModifyPWDActivity::class.java, null)
            }
            R.id.lin_item100 ->{ // 退出
                run_logout()
            }
        }
        //                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));//直接进入手机中的蓝牙设置界面
        //                run_test();
    }

    private fun showDownloadDialog() {
        val builder = AlertDialog.Builder(mContext)

        builder.setTitle("软件更新")
        val inflater = LayoutInflater.from(mContext)
        val v = inflater.inflate(R.layout.progress, null)
        progressBar = v.findViewById<View>(R.id.progress) as ProgressBar
        tvDownPlan = v.findViewById<View>(R.id.tv_downPlan) as TextView
        builder.setView(v)
        // 开发员用的，长按进度条，就关闭下载框
        tvDownPlan!!.setOnLongClickListener {
            downloadDialog!!.dismiss()
            true
        }
        // 如果用户点击取消就销毁掉这个系统
        //        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
        //            @Override
        //            public void onClick(DialogInterface dialog, int which) {
        ////                mContext.finish();
        //                dialog.dismiss();
        //            }
        //        });
        downloadDialog = builder.create()
        downloadDialog!!.show()
        downloadDialog!!.setCancelable(false)
        downloadDialog!!.setCanceledOnTouchOutside(false)
    }

    /**
     * 提示下载框
     */
    private fun showNoticeDialog(remark: String) {
        val alertDialog = AlertDialog.Builder(mContext)
                .setTitle("更新版本").setMessage(remark)
                .setPositiveButton("下载") { dialog, which ->
                    // 得到ip和端口
                    val spfConfig = spf(getResStr(R.string.saveConfig))
                    val ip = spfConfig.getString("ip", "192.168.3.198")
                    val port = spfConfig.getString("port", "8080")
                    val url = "http://$ip:$port/apks/bswms.apk"

                    showDownloadDialog()
                    mPresenter!!.downApk(mContext, url)
                    dialog.dismiss()
                }
                //                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                //                    public void onClick(DialogInterface dialog, int which) {
                //                        dialog.dismiss();
                //                    }
                //                })
                .create()// 创建
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()// 显示
    }

    /**
     * 得到本机的版本信息
     */
    private fun getAppVersionCode(context: Context?): Int {
        val pack: PackageManager
        val info: PackageInfo
        // String versionName = "";
        try {
            pack = context!!.packageManager
            info = pack.getPackageInfo(context.packageName, 0)
            return info.versionCode
            // versionName = info.versionName;
        } catch (e: Exception) {
            Log.e("getAppVersionName(Context context)：", e.toString())
        }

        return 0
    }

    /**
     * 获取服务端的App信息
     */
    private fun run_findAppInfo() {
        showLoadDialog("加载中...", false)
        val mUrl = getURL("findAppInfo")
        val formBody = FormBody.Builder()
                //                .add("limit", "10")
                //                .add("pageSize", "100")
                .build()

        val request = Request.Builder()
                .addHeader("cookie", session)
                .url(mUrl)
                .post(formBody)
                .build()

        // step 3：创建 Call 对象
        val call = okHttpClient.newCall(request)

        //step 4: 开始异步请求
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNSUCC1)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                if (!JsonUtil.isSuccess(result)) {
                    mHandler.sendEmptyMessage(UNSUCC1)
                    return
                }
                val msg = mHandler.obtainMessage(SUCC1, result)
                Log.e("MainTabFragment5 --> onResponse", result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 网络测试
     */
    private fun run_test() {
        showLoadDialog("测试中...", false)
        val mUrl = getURL("")
        val len = mUrl.indexOf("mdwms")
        val url = mUrl.substring(0, len)
        val request = Request.Builder()
                .url(url)
                .build()
        val call = okHttpClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // 网络异常
                mHandler.sendEmptyMessage(UNTEST)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                Log.e("onResponse", response.body().toString())
                // 网络没问题
                mHandler.sendEmptyMessage(TEST)
            }
        })
    }

    /**
     * 请求用户授权SD卡读写权限
     */
    private fun requestPermission() {
        // 判断sdk是是否大于等于6.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val check = mContext!!.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (check != PackageManager.PERMISSION_DENIED) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUESTCODE)

            } else {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUESTCODE)
            }
        } else { // 6.0以下，直接执行
            createFile()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUESTCODE) {
            if (permissions[0] == Manifest.permission.WRITE_EXTERNAL_STORAGE && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //用户同意
                createFile()
            } else {
                //用户不同意
                val alertDialog = AlertDialog.Builder(mContext)
                        .setTitle("去授权").setMessage("您已禁用了SD卡的读写权限,会导致部分功能不能用，去打开吧！")
                        .setPositiveButton("好的") { dialog, which ->
                            val mIntent = Intent()
                            mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            if (Build.VERSION.SDK_INT >= 9) {
                                mIntent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                                mIntent.data = Uri.fromParts("package", mContext!!.packageName, null)
                            } else if (Build.VERSION.SDK_INT <= 8) {
                                mIntent.action = Intent.ACTION_VIEW
                                mIntent.setClassName("com.android.settings", "com.android.setting.InstalledAppDetails")
                                mIntent.putExtra("com.android.settings.ApplicationPkgName", mContext!!.packageName)
                            }
                            startActivity(mIntent)
                        }
                        .setNegativeButton("不了", null).create()// 创建
                alertDialog.setCancelable(false)
                alertDialog.setCanceledOnTouchOutside(false)
                alertDialog.show()// 显示
            }
        }
    }

    private fun createFile() {
        val file = File(Comm.publicPaths + "updateFile")
        if (!file.exists()) {
            val isSuccess = file.mkdirs()
            Log.d("isSuccess:", "----------0------------------$isSuccess")
        }
    }

    override fun showUpdate(version: String) {}

    override fun showProgress(progress: Int) {
        context.progress = progress
        mHandler.sendEmptyMessage(UPDATE_PLAN)
    }

    override fun showFail(msg: String) {
        toasts(msg)
    }

    override fun showComplete(file: File) {
        if (downloadDialog != null) downloadDialog!!.dismiss()

        try {
            val authority = mContext!!.applicationContext.packageName + ".fileProvider"
            val fileUri = FileProvider.getUriForFile(mContext!!, authority, file)
            val intent = Intent(Intent.ACTION_VIEW)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            //7.0以上需要添加临时读取权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                intent.setDataAndType(fileUri, "application/vnd.android.package-archive")
            } else {
                val uri = Uri.fromFile(file)
                intent.setDataAndType(uri, "application/vnd.android.package-archive")
            }

            startActivity(intent)

            //弹出安装窗口把原程序关闭。
            //避免安装完毕点击打开时没反应
            killProcess(android.os.Process.myPid())
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    /**
     * 退出登录
     */
    private fun run_logout() {
        showLoadDialog("加载中...", false)
        val mUrl = getURL("logout")
        val formBody = FormBody.Builder()
                .build()

        val request = Request.Builder()
                .addHeader("cookie", session)
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                LogUtil.e("run_logout--onFailure", e.toString())
                mHandler.sendEmptyMessage(LOGOUT)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                LogUtil.e("run_logout--onFailure", result)
                mHandler.sendEmptyMessage(LOGOUT)
            }
        })
    }

}
