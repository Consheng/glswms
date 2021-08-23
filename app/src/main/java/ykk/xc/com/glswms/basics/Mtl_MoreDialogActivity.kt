package ykk.xc.com.glswms.basics

import android.content.Intent
import android.os.Handler
import android.os.Message
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.KeyEvent
import android.view.View
import butterknife.OnClick
import kotlinx.android.synthetic.main.ab_mtl_list_more.*
import okhttp3.*
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.basics.adapter.Mtl_MoreDialogAdapter
import ykk.xc.com.glswms.bean.k3Bean.ICItem_App
import ykk.xc.com.glswms.comm.BaseDialogActivity
import ykk.xc.com.glswms.comm.Comm
import ykk.xc.com.glswms.util.JsonUtil
import ykk.xc.com.glswms.util.basehelper.BaseRecyclerAdapter
import ykk.xc.com.glswms.util.xrecyclerview.XRecyclerView
import java.io.IOException
import java.io.Serializable
import java.lang.ref.WeakReference
import java.util.*

/**
 * 选择部门dialog
 */
class Mtl_MoreDialogActivity : BaseDialogActivity(), XRecyclerView.LoadingListener {

    companion object {
        private val SUCC1 = 200
        private val UNSUCC1 = 501
    }
    private val context = this
    private val listDatas = ArrayList<ICItem_App>()
    private var mAdapter: Mtl_MoreDialogAdapter? = null
    private val okHttpClient = OkHttpClient()
    private var limit = 1
    private var isRefresh: Boolean = false
    private var isLoadMore: Boolean = false
    private var isNextPage: Boolean = false
    private var isICInvBackUp = 0 // 是否查询盘点的物料
    private var stockId = 0 // 盘点的仓库
    private var stockAreaId = 0 // 盘点的库区
    private var storageRackId = 0 // 盘点的货架id
    private var stockPosId = 0 // 盘点的库位id
    private var strMtlId:String? = null // 拼接的物料id


    // 消息处理
    private val mHandler = MyHandler(this)

    private class MyHandler(activity: Mtl_MoreDialogActivity) : Handler() {
        private val mActivity: WeakReference<Mtl_MoreDialogActivity>

        init {
            mActivity = WeakReference(activity)
        }

        override fun handleMessage(msg: Message) {
            val m = mActivity.get()
            if (m != null) {
                m.hideLoadDialog()
                when (msg.what) {
                    SUCC1 // 成功
                    -> {
                        val list = JsonUtil.strToList2(msg.obj as String, ICItem_App::class.java)
                        m.listDatas.addAll(list!!)
                        m.mAdapter!!.notifyDataSetChanged()

                        if (m.isRefresh) {
                            m.xRecyclerView!!.refreshComplete(true)
                        } else if (m.isLoadMore) {
                            m.xRecyclerView!!.loadMoreComplete(true)
                        }

                        m.xRecyclerView!!.isLoadingMoreEnabled = m.isNextPage
                    }
                    UNSUCC1 // 数据加载失败！
                    -> {
                        m.mAdapter!!.notifyDataSetChanged()
                        m.toasts("抱歉，没有加载到数据！")
                    }
                }
            }
        }
    }

    override fun setLayoutResID(): Int {
        return R.layout.ab_mtl_list_more
    }

    override fun initView() {
        xRecyclerView!!.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
        xRecyclerView!!.layoutManager = LinearLayoutManager(context)
        mAdapter = Mtl_MoreDialogAdapter(context, listDatas)
        xRecyclerView!!.adapter = mAdapter
        xRecyclerView!!.setLoadingListener(context)

        xRecyclerView!!.isPullRefreshEnabled = false // 上啦刷新禁用
        xRecyclerView.setLoadingMoreEnabled(false); // 不显示下拉刷新的view

        mAdapter!!.onItemClickListener = BaseRecyclerAdapter.OnItemClickListener { adapter, holder, view, pos ->
            val m = listDatas[pos - 1]
            val isCheck = m.isCheck()
            if (isCheck) {
                m.isCheck = false
            } else {
                m.isCheck = true
            }
            mAdapter!!.notifyDataSetChanged()
        }
    }

    override fun initData() {
        val bundle = context.intent.extras
        if (bundle != null) {
            isICInvBackUp = bundle.getInt("isICInvBackUp")
            stockId = bundle.getInt("stockId")
            stockAreaId = bundle.getInt("stockAreaId")
            storageRackId = bundle.getInt("storageRackId")
            stockPosId = bundle.getInt("stockPosId")
            strMtlId = bundle.getString("strMtlId")
        }

        initLoadDatas()
    }


    // 监听事件
    @OnClick(R.id.btn_close, R.id.btn_search, R.id.btn_confirm)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_close -> {
                closeHandler(mHandler)
                context.finish()
            }
            R.id.btn_search -> initLoadDatas()
            R.id.btn_confirm -> {// 确认
                val size = listDatas.size
                if (size == 0) {
                    Comm.showWarnDialog(context, "请查询数据！")
                    return
                }
                val listMtl = ArrayList<ICItem_App>()
                for (i in 0 until size) {
                    val mtl = listDatas[i]
                    if (mtl.isCheck()) {
                        listMtl.add(mtl)
                    }
                }
                if (listMtl.size == 0) {
                    Comm.showWarnDialog(context, "请至少选择一行数据！")
                    return
                }
                val intent = Intent()
                intent.putExtra("obj", listMtl as Serializable)
                context.setResult(RESULT_OK, intent)
                context.finish()
            }
        }
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
        showLoadDialog("加载中...", false)
        val mUrl = getURL("icItem/findListByPage")
        val formBody = FormBody.Builder()
                .add("fnumberOrName", getValues(et_search).trim())
                // 查询盘点的物料
                .add("isICInvBackUp", isICInvBackUp.toString())
                .add("stockId", stockId.toString())
                .add("stockAreaId", stockAreaId.toString())
                .add("storageRackId", storageRackId.toString())
                .add("stockPosId", stockPosId.toString())
                .add("strMtlId", if(strMtlId != null) strMtlId else "")

                .add("limit", limit.toString())
                .add("pageSize", "30")
                .build()

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
            closeHandler(mHandler)
            context.finish()
        }
        return false
    }

    override fun onDestroy() {
        closeHandler(mHandler)
        super.onDestroy()
    }

}
