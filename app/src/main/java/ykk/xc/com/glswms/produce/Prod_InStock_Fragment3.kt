package ykk.xc.com.glswms.produce

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
import kotlinx.android.synthetic.main.prod_in_stock_fragment3.*
import kotlinx.android.synthetic.main.prod_in_stock_main.*
import okhttp3.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.bean.EventBusEntity
import ykk.xc.com.glswms.bean.ICStockBillEntry_App
import ykk.xc.com.glswms.bean.User
import ykk.xc.com.glswms.comm.BaseFragment
import ykk.xc.com.glswms.comm.Comm
import ykk.xc.com.glswms.produce.adapter.Prod_InStock_Fragment3_Adapter
import ykk.xc.com.glswms.util.BigdecimalUtil
import ykk.xc.com.glswms.util.JsonUtil
import ykk.xc.com.glswms.util.LogUtil
import ykk.xc.com.glswms.util.basehelper.BaseRecyclerAdapter
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 日期：2019-10-16 09:50
 * 描述：生产入库
 * 作者：ykk
 */
class Prod_InStock_Fragment3 : BaseFragment() {

    companion object {
        private val SUCC1 = 200
        private val UNSUCC1 = 500
        private val DELETE = 201
        private val UNDELETE = 501
        private val UPLOAD = 202
        private val UNUPLOAD = 502
    }
    private val context = this
    private var parent: Prod_InStock_MainActivity? = null

    val checkDatas = ArrayList<ICStockBillEntry_App>()
    private var okHttpClient: OkHttpClient? = null
    private var mAdapter: Prod_InStock_Fragment3_Adapter? = null
    private var user: User? = null
    private var mContext: Activity? = null
    private var curPos:Int = -1 // 当前行
    private var timesTamp:String? = null // 时间戳
    private val df = DecimalFormat("#.######")

    // 消息处理
    private val mHandler = MyHandler(this)

    private class MyHandler(activity: Prod_InStock_Fragment3) : Handler() {
        private val mActivity: WeakReference<Prod_InStock_Fragment3>

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
                    SUCC1 -> { // 查询分录 进入
                        m.checkDatas.clear()
                        val list = JsonUtil.strToList(msgObj, ICStockBillEntry_App::class.java)
                        m.checkDatas.addAll(list)

                        var sumNum = 0.0
                        var sumMoney = 0.0
                        list.forEach() {
                            sumNum += it.fqty
                            val mul = BigdecimalUtil.mul(it.fqty, it.fprice)
                            sumMoney += mul
                        }
                        m.tv_sumNum.text = m.df.format(sumNum)
                        m.tv_sumMoney.text = m.df.format(sumMoney)

                        m.mAdapter!!.notifyDataSetChanged()
                    }
                    UNSUCC1 -> { // 查询分录  失败
                        m.tv_sumNum.text = "0"
                        m.tv_sumMoney.text = "0"
                    }
                    DELETE -> { // 删除分录 进入
                        m.run_findEntryList()
                    }
                    UNDELETE -> { // 删除分录  失败
                        Comm.showWarnDialog(m.mContext,"服务器繁忙，请稍后再试！")
                    }
                    UPLOAD -> { // 上传单据 进入
                        val retMsg = JsonUtil.strToString(msgObj)
                        if(retMsg.length > 0) {
                            Comm.showWarnDialog(m.mContext, retMsg+"单，上传的数量大于源单可入库数，不能上传！")
                        } else {
                            // 滑动第一个页面
                            m.parent!!.viewPager!!.setCurrentItem(0, false)
                            m.parent!!.fragment1.reset() // 重置
                            m.toasts("上传成功")
                        }
                    }
                    UNUPLOAD -> { // 上传单据  失败
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "服务器繁忙，请稍后再试！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                }
            }
        }
    }

    @Subscribe
    fun onEventBus(entity: EventBusEntity) {
        when (entity.caseId) {
            12,21 -> { // 接收第一个页面（12）发来的指令，接收第二个页面（21）发来的指令
                run_findEntryList()
            }
        }
    }

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.prod_in_stock_fragment3, container, false)
    }

    override fun initView() {
        mContext = getActivity()
        parent = mContext as Prod_InStock_MainActivity

        recyclerView.addItemDecoration(DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(mContext)
        mAdapter = Prod_InStock_Fragment3_Adapter(mContext!!, checkDatas)
        recyclerView.adapter = mAdapter
        // 设值listview空间失去焦点
        recyclerView.isFocusable = false

        // 行事件
        mAdapter!!.setCallBack(object : Prod_InStock_Fragment3_Adapter.MyCallBack {
            override fun onDelete(entity: ICStockBillEntry_App, position: Int) {
                curPos = position
                run_removeEntry(entity.id)
            }
        })

        mAdapter!!.onItemClickListener = BaseRecyclerAdapter.OnItemClickListener { adapter, holder, view, pos ->
            EventBus.getDefault().post(EventBusEntity(31, checkDatas[pos]))
            // 滑动第二个页面
            parent!!.viewPager!!.setCurrentItem(1, false)
        }

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
        EventBus.getDefault().register(this)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
        }
    }

    @OnClick(R.id.btn_upload)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_upload -> { // 上传
                val size = checkDatas.size
                if(size == 0) {
                    Comm.showWarnDialog(mContext,"没有分录信息，不能上传！")
                    return
                }
                checkDatas.forEachIndexed { index, it ->
                    if(it.fdcStockId == 0) {
                        Comm.showWarnDialog(mContext,"第（"+(index+1)+"）行，请选择仓库信息！")
                        return
                    }
                    if(it.stock.fisStockMgr == 1 && it.fdcSPId == 0) {
                        Comm.showWarnDialog(mContext,"第（"+(index+1)+"）行，请选择仓位信息！")
                        return
                    }
                    if(it.fqty == 0.0) {
                        Comm.showWarnDialog(mContext,"第（"+(index+1)+"）行，请扫码或输入（入库数）！")
                        return
                    }
                    if(it.fsourceTranType > 0 && it.fqty > it.fsourceQty) {
                        Comm.showWarnDialog(mContext,"第（"+(index+1)+"）行，入库数量不能大于源单数！")
                        return
                    }
                }

                run_uploadToK3(parent!!.fragment1.icstockBill.toString())
            }
        }
    }

    override fun setListener() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
            }
        }
    }

    /**
     * 历史查询
     */
    private fun run_findEntryList() {
        showLoadDialog("加载中...", false)
        val mUrl = getURL("stockBill_WMS/findEntryList")
        val formBody = FormBody.Builder()
                .add("icstockBillId", parent!!.fragment1.icstockBill.id.toString())
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
                LogUtil.e("run_findEntryList --> onResponse", result)
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
     * 删除
     */
    private fun run_removeEntry(id : Int) {
        showLoadDialog("加载中...", false)
        val mUrl = getURL("stockBill_WMS/removeEntry")
        val formBody = FormBody.Builder()
                .add("id", id.toString())
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNDELETE)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                LogUtil.e("run_findEntryList --> onResponse", result)
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNDELETE, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(DELETE, result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 上传单据
     */
    private fun run_uploadToK3(icstockBillId : String) {
        showLoadDialog("加载中...", false)
        val mUrl = getURL("stockBill_WMS/uploadToK3")
        val formBody = FormBody.Builder()
                .add("strId", icstockBillId)
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNUPLOAD)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                LogUtil.e("run_uploadToK3 --> onResponse", result)
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNUPLOAD, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(UPLOAD, result)
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
        EventBus.getDefault().unregister(this)
        super.onDestroyView()
    }
}