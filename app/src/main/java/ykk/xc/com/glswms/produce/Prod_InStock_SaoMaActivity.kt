package ykk.xc.com.glswms.produce

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import butterknife.OnClick
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import kotlinx.android.synthetic.main.prod_in_stock_saoma.*
import okhttp3.*
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.basics.Stock_GroupDialogActivity
import ykk.xc.com.glswms.bean.ICStockBillEntryBarcode_App
import ykk.xc.com.glswms.bean.ICStockBillEntry_App
import ykk.xc.com.glswms.bean.ICStockBill_App
import ykk.xc.com.glswms.bean.User
import ykk.xc.com.glswms.bean.k3Bean.BomChild_App
import ykk.xc.com.glswms.bean.k3Bean.ProdOrder_App
import ykk.xc.com.glswms.bean.k3Bean.StockPlace_App
import ykk.xc.com.glswms.bean.k3Bean.Stock_App
import ykk.xc.com.glswms.comm.BaseDialogActivity
import ykk.xc.com.glswms.comm.BaseFragment
import ykk.xc.com.glswms.comm.BaseFragment.CAMERA_SCAN
import ykk.xc.com.glswms.comm.Comm
import ykk.xc.com.glswms.produce.adapter.Prod_InStock_SaoMa_Adapter
import ykk.xc.com.glswms.util.BigdecimalUtil
import ykk.xc.com.glswms.util.JsonUtil
import ykk.xc.com.glswms.util.LogUtil
import ykk.xc.com.glswms.util.basehelper.BaseRecyclerAdapter
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

/**
 *  成品扫码入库（扫码）
 */
class Prod_InStock_SaoMaActivity : BaseDialogActivity() {

    companion object {
        private val SEL_POSITION = 61
        private val SEL_PROD = 64
        private val SUCC1 = 200
        private val UNSUCC1 = 500
        private val SAVE = 202
        private val UNSAVE = 502

        private val SETFOCUS = 1
        private val SAOMA = 2
        private val RESULT_NUM = 3
        private val WRITE_CODE = 4
    }

    private val context = this
    private val checkDatas = ArrayList<ICStockBillEntry_App>()
    private var mAdapter: Prod_InStock_SaoMa_Adapter? = null
    private var okHttpClient: OkHttpClient? = null
    private var isTextChange: Boolean = false // 是否进入TextChange事件
    private var timesTamp: String? = null // 时间戳
    private var user: User? = null
    private var stock: Stock_App? = null
    private var stockPlace: StockPlace_App? = null
    private var smqFlag = '2' // 扫描类型1：位置扫描，2：物料扫描
    private var curPos: Int = -1 // 当前行
    private var icstockBillEntry = ICStockBillEntry_App() // 记录成品的行信息
    private var listBomChild: List<BomChild_App>? = null // 记录Bom扫码返回的信息
    private val df = DecimalFormat("#.######")

    // 消息处理
    private val mHandler = MyHandler(this)

    private class MyHandler(activity: Prod_InStock_SaoMaActivity) : Handler() {
        private val mActivity: WeakReference<Prod_InStock_SaoMaActivity>

        init {
            mActivity = WeakReference(activity)
        }

