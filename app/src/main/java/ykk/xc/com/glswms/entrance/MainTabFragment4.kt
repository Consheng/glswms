package ykk.xc.com.glswms.entrance


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.comm.BaseFragment
import ykk.xc.com.glswms.warehouse.OutInStock_Search_MainActivity


/**
 * 仓库
 */
class MainTabFragment4 : BaseFragment() {

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.aa_main_item4, container, false)
    }

    @OnClick(R.id.relative1, R.id.relative2, R.id.relative3, R.id.relative4, R.id.relative5, R.id.relative6, R.id.relative7)
    fun onViewClicked(view: View) {
        val bundle: Bundle? = null
        when (view.id) {
            R.id.relative1 -> { // 待上传
                val bundle = Bundle()
                bundle.putInt("pageId", 0)
                bundle.putString("billType", "QTRK")
                show(OutInStock_Search_MainActivity::class.java, bundle)
            }
            R.id.relative2 -> { // 其他入库
//                show(OtherInStock_MainActivity::class.java, null)
            }
            R.id.relative3 -> { // 其他出库
//                show(OtherOutStock_MainActivity::class.java, null)
            }
            R.id.relative4 -> { // 待确认
//                show(Ware_BillConfirmList_MainActivity::class.java, null)
            }
            R.id.relative5 -> { // 盘点
//                show(ICInvBackup_MainActivity::class.java, null)
            }
            R.id.relative6 -> { // 自由调拨
//                show(Ware_Transfer_MainActivity::class.java, null)
            }
            R.id.relative7 -> {// 库存查询
//                show(Ware_InventorySearchByDeptActivity::class.java, null)
            }
        }
    }
}
