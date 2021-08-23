package ykk.xc.com.glswms.warehouse

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.ware_outin_stock_search_fragment2__other_out_stock.*
import kotlinx.android.synthetic.main.ware_outin_stock_search_main.*
import okhttp3.*
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.bean.ICStockBill_App
import ykk.xc.com.glswms.bean.User
import ykk.xc.com.glswms.comm.BaseFragment
import ykk.xc.com.glswms.comm.Comm
import ykk.xc.com.glswms.util.JsonUtil
import ykk.xc.com.glswms.util.LogUtil
import ykk.xc.com.glswms.util.basehelper.BaseRecyclerAdapter
import ykk.xc.com.glswms.warehouse.adapter.OutInStockSearchFragment2_XSCK_Adapter
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 日期：2019-10-16 09:50
 * 描述：销售退货查询
 * 作者：ykk
 */
class OutInStock_Search_Fragment2_XSCK : BaseFragment() {

    companion object {
        private val SUCC1 = 200
        private val UNSUCC1 = 500
        private val DELETE = 201
        private val UNDELETE = 501
        private val UPLOAD = 202
        private val UNUPLOAD = 502

        private val VISIBLE = 1
    }
    private val context = this
    private var parent: OutInStock_Search_MainActivity? = null
    private val checkDatas = ArrayList<ICStockBill_App>()
    private var okHttpClient: OkHttpClient? = null
    private var mAdapter: OutInStockSearchFragment2_XSCK_Adapter? = null
    private var user: User? = null
    private var mContext: Activity? = null
    private var curPos:Int = -1 // 当前行
    private var timesTamp:String? = null // 时间戳
    private val df = DecimalFormat("#.######")
    var isInit = false
    var isLoadData = false

    // 消息处理
    private val mHandler = MyHandler(this)

