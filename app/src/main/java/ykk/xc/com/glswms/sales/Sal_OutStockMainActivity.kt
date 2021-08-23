package ykk.xc.com.glswms.sales

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.support.v4.app.Fragment
import android.support.v4.view.ViewPager
import android.view.KeyEvent
import android.view.View
import android.widget.TextView
import butterknife.OnClick
import com.huawei.hms.hmsscankit.ScanUtil
import com.huawei.hms.ml.scan.HmsScan
import kotlinx.android.synthetic.main.sal_out_main.*
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.comm.BaseActivity
import ykk.xc.com.glswms.comm.BaseFragment
import ykk.xc.com.glswms.comm.Comm
import ykk.xc.com.glswms.util.adapter.BaseFragmentAdapter
import java.text.DecimalFormat

/**
 * 日期：2019-10-16 09:14
 * 描述：销售出库
 * 作者：ykk
 */
class Sal_OutStockMainActivity : BaseActivity() {

    private val context = this
    private val TAG = "Sal_DS_OutStockMainActivity"
    private var curRadio: View? = null
    private var curRadioName: TextView? = null
    var isChange: Boolean = false // 返回的时候是否需要判断数据是否保存了
//    private val listMaps = ArrayList<Map<String, Any>>()
    private val df = DecimalFormat("#.####")
    val fragment1 = Sal_OutStockFragment1()
    var isMainSave = false // 主表信息是否保存

    override fun setLayoutResID(): Int {
        return R.layout.sal_out_main
    }

    override fun initData() {
        bundle()
        curRadio = viewRadio1
//        curRadioName = tv_radioName1
        val listFragment = ArrayList<Fragment>()
//        Bundle bundle2 = new Bundle();
//        bundle2.putSerializable("customer", customer);
//        fragment1.setArguments(bundle2); // 传参数
//        fragment2.setArguments(bundle2); // 传参数
//        Pur_ScInFragment1 fragment1 = new Pur_ScInFragment1();
//        Sal_OutFragment2 fragment2 = new Sal_OutFragment2();
//        Sal_OutFragment3 fragment3 = new Sal_OutFragment3();

        listFragment.add(fragment1)
        viewPager.setScanScroll(false); // 禁止左右滑动
        //ViewPager设置适配器
        viewPager.setAdapter(BaseFragmentAdapter(supportFragmentManager, listFragment))
        //设置ViewPage缓存界面数，默认为1
        viewPager.offscreenPageLimit = 1
        //ViewPager显示第一个Fragment
        viewPager!!.setCurrentItem(0)

        //ViewPager页面切换监听
        viewPager!!.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {

            }

            override fun onPageSelected(position: Int) {
//                when (position) {
//                    0 -> tabChange(viewRadio1!!, tv_radioName1, "表头", 0)
//                    1 -> tabChange(viewRadio2!!, tv_radioName2, "添加分录", 1)
//                    2 -> tabChange(viewRadio3!!, tv_radioName3, "表体", 2)
//                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })

    }

    private fun bundle() {
        val bundle = context.intent.extras
        if (bundle != null) {
        }
    }

    @OnClick(R.id.btn_close, R.id.lin_tab1, R.id.lin_tab2, R.id.lin_tab3, R.id.btn_appointment)
    fun onViewClicked(view: View) {
        // setCurrentItem第二个参数控制页面切换动画
        //  true:打开/false:关闭
        //  viewPager.setCurrentItem(0, false);

        when (view.id) {
            R.id.btn_close // 关闭
            -> {
                if (isChange) {
                    val build = AlertDialog.Builder(context)
                    build.setIcon(R.drawable.caution)
                    build.setTitle("系统提示")
                    build.setMessage("您有未保存的数据，继续关闭吗？")
                    build.setPositiveButton("是", object : DialogInterface.OnClickListener {
                        override fun onClick(dialog: DialogInterface, which: Int) {
                            context.finish()
                        }
                    })
                    build.setNegativeButton("否", null)
                    build.setCancelable(false)
                    build.show()

                } else {
                    context.finish()
                }
            }
            R.id.btn_appointment -> { // 预约
//                context.fragment1.appointment()
            }
            R.id.lin_tab1 -> {
//                tabChange(viewRadio1!!, tv_radioName1, "表头", 0)
            }
            R.id.lin_tab2 -> {
                if(isMainSave) {
//                    tabChange(viewRadio2!!, tv_radioName2, "添加分录", 1)
                } else {
                    Comm.showWarnDialog(context,"请先完善（表头）信息！")
                }
            }
            R.id.lin_tab3 -> {
                if(isMainSave) {
//                    tabChange(viewRadio3!!, tv_radioName3, "表体", 2)
                } else {
                    Comm.showWarnDialog(context,"请先完善（表头）信息！")
                }
            }
        }
    }

    /**
     * 选中之后改变样式
     */
    private fun tabSelected(v: View, tv: TextView) {
        curRadio!!.setBackgroundResource(R.drawable.check_off2)
        v.setBackgroundResource(R.drawable.check_on)
        curRadio = v
        curRadioName!!.setTextColor(Color.parseColor("#000000"))
        tv.setTextColor(Color.parseColor("#FF4400"))
        curRadioName = tv
    }

    private fun tabChange(view: View, tv: TextView, str: String, page: Int) {
        tabSelected(view, tv)
//        tv_title.text = str
        viewPager!!.setCurrentItem(page, false)
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            // 当选择蓝牙的时候按了返回键
            if (data == null) return
            when (requestCode) {
                BaseFragment.CAMERA_SCAN -> {// 扫一扫成功  返回
                    val hmsScan = data!!.getParcelableExtra(ScanUtil.RESULT) as HmsScan
                    if (hmsScan != null) {
                        fragment1.getScanData(hmsScan.originalValue)
                    }
                }
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            context.finish()
        }
        return false
    }
}