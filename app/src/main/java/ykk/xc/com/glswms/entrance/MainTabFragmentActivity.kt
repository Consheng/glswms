package ykk.xc.com.glswms.entrance

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Message
import android.os.Process
import android.support.v4.app.Fragment
import android.support.v4.content.FileProvider
import android.support.v4.view.ViewPager
import android.util.Log
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RelativeLayout
import android.widget.TextView
import butterknife.OnClick
import kotlinx.android.synthetic.main.aa_main.*
import okhttp3.*
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.bean.AppInfo
import ykk.xc.com.glswms.comm.ActivityCollector
import ykk.xc.com.glswms.comm.BaseActivity
import ykk.xc.com.glswms.util.IDownloadContract
import ykk.xc.com.glswms.util.IDownloadPresenter
import ykk.xc.com.glswms.util.JsonUtil
import ykk.xc.com.glswms.util.LogUtil
import ykk.xc.com.glswms.util.adapter.BaseFragmentAdapter
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*


class MainTabFragmentActivity : BaseActivity(), IDownloadContract.View {

    companion object {
        private val LOGOUT = 123
        private val UPDATE = 201
        private val UNUPDATE = 501

        private val UPDATE_PLAN = 1
    }
    private val context = this
    private var curRelative: RelativeLayout? = null
    private var curTv: TextView? = null
    private var curRadio: RadioButton? = null
    private var pageId = 0
    private val okHttpClient = OkHttpClient()
//    val fragment0 = MainTabFragment1()
    private var checkUpdate = false // 是否已经检查过更新
    private var mPresenter: IDownloadPresenter? = null

    /**
     * 显示下载的进度
     */
    private var downloadDialog: Dialog? = null
    private var progressBar: ProgressBar? = null
    private var tvDownPlan: TextView? = null
    private var mProgress = 0

    // 消息处理
    private val mHandler = MyHandler(this)

    private class MyHandler(activity: MainTabFragmentActivity) : Handler() {
        private val mActivity: WeakReference<MainTabFragmentActivity>

        init {
            mActivity = WeakReference(activity)
        }

        override fun handleMessage(msg: Message) {
            val m = mActivity.get()
            if (m != null) {
                m.hideLoadDialog()

                var errMsg: String? = null
                var msgObj: String? = null
                if (msg.obj is String) {
                    msgObj = msg.obj as String
                }
                when (msg.what) {
                    LOGOUT -> { // 退出返回信息 进入
                        // 移出记录的sessioin
                        val spfOther = m.spf(m.getResStr(R.string.saveOther))
                        spfOther.edit().remove("session").commit()

                        m.closeHandler(m.mHandler)
                        ActivityCollector.finishAll()
                        System.exit(0) //凡是非零都表示异常退出!0表示正常退出!
                    }
                    UPDATE -> { // 更新版本  成功
                        m.checkUpdate = true
                        val appInfo = JsonUtil.strToObject(msgObj, AppInfo::class.java)
                        if (m.getAppVersionCode(m.context) != appInfo!!.appVersion) {
                            m.showNoticeDialog(appInfo.appRemark)
                        }
                    }
                    UNUPDATE -> { // 更新版本  失败！
                    }
                    UPDATE_PLAN -> { // 更新进度
                        m.progressBar!!.progress = m.mProgress
                        m.tvDownPlan!!.text = String.format(Locale.CHINESE, "%d%%", m.mProgress)
                    }
                }
            }
        }
    }

    override fun setLayoutResID(): Int {
        return R.layout.aa_main
    }

