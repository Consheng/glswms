package ykk.xc.com.glswms.basics

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Message
import android.view.KeyEvent
import android.view.View
import butterknife.OnClick
import kotlinx.android.synthetic.main.ab_public_batch_num_input.*
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.comm.BaseDialogActivity
import ykk.xc.com.glswms.comm.Comm
import java.lang.ref.WeakReference
import java.text.DecimalFormat

/**
 * 单个批次数量输入dialog
 */
class BatchAndNumInputDialog : BaseDialogActivity() {

    private val context = this
    private val df = DecimalFormat("#.######")
    private var fqty = 0.0

    // 消息处理
    private val mHandler = MyHandler(this)

    private class MyHandler(activity: BatchAndNumInputDialog) : Handler() {
        private val mActivity: WeakReference<BatchAndNumInputDialog>

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
                    100 -> {
                        // 隐藏软键盘
                        m.hideKeyboard(m.currentFocus)
                        // 关闭
                        m.context.finish()
                    }
                }
            }
        }
    }

    override fun setLayoutResID(): Int {
        return R.layout.ab_public_batch_num_input
    }

    override fun initView() {
    }

    override fun initData() {
        val bundle = context.intent.extras
        if (bundle != null) {
            val mtlName = bundle.getString("mtlName")
            val batchCode = bundle.getString("batchCode")
            val fqty = bundle.getDouble("fqty")
            tv_mtlName.text = mtlName
            if(isNULLS(batchCode).length > 0) {
                et_batchCode.setText(batchCode)
                setEnables(et_batchCode, R.drawable.back_style_gray3b, false)
                setFocusable(et_fqty)
            }
            context.fqty = fqty
            if(fqty > 0) {
                et_fqty.setHint("可用:"+df.format(fqty))
            }
        }
    }

    // 监听事件
    @OnClick(R.id.btn_close, R.id.btn_confirm)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_close -> {
                mHandler.sendEmptyMessageDelayed(100, 200)
            }
            R.id.btn_confirm -> { // 确认
                val batchCode = getValues(et_batchCode)
                val fqty = Comm.parseDouble(getValues(et_fqty))
                if(batchCode.length == 0 && fqty > 0) {
                    Comm.showWarnDialog(context,"请输入批次号！")
                    return
                }
                if(fqty == 0.0 && batchCode.length > 0 ) {
                    Comm.showWarnDialog(context,"请输入数量！")
                    return
                }
                if(context.fqty > 0 && fqty > context.fqty) {
                    Comm.showWarnDialog(context,"输入的数量不能大于可用数！")
                    return
                }
                val intent = Intent()
                intent.putExtra("batchCode", batchCode)
                intent.putExtra("fqty", fqty)
                context.setResult(Activity.RESULT_OK, intent)

                mHandler.sendEmptyMessageDelayed(100, 200)
//                    // 隐藏软键盘
//                    hideKeyboard(currentFocus)
//                    context.finish()
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mHandler.sendEmptyMessageDelayed(100, 200)
        }
        return false
    }

}
