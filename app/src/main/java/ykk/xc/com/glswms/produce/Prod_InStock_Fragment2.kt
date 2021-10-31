package ykk.xc.com.glswms.produce

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
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
import kotlinx.android.synthetic.main.prod_in_stock_fragment1.tv_deptSel
import kotlinx.android.synthetic.main.prod_in_stock_fragment2.*
import okhttp3.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.basics.Mtl_DialogActivity
import ykk.xc.com.glswms.basics.Stock_GroupDialogActivity
import ykk.xc.com.glswms.bean.EventBusEntity
import ykk.xc.com.glswms.bean.ICStockBillEntryBarcode_App
import ykk.xc.com.glswms.bean.ICStockBillEntry_App
import ykk.xc.com.glswms.bean.User
import ykk.xc.com.glswms.bean.k3Bean.ICItem_App
import ykk.xc.com.glswms.bean.k3Bean.ProdOrder_App
import ykk.xc.com.glswms.bean.k3Bean.StockPlace_App
import ykk.xc.com.glswms.bean.k3Bean.Stock_App
import ykk.xc.com.glswms.comm.BaseFragment
import ykk.xc.com.glswms.comm.Comm
import ykk.xc.com.glswms.util.JsonUtil
import ykk.xc.com.glswms.util.LogUtil
import java.io.IOException
import java.io.Serializable
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

/**
 * 日期：2019-10-16 09:50
 * 描述：生产入库---添加明细
 * 作者：ykk
 */
class Prod_InStock_Fragment2 : BaseFragment() {

    companion object {
        private val SEL_POSITION = 61
        private val SEL_MTL = 62
        private val SEL_PROD = 64
        private val SUCC1 = 200
        private val UNSUCC1 = 500
        private val SUCC2 = 201
        private val UNSUCC2 = 501
        private val SUCC3 = 202
        private val UNSUCC3 = 502
        private val SAVE = 203
        private val UNSAVE = 503

        private val SETFOCUS = 1
        private val SAOMA = 2
        private val RESULT_PRICE = 3
        private val RESULT_NUM = 4
        private val RESULT_REMAREK = 5
        private val WRITE_CODE = 6
    }
    private val context = this
    private var okHttpClient: OkHttpClient? = null
    private var user: User? = null
    private var stock: Stock_App? = null
    private var stockPlace: StockPlace_App? = null
    private var mContext: Activity? = null
    private val df = DecimalFormat("#.######")
    private var parent: Prod_InStock_MainActivity? = null
    private var isTextChange: Boolean = false // 是否进入TextChange事件
    private var timesTamp:String? = null // 时间戳
    var icstockBillEntry = ICStockBillEntry_App()
    private var smICStockBillEntry: ICStockBillEntry_App? = null // 扫码返回的对象
    private var autoICStockBillEntry: ICStockBillEntry_App? = null // 用于自动保存记录的对象
    private var smqFlag = '2' // 扫描类型1：位置扫描，2：物料扫描，3：箱码扫描
    private var prodOrderTemp :ProdOrder_App? = null
    private var icstockBillEntryId = 0

