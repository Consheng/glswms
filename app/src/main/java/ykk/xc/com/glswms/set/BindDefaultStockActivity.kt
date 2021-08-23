package ykk.xc.com.glswms.set

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import butterknife.OnClick
import kotlinx.android.synthetic.main.aa_main_item6_bind_stock.*
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.basics.StockPos_DialogActivity
import ykk.xc.com.glswms.basics.Stock_DialogActivity
import ykk.xc.com.glswms.basics.Stock_GroupDialogActivity
import ykk.xc.com.glswms.bean.User
import ykk.xc.com.glswms.bean.k3Bean.StockPlace_App
import ykk.xc.com.glswms.bean.k3Bean.Stock_App
import ykk.xc.com.glswms.comm.BaseActivity
import ykk.xc.com.glswms.comm.Comm

/**
 * 绑定业务的默认仓库
 */
class BindDefaultStockActivity : BaseActivity() {

    companion object {
        private val SEL_PUR_STOCK = 10
        private val SEL_PUR_STOCKPOS = 11
        private val SEL_PUR_STOCK_RED = 20
        private val SEL_PUR_STOCKPOS_RED = 21
        private val SEL_PROD_STOCK = 30
        private val SEL_PROD_STOCKPOS = 31
        private val SEL_SAL_OUT_STOCK = 40
        private val SEL_SAL_OUT_STOCKPOS = 41
        private val SEL_SAL_OUT_STOCK_RED = 50
        private val SEL_SAL_OUT_STOCKPOS_RED = 51
        private val SEL_OTHER_IN_STOCK = 60
        private val SEL_OTHER_IN_STOCKPOS = 61
        private val SEL_OTHER_OUT_STOCK = 70
        private val SEL_OTHER_OUT_STOCKPOS = 71
        private val SEL_ZYDB_IN_STOCK = 80
        private val SEL_ZYDB_IN_STOCKPOS = 81
        private val SEL_ZYDB_OUT_STOCK = 90
        private val SEL_ZYDB_OUT_STOCKPOS = 91

        private val PUR_STOCK = "BIND_PUR_STOCK"
        private val PUR_STOCKPOS = "BIND_PUR_STOCKPOS"
        private val PUR_STOCK_RED = "BIND_PUR_STOCK_RED"
        private val PUR_STOCKPOS_RED = "BIND_PUR_STOCKPOS_RED"
        private val PROD_STOCK = "BIND_PROD_STOCK"
        private val PROD_STOCKPOS = "BIND_PROD_STOCKPOS"
        private val SAL_OUT_STOCK = "BIND_SAL_OUT_STOCK"
        private val SAL_OUT_STOCKPOS = "BIND_SAL_OUT_STOCKPOS"
        private val SAL_OUT_STOCK_RED = "BIND_SAL_OUT_STOCK_RED"
        private val SAL_OUT_STOCKPOS_RED = "BIND_SAL_OUT_STOCKPOS_RED"
        private val OTHER_IN_STOCK = "BIND_OTHER_IN_STOCK"
        private val OTHER_IN_STOCKPOS = "BIND_OTHER_IN_STOCKPOS"
        private val OTHER_OUT_STOCK = "BIND_OTHER_OUT_STOCK"
        private val OTHER_OUT_STOCKPOS = "BIND_OTHER_OUT_STOCKPOS"
        private val ZYDB_IN_STOCK = "BIND_ZYDB_IN_STOCK"
        private val ZYDB_IN_STOCKPOS = "BIND_ZYDB_IN_STOCKPOS"
        private val ZYDB_OUT_STOCK = "BIND_ZYDB_OUT_STOCK"
        private val ZYDB_OUT_STOCKPOS = "BIND_ZYDB_OUT_STOCKPOS"
    }

    private val context = this
    private var user: User? = null

    override fun setLayoutResID(): Int {
        return R.layout.aa_main_item6_bind_stock
    }

    override fun initView() {
        getUserInfo()

        // 显示仓库信息
        showLocalStockGroup()
    }

    override fun initData() {
    }

