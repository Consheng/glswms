package ykk.xc.com.glswms.set.adapter

import android.app.Activity
import android.text.Html
import android.widget.TextView
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.bean.ICStockBill_App
import ykk.xc.com.glswms.util.basehelper.BaseArrayRecyclerAdapter
import ykk.xc.com.glswms.util.basehelper.BaseRecyclerAdapter

class Set_NetworkErrorData_Fragment1_Adapter(private val context: Activity, datas: List<ICStockBill_App>) : BaseArrayRecyclerAdapter<ICStockBill_App>(datas) {
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.set_network_error_data_fragment1_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: ICStockBill_App, pos: Int) {
        // 初始化id
        val tv_pdaNo = holder.obtainView<TextView>(R.id.tv_pdaNo)
        val tv_missionBillNo = holder.obtainView<TextView>(R.id.tv_missionBillNo)
        val tv_fdate = holder.obtainView<TextView>(R.id.tv_fdate)

        // 赋值
        tv_pdaNo.text = Html.fromHtml("PDA单号:&nbsp;<font color='#6a5acd'>"+entity.pdaNo+"</font>")
        /*if(entity.missionBill != null) {
            tv_missionBillNo.text = Html.fromHtml("任务单:&nbsp;<font color='#FF4400'>"+entity.missionBill.billNo+"</font>")
            tv_missionBillNo.visibility = View.VISIBLE
        } else {
            tv_missionBillNo.visibility = View.INVISIBLE
        }*/
        tv_fdate.text = Html.fromHtml("创建日期:&nbsp;<font color='#000000'>"+entity.createDate.substring(0,19)+"</font>")
    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
        fun onDelete(entity: ICStockBill_App, position: Int)
    }

}
