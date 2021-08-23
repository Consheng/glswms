package ykk.xc.com.glswms.basics.adapter

import android.app.Activity
import android.view.View
import android.widget.TextView
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.bean.ICStockBillEntryBarcode_App
import ykk.xc.com.glswms.util.basehelper.BaseArrayRecyclerAdapter
import ykk.xc.com.glswms.util.basehelper.BaseRecyclerAdapter
import java.text.DecimalFormat

class MoreBatchInputAdapter(private val context: Activity, private val datas: List<ICStockBillEntryBarcode_App>) : BaseArrayRecyclerAdapter<ICStockBillEntryBarcode_App>(datas) {
    private var callBack: MyCallBack? = null
    private val df = DecimalFormat("#.######")

    override fun bindView(viewtype: Int): Int {
        return R.layout.ab_public_batch_input_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: ICStockBillEntryBarcode_App, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_batchCode = holder.obtainView<TextView>(R.id.tv_batchCode)
        val tv_fqty = holder.obtainView<TextView>(R.id.tv_fqty)
        val tv_del = holder.obtainView<TextView>(R.id.tv_del)
        // 赋值
        tv_row!!.text = (pos + 1).toString()
        tv_batchCode!!.text = entity.batchCode
        tv_fqty!!.text = df.format(entity.fqty)

        val click = View.OnClickListener { v ->
            when (v.id) {
                R.id.tv_del // 删除行
                -> if (callBack != null) {
                    callBack!!.onDelete(entity, pos)
                }
            }
        }
        tv_del!!.setOnClickListener(click)

    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
        fun onDelete(entity: ICStockBillEntryBarcode_App, position: Int)
    }
}
