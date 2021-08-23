package ykk.xc.com.glswms.basics

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.KeyEvent
import android.view.View
import butterknife.OnClick
import kotlinx.android.synthetic.main.ab_public_batch_input.*
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.basics.adapter.MoreBatchInputAdapter
import ykk.xc.com.glswms.bean.ICStockBillEntryBarcode_App
import ykk.xc.com.glswms.comm.BaseDialogActivity
import ykk.xc.com.glswms.comm.Comm
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.util.*

/**
 * 多批次输入dialog
 */
class MoreBatchInputDialog : BaseDialogActivity() {

    private val context = this
    private val listDatas = ArrayList<ICStockBillEntryBarcode_App>()
    private var mAdapter: MoreBatchInputAdapter? = null
    private val df = DecimalFormat("#.######")
    private var icstockBillEntryId = 0
    private var userName:String? = null
    private var barcode:String? = null
    private var againUse = 0 // 当外购入库保存为1，仓库收料操作为0，一个条码在外购入库用完还可以在仓库收料操作页面扫码，之后就不能扫

    // 消息处理
    private val mHandler = MyHandler(this)

    private class MyHandler(activity: MoreBatchInputDialog) : Handler() {
        private val mActivity: WeakReference<MoreBatchInputDialog>

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
                    100 -> {
                        // 隐藏软键盘
                        m.hideKeyboard(m.currentFocus)
                        // 关闭
                        m.context.finish()
                    }
                }
            }
        }
    }

    override fun setLayoutResID(): Int {
        return R.layout.ab_public_batch_input
    }

    override fun initView() {
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter = MoreBatchInputAdapter(context, listDatas)
        recyclerView.adapter = mAdapter
        // 设值listview空间失去焦点
        recyclerView.isFocusable = false

        // 行事件
        mAdapter!!.setCallBack(object : MoreBatchInputAdapter.MyCallBack {
            override fun onDelete(entity: ICStockBillEntryBarcode_App, position: Int) {
                listDatas.removeAt(position)
                // 合计
                countSumQty()
            }
        })
    }

    override fun initData() {
        val bundle = context.intent.extras
        if (bundle != null) {
            icstockBillEntryId = bundle.getInt("icstockBillEntryId")
            val list = bundle.getSerializable("icstockBillEntry_Barcodes") as List<ICStockBillEntryBarcode_App>
            userName = bundle.getString("userName")
            barcode = bundle.getString("barcode")
            againUse = bundle.getInt("againUse")
            listDatas.addAll(list)
            // 合计
            countSumQty()
        }
    }

    // 监听事件
    @OnClick(R.id.btn_close, R.id.btn_confirm, R.id.tv_addRow)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_close -> {
                mHandler.sendEmptyMessageDelayed(100, 200)
            }
            R.id.btn_confirm -> { // 确认
                val bool = addRow()
                if(bool) {
                    val intent = Intent()
                    intent.putExtra("icstockBillEntry_Barcodes", listDatas)
                    context.setResult(Activity.RESULT_OK, intent)

                    mHandler.sendEmptyMessageDelayed(100, 200)
//                    // 隐藏软键盘
//                    hideKeyboard(currentFocus)
//                    context.finish()
                }
            }
            R.id.tv_addRow -> { // 添加行
                addRow()
            }
        }
    }

    fun addRow() : Boolean {
        val batch = getValues(et_batchCode)
        val fqty = Comm.parseDouble(getValues(et_fqty))
        if(batch.length == 0 && fqty == 0.0) {
            return true
        }
        if(batch.length == 0 && fqty > 0) {
            Comm.showWarnDialog(context,"请输入批次号！")
            return false
        }

        if(fqty == 0.0 && batch.length > 0 ) {
            Comm.showWarnDialog(context,"请输入数量！")
            return false
        }
        listDatas.forEach {
            if(batch == it.batchCode) {
                Comm.showWarnDialog(context,"不能保存相同批次！")
                return false
            }
        }
        val m = ICStockBillEntryBarcode_App()
        m.parentId = icstockBillEntryId
        m.barcode = barcode
        m.batchCode = batch
        m.fqty = fqty
        m.isUniqueness = 'Y'
        m.createUserName = userName
        m.againUse = againUse
        listDatas.add(m)
        // 合计
        countSumQty()
        // 清空
        et_batchCode.setText("")
        et_fqty.setText("")
        setFocusable(et_fqty)
        setFocusable(et_batchCode)
        return true
    }

    /**
     * 合计
     */
    fun countSumQty() {
        var sumQty = 0.0
        listDatas.forEach {
            sumQty += it.fqty
        }
        tv_countSumQty.text = df.format(sumQty)
        mAdapter!!.notifyDataSetChanged()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mHandler.sendEmptyMessageDelayed(100, 200)
        }
        return false
    }

}