    override fun initData() {
        curRelative = relative1
        curTv = tab1
        curRadio = radio1

        val user = showUserByXml()
        tv_title!!.text = "操作员：" + user!!.username

        val listFragment = ArrayList<Fragment>()
        listFragment.add(MainTabFragment1())
        listFragment.add(MainTabFragment2())
        listFragment.add(MainTabFragment3())
        listFragment.add(MainTabFragment4())
        //        listFragment.add(new MainTabFragment5());
        listFragment.add(MainTabFragment6())
        //ViewPager设置适配器
        viewPager!!.adapter = BaseFragmentAdapter(supportFragmentManager, listFragment)
        //ViewPager显示第一个Fragment
        viewPager!!.currentItem = 0
        //ViewPager页面切换监听
        viewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
                when (position) {
//                    0 -> tabChange(relative0, tab0, radio0, 0)
                    0 -> tabChange(relative1, tab1, radio1, 0)
                    1 -> tabChange(relative2, tab2, radio2, 1)
                    2 -> tabChange(relative3, tab3, radio3, 2)
                    3 -> tabChange(relative4, tab4, radio4, 3)
                    4 ->
                        //                        tabChange(relative5, tab5, radio5, 4);
                        tabChange(relative6, tab6, radio6, 4)
                    5 -> {
                        //                        tabChange(relative6, tab6, radio6, 5);
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

        mPresenter = IDownloadPresenter(context)
        if (!checkUpdate) {
            // 执行更新版本请求
            run_findAppInfo()
        }
    }

    @OnClick(R.id.btn_close, R.id.btn_print, R.id.btn_refresh, R.id.relative0, R.id.relative1, R.id.relative2, R.id.relative3, R.id.relative4, R.id.relative5, R.id.relative6, R.id.radio0, R.id.radio1, R.id.radio2, R.id.radio3, R.id.radio4, R.id.radio5, R.id.radio6)
    fun onViewClicked(view: View) {
        // setCurrentItem第二个参数控制页面切换动画
        //  true:打开/false:关闭
        //  viewPager.setCurrentItem(0, false);
        when (view.id) {
            R.id.btn_close -> { // 退出
                val build = AlertDialog.Builder(context)
                build.setIcon(R.drawable.caution)
                build.setTitle("系统提示")
                build.setMessage("主人，确定要离开我吗？")
                build.setPositiveButton("是的") { dialog, which ->
                    run_logout()
                }
                build.setNegativeButton("取消", null)
                build.setCancelable(false)
                build.show()
            }
            R.id.btn_print -> {// 打印
//                show(PrintMainActivity::class.java, null)
            }
            R.id.btn_refresh -> { // 刷新页面
            }
            R.id.relative0 -> tabChange(relative0, tab0, radio0, 0)
            R.id.relative1 -> tabChange(relative1, tab1, radio1, 0)
            R.id.relative2 -> tabChange(relative2, tab2, radio2, 1)
            R.id.relative3 -> tabChange(relative3, tab3, radio3, 2)
            R.id.relative4 -> tabChange(relative4, tab4, radio4, 3)
            R.id.relative5 -> {
                //                tabChange(relative5, tab5, radio5, 4);
//                tabChange(relative6, tab6, radio6, 5)
            }
            R.id.relative6 -> tabChange(relative6, tab6, radio6, 4);

            // --------------------------------------------
            R.id.radio0 -> tabChange(relative0, tab0, radio0, 0)
            R.id.radio1 -> tabChange(relative1, tab1, radio1, 0)
            R.id.radio2 -> tabChange(relative2, tab2, radio2, 1)
            R.id.radio3 -> tabChange(relative3, tab3, radio3, 2)
            R.id.radio4 -> tabChange(relative4, tab4, radio4, 3)
            R.id.radio5 -> {
                //                tabChange(relative5, tab5, radio5, 4);
            }
            R.id.radio6 -> tabChange(relative6, tab6, radio6, 4)
        }
    }

    /**
     * 选中之后改变样式
     */
    private fun tabSelected(relative: RelativeLayout, tv: TextView, rb: RadioButton) {
        curRelative!!.setBackgroundColor(Color.parseColor("#EAEAEA"))
        curRadio!!.isChecked = false
        curTv!!.setTextColor(Color.parseColor("#1a1a1a"))
        relative.setBackgroundResource(R.drawable.back_style_blue)
        rb.isChecked = true
        tv.setTextColor(Color.parseColor("#6a5acd"))
        curRelative = relative
        curRadio = rb
        curTv = tv
    }

    private fun tabChange(relative: RelativeLayout?, tv: TextView?, radio: RadioButton?, page: Int) {
        pageId = page
        tabSelected(relative!!, tv!!, radio!!)
        viewPager!!.setCurrentItem(page, false)
        if(pageId == 0) {
            btn_print.visibility = View.GONE
            btn_refresh.visibility = View.VISIBLE
        } else {
            btn_print.visibility = View.VISIBLE
            btn_refresh.visibility = View.GONE
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

    /**
     * 获取服务端的App信息
     */
    private fun run_findAppInfo() {
        val mUrl = getURL("appInfo/findAppInfo")
        val formBody = FormBody.Builder()
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
                mHandler.sendEmptyMessage(UNUPDATE)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                Log.e("run_findAppInfo --> onResponse", result)
                if (!JsonUtil.isSuccess(result)) {
                    mHandler.sendEmptyMessage(UNUPDATE)
                    return
                }
                val msg = mHandler.obtainMessage(UPDATE, result)
                mHandler.sendMessage(msg)
            }
        })
    }

    private fun showDownloadDialog() {
        val builder = AlertDialog.Builder(context)

        builder.setTitle("软件更新")
        val inflater = LayoutInflater.from(context)
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
        //            public void delClick(DialogInterface dialog, int which) {
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
        val alertDialog = AlertDialog.Builder(context)
                .setTitle("更新版本").setMessage(remark)
                .setPositiveButton("下载") { dialog, which ->
                    // 得到ip和端口
                    val spfConfig = spf(getResStr(R.string.saveConfig))
                    val http = spfConfig.getString("http", "http")
                    val ip = spfConfig.getString("ip", "192.168.3.198")
                    val port = spfConfig.getString("port", "8080")
                    val url = "$http://$ip:$port/apks/glswms.apk"

                    showDownloadDialog()
                    mPresenter!!.downApk(context, url)
                    dialog.dismiss()
                }
                //                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                //                    public void delClick(DialogInterface dialog, int which) {
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

    override fun showUpdate(version: String) {}

    override fun showProgress(progress: Int) {
        context.mProgress = progress
        mHandler.sendEmptyMessage(UPDATE_PLAN)
    }

    override fun showFail(msg: String) {
        toasts(msg)
    }

    override fun showComplete(file: File) {
        if (downloadDialog != null) downloadDialog!!.dismiss()

        try {
            val intent = Intent(Intent.ACTION_VIEW)

            //7.0以上需要添加临时读取权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val authority = context.applicationContext.packageName + ".fileProvider"
                val fileUri = FileProvider.getUriForFile(context, authority, file)
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                intent.setDataAndType(fileUri, "application/vnd.android.package-archive")

            } else {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive")
            }
            startActivity(intent)

            //弹出安装窗口把原程序关闭。
            //避免安装完毕点击打开时没反应
            Process.killProcess(Process.myPid())

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        // 点击返回不销毁
        //        if((keyCode == KeyEvent.KEYCODE_BACK)&&(event.getAction() == KeyEvent.ACTION_DOWN)) {
        //            return false;
        //        }
        return false
    }

}
