package ykk.xc.com.glswms.basics.adapter

import android.app.Activity
import android.widget.TextView

import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.bean.k3Bean.Customer_App
import ykk.xc.com.glswms.util.basehelper.BaseArrayRecyclerAdapter
import ykk.xc.com.glswms.util.basehelper.BaseRecyclerAdapter

class Cust_DialogAdapter(private val context: Activity, private val datas: List<Customer_App>) : BaseArrayRecyclerAdapter<Customer_App>(datas) {
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.ab_cust_dialog_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: Customer_App, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_fnumber = holder.obtainView<TextView>(R.id.tv_fnumber)
        val tv_fname = holder.obtainView<TextView>(R.id.tv_fname)
        // 赋值
        tv_row!!.setText((pos + 1).toString())
        tv_fnumber!!.setText(entity.fnumber)
        tv_fname!!.setText(entity.fname)
    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
        fun onClick(entity: Customer_App, position: Int)
    }

}
