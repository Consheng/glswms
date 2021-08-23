package ykk.xc.com.glswms.sales

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
import kotlinx.android.synthetic.main.sal_out_stock_saoma.*
import okhttp3.*
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.basics.Stock_GroupDialogActivity
import ykk.xc.com.glswms.bean.ICStockBillEntryBarcode_App
import ykk.xc.com.glswms.bean.ICStockBillEntry_App
import ykk.xc.com.glswms.bean.ICStockBill_App
import ykk.xc.com.glswms.bean.User
import ykk.xc.com.glswms.bean.k3Bean.BomChild_App
import ykk.xc.com.glswms.bean.k3Bean.SeOrderEntry_App
import ykk.xc.com.glswms.bean.k3Bean.StockPlace_App
import ykk.xc.com.glswms.bean.k3Bean.Stock_App
import ykk.xc.com.glswms.comm.BaseDialogActivity
import ykk.xc.com.glswms.comm.BaseFragment
import ykk.xc.com.glswms.comm.BaseFragment.CAMERA_SCAN
import ykk.xc.com.glswms.comm.Comm
import ykk.xc.com.glswms.sales.adapter.Sal_OutStock_SaoMa_Adapter
import ykk.xc.com.glswms.util.JsonUtil
import ykk.xc.com.glswms.util.LogUtil
import ykk.xc.com.glswms.util.basehelper.BaseRecyclerAdapter
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

/**
 *  销售出库（扫描）
 */
class Sal_OutStock_SaoMaActivity : BaseDialogActivity() {

