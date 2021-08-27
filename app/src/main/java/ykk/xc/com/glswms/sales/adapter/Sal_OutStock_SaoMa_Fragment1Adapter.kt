package ykk.xc.com.glswms.sales.adapter

import android.app.Activity
import android.text.Html
import android.view.View
import android.widget.TextView
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.bean.ICStockBillEntry_App
import ykk.xc.com.glswms.comm.Comm
import ykk.xc.com.glswms.util.basehelper.BaseArrayRecyclerAdapter
import ykk.xc.com.glswms.util.basehelper.BaseRecyclerAdapter
import java.text.DecimalFormat

class Sal_OutStock_SaoMa_Fragment1Adapter(private val context: Activity, datas: List<ICStockBillEntry_App>) : BaseArrayRecyclerAdapter<ICStockBillEntry_App>(datas) {
    private val df = DecimalFormat("#.######")
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.sal_out_stock_saoma_fragment1_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: ICStockBillEntry_App, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val view_del = holder.obtainView<View>(R.id.view_del)
        val tv_mtlNumber = holder.obtainView<TextView>(R.id.tv_mtlNumber)
        val tv_mtlName = holder.obtainView<TextView>(R.id.tv_mtlName)
        val tv_sourceQty = holder.obtainView<TextView>(R.id.tv_sourceQty)
        val tv_fqty = holder.obtainView<TextView>(R.id.tv_fqty)

        // 赋值
        tv_row.text = (pos+1).toString()
        tv_mtlName.text = entity.icItem.fname
        tv_mtlNumber.text = Html.fromHtml("代码:&nbsp;<font color='#6a5acd'>"+entity.icItem.fnumber+"</font>")
        tv_sourceQty.text = Html.fromHtml("订单:&nbsp;<font color='#000000'>"+ entity.fsourceBillNo +"</font>（<font color='#6a5acd'>" + df.format(entity.fsourceQty) + "</font>&nbsp;<font>" + entity.unit.fname + "</font>）")
        tv_fqty.text = Html.fromHtml("扫码数:&nbsp;<font color='#FF0000'>"+ df.format(entity.fqty) +"</font>")

        val click = View.OnClickListener { v ->
            when (v.id) {
                R.id.view_del -> {// 删除行
                    if (callBack != null) {
                        callBack!!.onDelete(entity, pos)
                    }
                }
            }
        }
        view_del!!.setOnClickListener(click)
    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
        fun onDelete(entity: ICStockBillEntry_App, position: Int)
    }

}
