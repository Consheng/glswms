package ykk.xc.com.glswms.sales

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import com.huawei.hms.ml.scan.HmsScanAnalyzerOptions
import kotlinx.android.synthetic.main.sal_out_fragment1.*
import okhttp3.*
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.basics.Stock_GroupDialogActivity
import ykk.xc.com.glswms.bean.*
import ykk.xc.com.glswms.bean.k3Bean.Department_App
import ykk.xc.com.glswms.bean.k3Bean.SeOrderEntry_App
import ykk.xc.com.glswms.bean.k3Bean.StockPlace_App
import ykk.xc.com.glswms.bean.k3Bean.Stock_App
import ykk.xc.com.glswms.comm.BaseFragment
import ykk.xc.com.glswms.comm.Comm
import ykk.xc.com.glswms.sales.adapter.Sal_DS_OutStockFragment1Adapter
import ykk.xc.com.glswms.util.BigdecimalUtil
import ykk.xc.com.glswms.util.JsonUtil
import ykk.xc.com.glswms.util.LogUtil
import ykk.xc.com.glswms.util.basehelper.BaseRecyclerAdapter
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * 日期：2019-10-16 09:50
 * 描述：电商出库---添加明细
 * 作者：ykk
 */
class Sal_OutStockFragment1 : BaseFragment() {

    companion object {
        private val SEL_POSITION = 61
        private val SUCC1 = 200
        private val UNSUCC1 = 500
        private val SUCC2 = 201
        private val UNSUCC2 = 501
        private val SUCC3 = 202
        private val UNSUCC3 = 502
        private val SAVE = 204
        private val UNSAVE = 504
        private val FIND_DEPT = 205
        private val UNFIND_DEPT = 505
        private val FIND_STOCK_STATUS = 208
        private val UNFIND_STOCK_STATUS = 508

        private val SETFOCUS = 1
        private val SAOMA = 2
        private val WRITE_CODE = 3
        private val RESULT_NUM = 4
        private val RESULT_SALORDER_ORDER = 5
    }
    private val context = this
    private var okHttpClient: OkHttpClient? = null
    private var user: User? = null
    private var stock: Stock_App? = null
    private var stockPlace:StockPlace_App? = null
    private var mContext: Activity? = null
    private val df = DecimalFormat("#.######")
    private var parent: Sal_OutStockMainActivity? = null
    private var mAdapter: Sal_DS_OutStockFragment1Adapter? = null
    private var isTextChange: Boolean = false // 是否进入TextChange事件
    private var timesTamp:String? = null // 时间戳
    private var smqFlag = '1' // 扫描类型1：位置扫描，2：物料扫描
    private var curPos:Int = -1 // 当前行
    private val checkDatas = ArrayList<ICStockBillEntry_App>()
    private var dept : Department_App? = null // 记录发货部门
    private var defaultDept :Department_App? = null // 默认锁库发货部门

