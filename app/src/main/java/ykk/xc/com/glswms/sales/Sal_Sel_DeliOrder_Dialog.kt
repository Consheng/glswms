package ykk.xc.com.glswms.sales

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.KeyEvent
import android.view.View
import butterknife.OnClick
import kotlinx.android.synthetic.main.sal_sel_sal_order_dialog.*
import okhttp3.*
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.bean.k3Bean.SeOutStockEntry_App
import ykk.xc.com.glswms.comm.BaseDialogActivity
import ykk.xc.com.glswms.sales.adapter.Sal_Sel_DeliOrder_DialogAdapter
import ykk.xc.com.glswms.util.JsonUtil
import ykk.xc.com.glswms.util.basehelper.BaseRecyclerAdapter
import ykk.xc.com.glswms.util.xrecyclerview.XRecyclerView
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

/**
 * 选择销售订单dialog
 */
class Sal_Sel_DeliOrder_Dialog : BaseDialogActivity(), XRecyclerView.LoadingListener {

    companion object {
        private val SUCC1 = 200
        private val UNSUCC1 = 501
    }

    private val context = this
    private val listDatas = ArrayList<SeOutStockEntry_App>()
    private var mAdapter: Sal_Sel_DeliOrder_DialogAdapter? = null
    private val okHttpClient = OkHttpClient()
    private var limit = 1
    private var isRefresh: Boolean = false
    private var isLoadMore: Boolean = false
    private var isNextPage: Boolean = false
    private var strFitemId :String? = null // 拼接的物料id
    private var finterId = 0    // 销售订单id
//    private var fcustId = 0 // 客户id
    private var returnMinOrder = false  // 是否指定订单出库
    private val strFdetailId_NotIn = StringBuffer()

    // 消息处理
    private val mHandler = MyHandler(this)

    private class MyHandler(activity: Sal_Sel_DeliOrder_Dialog) : Handler() {
        private val mActivity: WeakReference<Sal_Sel_DeliOrder_Dialog>

        init {
            mActivity = WeakReference(activity)
        }

        override fun handleMessage(msg: Message) {
            val m = mActivity.get()
            if (m != null) {
                m.hideLoadDialog()
                when (msg.what) {
                    SUCC1 -> { // 成功
                        val list = JsonUtil.strToList2(msg.obj as String, SeOutStockEntry_App::class.java)
                        m.listDatas.addAll(list!!)
                        // 如果只有一行数据，就直接返回
                        if(m.listDatas.size == 1 || m.returnMinOrder) {
                            m.setResultByFinish(m.listDatas[0])
                        }

                        m.mAdapter!!.notifyDataSetChanged()

                        if (m.isRefresh) {
                            m.xRecyclerView!!.refreshComplete(true)
                        } else if (m.isLoadMore) {
                            m.xRecyclerView!!.loadMoreComplete(true)
                        }

                        m.xRecyclerView!!.isLoadingMoreEnabled = m.isNextPage
                    }
                    UNSUCC1 -> {// 数据加载失败！
                        m.mAdapter!!.notifyDataSetChanged()
                        m.toasts("抱歉，没有加载到数据！")
                    }
                }
            }
        }
    }

    override fun setLayoutResID(): Int {
        return R.layout.sal_sel_sal_order_dialog
    }

    override fun initView() {
        xRecyclerView!!.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        xRecyclerView!!.layoutManager = LinearLayoutManager(context)
        mAdapter = Sal_Sel_DeliOrder_DialogAdapter(context, listDatas)
        xRecyclerView!!.adapter = mAdapter
        xRecyclerView!!.setLoadingListener(context)

        xRecyclerView!!.isPullRefreshEnabled = false // 上啦刷新禁用
        //        xRecyclerView.setLoadingMoreEnabled(false); // 不显示下拉刷新的view

        mAdapter!!.onItemClickListener = BaseRecyclerAdapter.OnItemClickListener { adapter, holder, view, pos ->
            val m = listDatas[pos - 1]
            /*val list = ArrayList<SeOutStockEntry_App>()
            listDatas.forEach {
                if(m.finterId == it.finterId) {
                    list.add(it)
                }
            }*/
            setResultByFinish(m)
        }
    }

    override fun initData() {
        val bundle = context.intent.extras
        if (bundle != null) {
            strFitemId = bundle.getString("strFitemId")
            finterId = bundle.getInt("finterId")
//            fcustId = bundle.getInt("fcustId")
            returnMinOrder = bundle.getBoolean("returnMinOrder")

            if(bundle.containsKey("listDetailId")) {
                val listDetailId = bundle.getSerializable("listDetailId") as ArrayList<Int>
                if (listDetailId != null && listDetailId.size > 0) {
                    listDetailId.forEach {
                        strFdetailId_NotIn.append(it.toString() + ",")
                    }
                    // 去掉最后一个，
                    strFdetailId_NotIn.delete(strFdetailId_NotIn.length - 1, strFdetailId_NotIn.length)
                }
            }
        }

        initLoadDatas()
    }


    // 监听事件
    @OnClick(R.id.btn_close)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_close -> {
                closeHandler(mHandler)
                context.finish()
            }
        }
    }

    /**
     *  返回给上个页面
     */
    /*private fun setResultByFinish(list :ArrayList<SeOutStockEntry_App>) {
        val intent = Intent()
        intent.putExtra("obj", list)
        context.setResult(Activity.RESULT_OK, intent)
        context.finish()
    }*/

    /**
     *  返回给上个页面
     */
    private fun setResultByFinish(m :SeOutStockEntry_App) {
        val intent = Intent()
        intent.putExtra("obj", m)
        context.setResult(Activity.RESULT_OK, intent)
        context.finish()
    }

    private fun initLoadDatas() {
        limit = 1
        listDatas.clear()
        run_okhttpDatas()
    }

    /**
     * 通过okhttp加载数据
     */
    private fun run_okhttpDatas() {
        val formBody = FormBody.Builder()
                .add("strFitemId", strFitemId)
                .add("finterId", finterId.toString())
//                .add("fcustId", fcustId.toString())
                .add("fqtyGt0", "1")    // 查询有效数量的订单
                .add("fstatus", "1,2")    // 已审核的单据
                .add("fclosed", "0")    // 未关闭的单据头
                .add("strFdetailId_NotIn", strFdetailId_NotIn.toString())  // 单据中唯一id
                .add("sortWay", "ASC")  // 顺序排列
                .add("limit", limit.toString())
                .add("pageSize", "30")
                .build()
        showLoadDialog("加载中...", false)
        val mUrl = getURL("deliOrder/findListByPage")

        val request = Request.Builder()
                .addHeader("cookie", session)
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient.newCall(request)
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
                isNextPage = JsonUtil.isNextPage(result)

                val msg = mHandler.obtainMessage(SUCC1, result)
                Log.e("run_okhttpDatas --> onResponse", result)
                mHandler.sendMessage(msg)
            }
        })
    }

    override fun onRefresh() {
        isRefresh = true
        isLoadMore = false
        initLoadDatas()
    }

    override fun onLoadMore() {
        isRefresh = false
        isLoadMore = true
        limit += 1
        run_okhttpDatas()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            context.finish()
        }
        return false
    }

    override fun onDestroy() {
        closeHandler(mHandler)
        super.onDestroy()
    }

}