    private class MyHandler(activity: OutInStock_Search_Fragment2_XSCK) : Handler() {
        private val mActivity: WeakReference<OutInStock_Search_Fragment2_XSCK>

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
                    SUCC1 -> { // 查询单据 进入
                        m.checkDatas.clear()
                        val list = JsonUtil.strToList(msgObj, ICStockBill_App::class.java)
                        m.checkDatas.addAll(list)

                        m.mAdapter!!.notifyDataSetChanged()
                    }
                    UNSUCC1 -> { // 查询单据  失败
                        m.checkDatas.clear()
                        m.mAdapter!!.notifyDataSetChanged()
                        m.toasts("很抱歉，没有找到数据！")
                    }
                    DELETE -> { // 删除单据 进入
                        m.curPos = -1
                        m.run_findList()
                    }
                    UNDELETE -> { // 删除单据  失败
                        Comm.showWarnDialog(m.mContext,"服务器繁忙，请稍后再试！")
                    }
                    UPLOAD -> { // 上传单据 进入
                        val retMsg = JsonUtil.strToString(msgObj)
                        m.toasts("上传成功")
                        m.run_findList()
                    }
                    UNUPLOAD -> { // 上传单据  失败
                        Comm.showWarnDialog(m.mContext,"服务器繁忙，请稍后再试！")
                    }
                    VISIBLE -> m.parent!!.btn_batchUpload.visibility = View.GONE
                }
            }
        }
    }

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.ware_outin_stock_search_fragment2__other_out_stock, container, false)
    }

    override fun initView() {
        mContext = getActivity()
        parent = mContext as OutInStock_Search_MainActivity

        recyclerView.addItemDecoration(DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(mContext)
        mAdapter = OutInStockSearchFragment2_XSCK_Adapter(mContext!!, checkDatas)
        recyclerView.adapter = mAdapter
        // 设值listview空间失去焦点
        recyclerView.isFocusable = false

        // 行事件
        mAdapter!!.setCallBack(object : OutInStockSearchFragment2_XSCK_Adapter.MyCallBack {
            override fun onSearch(entity: ICStockBill_App, position: Int) {
                val bundle = Bundle()
                bundle.putInt("id", entity.id)
//                show(Sal_DS_OutStock_MainActivity::class.java, bundle)
            }
            override fun onUpload(entity: ICStockBill_App, position: Int) {
                val list = ArrayList<ICStockBill_App>()
                list.add(entity)
                val strJson = JsonUtil.objectToString(list)
                run_uploadToK3(strJson)
            }
            override fun onDelete(entity: ICStockBill_App, position: Int) {
                curPos = position
                run_remove(entity.id)
            }
        })

        mAdapter!!.onItemClickListener = BaseRecyclerAdapter.OnItemClickListener { adapter, holder, view, pos ->
            if(curPos > -1) {
                checkDatas[curPos].isShowButton = false
            }
            curPos = pos
            checkDatas[pos].isShowButton = true
            mAdapter!!.notifyDataSetChanged()
            // 如果是最后一个item,点击就滑动到最下面
            if(checkDatas.size > 2 && checkDatas.size-1 == pos) {
                recyclerView.post(Runnable { recyclerView.smoothScrollToPosition(pos) })
            }
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
        isInit = true

//        run_findList()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if(isVisibleToUser) {
            // 隐藏上传按钮
            mHandler.sendEmptyMessageDelayed(VISIBLE, 200)
            if( isInit && !isLoadData) {
                findFun()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(userVisibleHint == true) {
            findFun()
        }
    }

    /**
     * 查询
     */
    fun findFun() {
        isLoadData = true
        curPos = -1
        run_findList()
    }

    /**
     * 一键上传
     */
    fun batchUpload() {
        if(checkDatas.size > 0) {
            val build = AlertDialog.Builder(mContext)
            build.setIcon(R.drawable.caution)
            build.setTitle("系统提示")
            build.setMessage("您确定要全部上传吗？")
            build.setPositiveButton("是") { dialog, which ->
                val strJson = JsonUtil.objectToString(checkDatas)
                run_uploadToK3(strJson)
            }
            build.setNegativeButton("否", null)
            build.setCancelable(false)
            build.show()
        }
    }

//    @OnClick(R.id.tv_deptSel, R.id.tv_suppSel, R.id.tv_stockSel, R.id.btn_scan_stockPos, R.id.btn_stockPosSel, R.id.btn_scan, R.id.btn_mtlSel, R.id.btn_save, R.id.btn_clone)
//    fun onViewClicked(view: View) {
//        var bundle: Bundle? = null
//        when (view.id) {
//            R.id.tv_deptSel -> { // 选择部门
//                showForResult(Dept_DialogActivity::class.java, SEL_DEPT, null)
//            }
//            R.id.tv_suppSel -> { // 选择供应商
//                showForResult(Supplier_DialogActivity::class.java, SEL_SUPP, null)
//            }
//            R.id.tv_stockSel -> { // 选择收货仓库
//                showForResult(Stock_DialogActivity::class.java, SEL_STOCK, null)
//            }
//            R.id.btn_stockPosSel -> { // 选择调出库位
//                if(!checkSelStockPos()) return
//                var bundle = Bundle()
//                bundle.putInt("fspGroupId", stock!!.fspGroupId);
//                showForResult(StockPos_DialogActivity::class.java, SEL_STOCKPOS, bundle)
//            }
//            R.id.btn_mtlSel -> { // 选择物料
//                if(!checkSelStockPos()) return
//                bundle = Bundle()
//                bundle.putInt("fStockId", stock!!.fitemId)
//                bundle.putInt("fStockPlaceID", if(stockPos != null) stockPos!!.fspId else 0)
//                showForResult(StockTransfer_Material_DialogActivity::class.java, SEL_MTL, bundle)
//            }
//            R.id.btn_scan_stockPos -> { // 调用摄像头扫描（调出库位）
//                if(!checkSelStockPos()) return
//                showForResult(CaptureActivity::class.java, BaseFragment.CAMERA_SCAN, null)
//            }
//            R.id.btn_scan -> { // 调用摄像头扫描（物料）
//                if(!checkSelStockPos()) return
//                showForResult(CaptureActivity::class.java, BaseFragment.CAMERA_SCAN, null)
//            }
//            R.id.btn_save -> { // 保存
//                if(checkDatas.size == 0) {
//                    Comm.showWarnDialog(mContext,"请选择物料或者扫码条码！")
//                    return
//                }
//                run_save();
//            }
//            R.id.btn_clone -> { // 重置
//                if (parent!!.isChange) {
//                    val build = AlertDialog.Builder(mContext)
//                    build.setIcon(R.drawable.caution)
//                    build.setTitle("系统提示")
//                    build.setMessage("您有未保存的数据，继续重置吗？")
//                    build.setPositiveButton("是") { dialog, which -> reset(true) }
//                    build.setNegativeButton("否", null)
//                    build.setCancelable(false)
//                    build.show()
//
//                } else {
//                    reset(true)
//                }
//            }
//        }
//    }

    override fun setListener() {

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
//            SEL_MTL //查询物料	返回
//            -> if (resultCode == Activity.RESULT_OK) {
//                val list = data!!.getSerializableExtra("obj") as List<ICInventory>
//
//                getMtlAfter(list)
//            }

        }
    }

    /**
     * 查询单据
     */
    private fun run_findList() {
        showLoadDialog("加载中...")
        val mUrl = getURL("stockBill_WMS/findList")
        val formBody = FormBody.Builder()
                .add("createUserId", user!!.id.toString())
                .add("isToK3", "0") // 查询未上传的
                .add("billType", "XSCK") // 根据不同类型查询
                .add("childSize", "1") // 必须有分录的单据
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
     * 删除单据
     */
    private fun run_remove(id : Int) {
        showLoadDialog("加载中...", false)
        val mUrl = getURL("stockBill_WMS/remove")
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
                LogUtil.e("run_remove --> onResponse", result)
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
    private fun run_uploadToK3(strJson : String) {
        showLoadDialog("加载中...", false)
        val mUrl = getURL("stockBill_WMS/uploadToK3")
        val formBody = FormBody.Builder()
                .add("strJson", strJson)
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
        super.onDestroyView()
    }
}