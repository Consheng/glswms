package ykk.xc.com.glswms.produce

import android.app.Activity
import android.content.Intent
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.view.KeyEvent
import android.view.View
import butterknife.OnClick
import kotlinx.android.synthetic.main.prod_confirm_mtl_sel_list_dialog.*
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.bean.k3Bean.ICItem_App
import ykk.xc.com.glswms.comm.BaseDialogActivity
import ykk.xc.com.glswms.produce.adapter.Prod_Confirm_Mtl_Sel_List_DialogAdapter
import ykk.xc.com.glswms.util.basehelper.BaseRecyclerAdapter

/**
 * 选择物料列表确定dialog
 */
class Prod_Confirm_Mtl_Sel_List_Dialog : BaseDialogActivity() {

    private val context = this
    private val listDatas = ArrayList<ICItem_App>()
    private var mAdapter: Prod_Confirm_Mtl_Sel_List_DialogAdapter? = null

    override fun setLayoutResID(): Int {
        return R.layout.prod_confirm_mtl_sel_list_dialog
    }

    override fun initView() {
        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter = Prod_Confirm_Mtl_Sel_List_DialogAdapter(context!!, listDatas)
        recyclerView.adapter = mAdapter
        // 设值listview空间失去焦点
        recyclerView.isFocusable = false

        mAdapter!!.onItemClickListener = BaseRecyclerAdapter.OnItemClickListener { adapter, holder, view, pos ->
            val m = listDatas[pos - 1]
            setResultByFinish(m)
        }
    }

    override fun initData() {
        val bundle = context.intent.extras
        if (bundle != null) {
            var checkDatas = bundle.getSerializable("checkDatas") as ArrayList<ICItem_App>
            listDatas.addAll(checkDatas)
            mAdapter!!.notifyDataSetChanged()
        }
    }


    // 监听事件
    @OnClick(R.id.btn_close)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_close -> {
                context.finish()
            }
        }
    }

    /**
     *  返回给上个页面
     */
    private fun setResultByFinish(prodOrder :ICItem_App) {
        val intent = Intent()
        intent.putExtra("obj", prodOrder)
        context.setResult(Activity.RESULT_OK, intent)
        context.finish()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            context.finish()
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}