    // 消息处理
    private val mHandler = MyHandler(this)
    private class MyHandler(activity: Sal_OutStockFragment1) : Handler() {
        private val mActivity: WeakReference<Sal_OutStockFragment1>

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
                        when(m.smqFlag) {
                            '1'-> { // 仓库位置
                                m.stock = null
                                m.stockPlace = null
                                m.getStockGroup(msgObj)
                            }
                            '2'-> { // 物料
                                val bt = JsonUtil.strToObject(msgObj, BarcodeTable_App::class.java)
                                if(m.dept == null) {
                                    if(bt.dept != null) {
                                        m.dept = bt.dept
                                    } else {
                                        m.dept = m.defaultDept
                                    }
                                    m.tv_deptName.text = m.dept!!.fname
                                }
                                m.setICStockBill_Row(bt)
                            }
                        }
                        m.isTextChange = false
                    }
                    UNSUCC1 -> { // 扫码失败
                        m.isTextChange = false
                        when(m.smqFlag) {
                            '1' -> { // 仓库位置扫描
                                m.tv_positionName.text = ""
                            }
                        }
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "很抱歉，没有找到数据！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    SAVE -> { // 保存成功 进入
                        m.toasts("保存成功✔，开始打印")
                        m.btn_scan.isEnabled = false
                        m.et_code.isEnabled = false
                        m.isTextChange = true
                        // 延时执行，因为输入框失去焦点会改变样式
                        m.mHandler.postDelayed(Runnable {
                            m.lin_focusMtl.setBackgroundResource(R.drawable.back_style_gray3)
                        },300)

                        // 保存完成，就查询打印数据
                        m.run_findPrintData(m.checkDatas[0].icstockBill.expressNo)
                        /*
                        // 汇报最后一个工序
                        if(m.btTmp != null) {
                            m.run_saveProdReprot()
                        }*/
                    }
                    UNSAVE -> { // 保存失败
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "保存失败！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    FIND_DEPT -> { // 查询默认锁库发货部门 进入
                        m.defaultDept = JsonUtil.strToObject(msgObj, Department_App::class.java)
                        m.tv_deptName.text = m.defaultDept!!.fname
                        // 查询金蝶仓库状态
                        if(m.stock != null) {
                            m.run_findStockStatus(m.stock!!.fitemId.toString())
                        }
                    }
                    UNFIND_DEPT -> { // 查询默认锁库发货部门  失败
                        if (m.isNULLS(errMsg).length == 0) errMsg = "请在PC端维护部门的（默认锁库部门）标识！3秒后自动关闭..."
                        m.toasts(errMsg)
                        m.mHandler.postDelayed(Runnable {
                            m.mContext!!.finish()
                        },3000)
                    }
                    FIND_STOCK_STATUS -> { // 查询仓库状态
                        // 正常，不做处理
                    }
                    UNFIND_STOCK_STATUS -> { // 查询仓库状态  失败，就清空它
                        m.stock = null
                        m.stockPlace = null
                        m.tv_positionName.text = ""
                    }
                    SETFOCUS -> { // 当弹出其他窗口会抢夺焦点，需要跳转下，才能正常得到值
                        m.setFocusable(m.et_getFocus)
                        when(m.smqFlag) {
                            '1'-> m.setFocusable(m.et_positionCode)
                            '2'-> {
                                if(m.et_code.isEnabled) {
                                    m.setFocusable(m.et_code)
                                }
                            }
                        }
                    }
                    SAOMA -> { // 扫码之后
                        when(m.smqFlag) {
                            '2' -> {
                                if(m.stock == null ) { // 如果扫描的是箱码，未选择位置，就提示
                                    Comm.showWarnDialog(m.mContext,"请先扫描或选择位置！")
                                    m.isTextChange = false
                                    return
                                }
                            }
                        }
                        // 执行查询方法
                        m.run_smDatas()
                    }
                }
            }
        }
    }

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.sal_out_fragment1, container, false)
    }

    override fun initView() {
        mContext = getActivity()
        parent = mContext as Sal_OutStockMainActivity

        recyclerView.addItemDecoration(DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL))
        recyclerView.layoutManager = LinearLayoutManager(mContext)
        mAdapter = Sal_DS_OutStockFragment1Adapter(mContext!!, checkDatas)
        recyclerView.adapter = mAdapter
        // 设值listview空间失去焦点
        recyclerView.isFocusable = false

        // 行事件
        mAdapter!!.setCallBack(object : Sal_DS_OutStockFragment1Adapter.MyCallBack {
            //            override fun onModify(entity: ICStockBillEntry, position: Int) {
//                EventBus.getDefault().post(EventBusEntity(31, entity))
//                // 滑动第二个页面
//                parent!!.viewPager!!.setCurrentItem(1, false)
//            }
            override fun onDelete(entity: ICStockBillEntry_App, position: Int) {
//                curPos = position
//                run_removeEntry(entity.id)
            }
        })

        mAdapter!!.onItemClickListener = BaseRecyclerAdapter.OnItemClickListener { adapter, holder, view, pos ->
            val entry = checkDatas.get(pos)
            // 是否赠品，2000005代表是，2000006代表否
            if(entry.free == 1 || entry.icItem.snManager == 1) return@OnItemClickListener

            curPos = pos
            showInputDialog("数量", entry.fqty.toString(), "0.0", RESULT_NUM)
        }
    }

    override fun initData() {
        if (okHttpClient == null) {
            okHttpClient = OkHttpClient.Builder()
                    //                .connectTimeout(10, TimeUnit.SECONDS) // 设置连接超时时间（默认为10秒）
                    .writeTimeout(120, TimeUnit.SECONDS) // 设置写的超时时间
                    .readTimeout(120, TimeUnit.SECONDS) //设置读取超时时间
                    .build()
        }

        getUserInfo()
        timesTamp = user!!.getId().toString() + "-" + Comm.randomUUID()
        hideSoftInputMode(mContext, et_code)
        hideSoftInputMode(mContext, et_positionCode)

        showLocalStockGroup()
//        run_findLockDeliDept()
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
        }
    }

    @OnClick(R.id.tv_positionName, R.id.btn_positionScan, R.id.btn_positionSel, R.id.btn_scan, R.id.btn_save, R.id.btn_upload, R.id.btn_clone)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.tv_positionName -> { // 点击位置名称
                smqFlag = '1'
                mHandler.sendEmptyMessageDelayed(SETFOCUS,200)
            }
            R.id.btn_positionSel -> { // 选择位置
                smqFlag = '1'
                val bundle = Bundle()
                bundle.putSerializable("stock", stock)
                bundle.putSerializable("stockPlace", stockPlace)
                showForResult(context, Stock_GroupDialogActivity::class.java, SEL_POSITION, bundle)
            }
            R.id.btn_positionScan -> { // 调用摄像头扫描（位置）
                smqFlag = '1'
                ScanUtil.startScan(mContext, BaseFragment.CAMERA_SCAN, HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create());
            }
            R.id.btn_scan -> { // 调用摄像头扫描（物料）
                smqFlag = '2'
                if(stock == null ) {
                    Comm.showWarnDialog(mContext,"请先扫描或选择位置！")
                    return
                }
                ScanUtil.startScan(mContext, BaseFragment.CAMERA_SCAN, HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create());
            }
            R.id.btn_save -> { // 保存
                if(!checkSave()) return
                run_save()
            }
            R.id.btn_upload -> { // 保存
                if(!checkSave()) return
                run_save()
            }
            R.id.btn_clone -> { // 重置
                /*if (checkSaveHint()) {
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
                }*/
                reset()
            }
        }
    }

    /**
     * 检查数据
     */
    fun checkSave() : Boolean {
        if (stock == null) {
            Comm.showWarnDialog(mContext, "请选择位置！")
            return false;
        }
        if(checkDatas.size == 0) {
            Comm.showWarnDialog(mContext, "请扫码物料！")
            return false
        }
        if(!isFinish()) {
            Comm.showWarnDialog(mContext, "订单数量未扫完！")
            return false
        }
        return true;
    }

    /**
     * 选择了物料没有点击保存，点击了重置，需要提示
     */
    fun checkSaveHint() : Boolean {
        if(checkDatas.size > 0) {
            return true
        }
        return false
    }

    override fun setListener() {
        val click = View.OnClickListener { v ->
            setFocusable(et_getFocus)
            when (v.id) {
                R.id.et_positionCode -> setFocusable(et_positionCode)
                R.id.et_code -> setFocusable(et_code)
//                R.id.et_containerCode -> setFocusable(et_containerCode)
            }
        }
        et_positionCode!!.setOnClickListener(click)
        et_code!!.setOnClickListener(click)
//        et_containerCode!!.setOnClickListener(click)

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
            showInputDialog("输入条码", "", "none", WRITE_CODE)
            true
        }
        // 仓库---焦点改变
        et_positionCode.onFocusChangeListener = View.OnFocusChangeListener { v, hasFocus ->
            if(hasFocus) {
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
            if(hasFocus) {
                lin_focusMtl.setBackgroundResource(R.drawable.back_style_red_focus)
            } else {
                if (lin_focusMtl != null) {
                    lin_focusMtl.setBackgroundResource(R.drawable.back_style_gray4)
                }
            }
        }

    }

    /**
     *  显示本地默认仓库
     */
    private fun showLocalStockGroup() {
        // 显示记录的本地仓库
        val saveOther = getResStr(R.string.saveDefaultStock)
        val spfStock = spf(saveOther)
        if(spfStock.contains("BIND_SAL_OUT_STOCK")) {
            stock = showObjectByXml(Stock_App::class.java, "BIND_SAL_OUT_STOCK", saveOther)
            tv_positionName.text = stock!!.fname
            // 跳转到物料焦点
            smqFlag = '2'
            mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
        }
        if(spfStock.contains("BIND_SAL_OUT_STOCKPOS")) {
            stockPlace = showObjectByXml(StockPlace_App::class.java, "BIND_SAL_OUT_STOCKPOS", saveOther)
            tv_positionName.text = stockPlace!!.fname
        }
    }

    /**
     * 重置数据
     */
    private fun reset() {
        curPos = -1
        btn_scan.isEnabled = true
        et_code.isEnabled = true
        isTextChange = false
        checkDatas.clear()
        mAdapter!!.notifyDataSetChanged()
        dept = null
        tv_deptName.text = "部门："
        tv_needNum.text = "0"
        tv_finishNum.text = "0"

        timesTamp = user!!.getId().toString() + "-" + Comm.randomUUID()
        parent!!.isChange = false
        smqFlag = '2'
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
    }

    /**
     * 扫码销售订单的条码
     */
    private fun setICStockEntry_SalOrder(bt :BarcodeTable_App, list :List<SeOrderEntry_App>) {
        val icstockBill = ICStockBill_App()
        icstockBill.billType = "DS_XSCK" // 电商销售出库
        icstockBill.ftranType = 21
        icstockBill.frob = 1
        icstockBill.fselTranType = 81
        icstockBill.fcustId = list[0].seOrder.fcustId
//        icstockBill.fdeptId = list[0].seOrder.fdeptId
        icstockBill.fdeptId = dept!!.fitemId
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
        icstockBill.expressNo = list[0].seOrder.printExpressNo

//        var isManProduct = false // 是否主产品
        list!!.forEachIndexed { index, it ->
            val entry = ICStockBillEntry_App()
            entry.icstockBill = icstockBill
            entry.icstockBillId = 0
            entry.fitemId = it.icItem.fitemId
//            entry.fentryId = it.fentryid
            if(it.lockStock != null) {      // 锁库仓库
                entry.fdcStockId = it.lockStock.fitemId
                entry.stock = it.lockStock
            } else {
                entry.fdcStockId = stock!!.fitemId
                entry.stock = stock
            }
            if(it.lockStockPos != null) {   // 锁库库位
                entry.fdcSPId = it.lockStockPos.fspId
                entry.stockPlace = it.lockStockPos

            } else {
                if(stockPlace != null) {
                    entry.fdcSPId = stockPlace!!.fspId
                    entry.stockPlace = stockPlace
                }
            }

            if(it.free == 1) { // 是否赠品
                entry.fqty = it.fqty
            }
//            entry.fqty = it.useableQty
            entry.fprice = it.fprice
            entry.funitId = it.funitId
            entry.fsourceInterId = it.finterId
            entry.fsourceEntryId = it.fentryId
            entry.fsourceBillNo = it.seOrder.fbillNo
            entry.fsourceQty = it.fqty
            entry.fsourceTranType = 81
            entry.forderInterId = it.finterId
            entry.forderEntryId = it.fentryId
            entry.forderBillNo = it.seOrder.fbillNo
            entry.fdetailId = it.fdetailId
            entry.free = it.free

            entry.icItem = it.icItem
            entry.unit = it.unit

            entry.remark = ""
            entry.free = it.free
            if(it.isFocus > 0) { // 扫码对焦的行
                entry.fqty = 1.0
                // 记录条码
                if(it.icItem.batchManager == 1) { // 启用批次号
                    setBatchCode(entry, 1.0, bt.batchCode)

                } else if(it.icItem.snManager == 1) { // 启用序列号
                    setSnCode(entry, bt.snCode)

                } else { // 未启用
                    unStartBatchOrSnCode(entry, 1.0)
                }

                curPos = index

            } else {
                if(it.icstockBillEntryBarcodes != null && it.icstockBillEntryBarcodes.size > 0) {
                    it.icstockBillEntryBarcodes.forEach {
                        it.createUserName == user!!.username
                    }
                    entry.icstockBillEntryBarcodes.addAll(it.icstockBillEntryBarcodes)
                    entry.strBarcode = setStrBarcode(entry.icstockBillEntryBarcodes)
                }
            }

            checkDatas.add(entry)
            mAdapter!!.notifyDataSetChanged()
        }
    }


    /**
     * 扫描每一行的处理
     */
    private fun setICStockBill_Row(bt :BarcodeTable_App) {
        // 判断条码是否存在（启用批次，序列号）
        val listOrder = JsonUtil.stringToList(bt.relationObj, SeOrderEntry_App::class.java) as List<SeOrderEntry_App>
        if(checkDatas.size == 0) {
            setICStockEntry_SalOrder(bt, listOrder)

        } else {
            var isExist = false // 是否匹配

            checkDatas.forEachIndexed { index, it ->
                if (it.icstockBillEntryBarcodes.size > 0 && (it.icItem.batchManager.equals("Y") || it.icItem.snManager.equals("Y"))) {
                    it.icstockBillEntryBarcodes.forEach {
                        if (getValues(et_code).length > 0 && getValues(et_code) == it.barcode) {
                            Comm.showWarnDialog(mContext,"条码已使用！")
                            return
                        }
                    }
                }

                if(it.fsourceInterId == listOrder[0].finterId && it.fsourceEntryId == listOrder[0].fentryId) {
                    isExist = true
                    if(it.fqty >= it.fsourceQty) {
                        Comm.showWarnDialog(mContext,"第（"+(index+1)+"）行，数量已扫完！")
                        return
                    }
                    val addVal = BigdecimalUtil.add(it.fqty, 1.0)
                    it.fqty = addVal

                    // 记录条码
                    if(it.icItem.batchManager.equals("Y")) { // 启用批次号
                        setBatchCode(it, 1.0, bt.batchCode)

                    } else if(it.icItem.snManager.equals("Y")) { // 启用序列号
                        setSnCode(it, bt.snCode)

                    } else { // 未启用
                        unStartBatchOrSnCode(it, 1.0)
                    }

                }
            }
            if(!isExist) {
                Comm.showWarnDialog(mContext, "扫码的条码与订单不匹配！")
                return
            }
        }
        countNum()
        mAdapter!!.notifyDataSetChanged()

        if(!checkSave()) return
        // 1秒后执行保存功能
        mHandler.postDelayed(Runnable {
            run_save()
        },1000)
    }

    /**
     *  扫码之后    物料启用批次
     */
    fun setBatchCode(entry : ICStockBillEntry_App, fqty :Double, batchCode :String) {
        val entryBarcode = ICStockBillEntryBarcode_App()
        entryBarcode.parentId = entry.id
        entryBarcode.barcode = getValues(et_code)
        entryBarcode.batchCode = batchCode
        entryBarcode.snCode = ""
        entryBarcode.fqty = fqty
        entryBarcode.isUniqueness = 'Y'
        entryBarcode.againUse = 0
        entryBarcode.createUserName = user!!.username
        entryBarcode.billType = "DS_XSCK"

        entry.icstockBillEntryBarcodes.add(entryBarcode)
        entry.strBarcode = setStrBarcode(entry.icstockBillEntryBarcodes)
    }

    /**
     *  扫码之后    物料启用序列号
     */
    fun setSnCode(entry : ICStockBillEntry_App, snCode :String) {
        val entryBarcode = ICStockBillEntryBarcode_App()
        entryBarcode.parentId = entry.id
        entryBarcode.barcode = getValues(et_code)
        entryBarcode.batchCode = ""
        entryBarcode.snCode = snCode
        entryBarcode.fqty = 1.0
        entryBarcode.isUniqueness = 'Y'
        entryBarcode.againUse = 0
        entryBarcode.createUserName = user!!.username
        entryBarcode.billType = "DS_XSCK"

        entry.icstockBillEntryBarcodes.add(entryBarcode)
        entry.strBarcode = setStrBarcode(entry.icstockBillEntryBarcodes)
    }

    /**
     *  扫码之后    物料未启用
     */
    fun unStartBatchOrSnCode(entry : ICStockBillEntry_App, fqty :Double) {
        val entryBarcode = ICStockBillEntryBarcode_App()
        entryBarcode.parentId = entry.id
        entryBarcode.barcode = getValues(et_code)
        entryBarcode.batchCode = ""
        entryBarcode.snCode = ""
        entryBarcode.fqty = fqty
        entryBarcode.isUniqueness = 'N'
        entryBarcode.againUse = 0
        entryBarcode.createUserName = user!!.username
        entryBarcode.billType = "DS_XSCK"

        entry.icstockBillEntryBarcodes.add(entryBarcode)
        entry.strBarcode = setStrBarcode(entry.icstockBillEntryBarcodes)
    }

    /**
     * 设置拼接strBarcode的值
     */
    private fun setStrBarcode(list :List<ICStockBillEntryBarcode_App>) :String {
        val sb = StringBuffer()
        list.forEach {
            if(sb.indexOf(it.barcode) == -1 && sb.length == 0) sb.append(it.barcode)
            else if(sb.indexOf(it.barcode) == -1 && sb.length > 0) sb.append("，"+it.barcode)
        }
        return sb.toString()
    }

    /**
     * 统计数量
     */
    private fun countNum() {
        var needNum = 0.0
        var finishNum = 0.0
        checkDatas.forEach {
            needNum = BigdecimalUtil.add(needNum, it.fsourceQty)
            finishNum = BigdecimalUtil.add(finishNum, it.fqty)
        }
        tv_needNum.setText(df.format(needNum))
        tv_finishNum.setText(df.format(finishNum))
    }

    /**
     * 判断是否扫完数
     */
    private fun isFinish(): Boolean {
        checkDatas.forEach {
            if(it.fsourceQty > it.fqty) {
                return false
            }
        }
        return true
    }

    // 宿主调用的
    fun refreshOnActivityResult() {
        mHandler.sendEmptyMessage(SETFOCUS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SEL_POSITION -> {// 仓库	返回
                    stock = null
                    stockPlace = null
                    stock = data!!.getSerializableExtra("stock") as Stock_App
                    if (data!!.getSerializableExtra("stockPlace") != null) {
                        stockPlace = data!!.getSerializableExtra("stockPlace") as StockPlace_App
                    }
                    getStockGroup(null)
                }
                RESULT_NUM -> { // 数量	返回
                    val bundle = data!!.getExtras()
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        val num = parseDouble(value)
                        checkDatas.get(curPos).fqty = num

                        countNum()
                        mAdapter!!.notifyDataSetChanged()
                    }
                }
                RESULT_SALORDER_ORDER -> { // 选择单据   返回
//                    if(icstockBillEntry.fsourceTranType == 81) {
//                        val list = data!!.getSerializableExtra("obj") as List<SeOrderEntry>
//                        setICStockEntry_SeOrder(list)
//                    } else if(icstockBillEntry.fsourceTranType == 83){
//                        val list = data!!.getSerializableExtra("obj") as List<SeoutStockEntry>
//                        setICStockEntry_SeOutStock(list)
//                    }
                }
                WRITE_CODE -> {// 输入条码  返回
                    val bundle = data!!.extras
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        et_code!!.setText(value.toUpperCase())
                    }
                }
            }
        }
        mHandler.sendEmptyMessageDelayed(SETFOCUS,300)
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
     * 得到仓库组
     */
    fun getStockGroup(msgObj : String?) {
        if(msgObj != null) {
            stock = null
            stockPlace = null

            var caseId:Int = 0
            if(msgObj.indexOf("Stock_CaseId=1") > -1) {
                caseId = 1
            } else if(msgObj.indexOf("StockPlace_CaseId=2") > -1) {
                caseId = 2
            }

            when(caseId) {
                1 -> {
                    stock = JsonUtil.strToObject(msgObj, Stock_App::class.java)
                    tv_positionName.text = stock!!.fname
                }
                2 -> {
                    stockPlace = JsonUtil.strToObject(msgObj, StockPlace_App::class.java)
                    tv_positionName.text = stockPlace!!.fname
                    if(stockPlace!!.stock != null) stock = stockPlace!!.stock
                }
            }
        }

        if(stock != null ) {
            tv_positionName.text = stock!!.fname
        }
        if(stockPlace != null ) {
            tv_positionName.text = stockPlace!!.fname
        }

        if(stock != null) {
            // 自动跳到物料焦点
            smqFlag = '2'
            mHandler.sendEmptyMessage(SETFOCUS)
        }
    }

    /**
     * 扫码查询对应的方法
     */
    private fun run_smDatas() {
        showLoadDialog("加载中...", false)
        var mUrl:String? = null
        var barcode:String? = null
        var icstockBillId = ""
        var billType = "" // 单据类型
        var isWhole = "" // 是否查询整张销售订单
        when(smqFlag) {
            '1' -> {
                mUrl = getURL("stockPosition/findBarcodeGroup")
                barcode = getValues(et_positionCode)
            }
            '2' -> {
                mUrl = getURL("seOrder/findBarcode")
                barcode = getValues(et_code)
                isWhole = if(checkDatas.size == 0) "1" else ""
//                icstockBillId = parent!!.fragment1.icstockBill.id.toString()
//                billType = parent!!.fragment1.icstockBill.billType

            }
        }
        val formBody = FormBody.Builder()
                .add("barcode", barcode)
                .add("icstockBillId", icstockBillId)
                .add("billType", billType)
                .add("isWhole", isWhole)
//                .add("defaultDeptId", defaultDept!!.fitemId.toString())
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
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
                LogUtil.e("run_smDatas --> onResponse", result)
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNSUCC1, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(SUCC1, result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 保存
     */
    private fun run_save() {
        val listResult = java.util.ArrayList<ICStockBillEntry_App>()
        checkDatas.forEach {
            if(it.fqty > 0) listResult.add(it)
        }
        if(listResult.size == 0) {
            Comm.showWarnDialog(mContext,"请至少一行输入数量！")
            return
        }

        showLoadDialog("保存中...", false)
        var mUrl = getURL("stockBill_WMS/DS_SalOutStock")
        var mJson = JsonUtil.objectToString(listResult)
        val formBody = FormBody.Builder()
                .add("strJson", mJson)
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
     * 查询库存
     */
    private fun run_findInventoryQty() {
        showLoadDialog("加载中...")
        val mUrl = getURL("icInventory/findInventoryQty")
        val formBody = FormBody.Builder()
//                .add("fStockID", icstockBillEntry.fdcStockId.toString())
//                .add("fStockPlaceID",  icstockBillEntry.fdcSPId.toString())
//                .add("mtlId", icstockBillEntry.fitemId.toString())
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
                LogUtil.e("run_findListByParamWms --> onResponse", result)
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNSUCC2, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(SUCC2, result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 查询打印数据
     */
    private fun run_findPrintData(salOrderNo :String) {
        showLoadDialog("准备打印...", false)
        val mUrl = getURL("appPrint/printExpressNo")
        val formBody = FormBody.Builder()
                .add("expressNo", salOrderNo)
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNSUCC3)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                LogUtil.e("run_findPrintData --> onResponse", result)
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNSUCC3, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(SUCC3, result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 查询默认锁库发货默认部门
     */
    fun run_findLockDeliDept() {
        showLoadDialog("加载中...", false)
        val mUrl = getURL("department/findLockDeliDept")
        val formBody = FormBody.Builder()
                .add("isLockDeliDept", "1")
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNFIND_DEPT)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                LogUtil.e("run_findLockDeliDept --> onResponse", result)
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNFIND_DEPT, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(FIND_DEPT, result)
                mHandler.sendMessage(msg)
            }
        })
    }

    /**
     * 查询金蝶仓库的状态
     */
    private fun run_findStockStatus(strStockId :String) {
        var mUrl = getURL("stock/findStockStatus")
        val formBody = FormBody.Builder()
                .add("strStockId", strStockId)
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNFIND_STOCK_STATUS)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNFIND_STOCK_STATUS, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(FIND_STOCK_STATUS, result)
                LogUtil.e("run_findStockStatus --> onResponse", result)
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