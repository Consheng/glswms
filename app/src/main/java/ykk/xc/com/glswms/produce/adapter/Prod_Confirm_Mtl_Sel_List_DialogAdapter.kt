package ykk.xc.com.glswms.produce.adapter

import android.app.Activity
import android.text.Html
import android.widget.TextView
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.bean.k3Bean.Customer_App
import ykk.xc.com.glswms.bean.k3Bean.ICItem_App
import ykk.xc.com.glswms.comm.Comm
import ykk.xc.com.glswms.util.basehelper.BaseArrayRecyclerAdapter
import ykk.xc.com.glswms.util.basehelper.BaseRecyclerAdapter

class Prod_Confirm_Mtl_Sel_List_DialogAdapter(private val context: Activity, private val datas: List<ICItem_App>) : BaseArrayRecyclerAdapter<ICItem_App>(datas) {
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.prod_confirm_mtl_sel_list_dialog_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: ICItem_App, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_mtlNumber = holder.obtainView<TextView>(R.id.tv_mtlNumber)
        val tv_mtlName = holder.obtainView<TextView>(R.id.tv_mtlName)
        val tv_fmodel = holder.obtainView<TextView>(R.id.tv_fmodel)
        val tv_unitName = holder.obtainView<TextView>(R.id.tv_unitName)

        // 赋值
        tv_row.text = (pos+1).toString()
        tv_mtlName.text = entity.fname
        tv_mtlNumber.text = Html.fromHtml("代码:&nbsp;<font color='#6a5acd'>"+entity.fnumber+"</font>")
        tv_fmodel.text = Html.fromHtml("规格型号:&nbsp;<font color='#6a5acd'>"+ Comm.isNULLS(entity.fmodel)+"</font>")
        tv_unitName.text = Html.fromHtml("单位:&nbsp;<font color='#000000'>"+ entity.unit.fname +"</font>")

    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
        fun onClick(entity: Customer_App, position: Int)
    }

}
