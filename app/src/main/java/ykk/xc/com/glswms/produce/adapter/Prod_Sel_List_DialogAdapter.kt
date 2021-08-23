package ykk.xc.com.glswms.produce.adapter

import android.app.Activity
import android.text.Html
import android.view.View
import android.widget.TextView

import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.bean.k3Bean.Customer_App
import ykk.xc.com.glswms.bean.k3Bean.ProdOrder_App
import ykk.xc.com.glswms.comm.Comm
import ykk.xc.com.glswms.util.basehelper.BaseArrayRecyclerAdapter
import ykk.xc.com.glswms.util.basehelper.BaseRecyclerAdapter
import java.text.DecimalFormat

class Prod_Sel_List_DialogAdapter(private val context: Activity, private val datas: List<ProdOrder_App>) : BaseArrayRecyclerAdapter<ProdOrder_App>(datas) {
    private val df = DecimalFormat("#.######")
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.prod_sel_list_dialog_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: ProdOrder_App, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_mtlNumber = holder.obtainView<TextView>(R.id.tv_mtlNumber)
        val tv_mtlName = holder.obtainView<TextView>(R.id.tv_mtlName)
        val tv_fmodel = holder.obtainView<TextView>(R.id.tv_fmodel)
        val tv_num = holder.obtainView<TextView>(R.id.tv_num)
        val tv_sourceNo = holder.obtainView<TextView>(R.id.tv_sourceNo)
        val tv_deptName = holder.obtainView<TextView>(R.id.tv_deptName)

        // 赋值
        tv_row.text = (pos+1).toString()
        tv_mtlName.text = entity.icItem.fname
        tv_mtlNumber.text = Html.fromHtml("代码:&nbsp;<font color='#6a5acd'>"+entity.icItem.fnumber+"</font>")
        tv_fmodel.text = Html.fromHtml("规格型号:&nbsp;<font color='#6a5acd'>"+ Comm.isNULLS(entity.icItem.fmodel)+"</font>")
        tv_num.text = Html.fromHtml("数量:&nbsp;<font color='#FF0000'>"+ df.format(entity.useableQty) +"</font>&nbsp;<font color='#666666'>"+ entity.unit.fname +"</font>")
        tv_sourceNo.text = Html.fromHtml("生产订单:&nbsp;<font color='#000000'>"+ entity.fbillNo+"</font>")
        tv_deptName.text = Html.fromHtml("车间:&nbsp;<font color='#6a5acd'>"+ entity.dept.fname+"</font>")

    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
        fun onClick(entity: Customer_App, position: Int)
    }

}