        override fun handleMessage(msg: Message) {
            val m = mActivity.get()
            if (m != null) {
                m.hideLoadDialog()

                var errMsg: String? = null
                var msgObj: String? = null
                if (msg.obj is String) {
                    msgObj = msg.obj as String
                }
                when (msg.what) {
                    SUCC1 -> { // 扫码成功 进入
                        when (m.smqFlag) {
                            '1' -> { // 仓库位置
                                m.resetStockGroup()
                                m.getStockGroup(msgObj)
                            }
                            '2' -> { // 物料
                                val list = JsonUtil.strToList(msgObj, BomChild_App::class.java)
                                m.listBomChild = list
                                if (m.checkDatas.size == 0) { // 匹配父项物料信息
                                    // 打开生产订单列表供选择
                                    val bundle = Bundle()
                                    bundle.putInt("fitemId", list[0].bom.fitemId)
                                    bundle.putBoolean("returnMinOrder", true) // 是否指定订单出库
                                    m.showForResult(Prod_Sel_List_Dialog::class.java, SEL_PROD, bundle)

                                } else { // 匹配子项物料信息
                                    m.getMaterial(list[0])
                                }
                            }
                        }
                    }
                    UNSUCC1 -> { // 扫码失败
                        when (m.smqFlag) {
                            '1' -> m.tv_positionName.text = ""
                            '2' -> m.tv_icItemName.text = ""
                        }
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "很抱歉，没有找到数据！"
                        Comm.showWarnDialog(m.context, errMsg)
                    }
                    SAVE -> { // 保存成功 进入
                        m.toasts("保存成功✔")
                        m.reset()
                    }
                    UNSAVE -> { // 保存失败
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "保存失败！"
                        Comm.showWarnDialog(m.context, errMsg)
                    }
                    SETFOCUS -> { // 当弹出其他窗口会抢夺焦点，需要跳转下，才能正常得到值
                        m.setFocusable(m.et_getFocus)
                        when (m.smqFlag) {
                            '1' -> m.setFocusable(m.et_positionCode)
                            '2' -> m.setFocusable(m.et_code)
                        }
                    }
                    SAOMA -> { // 扫码之后
                        // 执行查询方法
                        m.run_smDatas()
                    }
                }
            }
        }
    }

    override fun setLayoutResID(): Int {
        return R.layout.prod_in_stock_saoma
    }

    override fun initView() {
        if (okHttpClient == null) {
            okHttpClient = OkHttpClient.Builder()
                    //                .connectTimeout(10, TimeUnit.SECONDS) // 设置连接超时时间（默认为10秒）
                    .writeTimeout(120, TimeUnit.SECONDS) // 设置写的超时时间
                    .readTimeout(120, TimeUnit.SECONDS) //设置读取超时时间
                    .build()
        }

        recyclerView.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(context)
        mAdapter = Prod_InStock_SaoMa_Adapter(context, checkDatas)
        recyclerView.adapter = mAdapter
        // 设值listview空间失去焦点
        recyclerView.isFocusable = false

        mAdapter!!.onItemClickListener = BaseRecyclerAdapter.OnItemClickListener { adapter, holder, view, pos ->
            curPos = pos
            showInputDialog("扫码数", checkDatas[pos].fqty.toString(), "0.0", RESULT_NUM)
        }
    }

    override fun initData() {
        getUserInfo()
        timesTamp = user!!.getId().toString() + "-" + Comm.randomUUID()
        hideSoftInputMode(et_positionCode)
        hideSoftInputMode(et_code)
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)

