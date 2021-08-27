package ykk.xc.com.glswms.sales

import android.app.Activity
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
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import kotlinx.android.synthetic.main.sal_out_stock_saoma_fragment2.*
import okhttp3.*
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.basics.Stock_GroupDialogActivity
import ykk.xc.com.glswms.bean.ICStockBillEntryBarcode_App
import ykk.xc.com.glswms.bean.ICStockBillEntry_App
import ykk.xc.com.glswms.bean.ICStockBill_App
import ykk.xc.com.glswms.bean.User
import ykk.xc.com.glswms.bean.k3Bean.BomChild_App
import ykk.xc.com.glswms.bean.k3Bean.SeOutStockEntry_App
import ykk.xc.com.glswms.bean.k3Bean.StockPlace_App
import ykk.xc.com.glswms.bean.k3Bean.Stock_App
import ykk.xc.com.glswms.comm.BaseFragment
import ykk.xc.com.glswms.comm.Comm
import ykk.xc.com.glswms.sales.adapter.Sal_OutStock_SaoMa_Fragment2Adapter
import ykk.xc.com.glswms.util.BigdecimalUtil
import ykk.xc.com.glswms.util.JsonUtil
import ykk.xc.com.glswms.util.LogUtil
import ykk.xc.com.glswms.util.basehelper.BaseRecyclerAdapter
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 日期：2021-08-23 17:28
 * 描述：发货通知单出库
 * 作者：ykk
 */
class Sal_OutStock_SaoMaFragment2 : BaseFragment() {

    companion object {
        private val SEL_POSITION = 61
        private val SEL_ORDER = 64
        private val SUCC1 = 200
        private val UNSUCC1 = 500
        private val SUCC2 = 201
        private val UNSUCC2 = 501
        private val SAVE = 202
        private val UNSAVE = 502

        private val SETFOCUS = 1
        private val SAOMA = 2
        private val RESULT_NUM = 3
        private val WRITE_CODE = 4
    }
    private val context = this
    private var mContext: Activity? = null
    private var parent: Sal_OutStock_SaoMaMainActivity? = null
    private val checkDatas = ArrayList<ICStockBillEntry_App>()
    private var mAdapter: Sal_OutStock_SaoMa_Fragment2Adapter? = null
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