    // 消息处理
    private val mHandler = MyHandler(this)
    private class MyHandler(activity: Prod_InStock_Fragment2) : Handler() {
        private val mActivity: WeakReference<Prod_InStock_Fragment2>

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
                                m.resetStockGroup()
                                m.getStockGroup(msgObj)
                            }
                            '2'-> { // 物料
                                val list = JsonUtil.strToList(msgObj, ICItem_App::class.java)

                                if(list.size > 1) {
                                    // 打开物料列表供选择
                                    val bundle = Bundle()
                                    bundle.putSerializable("checkDatas", list as Serializable)
                                    m.showForResult(m.context, Prod_Confirm_Mtl_Sel_List_Dialog::class.java, SEL_MTL, bundle)

                                } else {
                                    // 打开生产订单列表供选择
                                    val bundle = Bundle()
                                    bundle.putInt("fitemId", list[0].fitemId)
                                    bundle.putInt("deptId", m.parent!!.fragment1.icstockBill.fdeptId)
                                    m.showForResult(m.context, Prod_Sel_List_Dialog::class.java, SEL_PROD, bundle)
                                }

                                /*
                                val mtlId = JsonUtil.strToString(msgObj)
                                // 打开生产订单列表供选择
                                val bundle = Bundle()
                                bundle.putInt("fitemId", mtlId.toInt())
                                bundle.putInt("deptId", m.parent!!.fragment1.icstockBill.fdeptId)
                                m.showForResult(m.context, Prod_Sel_List_Dialog::class.java, SEL_PROD, bundle)*/

                                /*val list = JsonUtil.strToList(msgObj, String::class.java)
                                val prodOrder = JsonUtil.stringToObject(list[0], ProdOrder_App::class.java) // 生产订单
                                val icEntry = JsonUtil.stringToObject(list[1], ICStockBillEntry_App::class.java) // 出入库分录
                                if(m.getValues(m.tv_mtlName).length > 0 && m.smICStockBillEntry != null && m.smICStockBillEntry!!.fsourceInterId != icEntry.fsourceInterId) {
                                    m.autoSave(icEntry) // 如果扫描第二次和第一次id一样，且分录id大于0，就自动保存

                                } else {
                                    m.getMaterial(prodOrder, icEntry)
                                }*/
                            }
                        }
                    }
                    UNSUCC1 -> { // 扫码失败
                        when(m.smqFlag) {
                            '1' -> { // 仓库位置扫描
                                m.tv_positionName.text = ""
                            }
                            '2' -> { // 物料扫描
                                m.tv_icItemName.text = ""
                            }
                        }
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "很抱歉，没有找到数据！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    SUCC2 -> { // 查询库存 进入
                        val fqty = JsonUtil.strToString(msgObj)
                        m.tv_stockQty.text = Html.fromHtml("即时库存：<font color='#6a5acd'>"+m.df.format(m.parseDouble(fqty))+"</font>")
                    }
                    UNSUCC2 -> { // 查询库存  失败
                        m.tv_stockQty.text = "即时库存：0"
                    }
                    SUCC3 -> { // 查询分录id 进入
                        val str = JsonUtil.strToString(msgObj) // 由id和数量拼接（1:10.5）
                        val arr = str.split(":")
                        m.icstockBillEntryId = m.parseInt(arr[0])
                        m.icstockBillEntry.fqty = m.parseDouble(arr[1])
                        m.tv_num.text = m.df.format(m.icstockBillEntry.fqty)
                        m.selProdAfter(m.prodOrderTemp!!)
                    }
                    UNSUCC3 -> { // 查询分录id  失败
                        m.icstockBillEntryId = 0
                        m.selProdAfter(m.prodOrderTemp!!)
                    }
                    SAVE -> { // 保存成功 进入
                        // 保存了分录，供应商就不能修改
//                        m.parent!!.fragment1.prdMoEntryList = null
                        EventBus.getDefault().post(EventBusEntity(21)) // 发送指令到fragment3，告其刷新
                        m.reset(1)
//                        m.toasts("保存成功✔")
                        // 如果有自动保存的对象，保存后就显示下一个
                        if(m.autoICStockBillEntry != null) {
                            m.toasts("自动保存成功✔")
                            m.getMaterial(null, m.autoICStockBillEntry!!)
                            m.autoICStockBillEntry = null

                        } else {
                            m.toasts("保存成功✔")
                        }
                    }
                    UNSAVE -> { // 保存失败
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "保存失败！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    SETFOCUS -> { // 当弹出其他窗口会抢夺焦点，需要跳转下，才能正常得到值
                        m.setFocusable(m.et_getFocus)
                        when(m.smqFlag) {
                            '1'-> m.setFocusable(m.et_positionCode)
                            '2'-> m.setFocusable(m.et_code)
                        }
                    }
                    SAOMA -> { // 扫码之后
                        when(m.smqFlag) {
                            '2' -> {
                                if (!m.checkSelStockPos()) return
//                                if(m.getValues(m.tv_mtlName).length > 0) {
//                                    Comm.showWarnDialog(m.mContext,"请先保存当前数据！")
//                                    m.isTextChange = false
//                                    return
//                                }
                            }
                        }
                        // 执行查询方法
                        m.run_smDatas()
                    }
                }
            }
        }
    }

    @Subscribe
    fun onEventBus(entity: EventBusEntity) {
        when (entity.caseId) {
            11 -> { // 接收第一个页面发来的指令
                reset(0)
            }
            31 -> { // 接收第三个页面发来的指令
                var icEntry = entity.obj as ICStockBillEntry_App
                btn_save.text = "保存"
                getICStockBillEntry(icEntry)
            }
        }
    }

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.prod_in_stock_fragment2, container, false)
    }

    override fun initView() {
        mContext = getActivity()
        parent = mContext as Prod_InStock_MainActivity
        EventBus.getDefault().register(this) // 注册EventBus

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
        hideSoftInputMode(mContext, et_positionCode)
        hideSoftInputMode(mContext, et_code)

        parent!!.fragment1.icstockBill.fselTranType = 85
        icstockBillEntry.fsourceTranType = 85

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
        if(spfStock.contains("BIND_PROD_STOCK")) {
            stock = showObjectByXml(Stock_App::class.java, "BIND_PROD_STOCK", saveDefaultStock)
        }
        if(spfStock.contains("BIND_PROD_STOCKPOS")) {
            stockPlace = showObjectByXml(StockPlace_App::class.java, "BIND_PROD_STOCKPOS", saveDefaultStock)
        }
        getStockGroup(null)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
        }
    }

    @OnClick(R.id.btn_scan, R.id.btn_mtlSel, R.id.btn_positionScan, R.id.btn_positionSel, R.id.tv_num,
             R.id.tv_remark, R.id.btn_save, R.id.btn_clone, R.id.tv_positionName, R.id.tv_icItemName)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_positionSel -> { // 选择仓库
                smqFlag = '1'
                val bundle = Bundle()
                bundle.putSerializable("stock", stock)
                bundle.putSerializable("stockPlace", stockPlace)
                showForResult(context, Stock_GroupDialogActivity::class.java, SEL_POSITION, bundle)
            }
            R.id.btn_mtlSel -> { // 选择物料
                if (!checkSelStockPos()) return
                smqFlag = '2'
                val bundle = Bundle()
                showForResult(Mtl_DialogActivity::class.java, SEL_MTL, bundle)
            }
            R.id.btn_positionScan -> { // 调用摄像头扫描（位置）
                smqFlag = '1'
                ScanUtil.startScan(mContext, BaseFragment.CAMERA_SCAN, HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create());
            }
            R.id.btn_scan -> { // 调用摄像头扫描（物料）
                if (!checkSelStockPos()) return
                smqFlag = '2'
                ScanUtil.startScan(mContext, BaseFragment.CAMERA_SCAN, HmsScanAnalyzerOptions.Creator().setHmsScanTypes(HmsScan.ALL_SCAN_TYPE).create());
            }
            R.id.tv_positionName -> { // 位置点击
                smqFlag = '1'
                mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
            }
            R.id.tv_icItemName -> { // 物料点击
                smqFlag = '2'
                mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
            }
            R.id.tv_num -> { // 数量
                showInputDialog("数量", icstockBillEntry.fqty.toString(), "0.0", RESULT_NUM)
            }
            R.id.tv_remark -> { // 备注
                showInputDialog("备注", icstockBillEntry.remark, "none", RESULT_REMAREK)
            }
            R.id.btn_save -> { // 保存
                if(!checkSave()) return
                icstockBillEntry.icstockBillId = parent!!.fragment1.icstockBill.id
//                icstockBillEntry.fkfDate = getValues(tv_fkfDate)
                run_save(null)
            }
            R.id.btn_clone -> { // 重置
                if (checkSaveHint()) {
                    val build = AlertDialog.Builder(mContext)
                    build.setIcon(R.drawable.caution)
                    build.setTitle("系统提示")
                    build.setMessage("您有未保存的数据，继续重置吗？")
                    build.setPositiveButton("是") { dialog, which -> reset(0) }
                    build.setNegativeButton("否", null)
                    build.setCancelable(false)
                    build.show()

                } else {
                    reset(0)
                }
            }
        }
    }

    /**
     *  自动保存信息
     */
    private fun autoSave(icEntry: ICStockBillEntry_App) {
        // 上次扫的和这次的不同，就自动保存
        if(!checkSave()) return
        icstockBillEntry.icstockBillId = parent!!.fragment1.icstockBill.id

        autoICStockBillEntry = icEntry // 加到自动保存对象
        run_save(null)
    }

    /**
     * 检查数据
     */
    fun checkSave() : Boolean {
        if(icstockBillEntry.fitemId == 0) {
            Comm.showWarnDialog(mContext, "请扫码物料条码！")
            return false
        }
        if (icstockBillEntry.fdcStockId == 0 || stock == null) {
            Comm.showWarnDialog(mContext, "请选择位置！")
            return false
        }
        if (icstockBillEntry.fqty == 0.0) {
            Comm.showWarnDialog(mContext, "请输入数量！")
            return false
        }
        if (icstockBillEntry.fqty > icstockBillEntry.fsourceQty) {
            Comm.showWarnDialog(mContext, "入库数量不能大于源单数！")
            return false
        }
        return true
    }

    /**
     * 检查是否选择位置
     */
    fun checkSelStockPos() : Boolean {
        if (icstockBillEntry.fdcStockId == 0 || stock == null) {
            Comm.showWarnDialog(mContext, "请选择位置！")
            return false
        }
        return true
    }

    /**
     * 选择了物料没有点击保存，点击了重置，需要提示
     */
    fun checkSaveHint() : Boolean {
        if(icstockBillEntry.fitemId > 0) {
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
     * 0：表示点击重置，1：表示保存后重置
     */
    private fun reset(flag : Int) {
//        if(parent!!.fragment1.icstockBill.fselTranType == 0 && flag == 0 ) {
        if(flag == 0 ) {
            icstockBillEntry.fsourceTranType = 0
            icstockBillEntry.fdcStockId = 0
            icstockBillEntry.fdcSPId = 0
            showLocalStockGroup()
        }
//        setEnables(tv_num, R.drawable.back_style_blue, true)
        et_code.setText("")
        et_positionCode.setText("")
        btn_save.text = "添加"
        tv_mtlName.text = ""
        tv_mtlNumber.text = "物料代码："
        tv_fmodel.text = "规格型号："
        tv_custNumber.text = "客户编码："
        tv_custDescribe.text = "客户描述："
        tv_boxQty.text = "箱数："
        tv_unitName.text = "单位："
        tv_sourceNo.text = "生产订单："
        tv_stockQty.text = "即时库存：0"
        tv_num.text = ""
        tv_sourceQty.text = ""
        tv_remark.text = ""

        icstockBillEntry.id = 0
        icstockBillEntry.icstockBillId = parent!!.fragment1.icstockBill.id
        icstockBillEntry.fitemId = 0
//        icstockBillEntry.fdcStockId = 0
//        icstockBillEntry.fdcSPId = 0
        icstockBillEntry.fqty = 0.0
        icstockBillEntry.fprice = 0.0
        icstockBillEntry.funitId = 0
        icstockBillEntry.remark = ""

        icstockBillEntry.icItem = null
        icstockBillEntry.icstockBillEntryBarcodes.clear()
        smICStockBillEntry = null

        timesTamp = user!!.getId().toString() + "-" + Comm.randomUUID()
        parent!!.isChange = false
        smqFlag = '2'
        mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
    }

    private fun getMaterial(prodOrder : ProdOrder_App?, icEntry : ICStockBillEntry_App) {
        if(parent!!.fragment1.icstockBill.fdeptId > 0 && prodOrder != null && parent!!.fragment1.icstockBill.fdeptId != prodOrder!!.dept.fitemId) {
            Comm.showWarnDialog(mContext,"请扫描相同（生产车间）的生产任务单条码！")
            return
        }
        if(parent!!.fragment1.icstockBill.fdeptId == 0) {
            val dept = prodOrder!!.dept
            parent!!.fragment1.tv_deptSel.text = dept.fname
            parent!!.fragment1.icstockBill.fdeptId = dept.fitemId
            parent!!.fragment1.icstockBill.department = dept
            // 显示生产成品仓
//            stock = dept.productStock
//            getStockGroup(null)
            // 发送指令到fragment1，告其保存
            parent!!.fragment1.saveNeedHint = false
            parent!!.fragment1.run_save()
        }

        smICStockBillEntry = icEntry

        if (icstockBillEntry.fqty >= icEntry.fsourceQty) {
            Comm.showWarnDialog(mContext, "数量已经扫完！")
            return
        }

        btn_save.text = "保存"
        // 记录条码，相同的不保存
        var isAddBarcode = true // 是否添加条码到记录表
        val icstockBillEntryBarcodes = icstockBillEntry.icstockBillEntryBarcodes
        icstockBillEntryBarcodes.forEach {
            if (getValues(et_code).equals(it.barcode)) {
                isAddBarcode = false
            }
        }
        if (isAddBarcode) {
            val icsBarcode = ICStockBillEntryBarcode_App()
            icsBarcode.parentId = smICStockBillEntry!!.id
            icsBarcode.barcode = getValues(et_code)
            icsBarcode.batchCode = ""
            icsBarcode.snCode = ""
            icsBarcode.fqty = icEntry.fsourceQty
            icsBarcode.isUniqueness = 'N'
            icsBarcode.againUse = 0
            icsBarcode.createUserName = user!!.username
            icsBarcode.billType = "SCRK"
            // 添加到list
            icstockBillEntryBarcodes.add(icsBarcode)
            icEntry.icstockBillEntryBarcodes = icstockBillEntryBarcodes
        }

        getICStockBillEntry(icEntry)
    }

    fun getICStockBillEntry(icEntry: ICStockBillEntry_App) {
        icstockBillEntry.id = icEntry.id
        icstockBillEntry.icstockBillId = icEntry.icstockBillId
        icstockBillEntry.fitemId = icEntry.fitemId
        icstockBillEntry.fentryId = icEntry.fentryId
        icstockBillEntry.fqty = icEntry.fqty
        icstockBillEntry.fdcStockId = icEntry.fdcStockId
        icstockBillEntry.fdcSPId = icEntry.fdcSPId
        icstockBillEntry.fsourceTranType = icEntry.fsourceTranType
        icstockBillEntry.fsourceInterId = icEntry.fsourceInterId
        icstockBillEntry.fsourceEntryId = icEntry.fsourceEntryId
        icstockBillEntry.fsourceBillNo = icEntry.fsourceBillNo
        icstockBillEntry.fsourceQty = icEntry.fsourceQty
        icstockBillEntry.fdetailId = icEntry.fdetailId
        icstockBillEntry.forderInterId = icEntry.forderInterId
        icstockBillEntry.forderEntryId = icEntry.forderEntryId
        icstockBillEntry.forderBillNo = icEntry.forderBillNo

        icstockBillEntry.fprice = icEntry.fprice
        icstockBillEntry.funitId = icEntry.funitId
        icstockBillEntry.remark = icEntry.remark

        icstockBillEntry.icItem = icEntry.icItem
        icstockBillEntry.unit = icEntry.unit
        icstockBillEntry.icstockBillEntryBarcodes = icEntry.icstockBillEntryBarcodes

        tv_mtlName.text = icEntry.icItem.fname
        tv_mtlNumber.text = Html.fromHtml("物料代码：<font color='#6a5acd'>"+ icEntry.icItem.fnumber +"</font>")
        tv_fmodel.text = Html.fromHtml("规格型号：<font color='#6a5acd'>"+ isNULLS(icEntry.icItem.fmodel) +"</font>")
        tv_custNumber.text = Html.fromHtml("客户编码：<font color='#6a5acd'>"+ isNULLS(icEntry.icItem.custNumber) +"</font>")
        tv_custDescribe.text = Html.fromHtml("客户描述：<font color='#6a5acd'>"+ isNULLS(icEntry.icItem.custDescribe) +"</font>")
        tv_boxQty.text = Html.fromHtml("箱数：<font color='#6a5acd'>"+ isNULLS(icEntry.icItem.boxQty) +"</font>")
        tv_unitName.text = Html.fromHtml("单位：<font color='#000000'>"+ icEntry.unit.fname +"</font>")
        tv_sourceNo.text = Html.fromHtml("生产订单：<font color='#6a5acd'>"+ icEntry.fsourceBillNo +"</font>")
        tv_sourceQty.text = if(icEntry.fsourceQty > 0) df.format(icEntry.fsourceQty) else ""
        tv_remark.text = icEntry.remark

        // 显示仓库
        if(icEntry.fdcStockId > 0) {
            stock = icEntry.stock
            stockPlace = icEntry.stockPlace
        }
        getStockGroup(null)

        tv_num.text = df.format(icEntry.fqty)
    }

    /**
     *  计算入库数量
     */
    private fun countInStockQty() {
        var fqty = 0.0
        icstockBillEntry.icstockBillEntryBarcodes.forEach {
            fqty += it.fqty
        }

        icstockBillEntry.fqty = fqty
        tv_num.text = df.format(fqty)
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
                SEL_MTL -> { //查询物料	返回
                    val icItem = data!!.getSerializableExtra("obj") as ICItem_App
                    // 打开生产订单列表供选择
                    val bundle = Bundle()
                    bundle.putInt("fitemId", icItem.fitemId)
                    bundle.putInt("deptId", parent!!.fragment1.icstockBill.fdeptId)
                    showForResult(context, Prod_Sel_List_Dialog::class.java, SEL_PROD, bundle)
                }
                SEL_PROD -> { // 查询生产订单 返回
                    val prodOrder = data!!.getSerializableExtra("obj") as ProdOrder_App
                    prodOrderTemp = prodOrder
                    run_findEntryId(parent!!.fragment1.icstockBill.id.toString(), prodOrder.finterId.toString())
                }
                RESULT_PRICE -> { // 单价	返回
                    val bundle = data!!.getExtras()
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        val price = parseDouble(value)
//                        tv_price.text = df.format(price)
//                        icstockBillEntry.fprice = price
//                        if(icstockBillEntry.fqty > 0) {
//                            val mul = BigdecimalUtil.mul(price, icstockBillEntry.fqty)
//                            tv_sumMoney.text = df.format(mul)
//                        }
                    }
                }
                RESULT_NUM -> { // 数量	返回
                    val bundle = data!!.getExtras()
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        val num = parseDouble(value)
                        tv_num.text = df.format(num)
                        icstockBillEntry.fqty = num
//                        if(icstockBillEntry.fprice > 0) {
//                            val mul = BigdecimalUtil.mul(num, icstockBillEntry.fprice)
//                            tv_sumMoney.text = df.format(mul)
//                        }
                    }
                }
                RESULT_REMAREK -> { // 备注	返回
                    val bundle = data!!.getExtras()
                    if (bundle != null) {
                        val value = bundle.getString("resultValue", "")
                        tv_remark.text = value
                        icstockBillEntry.remark = value
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

    private fun selProdAfter(prodOrder: ProdOrder_App) {
        var icEntry = ICStockBillEntry_App()
        icEntry.id = icstockBillEntryId
        icEntry.icstockBillId = parent!!.fragment1.icstockBill.id
        icEntry.fitemId = prodOrder.fitemId
        icEntry.funitId = prodOrder.funitId
        if(icstockBillEntry.fqty < prodOrder.useableQty) {
            icEntry.fqty = icstockBillEntry.fqty + 1
        } else {
            icEntry.fqty = prodOrder.useableQty
        }
        icEntry.fprice = 0.0
        icEntry.fsourceTranType = 85
        icEntry.fsourceInterId = prodOrder.finterId
        icEntry.fsourceEntryId = prodOrder.finterId
        icEntry.fsourceBillNo = prodOrder.fbillNo
        icEntry.fsourceQty = prodOrder.useableQty
        icEntry.fdetailId = prodOrder.finterId
        icEntry.forderInterId = prodOrder.fsourceInterId
        icEntry.forderEntryId = prodOrder.fsourceEntryId
        icEntry.forderBillNo = prodOrder.fsourceBillNo

        icEntry.icItem = prodOrder.icItem
        icEntry.unit = prodOrder.unit
        if(getValues(tv_mtlName).length > 0 && smICStockBillEntry != null && smICStockBillEntry!!.fsourceInterId != icEntry.fsourceInterId) {
            autoSave(icEntry) // 如果扫描第二次和第一次id一样，且分录id大于0，就自动保存

        } else {
            getMaterial(prodOrder, icEntry)
        }
    }

    fun resetStockGroup() {
        stock = null
        stockPlace = null
    }

    /**
     * 得到仓库组
     */
    fun getStockGroup(msgObj : String?) {
        // 重置数据
        icstockBillEntry.fdcStockId = 0
        icstockBillEntry.fdcSPId = 0
        icstockBillEntry.stock = null
        icstockBillEntry.stockPlace = null
        tv_positionName.text = ""

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
            icstockBillEntry.fdcStockId = stock!!.fitemId
            icstockBillEntry.stock = stock
        }
        if(stockPlace != null ) {
            tv_positionName.text = stockPlace!!.fname
            icstockBillEntry.fdcSPId = stockPlace!!.fspId
            icstockBillEntry.stockPlace = stockPlace
        }

        if(stock != null) {
            // 自动跳到物料焦点
            smqFlag = '2'
            mHandler.sendEmptyMessage(SETFOCUS)
        }

        if(icstockBillEntry.fitemId > 0 && icstockBillEntry.fdcStockId > 0) {
            // 查询即时库存
            run_findQty()
        }
    }

    /**
     * 扫码查询对应的方法
     */
    private fun run_smDatas() {
        isTextChange = false
        showLoadDialog("加载中...", false)
        var mUrl:String? = null
        var barcode:String? = null
        when(smqFlag) {
            '1' -> {
                mUrl = getURL("stockPosition/findBarcodeGroup")
                barcode = getValues(et_positionCode)
            }
            '2' -> {
//                mUrl = getURL("prodOrder/findMtlIdByBarcode")
                mUrl = getURL("prodOrder/findBarcodeByCheck")
                barcode = getValues(et_code)
            }
        }
        val formBody = FormBody.Builder()
                .add("barcode", barcode)
                .add("deptId", parent!!.fragment1.icstockBill.fdeptId.toString())
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
    private fun run_save(list: List<ICStockBillEntry_App>?) {
        showLoadDialog("保存中...", false)
        var mUrl:String? = null
        var mJson:String? = null
        var isInStock:String? = null // 装箱是否入库
        var updateBarCodeTableBatchCode:String? = null // 保存条码时，是否更新BarCodeTable 的批次号
        if(list != null) {
            mUrl = getURL("stockBill_WMS/saveEntryList")
            mJson = JsonUtil.objectToString(list)
            isInStock = "1"
            updateBarCodeTableBatchCode = ""
        } else {
            mUrl = getURL("stockBill_WMS/saveEntry")
            mJson = JsonUtil.objectToString(icstockBillEntry)
            isInStock = ""
            updateBarCodeTableBatchCode = "1"
        }
        val formBody = FormBody.Builder()
                .add("strJson", mJson)
                .add("isInStock", isInStock)
                .add("updateBarCodeTableBatchCode", updateBarCodeTableBatchCode)
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
    private fun run_findQty() {
        showLoadDialog("加载中...", false)
        val mUrl = getURL("icInventory/findQty")
        val formBody = FormBody.Builder()
                .add("fitemId", icstockBillEntry.fitemId.toString())
                .add("fstockId", icstockBillEntry.fdcStockId.toString())
                .add("fstockPlaceId",  icstockBillEntry.fdcSPId.toString())
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
                LogUtil.e("run_findQty --> onResponse", result)
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
     * 查询分录entryId
     */
    private fun run_findEntryId(icstockBillId :String, fsourceInterId :String) {
        showLoadDialog("加载中...", false)
        val mUrl = getURL("stockBill_WMS/findEntryId")
        val formBody = FormBody.Builder()
                .add("icstockBillId", icstockBillId)
                .add("fsourceInterId", fsourceInterId)
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
                LogUtil.e("run_findEntryId --> onResponse", result)
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
     * 得到用户对象
     */
    private fun getUserInfo() {
        if (user == null) user = showUserByXml()
    }

    override fun onDestroyView() {
        closeHandler(mHandler)
        mBinder!!.unbind()
        EventBus.getDefault().unregister(this)
        super.onDestroyView()
    }
}