        // 显示本地默认仓库
        showLocalStockGroup()
    }

    /**
     *  显示本地默认仓库
     */
    fun showLocalStockGroup() {
        val saveDefaultStock = getResStr(R.string.saveDefaultStock)
        val spfStock = spf(saveDefaultStock)
        // 显示调入仓库---------------------
        if (spfStock.contains("BIND_PROD_STOCK")) {
            stock = showObjectByXml(Stock_App::class.java, "BIND_PROD_STOCK", saveDefaultStock)
        }
        if (spfStock.contains("BIND_PROD_STOCKPOS")) {
            stockPlace = showObjectByXml(StockPlace_App::class.java, "BIND_PROD_STOCKPOS", saveDefaultStock)
        }
        getStockGroup(null)
    }

    // 监听事件
    @OnClick(R.id.btn_close, R.id.btn_positionSel, R.id.btn_positionScan, R.id.btn_scan, R.id.tv_positionName, R.id.tv_icItemName, R.id.btn_save, R.id.btn_clone)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_close -> {
                if (checkDatas.size > 0) {
                    val build = AlertDialog.Builder(context)
                    build.setIcon(R.drawable.caution)
                    build.setTitle("系统提示")
                    build.setMessage("您有未保存的数据，继续关闭吗？")
                    build.setPositiveButton("是") { dialog, which ->
                        closeHandler(mHandler)
                        context.finish()
                    }
                    build.setNegativeButton("否", null)
                    build.setCancelable(false)
                    build.show()

                } else {
                    closeHandler(mHandler)
                    context.finish()
                }
            }
            R.id.btn_positionSel -> { // 选择仓库
                smqFlag = '1'
                val bundle = Bundle()
                bundle.putSerializable("stock", stock)
                bundle.putSerializable("stockPlace", stockPlace)
                showForResult(Stock_GroupDialogActivity::class.java, SEL_POSITION, bundle)
            }
            R.id.btn_positionScan -> { // 调用摄像头扫描（位置）
                smqFlag = '1'
                ScanUtil.startScan(context, CAMERA_SCAN, HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create());
            }
            R.id.btn_scan -> { // 调用摄像头扫描（物料）
                smqFlag = '2'
                ScanUtil.startScan(context, CAMERA_SCAN, HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create());
            }
            R.id.tv_positionName -> { // 位置点击
                smqFlag = '1'
                mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
            }
            R.id.tv_icItemName -> { // 物料点击
                smqFlag = '2'
                mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
            }
            R.id.btn_save -> { // 保存
                if (!checkSave()) return
                run_save()
            }
            R.id.btn_clone -> { // 重置
                if (checkDatas.size > 0) {
                    val build = AlertDialog.Builder(context)
                    build.setIcon(R.drawable.caution)
                    build.setTitle("系统提示")
                    build.setMessage("您有未保存的数据，继续重置吗？")
                    build.setPositiveButton("是") { dialog, which -> reset() }
                    build.setNegativeButton("否", null)
                    build.setCancelable(false)
                    build.show()

                } else {
                    reset()
                }
            }
        }
    }

    override fun setListener() {
        val click = View.OnClickListener { v ->
            setFocusable(et_getFocus)
            when (v.id) {
                R.id.et_positionCode -> setFocusable(et_positionCode)
                R.id.et_code -> setFocusable(et_code)
            }
        }
        et_positionCode!!.setOnClickListener(click)
        et_code!!.setOnClickListener(click)

        // 仓库---数据变化
        et_positionCode!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.length == 0) return
                if (!isTextChange) {
                    isTextChange = true
                    smqFlag = '1'
                    mHandler.sendEmptyMessageDelayed(SAOMA, 300)
                }
            }
        })
        // 仓库---长按输入条码
        et_positionCode!!.setOnLongClickListener {
            smqFlag = '1'
            showInputDialog("输入条码", getValues(et_positionCode), "none", WRITE_CODE)
            true
        }
        // 仓库---焦点改变
        et_positionCode.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                lin_focusPosition.setBackgroundResource(R.drawable.back_style_red_focus)
            } else {
                if (lin_focusPosition != null) {
                    lin_focusPosition!!.setBackgroundResource(R.drawable.back_style_gray4)
                }
            }
        }

        // 物料---数据变化
        et_code!!.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.length == 0) return
                if (!isTextChange) {
                    isTextChange = true
                    smqFlag = '2'
                    mHandler.sendEmptyMessageDelayed(SAOMA, 300)
                }
            }
        })
        // 物料---长按输入条码
        et_code!!.setOnLongClickListener {
            smqFlag = '2'
            showInputDialog("输入条码号", getValues(et_code), "none", WRITE_CODE)
            true
        }
        // 物料---焦点改变
        et_code.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                lin_focusMtl.setBackgroundResource(R.drawable.back_style_red_focus)
            } else {
                if (lin_focusMtl != null) {
                    lin_focusMtl.setBackgroundResource(R.drawable.back_style_gray4)
                }
            }
        }
    }

    /**
     * 检查数据
     */
    fun checkSave(): Boolean {
        if (checkDatas.size == 0) {
            Comm.showWarnDialog(context, "请扫码（条码）！")
            return false
        }
        var isSave = false // 是否可以保存
        checkDatas.forEachIndexed { index, it ->
            if (it.fdcStockId == 0 || stock == null) {
                Comm.showWarnDialog(context, "请选择（位置）！")
                return false
            }
            if ( (it.fqty+it.wptQty) > 0.0) {
                isSave = true
            }
            /*if (it.fqty == 0.0) {
                Comm.showWarnDialog(context, "第" + (index + 1) + "行，请输入（扫码数）！")
                return false
            }*/
//            if (it.fqty < it.fsourceQty) {
//                Comm.showWarnDialog(context, "第" + (index + 1) + "行，数量未扫完！")
//                return false
//            }
            if (it.fsourceQty > 0 && it.fqty > it.fsourceQty) {
                Comm.showWarnDialog(context, "第" + (index + 1) + "行，（扫码数）+（未配数）不能大于（配套数）！")
                return false
            }
        }
        if(!isSave) {
            Comm.showWarnDialog(context, "至少填入一行数量！")
            return false
        }

        return true
    }

    /**
     *  重置
     */
    private fun reset() {
        curPos = -1
        isTextChange = false
        et_code.setText("")
        tv_mtlName.text = "成品名称："
        tv_mtlNumber.text = "成品代码："
        tv_deptName.text = "车间："
        tv_prodInfo.text = "生产订单："
        tv_inStockQty.text = "入库数："

        listBomChild = null
        checkDatas.clear()
        mAdapter!!.notifyDataSetChanged()

        timesTamp = user!!.getId().toString() + "-" + Comm.randomUUID()
        smqFlag = '2'
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
    }

    fun resetStockGroup() {
        stock = null
        stockPlace = null
    }

    /**
     * 得到仓库组
     */
    private fun getStockGroup(msgObj: String?) {
        // 重置数据
        if (checkDatas.size > 0) {
            checkDatas.forEach {
                it.fdcStockId = 0
                it.fdcSPId = 0
                it.stock = null
                it.stockPlace = null
            }
        }
        tv_positionName.text = ""

        if (msgObj != null) {
            stock = null
            stockPlace = null

            var caseId: Int = 0
            if (msgObj.indexOf("Stock_CaseId=1") > -1) {
                caseId = 1
            } else if (msgObj.indexOf("StockPlace_CaseId=2") > -1) {
                caseId = 2
            }

            when (caseId) {
                1 -> {
                    stock = JsonUtil.strToObject(msgObj, Stock_App::class.java)
                    tv_positionName.text = stock!!.fname
                }
                2 -> {
                    stockPlace = JsonUtil.strToObject(msgObj, StockPlace_App::class.java)
                    tv_positionName.text = stockPlace!!.fname
                    if (stockPlace!!.stock != null) stock = stockPlace!!.stock
                }
            }
        }

        if (stock != null) {
            tv_positionName.text = stock!!.fname
            if (checkDatas.size > 0) {
                checkDatas.forEach {
                    it.fdcStockId = stock!!.fitemId
                    it.stock = stock
                }
            }
        }
        if (stockPlace != null) {
            tv_positionName.text = stockPlace!!.fname
            if (checkDatas.size > 0) {
                checkDatas.forEach {
                    it.fdcSPId = stockPlace!!.fspId
                    it.stockPlace = stockPlace
                }
            }
        }

        if (stock != null) {
            // 自动跳到物料焦点
            smqFlag = '2'
            mHandler.sendEmptyMessage(SETFOCUS)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                SEL_POSITION -> {// 仓库	返回
                    resetStockGroup()
                    stock = data!!.getSerializableExtra("stock") as Stock_App
                    if (data!!.getSerializableExtra("stockPlace") != null) {
                        stockPlace = data!!.getSerializableExtra("stockPlace") as StockPlace_App
                    }
                    getStockGroup(null)
                }
                SEL_PROD -> { // 查询生产订单 返回
                    val prodOrder = data!!.getSerializableExtra("obj") as ProdOrder_App
                    setICStockBillEntry(prodOrder)
                }
                RESULT_NUM -> { // 数量	返回
                    val bundle = data!!.getExtras()
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        val num = parseDouble(value)
                        if (checkDatas[curPos].fsourceQty == 0.0 || num > checkDatas[curPos].fsourceQty) {
                            Comm.showWarnDialog(context, "第" + (curPos + 1) + "行，数量已经扫完！")
                            return
                        }
                        checkDatas[curPos].fqty = num
                        mAdapter!!.notifyDataSetChanged()
                        // 计算入库数量
                        countInStockQty()
                    }
                }
                WRITE_CODE -> {// 输入条码  返回
                    val bundle = data!!.extras
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        when (smqFlag) {
                            '1' -> setTexts(et_positionCode, value.toUpperCase())
                            '2' -> setTexts(et_code, value.toUpperCase())
                        }
                    }
                }
                BaseFragment.CAMERA_SCAN -> {// 扫一扫成功  返回
                    val hmsScan = data!!.getParcelableExtra(ScanUtil.RESULT) as HmsScan
                    if (hmsScan != null) {
                        when (smqFlag) {
                            '1' -> setTexts(et_positionCode, hmsScan.originalValue)
                            '2' -> setTexts(et_code, hmsScan.originalValue)
                        }
                    }
                }
            }
        }
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
    }

    /**
     * 设置表头数据
     */
    private fun setICStockBill(fdeptId: Int): ICStockBill_App {
        var icstockBill = ICStockBill_App() // 保存的对象
        icstockBill.billType = "SCRK"
        icstockBill.ftranType = 2
        icstockBill.frob = 1
        icstockBill.fselTranType = 85
        icstockBill.fdeptId = fdeptId
        icstockBill.fempId = user!!.empId
        icstockBill.yewuMan = user!!.empName
        icstockBill.fsmanagerId = user!!.empId
        icstockBill.baoguanMan = user!!.empName
        icstockBill.fmanagerId = user!!.empId
        icstockBill.fuzheMan = user!!.empName
        icstockBill.ffmanagerId = user!!.empId
        icstockBill.yanshouMan = user!!.empName
        icstockBill.fbillerId = user!!.erpUserId
        icstockBill.createUserId = user!!.id
        icstockBill.createUserName = user!!.username
        return icstockBill
    }

    /**
     * 设置分录数据
     */
    private fun setICStockBillEntry(prodOrder: ProdOrder_App) {
        icstockBillEntry.icstockBillId = 0
        icstockBillEntry.icstockBill = setICStockBill(prodOrder.fworkShop)
        icstockBillEntry.fitemId = prodOrder.fitemId
        icstockBillEntry.funitId = prodOrder.funitId
        icstockBillEntry.fqty = 0.0
        icstockBillEntry.fprice = 0.0
        icstockBillEntry.fsourceTranType = 85
        icstockBillEntry.fsourceInterId = prodOrder.finterId
        icstockBillEntry.fsourceEntryId = prodOrder.finterId
        icstockBillEntry.fsourceBillNo = prodOrder.fbillNo
        icstockBillEntry.fsourceQty = prodOrder.useableQty
        icstockBillEntry.fdetailId = prodOrder.finterId
        icstockBillEntry.forderInterId = prodOrder.fsourceInterId
        icstockBillEntry.forderEntryId = prodOrder.fsourceEntryId
        icstockBillEntry.forderBillNo = prodOrder.fsourceBillNo

        icstockBillEntry.icItem = prodOrder.icItem
        icstockBillEntry.unit = prodOrder.unit

        tv_mtlName.text = Html.fromHtml("成品名称:&nbsp;<font color='#6a5acd'>" + prodOrder.icItem.fname + "</font>")
        tv_mtlNumber.text = Html.fromHtml("成品代码:&nbsp;<font color='#6a5acd'>" + prodOrder.icItem.fnumber + "</font>")
        tv_deptName.text = Html.fromHtml("车间:&nbsp;<font color='#6a5acd'>" + prodOrder.dept.fname + "</font>")
        tv_prodInfo.text = Html.fromHtml("生产订单:&nbsp;<font color='#000000'>" + prodOrder.fbillNo + "</font>（<font color='#6a5acd'>" + df.format(prodOrder.useableQty) + "</font>&nbsp;<font>" + prodOrder.unit.fname + "</font>）")
//        tv_inStockQty.text = Html.fromHtml("入库数:&nbsp;<font color='#009900'>" + df.format(prodOrder.useableQty) + "</font>")
        tv_inStockQty.text = Html.fromHtml("入库数:&nbsp;<font color='#009900'>0</font>")

        // 记录条码
        val icstockBillEntryBarcodes = ArrayList<ICStockBillEntryBarcode_App>()
        val icsBarcode = ICStockBillEntryBarcode_App()
        icsBarcode.parentId = 0
        icsBarcode.barcode = getValues(et_code)
        icsBarcode.batchCode = ""
        icsBarcode.snCode = ""
        icsBarcode.fqty = prodOrder.useableQty
        icsBarcode.isUniqueness = 'N'
        icsBarcode.againUse = 0
        icsBarcode.createUserName = user!!.username
        icsBarcode.billType = "SCRK"
        // 添加到list
        icstockBillEntryBarcodes.add(icsBarcode)

        // 先把子项的显示到列表中，父项的保存时加入列表
        listBomChild!!.forEach {
            val entry = ICStockBillEntry_App()
            entry.icstockBillId = 0
            entry.icstockBill = setICStockBill(prodOrder.fworkShop)
            entry.fitemId = it.fitemId
            entry.funitId = it.icItem.funitId
            var mulVal = 0.0
            if(it.ptQty == 0.0) {
                mulVal = BigdecimalUtil.mul(it.fqty, prodOrder.useableQty)

            } else {
                mulVal = it.ptQty
            }
            mulVal = mulVal - it.smSumQty

            // 当配套数量大于未配数，扫码数+1
            if (mulVal > it.remainQty && it.icItem.barcode.equals(getValues(et_code))) {
                entry.fqty = 1.0
                entry.icstockBillEntryBarcodes = icstockBillEntryBarcodes
            }
//            entry.fqty = 0.0
            entry.fprice = 0.0
            entry.fsourceTranType = -1  // -1标识不做入库条件，只做查询
            entry.fsourceInterId = 0
            entry.fsourceEntryId = 0
            entry.fsourceBillNo = ""
            entry.fsourceQty = mulVal
            entry.fdetailId = 0
            entry.forderInterId = 0
            entry.forderEntryId = 0
            entry.forderBillNo = ""

            entry.icItem = it.icItem
            entry.unit = it.icItem.unit

            entry.mulNum = it.fqty
            if(it.ptQty > 0) {
                entry.ptQty = it.ptQty
                entry.wptQty = it.remainQty
                entry.scrkScanRecordEntryId = it.scrkScanRecordEntryId
            }

            checkDatas.add(entry)
        }
        getStockGroup(null)

        mAdapter!!.notifyDataSetChanged()
        // 计算入库数量
        countInStockQty()
    }

    /**
     *  子项扫码信息
     */
    private fun getMaterial(m: BomChild_App) {
        var bool = false
        curPos = -1
        checkDatas.forEachIndexed { index, it ->
            if (m.fitemId == it.fitemId) {
                if (it.fsourceQty == 0.0 || it.fqty >= it.fsourceQty) {
                    Comm.showWarnDialog(context, "第" + (index + 1) + "行，数量已经扫完！")
                    return
                }
                bool = true
                it.fqty += 1
                curPos = index
            }
        }
        if (!bool) {
            Comm.showWarnDialog(context, "扫码的条码不能匹配子项数据！")
            return
        }

        // 记录条码，相同的不保存
        var isAddBarcode = true // 是否添加条码到记录表
        val icstockBillEntryBarcodes = checkDatas[curPos].icstockBillEntryBarcodes
        icstockBillEntryBarcodes.forEach {
            if (getValues(et_code).equals(it.barcode)) {
                isAddBarcode = false
            }
        }
        if (isAddBarcode) {
            val icsBarcode = ICStockBillEntryBarcode_App()
            icsBarcode.parentId = 0
            icsBarcode.barcode = getValues(et_code)
            icsBarcode.batchCode = ""
            icsBarcode.snCode = ""
            icsBarcode.fqty = icstockBillEntry.fsourceQty
            icsBarcode.isUniqueness = 'N'
            icsBarcode.againUse = 0
            icsBarcode.createUserName = user!!.username
            icsBarcode.billType = "SCRK"
            // 添加到list
            checkDatas[curPos].icstockBillEntryBarcodes.add(icsBarcode)
        }
        mAdapter!!.notifyDataSetChanged()

        // 计算入库数量
        countInStockQty()

        // 扫完了，自动保存
//        if(isFinish()) {
//            run_save()
//        }
    }

    /**
     *  计算入库数量
     */
    private fun countInStockQty() {
        var isCountInStockQty = true // 是否计算入库数量
        var inStockQty = 0.0    // 最小计算后的入库数
        checkDatas.forEachIndexed { index, it ->
            if( (it.fqty+it.wptQty) == 0.0) {
                isCountInStockQty = false
            }

            // 计算扫码数 + 上次未配套数量的和
            val addVal = BigdecimalUtil.add(it.fqty, it.wptQty)
            // 计算（扫码数 + 上次未配套数量的和）/ 物料用量，得到本次入库数
            val divVal = BigdecimalUtil.div(addVal, it.mulNum)
            // 计算之后的入库数
            val inStockQtyTemp = (divVal.toInt()).toDouble()
            if(index == 0 && inStockQty == 0.0) {
                inStockQty = inStockQtyTemp

            } else if( inStockQtyTemp < inStockQty) {
                inStockQty = inStockQtyTemp
            }
        }

        if(isCountInStockQty) {
            icstockBillEntry.fqty = inStockQty
            tv_inStockQty.text = Html.fromHtml("入库数:&nbsp;<font color='#009900'>"+ df.format(inStockQty) +"</font>")
        } else {
            icstockBillEntry.fqty = 0.0
            tv_inStockQty.text = Html.fromHtml("入库数:&nbsp;<font color='#009900'>0</font>")
        }

    }

    /**
     * 判断是否扫完数
     */
    private fun isFinish(): Boolean {
        checkDatas.forEach {
            if(it.fsourceQty != it.fqty) {
                return false
            }
        }
        return true
    }

    /**
     * 通过okhttp加载数据
     */
    private fun run_smDatas() {
        isTextChange = false
        showLoadDialog("加载中...", false)
        val mUrl = getURL("prodOrder/findMtlIdByBarcode2")
        val formBody = FormBody.Builder()
                .add("scanType", if (checkDatas.size > 0) "1" else "0")
                .add("barcode", getValues(et_code))
                .build()

        val request = Request.Builder()
                .addHeader("cookie", session)
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNSUCC1)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                if (!JsonUtil.isSuccess(result)) {
                    mHandler.sendEmptyMessage(UNSUCC1)
                    return
                }

                val msg = mHandler.obtainMessage(SUCC1, result)
                Log.e("run_smDatas --> onResponse", result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 保存
     */
    private fun run_save() {
        showLoadDialog("保存中...", false)

        val datas = ArrayList<ICStockBillEntry_App>()
        icstockBillEntry.fdcStockId = checkDatas[0].fdcStockId
        icstockBillEntry.fdcSPId = checkDatas[0].fdcSPId
        icstockBillEntry.stock = checkDatas[0].stock
        icstockBillEntry.stockPlace = checkDatas[0].stockPlace
        datas.add(icstockBillEntry)
        datas.addAll(checkDatas)

        var mUrl = getURL("stockBill_WMS/save_ProdInStock")
        val formBody = FormBody.Builder()
                .add("strJson", JsonUtil.objectToString(datas))
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNSAVE)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNSAVE, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(SAVE, result)
                LogUtil.e("run_save --> onResponse", result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 得到用户对象
     */
    private fun getUserInfo() {
        if (user == null) user = showUserByXml()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            context.finish()
        }
        return false
    }

    override fun onDestroy() {
        closeHandler(mHandler)
        super.onDestroy()
    }

}
