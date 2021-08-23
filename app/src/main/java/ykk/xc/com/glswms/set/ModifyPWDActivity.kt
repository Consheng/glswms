package ykk.xc.com.glswms.set

import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import butterknife.OnClick
import kotlinx.android.synthetic.main.login_modify_pwd.*
import okhttp3.*
import ykk.xc.com.glswms.LoginKt
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.bean.User
import ykk.xc.com.glswms.comm.ActivityCollector
import ykk.xc.com.glswms.comm.BaseActivity
import ykk.xc.com.glswms.comm.Comm
import ykk.xc.com.glswms.util.JsonUtil
import java.io.IOException
import java.lang.ref.WeakReference

/**
 * 修改密码界面
 */
class ModifyPWDActivity : BaseActivity() {

    companion object {
        private val SUCC1 = 200
        private val UNSUCC1 = 500
    }
    private val context = this

    private val okHttpClient = OkHttpClient()
    private val result: String? = null
    private var user: User? = null

    // 消息处理
    private val mHandler = MyHandler(this)
    private class MyHandler(activity: ModifyPWDActivity) : Handler() {
        private val mActivity: WeakReference<ModifyPWDActivity>

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
                    SUCC1 -> {// 确认成功
                        // 修改好了密码就清空密码，到登录界面去输入
                        m.user!!.password = ""
                        // user对象保存到xml
                        m.saveUserToXml(m.user)

                        // 把之前打开所以界面都关闭
                        ActivityCollector.finishAll()
                        // 打开登录界面
                        m.show(LoginKt::class.java, null)

                    }
                    UNSUCC1 -> {// 登录失败！
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "服务器繁忙，请稍后再试！"
                        Comm.showWarnDialog(m.context, errMsg)
                    }
                }
            }
        }
    }

    override fun setLayoutResID(): Int {
        return R.layout.login_modify_pwd
    }

    override fun initData() {
        getUserInfo()
    }

    @OnClick(R.id.btn_close, R.id.btn_save)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_close -> { // 服务器设置
                context.finish()
            }
            R.id.btn_save -> {// 确认修改
                val pwd = getValues(et_pwd).trim()
                if (pwd.length == 0) {
                    Comm.showWarnDialog(context,"请输入原密码！！")
                    return
                }
                val pwd2 = getValues(et_pwd2).trim()
                if (pwd2.length == 0) {
                    Comm.showWarnDialog(context,"请输入新密码！")
                    toasts("请输入新密码！")
                    return
                }
                val pwd3 = getValues(et_pwd3).trim()
                if (pwd3.length == 0) {
                    Comm.showWarnDialog(context,"请输入确认密码！")
                    return
                }
                hideKeyboard(currentFocus)
                run_save()
            }
        }
    }

    /**
     * 保存修改
     */
    private fun run_save() {
        showLoadDialog("操作中...")
        val mUrl = getURL("user/updatePassword_App")
        val formBody = FormBody.Builder()
                .add("userId", user!!.id.toString())
                .add("oldPassword", getValues(et_pwd).trim())
                .add("newPassword", getValues(et_pwd2).trim())
                .add("confirmPassword", getValues(et_pwd3).trim())
                .build()
        val request = Request.Builder()
                .addHeader("cookie", getSession())
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
                Log.e("run_save --> onResponse", result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 得到用户对象
     */
    private fun getUserInfo() {
        if (user == null) user = showUserByXml()
    }

    override fun onDestroy() {
        closeHandler(mHandler)
        super.onDestroy()
    }

}
