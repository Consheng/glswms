package ykk.xc.com.glswms.basics.adapter

import android.app.Activity
import android.widget.TextView
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.bean.k3Bean.Stock_App
import ykk.xc.com.glswms.util.basehelper.BaseArrayRecyclerAdapter
import ykk.xc.com.glswms.util.basehelper.BaseRecyclerAdapter

class Stock_DialogAdapter(private val context: Activity, private val datas: List<Stock_App>) : BaseArrayRecyclerAdapter<Stock_App>(datas) {
    private var callBack: MyCallBack? = null

    override fun bindView(viewtype: Int): Int {
        return R.layout.ab_stock_dialog_item
    }

    override fun onBindHoder(holder: BaseRecyclerAdapter.RecyclerHolder, entity: Stock_App, pos: Int) {
        // 初始化id
        val tv_row = holder.obtainView<TextView>(R.id.tv_row)
        val tv_fnumber = holder.obtainView<TextView>(R.id.tv_fnumber)
        val tv_fname = holder.obtainView<TextView>(R.id.tv_fname)
        // 赋值
        tv_row!!.text = (pos + 1).toString()
        tv_fnumber!!.text = entity.fnumber
        tv_fname!!.text = entity.fname
    }

    fun setCallBack(callBack: MyCallBack) {
        this.callBack = callBack
    }

    interface MyCallBack {
        fun onClick(entity: Stock_App, position: Int)
    }


    /*之下的方法都是为了方便操作，并不是必须的*/

    //在指定位置插入，原位置的向后移动一格
    //    public boolean addItem(int position, String msg) {
    //        if (position < datas.size() && position >= 0) {
    //            datas.add(position, msg);
    //            notifyItemInserted(position);
    //            return true;
    //        }
    //        return false;
    //    }
    //
    //    //去除指定位置的子项
    //    public boolean removeItem(int position) {
    //        if (position < datas.size() && position >= 0) {
    //            datas.remove(position);
    //            notifyItemRemoved(position);
    //            return true;
    //        }
    //        return false;
    //    }
    //清空显示数据
    //    public void clearAll() {
    //        datas.clear();
    //        notifyDataSetChanged();
    //    }


}
