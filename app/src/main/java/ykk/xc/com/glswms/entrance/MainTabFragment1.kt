package ykk.xc.com.glswms.entrance


import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.os.Process.killProcess
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
import ykk.xc.com.glswms.comm.BaseFragment
import ykk.xc.com.glswms.util.IDownloadContract
import ykk.xc.com.glswms.util.IDownloadPresenter
import ykk.xc.com.glswms.util.JsonUtil
import ykk.xc.com.glswms.warehouse.OutInStock_Search_MainActivity
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

/**
 * 采购
 */
class MainTabFragment1 : BaseFragment(), IDownloadContract.View {

    private val context = this
    private var mContext: Activity? = null
    private val okHttpClient = OkHttpClient()
    private var mPresenter: IDownloadPresenter? = null
//    private var isCheckUpdate = false // 是否已经检查过更新

    // 消息处理
    private val mHandler = MyHandler(this)

    /**
     * 显示下载的进度
     */
    private var downloadDialog: Dialog? = null
    private var progressBar: ProgressBar? = null
    private var tvDownPlan: TextView? = null
    private var progress: Int = 0

    private class MyHandler(activity: MainTabFragment1) : Handler() {
        private val mActivity: WeakReference<MainTabFragment1>

        init {
            mActivity = WeakReference(activity)
        }

        override fun handleMessage(msg: Message) {
            val m = mActivity.get()
            if (m != null) {
                m.hideLoadDialog()

                when (msg.what) {
                    UPDATE // 更新版本  成功
                    -> {
//                        m.isCheckUpdate = true
                        val appInfo = JsonUtil.strToObject(msg.obj as String, AppInfo::class.java)
                        if (m.getAppVersionCode(m.mContext) != appInfo!!.appVersion) {
                            m.showNoticeDialog(appInfo.appRemark)
                        }
                    }
                    UNUPDATE // 更新版本  失败！
                    -> {
                    }
                    UPDATE_PLAN // 更新进度
                    -> {
                        m.progressBar!!.progress = m.progress
                        m.tvDownPlan!!.text = String.format(Locale.CHINESE, "%d%%", m.progress)
                    }
                }
            }
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mContext = context as Activity?
    }

    //SDK API<23时，onAttach(Context)不执行，需要使用onAttach(Activity)。Fragment自身的Bug，v4的没有此问题
    override fun onAttach(activity: Activity?) {
        super.onAttach(activity)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            mContext = activity
        }
    }

    override fun onDetach() {
        super.onDetach()
        mContext = null
    }

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.aa_main_item1, container, false)
    }

    override fun initData() {
//        mPresenter = IDownloadPresenter(context)
//        if (!isCheckUpdate) {
//            // 执行更新版本请求
//            run_findAppInfo()
//        }
    }

    @OnClick(R.id.relative1, R.id.relative2, R.id.relative3, R.id.relative4, R.id.relative5)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.relative1 -> { // 待上传
                val bundle = Bundle()
                bundle.putInt("pageId", 5)
                bundle.putString("billType", "CGSHRK")
                show(OutInStock_Search_MainActivity::class.java, bundle)
            }
            R.id.relative2 -> { // 外购入库
//                show(Pur_Receive_InStock_MainActivity::class.java, null)
            }
            R.id.relative3 -> { // 自由入库
//                show(Pur_InStock_MainActivity::class.java, null)
            }
            R.id.relative4  -> { // 采购退货
//                show(Pur_InStock_RED_MainActivity::class.java, null)
            }
            R.id.relative5 -> { // 仓库收货上传
//                val bundle = Bundle()
//                bundle.putInt("pageId", 6)
//                bundle.putString("billType", "CGSHCZ")
//                show(OutInStock_Search_MainActivity::class.java, bundle)
            }
        }//                show(Pur_OrderSearchActivity.class, null);
        //                show(Pur_InStock_MainActivity.class,null);
        //                show(Pur_ProdBoxMainActivity.class, null);
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
        context.progress = progress
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
                val authority = mContext!!.applicationContext.packageName + ".fileProvider"
                val fileUri = FileProvider.getUriForFile(mContext!!, authority, file)
                intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                intent.setDataAndType(fileUri, "application/vnd.android.package-archive")

            } else {
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive")
            }
            startActivity(intent)

            //弹出安装窗口把原程序关闭。
            //避免安装完毕点击打开时没反应
            killProcess(android.os.Process.myPid())

        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    companion object {
        private val UPDATE = 201
        private val UNUPDATE = 501
        private val UPDATE_PLAN = 1
    }
}
