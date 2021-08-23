package ykk.xc.com.glswms.produce.adapter

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

class Prod_InStock_SaoMa_Adapter(private val context: Activity, datas: List<ICStockBillEntry_App>) : BaseArrayRecyclerAdapter<ICStockBillEntry_App>(datas) {
    private val df = DecimalFormat("#.######")
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.prod_in_stock_saoma_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: ICStockBillEntry_App, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_mtlNumber = holder.obtainView<TextView>(R.id.tv_mtlNumber)
        val tv_mtlName = holder.obtainView<TextView>(R.id.tv_mtlName)
        val tv_ptQty = holder.obtainView<TextView>(R.id.tv_ptQty)
        val tv_wptQty = holder.obtainView<TextView>(R.id.tv_wptQty)
        val tv_smQty = holder.obtainView<TextView>(R.id.tv_smQty)

        // 赋值
        tv_row.text = (pos+1).toString()
        tv_mtlName.text = entity.icItem.fname
        tv_mtlNumber.text = Html.fromHtml("代码:&nbsp;<font color='#6a5acd'>"+entity.icItem.fnumber+"</font>")
        tv_ptQty.text = Html.fromHtml("配套数:&nbsp;<font color='#6a5acd'>"+ df.format(entity.fsourceQty) +"</font>&nbsp;<font color='#666666'>"+ entity.unit.fname +"</font>")
        tv_wptQty.text = Html.fromHtml("未配数:&nbsp;<font color='#000000'>"+ df.format(entity.wptQty) +"</font>")
        tv_smQty.text = Html.fromHtml("扫码数:&nbsp;<font color='#FF0000'>"+ df.format(entity.fqty) +"</font>")

    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
        fun onDelete(entity: ICStockBillEntry_App, position: Int)
    }

}