    private class MyHandler(activity: Sal_OutStock_SaoMaFragment2) : Handler() {
        private val mActivity: WeakReference<Sal_OutStock_SaoMaFragment2>

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
                                // 打开发货通知单列表供选择
                                val bundle = Bundle()
                                bundle.putString("strFitemId", strFitemId)
                                bundle.putInt("finterId", finterId)
                                bundle.putInt("fcustId", if(m.checkDatas.size > 0) m.checkDatas[0].icstockBill.fcustId else 0)
//                                bundle.putSerializable("listDetailId", listDetailId)
                                bundle.putBoolean("returnMinOrder", if(m.cb_returnMinOrder.isChecked) false else true) // 是否指定订单出库
                                m.showForResult(Sal_Sel_DeliOrder_Dialog::class.java, SEL_ORDER, bundle)
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
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    SUCC2 -> { // 查询销售订单可下推数量 进入
                        val mapResult = JsonUtil.strToObject(msgObj, Map::class.java)
                        m.checkDatas.forEachIndexed { index, it ->
                            val keyName = it.forderInterId.toString()+"_"+it.forderEntryId.toString()
                            val commitQty = m.parseDouble(mapResult.get(keyName))
                            var subVal = 0.0
                            if(it.fqty > it.fsourceQty) {
                                subVal = BigdecimalUtil.sub(it.fqty, it.fsourceQty)
                            }
                            // 如果出库数大于销售订单可下推数，就提示
                            if(subVal > 0 && mapResult.containsKey(keyName) && subVal > commitQty ) {
                                Comm.showWarnDialog(m.mContext,"第（"+(index+1)+"）行（数量）不能超于（销售订单可下推数）（"+commitQty+"），数量已超（"+subVal+"）！")
                                return
                            }
                        }
                        m.run_save()
                    }
                    UNSUCC2 -> { // 查询销售订单可下推数量 失败
                        m.run_save()
                    }
                    SAVE -> { // 保存成功 进入
                        m.toasts("保存成功✔")
                        m.reset()
                    }
                    UNSAVE -> { // 保存失败
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "保存失败！"
                        Comm.showWarnDialog(m.mContext, errMsg)
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

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.sal_out_stock_saoma_fragment2, container, false)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
        }
    }

    override fun initView() {
        mContext = getActivity()
        parent = mContext as Sal_OutStock_SaoMaMainActivity

        if (okHttpClient == null) {
            okHttpClient = OkHttpClient.Builder()
                    //                .connectTimeout(10, TimeUnit.SECONDS) // 设置连接超时时间（默认为10秒）
                    .writeTimeout(120, TimeUnit.SECONDS) // 设置写的超时时间
                    .readTimeout(120, TimeUnit.SECONDS) //设置读取超时时间
                    .build()
        }

        recyclerView.addItemDecoration(DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(mContext)
        mAdapter = Sal_OutStock_SaoMa_Fragment2Adapter(mContext!!, checkDatas)
        recyclerView.adapter = mAdapter
        // 设值listview空间失去焦点
        recyclerView.isFocusable = false

        // 行事件
        mAdapter!!.setCallBack(object : Sal_OutStock_SaoMa_Fragment2Adapter.MyCallBack {
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
        hideSoftInputMode(mContext, et_positionCode)
        hideSoftInputMode(mContext, et_code)

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
    @OnClick(R.id.btn_positionSel, R.id.btn_positionScan, R.id.btn_scan, R.id.tv_positionName, R.id.tv_icItemName, R.id.btn_save, R.id.btn_clone)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_positionSel -> { // 选择仓库
                smqFlag = '1'
                val bundle = Bundle()
                bundle.putSerializable("stock", stock)
                bundle.putSerializable("stockPlace", stockPlace)
                showForResult(Stock_GroupDialogActivity::class.java, SEL_POSITION, bundle)
            }
            R.id.btn_positionScan -> { // 调用摄像头扫描（位置）
                smqFlag = '1'
                ScanUtil.startScan(mContext, CAMERA_SCAN, HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create());
            }
            R.id.btn_scan -> { // 调用摄像头扫描（物料）
                smqFlag = '2'
                ScanUtil.startScan(mContext, CAMERA_SCAN, HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create());
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
                var isBool = false
                checkDatas.forEachIndexed { index, it ->
                    if (it.fqty != it.fsourceQty) {
                        isBool = true
                    }
                }
                if(isBool) {
                    val build = AlertDialog.Builder(mContext)
                    build.setIcon(R.drawable.caution)
                    build.setTitle("系统提示")
                    build.setMessage("有部分扫码数和发货数不一致，是否出库？")
                    build.setPositiveButton("是") { dialog, which -> run_findCanCommitQty() }
                    build.setNegativeButton("否", null)
                    build.setCancelable(false)
                    build.show()
                } else {
                    run_findCanCommitQty()
                }
//                run_save()
            }
            R.id.btn_clone -> { // 重置
                if (checkDatas.size > 0) {
                    val build = AlertDialog.Builder(mContext)
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
            Comm.showWarnDialog(mContext, "请扫描（条码）！")
            return false
        }
        checkDatas.forEachIndexed { index, it ->
            if (it.fdcStockId == 0 || stock == null) {
                Comm.showWarnDialog(mContext, "请选择（位置）！")
                return false
            }
            if (it.fqty == 0.0) {
                Comm.showWarnDialog(mContext, "第" + (index + 1) + "行，请输入（扫码数）！")
                return false
            }
//            if (it.fqty < it.fsourceQty) {
//                Comm.showWarnDialog(context, "第" + (index + 1) + "行，数量未扫完！")
//                return false
//            }
            /*if (it.fqty > it.fsourceQty) {
                Comm.showWarnDialog(mContext, "第" + (index + 1) + "行，（扫码数）不能大于（订单数）！")
                return false
            }*/
        }

        return true
    }

    /**
     *  重置
     */
    private fun reset() {
        curPos = -1
        isTextChange = false
        tv_custName.text = "客户："
        tv_orderNo.text = "发货单："
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
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SEL_POSITION -> {// 仓库	返回
                    resetStockGroup()
                    stock = data!!.getSerializableExtra("stock") as Stock_App
                    if (data!!.getSerializableExtra("stockPlace") != null) {
                        stockPlace = data!!.getSerializableExtra("stockPlace") as StockPlace_App
                    }
                    getStockGroup(null)
                }
                SEL_ORDER -> { // 查询发货通知单 返回
//                    val list = data!!.getSerializableExtra("obj") as ArrayList<SeOutStockEntry_App>
//                    setICStockBillEntry(list)
                    val seOrderEntry = data!!.getSerializableExtra("obj") as SeOutStockEntry_App
                    tv_custName.text = seOrderEntry.seOutStock.cust.fname
                    tv_orderNo.text = Html.fromHtml("发货单:&nbsp;<font color='#6a5acd'>"+seOrderEntry.seOutStock.fbillNo+"</font>")

                    // 为了扫码一次添加数量1
                    var isAddRow = true // 是否新增行
                    if(checkDatas.size > 0) { // 刷新行
                        var entry : ICStockBillEntry_App? = null
                        var position = 0 // 行数
                        for(icsbe in checkDatas) {
                            if(seOrderEntry.fitemId == icsbe.fitemId) {
                                entry = icsbe
                                break
                            }
                            position += 1
                        }

                        if(entry != null) {
                            if(entry!!.fqty == entry!!.fsourceQty) {
                                Comm.showWarnDialog(mContext,"第（"+(position+1).toString()+"）行，数量已经扫完！")
                                return
                            }
                            if(entry!!.fqty > entry!!.fsourceQty) {
                                Comm.showWarnDialog(mContext,"第（"+(position+1).toString()+"）行，扫描数不能大于订单数！")
                                return
                            }

                            for(icsbeB in entry!!.icstockBillEntryBarcodes) {
                                if(icsbeB.equals(getValues(et_code))) {
                                    icsbeB.fqty += 1.0
                                    entry.fqty += 1.0

                                    break

                                } else {
                                    // 添加条码
                                    val icsBarcode = ICStockBillEntryBarcode_App()
                                    icsBarcode.parentId = 0
                                    icsBarcode.barcode = getValues(et_code)
                                    icsBarcode.batchCode = ""
                                    icsBarcode.snCode = ""
                                    icsBarcode.fqty = 1.0
                                    icsBarcode.isUniqueness = 'N'
                                    icsBarcode.againUse = 0
                                    icsBarcode.createUserName = user!!.username
                                    icsBarcode.billType = "XSCK"
                                    entry.icstockBillEntryBarcodes.add(icsBarcode)

                                    entry.fqty += 1.0

                                    break
                                }
                            }
                            checkDatas[position] = entry
                            mAdapter!!.notifyDataSetChanged()
                            isAddRow = false
                        }

                    }
                    if(isAddRow) {
                        val entry = setICStockBillEntry(seOrderEntry)
                        checkDatas.add(entry)
                        getStockGroup(null)

                        mAdapter!!.notifyDataSetChanged()
                    }
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
            }
        }
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
    }

    /**
     * 调用华为扫码接口，返回的值
     */
    fun getScanData(barcode :String) {
        when (smqFlag) {
            '1' -> setTexts(et_positionCode, barcode)
            '2' -> setTexts(et_code, barcode)
        }
    }

    /**
     * 设置表头数据
     */
    private fun setICStockBill(fcustId: Int, fdeptId: Int): ICStockBill_App {
        var icstockBill = ICStockBill_App() // 保存的对象
        icstockBill.billType = "XSCK2"
        icstockBill.ftranType = 21
        icstockBill.frob = 1
        icstockBill.fselTranType = 83
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
    private fun setICStockBillEntry(seOutStockEntry: SeOutStockEntry_App): ICStockBillEntry_App {
        val entry = ICStockBillEntry_App()
        entry.icstockBillId = 0
        entry.icstockBill = setICStockBill(seOutStockEntry.seOutStock.fcustId, seOutStockEntry.seOutStock.fdeptId)
        entry.fitemId = seOutStockEntry.fitemId
        entry.funitId = seOutStockEntry.funitId
        entry.fqty = seOutStockEntry.useableQty
        entry.fprice = seOutStockEntry.fprice
        entry.fsourceTranType = 83
        entry.fsourceInterId = seOutStockEntry.finterId
        entry.fsourceEntryId = seOutStockEntry.fentryId
        entry.fsourceBillNo = seOutStockEntry.seOutStock.fbillNo
        entry.fsourceQty = seOutStockEntry.useableQty
        entry.fdetailId = seOutStockEntry.fdetailId
        entry.forderInterId = seOutStockEntry.fsourceInterId
        entry.forderEntryId = seOutStockEntry.fsourceEntryId
        entry.forderBillNo = seOutStockEntry.fsourceBillNo

        entry.icItem = seOutStockEntry.icItem
        entry.unit = seOutStockEntry.unit

        // 添加条码
        val icsBarcode = ICStockBillEntryBarcode_App()
        icsBarcode.parentId = 0
        icsBarcode.barcode = getValues(et_code)
        icsBarcode.batchCode = ""
        icsBarcode.snCode = ""
        icsBarcode.fqty = seOutStockEntry.useableQty
        icsBarcode.isUniqueness = 'N'
        icsBarcode.againUse = 0
        icsBarcode.createUserName = user!!.username
        icsBarcode.billType = "XSCK2"
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
                    Comm.showWarnDialog(mContext, "第" + (index + 1) + "行，数量已经扫完！")
                    return
                }
                bool = true
                it.fqty += 1
                curPos = index
            }
        }
        if (!bool) {
            Comm.showWarnDialog(mContext, "扫码的条码不能匹配子项数据！")
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
        val mUrl = getURL("deliOrder/findBarcode")
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
                LogUtil.e("run_smDatas --> onResponse", result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 保存
     */
    private fun run_save() {
        showLoadDialog("保存中...", false)

        var mUrl = getURL("stockBill_WMS/save_DeliOrderOutStock")
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
     * 查询销售订单可下推数量
     */
    private fun run_findCanCommitQty() {
        showLoadDialog("保存中...", false)

        val list = ArrayList<Map<String, Int>>()
        checkDatas.forEach {
            val map = HashMap<String, Int>()
            map.put("finterId", it.forderInterId)
            map.put("fentryId", it.forderEntryId)
            list.add(map)
        }

        var mUrl = getURL("seOrder/findCanCommitQty")
        val formBody = FormBody.Builder()
                .add("strJson", JsonUtil.objectToString(list))
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNSUCC2)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNSUCC2, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(SUCC2, result)
                LogUtil.e("run_findCanCommitQty --> onResponse", result)
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

    override fun onDestroyView() {
        closeHandler(mHandler)
        mBinder!!.unbind()
        super.onDestroyView()
    }
}