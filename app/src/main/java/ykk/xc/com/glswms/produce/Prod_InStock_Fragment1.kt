package ykk.xc.com.glswms.produce

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import butterknife.OnClick
import kotlinx.android.synthetic.main.prod_in_stock_fragment1.*
import kotlinx.android.synthetic.main.prod_in_stock_main.*
import okhttp3.*
import org.greenrobot.eventbus.EventBus
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.basics.Dept_DialogActivity
import ykk.xc.com.glswms.basics.Emp_DialogActivity
import ykk.xc.com.glswms.basics.Stock_DialogActivity
import ykk.xc.com.glswms.bean.EventBusEntity
import ykk.xc.com.glswms.bean.ICStockBill_App
import ykk.xc.com.glswms.bean.User
import ykk.xc.com.glswms.bean.k3Bean.Department_App
import ykk.xc.com.glswms.bean.k3Bean.Emp
import ykk.xc.com.glswms.bean.k3Bean.Stock_App
import ykk.xc.com.glswms.comm.BaseFragment
import ykk.xc.com.glswms.comm.Comm
import ykk.xc.com.glswms.util.JsonUtil
import ykk.xc.com.glswms.util.LogUtil
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit

/**
 * 日期：2019-10-16 09:50
 * 描述：生产入库
 * 作者：ykk
 */
class Prod_InStock_Fragment1 : BaseFragment() {

    companion object {
        private val SEL_DEPT = 10
        private val SEL_EMP1 = 12
        private val SEL_EMP2 = 13
        private val SEL_EMP3 = 14
        private val SEL_EMP4 = 15
        private val SEL_STOCK = 16
        private val SAVE = 202
        private val UNSAVE = 502
        private val FIND_ICSTOCKBILL = 204
        private val UNFIND_ICSTOCKBILL = 504

        private val SETFOCUS = 1
    }

    private val context = this
    private var okHttpClient: OkHttpClient? = null
    private var user: User? = null
    private var mContext: Activity? = null
    private var parent: Prod_InStock_MainActivity? = null
    private var timesTamp:String? = null // 时间戳
    var icstockBill = ICStockBill_App() // 保存的对象
    private val df = DecimalFormat("#.###") // 重量保存三位小数
    private var icstockBillId = 0 // 上个页面传来的id
    var saveNeedHint = true // 保存之后需要提示

    // 消息处理
    private val mHandler = MyHandler(this)

