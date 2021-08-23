package ykk.xc.com.glswms.entrance


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.bean.User
import ykk.xc.com.glswms.comm.BaseFragment
import ykk.xc.com.glswms.comm.Comm
import ykk.xc.com.glswms.produce.Prod_InStock_MainActivity
import ykk.xc.com.glswms.produce.Prod_InStock_SaoMaActivity
import ykk.xc.com.glswms.warehouse.OutInStock_Search_MainActivity

/**
 * 生产
 */
class MainTabFragment2 : BaseFragment() {

    private var user: User? = null

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.aa_main_item2, container, false)
    }

    override fun initView() {
        getUserInfo()
    }

    @OnClick(R.id.relative1, R.id.relative2, R.id.relative3, R.id.relative4, R.id.relative5, R.id.relative6)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.relative1 -> {  // 待上传
                val bundle = Bundle()
                bundle.putInt("pageId", 0)
                bundle.putString("billType", "SCRK")
                show(OutInStock_Search_MainActivity::class.java, bundle)
//                show(Prod_Box_MainActivity::class.java, null)
            }
            R.id.relative2 -> { // 半成品入库
                show(Prod_InStock_MainActivity::class.java, null)
            }
            R.id.relative3  -> { // 成品入库
                show(Prod_InStock_SaoMaActivity::class.java, null)
            }
            R.id.relative4  -> { // 完工汇报
//                show(Prod_Report_SearchActivity::class.java, null)
            }
            R.id.relative5  -> { // 报工审核
                /*if(user!!.postType == 1) {
                    Comm.showWarnDialog(activity,"您没有权限，主管权限才能进入！")
                } else {
                    show(Prod_Report_Pass_MainActivity::class.java, null)
                }*/
            }
            R.id.relative6  -> { // 报工查询
//                show(Prod_Report_SearchActivity::class.java, null)
            }
        }
    }

    /**
     * 得到用户对象
     */
    private fun getUserInfo() {
        if (user == null) user = showUserByXml()
    }
}
