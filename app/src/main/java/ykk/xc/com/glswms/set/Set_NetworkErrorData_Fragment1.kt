package ykk.xc.com.glswms.set

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import kotlinx.android.synthetic.main.set_network_error_data_fragment1.*
import okhttp3.*
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.bean.ICStockBill_App
import ykk.xc.com.glswms.bean.User
import ykk.xc.com.glswms.comm.BaseFragment
import ykk.xc.com.glswms.comm.Comm
import ykk.xc.com.glswms.set.adapter.Set_NetworkErrorData_Fragment1_Adapter
import ykk.xc.com.glswms.util.JsonUtil
import ykk.xc.com.glswms.util.LogUtil
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

/**
 * 日期：2019-10-16 09:50
 * 描述：网络异常数据---查询列表
 * 作者：ykk
 */
class Set_NetworkErrorData_Fragment1 : BaseFragment() {

    companion object {
        private val SUCC1 = 201
        private val UNSUCC1 = 501
        private val SAVE = 202
        private val UNSAVE = 502

    }

    private val context = this
    private var okHttpClient: OkHttpClient? = null
    private var user: User? = null
    private var mContext: Activity? = null
    private var parent: Set_NetworkErrorData_MainActivity? = null
    private var mAdapter: Set_NetworkErrorData_Fragment1_Adapter? = null
    private val listDatas = java.util.ArrayList<ICStockBill_App>()
    private var timesTamp: String? = null // 时间戳

    // 消息处理
    private val mHandler = MyHandler(this)

    private class MyHandler(activity: Set_NetworkErrorData_Fragment1) : Handler() {
        private val mActivity: WeakReference<Set_NetworkErrorData_Fragment1>

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
                    SUCC1 -> { // 扫码成功 进入
                        m.listDatas.clear()
                        val list = JsonUtil.strToList(msgObj, ICStockBill_App::class.java)
                        m.listDatas.addAll(list)

                        m.mAdapter!!.notifyDataSetChanged()
                        m.btn_clear.visibility = View.VISIBLE
                    }
                    UNSUCC1 -> { // 扫码失败
                        m.btn_clear.visibility = View.GONE
                        m.listDatas.clear()
                        m.mAdapter!!.notifyDataSetChanged()
                        m.toasts("很抱歉，没有找到数据！")
                    }
                    SAVE -> { // 清理数据 进入
                        m.btn_clear.visibility = View.GONE
                        m.listDatas.clear()
                        m.mAdapter!!.notifyDataSetChanged()
                        m.toasts("清理成功")
                    }
                    UNSAVE -> { // 清理数据  失败
                        Comm.showWarnDialog(m.mContext,"操作有误，请稍后再试！")
                    }
                }
            }
        }
    }

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.set_network_error_data_fragment1, container, false)
    }

    override fun initView() {
        mContext = getActivity()
        parent = mContext as Set_NetworkErrorData_MainActivity

        recyclerView.addItemDecoration(DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(mContext)
        mAdapter = Set_NetworkErrorData_Fragment1_Adapter(mContext!!, listDatas)
        recyclerView.adapter = mAdapter
        // 设值listview空间失去焦点
        recyclerView.isFocusable = false
    }

    override fun initData() {
        if (okHttpClient == null) {
            okHttpClient = OkHttpClient.Builder()
                    //                .connectTimeout(10, TimeUnit.SECONDS) // 设置连接超时时间（默认为10秒）
                    .writeTimeout(120, TimeUnit.SECONDS) // 设置写的超时时间
                    .readTimeout(120, TimeUnit.SECONDS) //设置读取超时时间
                    .build()
        }

        getUserInfo()
        timesTamp = user!!.getId().toString() + "-" + Comm.randomUUID()

        run_findList()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
        }
    }

    @OnClick(R.id.btn_clear)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_clear -> { // 一键清空
                val sbId = StringBuffer()
                listDatas.forEach {
                    if(sbId.length == 0) sbId.append(it.id)
                    else sbId.append(","+it.id)
                }
                run_clear(sbId.toString())
            }
        }
    }

    fun search() {
        run_findList()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            /*SEL_MTL -> { //查询物料	返回
                if (resultCode == Activity.RESULT_OK) {
                    val icItem = data!!.getSerializableExtra("obj") as ICItem
                    getMtlAfter(icItem)
                }
            }*/
        }
    }

    /**
     * 扫码查询对应的方法
     */
    private fun run_findList() {
        showLoadDialog("加载中...", false)
        val mUrl = getURL("stockBill_WMS/findList")
        val formBody = FormBody.Builder()
                .add("createUserId", user!!.id.toString())
                .add("isToK3", "0") // 查询未上传的
                .add("childSize", "0") // 没有分录的单据
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNSUCC1)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                LogUtil.e("run_findList --> onResponse", result)
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNSUCC1, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(SUCC1, result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 清除数据
     */
    private fun run_clear(strId: String) {
        showLoadDialog("清理中...", false)
        val mUrl = getURL("stockBill_WMS/removeByListId")
        val formBody = FormBody.Builder()
                .add("strId", strId)
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNSAVE)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNSAVE, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(SAVE, result)
                LogUtil.e("run_clear --> onResponse", result)
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

    override fun onDestroyView() {
        closeHandler(mHandler)
        mBinder!!.unbind()
        super.onDestroyView()
    }
}