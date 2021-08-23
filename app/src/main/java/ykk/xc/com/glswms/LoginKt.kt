package ykk.xc.com.glswms

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import butterknife.OnClick
import kotlinx.android.synthetic.main.login.*
import okhttp3.*
import ykk.xc.com.glswms.bean.User
import ykk.xc.com.glswms.comm.BaseActivity
import ykk.xc.com.glswms.comm.Comm
import ykk.xc.com.glswms.comm.Consts
import ykk.xc.com.glswms.entrance.MainTabFragmentActivity
import ykk.xc.com.glswms.entrance.page5.ServiceSetActivity
import ykk.xc.com.glswms.util.JsonUtil
import java.io.File
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

/**
 * 登录界面
 */
class LoginKt : BaseActivity() {

    private val context = this

    private val okHttpClient = OkHttpClient()
    private val result: String? = null
    private var isEntryPermission = false // 是否打开了设置权限的页面


    // 消息处理
    private val mHandler = MyHandler(this)
    private class MyHandler(activity: LoginKt) : Handler() {
        private val mActivity: WeakReference<LoginKt>

        init {
            mActivity = WeakReference(activity)
        }

        override fun handleMessage(msg: Message) {
            val m = mActivity.get()
            if (m != null) {
                m.hideLoadDialog()

                var msgObj: String? = null
                if (msg.obj is String) {
                    msgObj = msg.obj as String
                }
                when (msg.what) {
                    SUCC1 -> {// 登录成功
                        val user = JsonUtil.strToObject(msg.obj as String, User::class.java)
                        user!!.password = m.getValues(m.et_pwd).trim { it <= ' ' }
                        // user对象保存到xml
                        m.saveUserToXml(user)
                        // 保存系统参数设置信息到Xml
                        /*val systemSets: List<SystemSet>? = null
                        if (systemSets != null && systemSets.size > 0) {
                            val sp = m.context.getSharedPreferences(m.getResStr(R.string.saveSystemSet), Context.MODE_PRIVATE)
                            val editor = sp.edit()
                            for (i in systemSets.indices) {
                                val sysSet = systemSets[i]
                                editor.putString(sysSet.setItem.name, sysSet.value.toString())
                            }
                            editor.commit()
                        }*/
                        m.show(MainTabFragmentActivity::class.java, null)
                        m.context.finish()
                    }
                    UNSUCC1 -> { // 登录失败！
                        val str = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(str).length > 0) {
                            m.toasts(str)
                        } else {
                            m.toasts("服务器繁忙，请稍候再试！")
                        }
                    }
                }
            }
        }
    }

    override fun setLayoutResID(): Int {
        return R.layout.login
    }

    override fun initData() {
        val spfConfig = spf(getResStr(R.string.saveConfig))
        val ip = spfConfig.getString("ip", "192.168.3.198")
        val port = spfConfig.getString("port", "8080")
        Consts.setIp(ip)
        Consts.setPort(port)
        // 保存在xml中的对象
        val user = showUserByXml()
        if (user != null) {
            setTexts(et_userName, user.username)
            setTexts(et_pwd, user.password)
        }
        requestPermission()
    }

    override fun onResume() {
        super.onResume()
        if (isEntryPermission) {
            isEntryPermission = false
            requestPermission()
        }
    }

    @OnClick(R.id.btn_set, R.id.btn_login)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_set -> {// 服务器设置
                show(ServiceSetActivity::class.java, null)
            }
            R.id.btn_login -> {// 登录
                val userName = getValues(et_userName).trim { it <= ' ' }
                if (userName.length == 0) {
                    toasts("请输入账号！")
                    return
                }
                val pwd = getValues(et_pwd).trim { it <= ' ' }
                if (pwd.length == 0) {
                    toasts("请输入密码！")
                    return
                }
                hideKeyboard(currentFocus)
                run_appLogin()
            }
        }
    }

    /**
     * 登录的方法
     */
    private fun run_appLogin() {
        showLoadDialog("登录中...", false)
        val mUrl = getURL("appLogin")
        val formBody = FormBody.Builder()
                .add("username", getValues(et_userName).trim { it <= ' ' })
                .add("password", getValues(et_pwd).trim { it <= ' ' })
                .build()
        val request = Request.Builder()
                .url(mUrl)
                .post(formBody)
                .build()
        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNSUCC1)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNSUCC1, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(SUCC1, result)
                Log.e("run_appLogin --> onResponse", result)
                mHandler.sendMessage(msg)

                //获取session的操作，session放在cookie头，且取出后含有“；”，取出后为下面的 s （也就是jsesseionid）
                val headers = response.headers()
                Log.d("info_headers", "header $headers")
                val cookies = headers.values("Set-Cookie")
                val session = cookies[0]
                Log.d("info_cookies", "onResponse-size: $cookies")
                val s = session.substring(0, session.indexOf(";"))
                // 保存到xml中
                val spfOther = spf(getResStr(R.string.saveOther))
                spfOther.edit().putString("session", s).commit()


            }
        })
    }

    /**
     * 请求用户授权SD卡读写权限
     */
    private fun requestPermission() {
        // 判断sdk是是否大于等于6.0
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)

            // 逐个判断哪些权限未授权，将未授权的权限存储到mPermissionList中
            val mPermissionList = ArrayList<String>()

            mPermissionList.clear()//清空已经允许的没有通过的权限

            //逐个判断是否还有未通过的权限
            for (i in permissions.indices) {
                if (checkSelfPermission(permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i])//添加还未授予的权限到mPermissionList中
                }
            }

            //申请权限
            if (mPermissionList.size > 0) {//有权限没有通过，需要申请
                requestPermissions(permissions, REQUESTCODE)
            } else {
                //权限已经都通过了，可以将程序继续打开了
                createFile()
            }

            // 之前的写法：    读写文件
            //            int checkSelfPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            //            if (checkSelfPermission != PackageManager.PERMISSION_DENIED) {
            //                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUESTCODE);
            //
            //            } else {
            //                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUESTCODE);
            //            }
        } else { // 6.0以下，直接执行
            createFile()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUESTCODE // 读写权限
            -> {
                var hasPermissionDismiss = false//有权限没有通过
                for (i in grantResults.indices) {
                    if (grantResults[i] == -1) {
                        hasPermissionDismiss = true
                        break
                    }
                }
                if (hasPermissionDismiss) {//如果有没有被允许的权限
                    showPermissionDialog()
                } else {
                    //权限已经都通过了
                    createFile()
                }
            }
        }// 之前的写法
        //                if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE) && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
        //                    //用户同意
        //                    createFile();
        //                } else {
        //                    //用户不同意
        //                    AlertDialog alertDialog = new AlertDialog.Builder(context)
        //                            .setTitle("授权提示").setMessage("您已禁用了SD卡的读写权限,会导致部分功能不能用，去打开吧！")
        //                            .setPositiveButton("好的", new DialogInterface.OnClickListener() {
        //                                public void onClick(DialogInterface dialog, int which) {
        //                                    Intent mIntent = new Intent();
        //                                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //                                    if (Build.VERSION.SDK_INT >= 9) {
        //                                        mIntent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        //                                        mIntent.setData(Uri.fromParts("package", context.getPackageName(), null));
        //                                    } else if (Build.VERSION.SDK_INT <= 8) {
        //                                        mIntent.setAction(Intent.ACTION_VIEW);
        //                                        mIntent.setClassName("com.android.settings", "com.android.setting.InstalledAppDetails");
        //                                        mIntent.putExtra("com.android.settings.ApplicationPkgName", context.getPackageName());
        //                                    }
        //
        //                                    context.startActivity(mIntent);
        //                                    isEntryPermission = true;
        //                                }
        //                            })
        ////                        .setNegativeButton("不了", null)
        //                            .create();// 创建
        //                    alertDialog.setCancelable(false);
        //                    alertDialog.setCanceledOnTouchOutside(false);
        //                    alertDialog.show();// 显示
        //                }
    }

    private fun showPermissionDialog() {
        //用户不同意
        val alertDialog = AlertDialog.Builder(context)
                .setTitle("授权提示").setMessage("您已禁用了部分权限，请手动授予！")
                .setPositiveButton("好的") { dialog, which ->
                    val mIntent = Intent()
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    if (Build.VERSION.SDK_INT >= 9) {
                        mIntent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
                        mIntent.data = Uri.fromParts("package", context.packageName, null)
                    } else if (Build.VERSION.SDK_INT <= 8) {
                        mIntent.action = Intent.ACTION_VIEW
                        mIntent.setClassName("com.android.settings", "com.android.setting.InstalledAppDetails")
                        mIntent.putExtra("com.android.settings.ApplicationPkgName", context.packageName)
                    }

                    context.startActivity(mIntent)
                    isEntryPermission = true
                }
                //                        .setNegativeButton("不了", null)
                .create()// 创建
        alertDialog.setCancelable(false)
        alertDialog.setCanceledOnTouchOutside(false)
        alertDialog.show()// 显示
    }

    private fun createFile() {
        val file = File(Comm.publicPaths + "updateFile")
        if (!file.exists()) {
            val isSuccess = file.mkdirs()
            Log.d("isSuccess:", "----------0------------------$isSuccess")
        }
    }

    override fun onDestroy() {
        closeHandler(mHandler)
        super.onDestroy()
    }

    companion object {
        private val SUCC1 = 200
        private val UNSUCC1 = 500
        private val REQUESTCODE = 101
    }
}