    companion object {
        private val SEL_POSITION = 61
        private val SEL_ORDER = 64
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
    private var mAdapter: Sal_OutStock_SaoMa_Adapter? = null
    private var okHttpClient: OkHttpClient? = null
    private var isTextChange: Boolean = false // 是否进入TextChange事件
    private var timesTamp: String? = null // 时间戳
    private var user: User? = null
    private var stock: Stock_App? = null
    private var stockPlace: StockPlace_App? = null
    private var smqFlag = '2' // 扫描类型1：位置扫描，2：物料扫描
    private var curPos: Int = -1 // 当前行
    private var icstockBillEntry = ICStockBillEntry_App() // 记录成品的行信息

    // 消息处理
    private val mHandler = MyHandler(this)

    private class MyHandler(activity: Sal_OutStock_SaoMaActivity) : Handler() {
        private val mActivity: WeakReference<Sal_OutStock_SaoMaActivity>

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
                                val strFitemId = JsonUtil.strToString(msgObj)
                                val listDetailId = ArrayList<Int>()
                                m.checkDatas.forEach {
                                    listDetailId.add(it.fdetailId)
                                }
                                var finterId = if(m.checkDatas.size > 0) m.checkDatas[0].fsourceInterId else 0
                                // 打开销售订单列表供选择
                                val bundle = Bundle()
                                bundle.putString("strFitemId", strFitemId)
                                bundle.putInt("finterId", finterId)
//                                bundle.putInt("fcustId", if(m.checkDatas.size > 0) m.checkDatas[0].icstockBill.fcustId else 0)
                                bundle.putSerializable("listDetailId", listDetailId)
                                bundle.putBoolean("returnMinOrder", if(m.cb_returnMinOrder.isChecked) false else true) // 是否指定订单出库
                                m.showForResult(Sal_Sel_List_Dialog::class.java, SEL_ORDER, bundle)
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
        return R.layout.sal_out_stock_saoma
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
        mAdapter = Sal_OutStock_SaoMa_Adapter(context, checkDatas)
        recyclerView.adapter = mAdapter
        // 设值listview空间失去焦点
        recyclerView.isFocusable = false

        // 行事件
        mAdapter!!.setCallBack(object : Sal_OutStock_SaoMa_Adapter.MyCallBack {
            override fun onDelete(entity: ICStockBillEntry_App, position: Int) {
                checkDatas.removeAt(position)
                mAdapter!!.notifyDataSetChanged()
            }
        })

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
        if (spfStock.contains("BIND_SAL_OUT_STOCK")) {
            stock = showObjectByXml(Stock_App::class.java, "BIND_SAL_OUT_STOCK", saveDefaultStock)
        }
        if (spfStock.contains("BIND_SAL_OUT_STOCKPOS")) {
            stockPlace = showObjectByXml(StockPlace_App::class.java, "BIND_SAL_OUT_STOCKPOS", saveDefaultStock)
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
        checkDatas.forEachIndexed { index, it ->
            if (it.fdcStockId == 0 || stock == null) {
                Comm.showWarnDialog(context, "请选择（位置）！")
                return false
            }
            if (it.fqty == 0.0) {
                Comm.showWarnDialog(context, "第" + (index + 1) + "行，请输入（扫码数）！")
                return false
            }
//            if (it.fqty < it.fsourceQty) {
//                Comm.showWarnDialog(context, "第" + (index + 1) + "行，数量未扫完！")
//                return false
//            }
            if (it.fqty > it.fsourceQty) {
                Comm.showWarnDialog(context, "第" + (index + 1) + "行，（扫码数）不能大于（订单数）！")
                return false
            }
        }

        return true
    }

    /**
     *  重置
     */
    private fun reset() {
        curPos = -1
        isTextChange = false
        tv_custName.text = ""
        et_code.setText("")

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
                SEL_ORDER -> { // 查询生产订单 返回
//                    val list = data!!.getSerializableExtra("obj") as ArrayList<SeOrderEntry_App>
//                    setICStockBillEntry(list)
                    val seOrderEntry = data!!.getSerializableExtra("obj") as SeOrderEntry_App
                    tv_custName.text = seOrderEntry.seOrder.cust.fname
                    val entry = setICStockBillEntry(seOrderEntry)

                    checkDatas.add(entry)
                    getStockGroup(null)

                    mAdapter!!.notifyDataSetChanged()
                    tv_orderNo.text = Html.fromHtml("订单号:&nbsp;<font color='#6a5acd'>"+seOrderEntry.seOrder.fbillNo+"</font>")
                }
                RESULT_NUM -> { // 数量	返回
                    val bundle = data!!.getExtras()
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        val num = parseDouble(value)
                        checkDatas[curPos].fqty = num
                        mAdapter!!.notifyDataSetChanged()
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
    private fun setICStockBill(fcustId: Int, fdeptId: Int): ICStockBill_App {
        var icstockBill = ICStockBill_App() // 保存的对象
        icstockBill.billType = "XSCK"
        icstockBill.ftranType = 21
        icstockBill.frob = 1
        icstockBill.fselTranType = 81
        icstockBill.fcustId = fcustId
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
     * 设置表体数据
     */
    private fun setICStockBillEntry(seOrderEntry: SeOrderEntry_App): ICStockBillEntry_App {
        val entry = ICStockBillEntry_App()
        entry.icstockBillId = 0
        entry.icstockBill = setICStockBill(seOrderEntry.seOrder.fcustId, seOrderEntry.seOrder.fdeptId)
        entry.fitemId = seOrderEntry.fitemId
        entry.funitId = seOrderEntry.funitId
        entry.fqty = seOrderEntry.useableQty
        entry.fprice = seOrderEntry.fprice
        entry.fsourceTranType = 81
        entry.fsourceInterId = seOrderEntry.finterId
        entry.fsourceEntryId = seOrderEntry.fentryId
        entry.fsourceBillNo = seOrderEntry.seOrder.fbillNo
        entry.fsourceQty = seOrderEntry.useableQty
        entry.fdetailId = seOrderEntry.fdetailId
        entry.forderInterId = seOrderEntry.finterId
        entry.forderEntryId = seOrderEntry.fentryId
        entry.forderBillNo = seOrderEntry.seOrder.fbillNo

        entry.icItem = seOrderEntry.icItem
        entry.unit = seOrderEntry.unit

        // 添加条码
        val icsBarcode = ICStockBillEntryBarcode_App()
        icsBarcode.parentId = 0
        icsBarcode.barcode = getValues(et_code)
        icsBarcode.batchCode = ""
        icsBarcode.snCode = ""
        icsBarcode.fqty = seOrderEntry.useableQty
        icsBarcode.isUniqueness = 'N'
        icsBarcode.againUse = 0
        icsBarcode.createUserName = user!!.username
        icsBarcode.billType = "XSCK"
        entry.icstockBillEntryBarcodes.add(icsBarcode)

        return entry
    }

    /**
     *  子项扫码信息
     */
    private fun getMaterial(m: BomChild_App) {
        var bool = false
        curPos = -1
        checkDatas.forEachIndexed { index, it ->
            if (m.fitemId == it.fitemId) {
                if (it.fqty >= it.fsourceQty) {
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

        // 扫完了，自动保存
        if(isFinish()) {
            run_save()
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
        val mUrl = getURL("seOrder/findBarcode")
        val formBody = FormBody.Builder()
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

        var mUrl = getURL("stockBill_WMS/save_SalOutStock")
        val formBody = FormBody.Builder()
                .add("strJson", JsonUtil.objectToString(checkDatas))
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