    private class MyHandler(activity: Prod_InStock_Fragment1) : Handler() {
        private val mActivity: WeakReference<Prod_InStock_Fragment1>

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
                    SAVE -> {// 保存成功 进入
                        val strId_pdaNo = JsonUtil.strToString(msgObj)
                        if(m.icstockBill.id == 0) {
                            val arr = strId_pdaNo.split(":") // id和pdaNo数据拼接（1:IC201912121）
                            m.icstockBill.id = m.parseInt(arr[0])
                            m.icstockBill.pdaNo = arr[1]
                            m.tv_pdaNo.text = arr[1]
                        }
                        if(m.saveNeedHint) {
                            m.parent!!.isMainSave = true
                            m.parent!!.viewPager.setScanScroll(true) // 放开左右滑动
                            m.toasts("保存成功✔")
                            // 滑动第二个页面
                            m.parent!!.viewPager!!.setCurrentItem(1, false)
                            m.parent!!.isChange = if(m.icstockBillId == 0) true else false
                        }
                        m.saveNeedHint = true
                    }
                    UNSAVE -> { // 保存失败
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "保存失败！"
                        Comm.showWarnDialog(m.mContext, errMsg)
                    }
                    FIND_ICSTOCKBILL -> { // 查询主表信息 成功
                        val icsBill = JsonUtil.strToObject(msgObj, ICStockBill_App::class.java)
                        m.setICStockBill(icsBill)
                    }
                    UNFIND_ICSTOCKBILL -> { // 查询主表信息 失败
                        errMsg = JsonUtil.strToString(msgObj)
                        if (m.isNULLS(errMsg).length == 0) errMsg = "查询信息有错误！2秒后自动关闭..."
                        Comm.showWarnDialog(m.mContext, errMsg)
                        m.mHandler.postDelayed(Runnable {
                            m.mContext!!.finish()
                        },2000)
                    }
                    SETFOCUS -> { // 当弹出其他窗口会抢夺焦点，需要跳转下，才能正常得到值
                        m.setFocusable(m.et_getFocus)
                        m.setFocusable(m.et_code)
                    }
                }
            }
        }
    }

    fun setICStockBill(m : ICStockBill_App) {
        icstockBill.id = m.id
        icstockBill.pdaNo = m.pdaNo
        icstockBill.fdate = m.fdate
        icstockBill.fsupplyId = m.fsupplyId
        icstockBill.fdeptId = m.fdeptId
        icstockBill.fempId = m.fempId
        icstockBill.fsmanagerId = m.fsmanagerId
        icstockBill.fmanagerId = m.fmanagerId
        icstockBill.ffmanagerId = m.ffmanagerId
        icstockBill.fbillerId = m.fbillerId
        icstockBill.fselTranType = m.fselTranType

        icstockBill.yewuMan = m.yewuMan          // 业务员
        icstockBill.baoguanMan = m.baoguanMan          // 保管人
        icstockBill.fuzheMan = m.fuzheMan           // 负责人
        icstockBill.yanshouMan = m.yanshouMan            // 验收人
        icstockBill.createUserId = m.createUserId        // 创建人id
        icstockBill.createUserName = m.createUserName        // 创建人
        icstockBill.createDate = m.createDate            // 创建日期
        icstockBill.isToK3 = m.isToK3                   // 是否提交到K3
        icstockBill.k3Number = m.k3Number                // k3返回的单号
        icstockBill.dcllStockId = m.dcllStockId

        icstockBill.supplier = m.supplier
        icstockBill.department = m.department
        icstockBill.dcllStock = m.dcllStock

        tv_pdaNo.text = m.pdaNo
        tv_inDateSel.text = m.fdate
        if(m.department != null) {
            tv_deptSel.text = m.department.fname
        }
        tv_emp2Sel.text = m.baoguanMan
        tv_emp4Sel.text = m.yanshouMan
        if(m.dcllStock != null) {
            tv_dcllStockSel.text = m.dcllStock.fname
        }

        parent!!.isChange = false
        parent!!.isMainSave = true
        parent!!.viewPager.setScanScroll(true) // 放开左右滑动
        EventBus.getDefault().post(EventBusEntity(12)) // 发送指令到fragment3，查询分类信息
    }

    override fun setLayoutResID(inflater: LayoutInflater, container: ViewGroup): View {
        return inflater.inflate(R.layout.prod_in_stock_fragment1, container, false)
    }

    override fun initView() {
        mContext = getActivity()
        parent = mContext as Prod_InStock_MainActivity

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
        tv_inDateSel.text = Comm.getSysDate(7)
        tv_operationManName.text = user!!.erpUserName
        tv_emp2Sel.text = user!!.empName
        tv_emp4Sel.text = user!!.empName

        icstockBill.billType = "SCRK"
        icstockBill.ftranType = 2
        icstockBill.frob = 1
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

        bundle()
    }

    fun bundle() {
        val bundle = mContext!!.intent.extras
        if(bundle != null) {
            if(bundle.containsKey("id")) { // 查询过来的
                icstockBillId = bundle.getInt("id") // ICStockBill主表id
                // 查询主表信息
                run_findStockBill(icstockBillId)
            }
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            mHandler.sendEmptyMessageDelayed(SETFOCUS, 200)
        }
    }

    @OnClick(R.id.tv_inDateSel, R.id.tv_deptSel, R.id.tv_dcllStockSel, R.id.tv_emp2Sel, R.id.tv_emp4Sel,
             R.id.btn_save, R.id.btn_clone)
    fun onViewClicked(view: View) {
        var bundle: Bundle? = null
        when (view.id) {
            R.id.tv_inDateSel -> { // 选择日期
                Comm.showDateDialog(mContext, tv_inDateSel, 0)
            }
            R.id.tv_deptSel -> { // 选择部门
                showForResult(Dept_DialogActivity::class.java, SEL_DEPT, null)
            }
            R.id.tv_dcllStockSel -> { // 选择仓库
                showForResult(Stock_DialogActivity::class.java, SEL_STOCK, null)
            }
//            R.id.tv_emp1Sel -> { // 选择业务员
//                bundle = Bundle()
//                bundle.putString("accountType", "SC")
//                showForResult(Emp_DialogActivity::class.java, SEL_EMP1, bundle)
//            }
            R.id.tv_emp2Sel -> { // 选择保管者
                bundle = Bundle()
                bundle.putString("accountType", "SC")
                showForResult(Emp_DialogActivity::class.java, SEL_EMP2, bundle)
            }
//            R.id.tv_emp3Sel -> { // 选择负责人
//                bundle = Bundle()
//                bundle.putString("accountType", "SC")
//                showForResult(Emp_DialogActivity::class.java, SEL_EMP3, bundle)
//            }
            R.id.tv_emp4Sel -> { // 选择验收人
                bundle = Bundle()
                bundle.putString("accountType", "SC")
                showForResult(Emp_DialogActivity::class.java, SEL_EMP4, bundle)
            }
            R.id.btn_save -> { // 保存
                if(!checkSave(true)) return
                run_save()
            }
            R.id.btn_clone -> { // 重置
                if (parent!!.isChange) {
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

    /**
     * 保存检查数据判断
     */
    fun checkSave(isHint :Boolean) : Boolean {
//        if (icstockBill.fdeptId == 0) {
//            Comm.showWarnDialog(mContext, "请选择部门！")
//            return false
//        }
        if (icstockBill.dcllStockId == 0) {
            Comm.showWarnDialog(mContext, "请选择倒冲仓库！")
            return false
        }
        if(icstockBill.fsmanagerId == 0) {
            if(isHint) Comm.showWarnDialog(mContext, "请选择保管人！")
            return false
        }
        if(icstockBill.ffmanagerId == 0) {
            if(isHint) Comm.showWarnDialog(mContext, "请选择验收人！")
            return false
        }
        return true
    }

    override fun setListener() {

    }

    fun reset() {
//        setEnables(tv_deptSel, R.drawable.back_style_blue2, true)
        parent!!.isMainSave = false
        parent!!.viewPager.setScanScroll(false) // 禁止滑动
        tv_pdaNo.text = ""
        tv_inDateSel.text = Comm.getSysDate(7)
        tv_deptSel.text = ""
        tv_dcllStockSel.text = ""
//        tv_emp2Sel.text = ""
//        tv_emp4Sel.text = ""
//        tv_weightUnitType.text = "千克（kg）"
        icstockBill.id = 0
        icstockBill.fselTranType = 0
        icstockBill.pdaNo = ""
        icstockBill.fsupplyId = 0
        icstockBill.fdeptId = 0
        icstockBill.dcllStockId = 0
        icstockBill.dcllStock = null
//        icstockBill.fempId = 0
//        icstockBill.fsmanagerId = 0
//        icstockBill.fmanagerId = 0
//        icstockBill.ffmanagerId = 0
//        icstockBill.yewuMan = ""
//        icstockBill.baoguanMan = ""
//        icstockBill.fuzheMan = ""
//        icstockBill.yanshouMan = ""
        icstockBill.supplier = null
        icstockBill.department = null

        timesTamp = user!!.getId().toString() + "-" + Comm.randomUUID()
        parent!!.isChange = false
        saveNeedHint = true
        EventBus.getDefault().post(EventBusEntity(11)) // 发送指令到fragment2，告其清空
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                SEL_DEPT -> {//查询部门	返回
                    val dept = data!!.getSerializableExtra("obj") as Department_App
//                    if(dept.productStockId == 0) {
//                        Comm.showWarnDialog(mContext,"该仓库没有设置成品仓！")
//                        return
//                    }
                    tv_deptSel.text = dept!!.fname
                    icstockBill.fdeptId = dept.fitemId
                    icstockBill.department = dept
                }
                SEL_STOCK -> {//查询仓库	返回
                    val stock = data!!.getSerializableExtra("obj") as Stock_App
                    tv_dcllStockSel.text = stock.fname
                    icstockBill.dcllStockId = stock.fitemId
                    icstockBill.dcllStock = stock
                }
                SEL_EMP2 -> {//查询保管人	返回
                    val emp = data!!.getSerializableExtra("obj") as Emp
                    tv_emp2Sel.text = emp!!.fname
                    icstockBill.fsmanagerId = emp.fitemId
                    icstockBill.baoguanMan = emp.fname
                }
                SEL_EMP4 -> {//查询验收人	返回
                    val emp = data!!.getSerializableExtra("obj") as Emp
                    tv_emp4Sel.text = emp!!.fname
                    icstockBill.ffmanagerId = emp.fitemId
                    icstockBill.yanshouMan = emp.fname
                }
            }
        }
    }

    /**
     * 保存
     */
    fun run_save() {
        icstockBill.fdate = getValues(tv_inDateSel)

        showLoadDialog("保存中...", false)
        val mUrl = getURL("stockBill_WMS/save")

        val mJson = JsonUtil.objectToString(icstockBill)
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
     *  查询主表信息
     */
    private fun run_findStockBill(id: Int) {
        val mUrl = getURL("stockBill_WMS/findStockBill")

        val formBody = FormBody.Builder()
                .add("id", id.toString())
                .build()

        val request = Request.Builder()
                .addHeader("cookie", getSession())
                .url(mUrl)
                .post(formBody)
                .build()

        val call = okHttpClient!!.newCall(request)
        call.enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                mHandler.sendEmptyMessage(UNFIND_ICSTOCKBILL)
            }

            @Throws(IOException::class)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body()
                val result = body.string()
                if (!JsonUtil.isSuccess(result)) {
                    val msg = mHandler.obtainMessage(UNFIND_ICSTOCKBILL, result)
                    mHandler.sendMessage(msg)
                    return
                }
                val msg = mHandler.obtainMessage(FIND_ICSTOCKBILL, result)
                LogUtil.e("run_missionBillModifyStatus --> onResponse", result)
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