    // 监听事件
    @OnClick(R.id.btn_close, R.id.btn_save, R.id.btn_clear,
            R.id.tv_purStock, R.id.tv_purStock_red, R.id.tv_prodStock, R.id.tv_salOutStock, R.id.tv_salOutStock_red,
            R.id.tv_otherInStock, R.id.tv_otherOutStock, R.id.tv_zydbInStock, R.id.tv_zydbOutStock)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_close -> {
                context.finish()
            }
            R.id.btn_save -> {
            }
            R.id.btn_clear -> {
                val saveDefaultStock = getResStr(R.string.saveDefaultStock)
                val editor = spf(saveDefaultStock).edit()
                editor.clear()
                editor.commit()

                tv_purStock.text = ""
                tv_purStock_red.text = ""
                tv_prodStock.text = ""
                tv_salOutStock.text = ""
                tv_salOutStock_red.text = ""
                tv_otherInStock.text = ""
                tv_otherOutStock.text = ""
                tv_zydbInStock.text = ""
                tv_zydbOutStock.text = ""
            }
            R.id.tv_purStock -> { // 采购入库
                val bundle = Bundle()
                showForResult(Stock_DialogActivity::class.java, SEL_PUR_STOCK, bundle)
            }
            R.id.tv_purStock_red -> { // 采购退料
                val bundle = Bundle()
                showForResult(Stock_DialogActivity::class.java, SEL_PUR_STOCK_RED, bundle)
            }
            R.id.tv_prodStock -> { // 生产入库
                val bundle = Bundle()
                showForResult(Stock_DialogActivity::class.java, SEL_PROD_STOCK, bundle)
            }
            R.id.tv_salOutStock -> { // 销售出库
                val bundle = Bundle()
                showForResult(Stock_DialogActivity::class.java, SEL_SAL_OUT_STOCK, bundle)
            }
            R.id.tv_salOutStock_red -> { // 销售退货
                val bundle = Bundle()
                showForResult(Stock_DialogActivity::class.java, SEL_SAL_OUT_STOCK_RED, bundle)
            }
            R.id.tv_otherInStock -> { // 其他入库
                val bundle = Bundle()
                showForResult(Stock_DialogActivity::class.java, SEL_OTHER_IN_STOCK, bundle)
            }
            R.id.tv_otherOutStock -> { // 其他出库
                val bundle = Bundle()
                showForResult(Stock_DialogActivity::class.java, SEL_OTHER_OUT_STOCK, bundle)
            }
            R.id.tv_zydbInStock -> { // 自由调拨（调入仓库）
                val bundle = Bundle()
                showForResult(Stock_DialogActivity::class.java, SEL_ZYDB_IN_STOCK, bundle)
            }
            R.id.tv_zydbOutStock -> { // 自由调拨（调出仓库）
                val bundle = Bundle()
                showForResult(Stock_DialogActivity::class.java, SEL_ZYDB_OUT_STOCK, bundle)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SEL_PUR_STOCK -> { // 采购入库（仓库）  返回
                    val stock = data!!.getSerializableExtra("obj") as Stock_App
                    tv_purStock.text = stock.fname
                    saveLocalStockGroup(stock, null, PUR_STOCK)
                    if(stock.fisStockMgr == 1) {
                        val bundle = Bundle()
                        bundle.putInt("fspGroupId", stock.fspGroupId)
                        showForResult(StockPos_DialogActivity::class.java, SEL_PUR_STOCKPOS, bundle)
                    }
                }
                SEL_PUR_STOCKPOS -> { // 采购入库（库位）  返回
                    val stockPlace = data!!.getSerializableExtra("obj") as StockPlace_App
                    tv_purStock.text = stockPlace.fname
                    saveLocalStockGroup(null, stockPlace, PUR_STOCKPOS)
                }
                SEL_PUR_STOCK_RED -> { // 采购退料（仓库）  返回
                    val stock = data!!.getSerializableExtra("obj") as Stock_App
                    tv_purStock_red.text = stock.fname
                    saveLocalStockGroup(stock, null, PUR_STOCK_RED)
                    if(stock.fisStockMgr == 1) {
                        val bundle = Bundle()
                        bundle.putInt("fspGroupId", stock.fspGroupId)
                        showForResult(StockPos_DialogActivity::class.java, SEL_PUR_STOCKPOS_RED, bundle)
                    }
                }
                SEL_PUR_STOCKPOS_RED -> { // 采购退料（库位）  返回
                    val stockPlace = data!!.getSerializableExtra("obj") as StockPlace_App
                    tv_purStock_red.text = stockPlace.fname
                    saveLocalStockGroup(null, stockPlace, PUR_STOCKPOS_RED)
                }
                SEL_PROD_STOCK -> { // 生产入库（仓库）  返回
                    val stock = data!!.getSerializableExtra("obj") as Stock_App
                    tv_prodStock.text = stock.fname
                    saveLocalStockGroup(stock, null, PROD_STOCK)
                    if(stock.fisStockMgr == 1) {
                        val bundle = Bundle()
                        bundle.putInt("fspGroupId", stock.fspGroupId)
                        showForResult(StockPos_DialogActivity::class.java, SEL_PROD_STOCKPOS, bundle)
                    }
                }
                SEL_PROD_STOCKPOS -> { // 生产入库（库位）  返回
                    val stockPlace = data!!.getSerializableExtra("obj") as StockPlace_App
                    tv_prodStock.text = stockPlace.fname
                    saveLocalStockGroup(null, stockPlace, PROD_STOCKPOS)
                }
                SEL_SAL_OUT_STOCK -> { // 销售出库（仓库）  返回
                    val stock = data!!.getSerializableExtra("obj") as Stock_App
                    tv_salOutStock.text = stock.fname
                    saveLocalStockGroup(stock, null, SAL_OUT_STOCK)
                    if(stock.fisStockMgr == 1) {
                        val bundle = Bundle()
                        bundle.putInt("fspGroupId", stock.fspGroupId)
                        showForResult(StockPos_DialogActivity::class.java, SEL_SAL_OUT_STOCKPOS, bundle)
                    }
                }
                SEL_SAL_OUT_STOCKPOS -> { // 销售出库（库位）  返回
                    val stockPlace = data!!.getSerializableExtra("obj") as StockPlace_App
                    tv_salOutStock.text = stockPlace.fname
                    saveLocalStockGroup(null, stockPlace, SAL_OUT_STOCKPOS)
                }
                SEL_SAL_OUT_STOCK_RED -> { // 销售出库（仓库）  返回
                    val stock = data!!.getSerializableExtra("obj") as Stock_App
                    tv_salOutStock_red.text = stock.fname
                    saveLocalStockGroup(stock, null, SAL_OUT_STOCK_RED)
                    if(stock.fisStockMgr == 1) {
                        val bundle = Bundle()
                        bundle.putInt("fspGroupId", stock.fspGroupId)
                        showForResult(StockPos_DialogActivity::class.java, SEL_SAL_OUT_STOCKPOS_RED, bundle)
                    }
                }
                SEL_SAL_OUT_STOCKPOS_RED -> { // 销售出库（库位）  返回
                    val stockPlace = data!!.getSerializableExtra("obj") as StockPlace_App
                    tv_salOutStock_red.text = stockPlace.fname
                    saveLocalStockGroup(null, stockPlace, SAL_OUT_STOCKPOS_RED)
                }
                SEL_OTHER_IN_STOCK -> { // 其他入库（仓库）  返回
                    val stock = data!!.getSerializableExtra("obj") as Stock_App
                    tv_otherInStock.text = stock.fname
                    saveLocalStockGroup(stock, null, OTHER_IN_STOCK)
                    if(stock.fisStockMgr == 1) {
                        val bundle = Bundle()
                        bundle.putInt("fspGroupId", stock.fspGroupId)
                        showForResult(StockPos_DialogActivity::class.java, SEL_OTHER_IN_STOCKPOS, bundle)
                    }
                }
                SEL_OTHER_IN_STOCKPOS -> { // 其他入库（库位）  返回
                    val stockPlace = data!!.getSerializableExtra("obj") as StockPlace_App
                    tv_otherInStock.text = stockPlace.fname
                    saveLocalStockGroup(null, stockPlace, OTHER_IN_STOCKPOS)
                }
                SEL_OTHER_OUT_STOCK -> { // 其他出库（仓库）  返回
                    val stock = data!!.getSerializableExtra("obj") as Stock_App
                    tv_otherOutStock.text = stock.fname
                    saveLocalStockGroup(stock, null, OTHER_OUT_STOCK)
                    if(stock.fisStockMgr == 1) {
                        val bundle = Bundle()
                        bundle.putInt("fspGroupId", stock.fspGroupId)
                        showForResult(StockPos_DialogActivity::class.java, SEL_OTHER_OUT_STOCKPOS, bundle)
                    }
                }
                SEL_OTHER_OUT_STOCKPOS -> { // 其他出库（库位）  返回
                    val stockPlace = data!!.getSerializableExtra("obj") as StockPlace_App
                    tv_otherOutStock.text = stockPlace.fname
                    saveLocalStockGroup(null, stockPlace, OTHER_OUT_STOCKPOS)
                }
                SEL_ZYDB_IN_STOCK -> { // 自由调拨（调入仓库）  返回
                    val stock = data!!.getSerializableExtra("obj") as Stock_App
                    if(stock.fname.equals(getValues(tv_zydbOutStock))) {
                        Comm.showWarnDialog(context,"自由调拨（调入仓库）和（调出仓库）不能一样！")
                        return
                    }
                    tv_zydbInStock.text = stock.fname
                    saveLocalStockGroup(stock, null, ZYDB_IN_STOCK)
                    if(stock.fisStockMgr == 1) {
                        val bundle = Bundle()
                        bundle.putInt("fspGroupId", stock.fspGroupId)
                        showForResult(StockPos_DialogActivity::class.java, SEL_ZYDB_IN_STOCKPOS, bundle)
                    }
                }
                SEL_ZYDB_IN_STOCKPOS -> { // 自由调拨（调入库位）  返回
                    val stockPlace = data!!.getSerializableExtra("obj") as StockPlace_App
                    tv_zydbInStock.text = stockPlace.fname
                    saveLocalStockGroup(null, stockPlace, ZYDB_IN_STOCKPOS)
                }
                SEL_ZYDB_OUT_STOCK -> { // 自由调拨（调出仓库）  返回
                    val stock = data!!.getSerializableExtra("obj") as Stock_App
                    if(stock.fname.equals(getValues(tv_zydbInStock))) {
                        Comm.showWarnDialog(context,"自由调拨（调出仓库）和（调入仓库）不能一样！")
                        return
                    }
                    tv_zydbOutStock.text = stock.fname
                    saveLocalStockGroup(stock, null, ZYDB_OUT_STOCK)
                    if(stock.fisStockMgr == 1) {
                        val bundle = Bundle()
                        bundle.putInt("fspGroupId", stock.fspGroupId)
                        showForResult(StockPos_DialogActivity::class.java, SEL_ZYDB_OUT_STOCKPOS, bundle)
                    }
                }
                SEL_ZYDB_OUT_STOCKPOS -> { // 自由调拨（调出库位）  返回
                    val stockPlace = data!!.getSerializableExtra("obj") as StockPlace_App
                    tv_zydbOutStock.text = stockPlace.fname
                    saveLocalStockGroup(null, stockPlace, ZYDB_OUT_STOCKPOS)

                }
            }
        }
    }

    /**
     * 保存仓库信息到本地
     */
    private fun saveLocalStockGroup(stock: Stock_App?, stockPos :StockPlace_App?, key: String) {
        val saveDefaultStock = getResStr(R.string.saveDefaultStock)
        if(stock != null) { // 保存仓库对象，并删除库位对象
            saveObjectToXml(stock, key, saveDefaultStock)
            // 删除库位对象
            spfRemove(key+"POS", saveDefaultStock)

        } else if(stockPos != null) { // 保存库位对象
            saveObjectToXml(stockPos, key, saveDefaultStock)
        }
    }

    /**
     * 显示保存本地的仓库信息
     */
    private fun showLocalStockGroup() {
        // 显示记录的本地仓库
        val saveDefaultStock = getResStr(R.string.saveDefaultStock)
        val spfStock = spf(saveDefaultStock)

        // 先清空
        tv_purStock.text = ""
        tv_purStock_red.text = ""
        tv_prodStock.text = ""
        tv_salOutStock.text = ""
        tv_salOutStock_red.text = ""
        tv_otherInStock.text = ""
        tv_otherOutStock.text = ""
        tv_zydbInStock.text = ""
        tv_zydbOutStock.text = ""

        if (spfStock.contains(PUR_STOCK)) {
            val stock = showObjectByXml(Stock_App::class.java, PUR_STOCK, saveDefaultStock)
            val stockPlace = showObjectByXml(StockPlace_App::class.java, PUR_STOCKPOS, saveDefaultStock)
            setTextValue(stock, stockPlace, tv_purStock)
        }
        if (spfStock.contains(PUR_STOCK_RED)) {
            val stock = showObjectByXml(Stock_App::class.java, PUR_STOCK_RED, saveDefaultStock)
            val stockPlace = showObjectByXml(StockPlace_App::class.java, PUR_STOCKPOS_RED, saveDefaultStock)
            setTextValue(stock, stockPlace, tv_purStock_red)
        }
        if (spfStock.contains(PROD_STOCK)) {
            val stock = showObjectByXml(Stock_App::class.java, PROD_STOCK, saveDefaultStock)
            val stockPlace = showObjectByXml(StockPlace_App::class.java, PROD_STOCKPOS, saveDefaultStock)
            setTextValue(stock, stockPlace, tv_prodStock)
        }
        if (spfStock.contains(SAL_OUT_STOCK)) {
            val stock = showObjectByXml(Stock_App::class.java, SAL_OUT_STOCK, saveDefaultStock)
            val stockPlace = showObjectByXml(StockPlace_App::class.java, SAL_OUT_STOCKPOS, saveDefaultStock)
            setTextValue(stock, stockPlace, tv_salOutStock)
        }
        if (spfStock.contains(SAL_OUT_STOCK_RED)) {
            val stock = showObjectByXml(Stock_App::class.java, SAL_OUT_STOCK_RED, saveDefaultStock)
            val stockPlace = showObjectByXml(StockPlace_App::class.java, SAL_OUT_STOCKPOS_RED, saveDefaultStock)
            setTextValue(stock, stockPlace, tv_salOutStock_red)
        }
        if (spfStock.contains(OTHER_IN_STOCK)) {
            val stock = showObjectByXml(Stock_App::class.java, OTHER_IN_STOCK, saveDefaultStock)
            val stockPlace = showObjectByXml(StockPlace_App::class.java, OTHER_IN_STOCKPOS, saveDefaultStock)
            setTextValue(stock, stockPlace, tv_otherInStock)
        }
        if (spfStock.contains(OTHER_OUT_STOCK)) {
            val stock = showObjectByXml(Stock_App::class.java, OTHER_OUT_STOCK, saveDefaultStock)
            val stockPlace = showObjectByXml(StockPlace_App::class.java, OTHER_OUT_STOCKPOS, saveDefaultStock)
            setTextValue(stock, stockPlace, tv_otherOutStock)
        }
        if (spfStock.contains(ZYDB_IN_STOCK)) {
            val stock = showObjectByXml(Stock_App::class.java, ZYDB_IN_STOCK, saveDefaultStock)
            val stockPlace = showObjectByXml(StockPlace_App::class.java, ZYDB_IN_STOCKPOS, saveDefaultStock)
            setTextValue(stock, stockPlace, tv_zydbInStock)
        }
        if (spfStock.contains(ZYDB_OUT_STOCK)) {
            val stock = showObjectByXml(Stock_App::class.java, ZYDB_OUT_STOCK, saveDefaultStock)
            val stockPlace = showObjectByXml(StockPlace_App::class.java, ZYDB_OUT_STOCKPOS, saveDefaultStock)
            setTextValue(stock, stockPlace, tv_zydbOutStock)
        }
    }

    /**
     * 设置显示仓库还是库位名称
     */
    private fun setTextValue(stock :Stock_App?, stockPlace :StockPlace_App?, tv :TextView) {
        if(stock != null) tv.text = stock.fname
        if(stockPlace != null) tv.text = stockPlace.fname
    }


    /**
     * 得到用户对象
     */
    private fun getUserInfo() {
        if (user == null) user = showUserByXml()
    }
}
