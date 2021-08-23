package ykk.xc.com.glswms.basics

import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView.OnItemClickListener
import android.widget.BaseAdapter
import android.widget.TextView
import butterknife.OnClick
import kotlinx.android.synthetic.main.ab_public_input.*
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.comm.BaseDialogActivity
import java.lang.ref.WeakReference
import java.text.DecimalFormat

class PublicInputDialog : BaseDialogActivity() {

    private val context = this
    //    private GridView gridNums;
    //    private Button btn_confirm, btn_close;
    //    private TextView tv_hintName, tvClear, tv_tmp;
    //    private EditText et_input;
    private val nums = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0", ".", "-")
    private var inputType = "0"
    private var df = DecimalFormat("#.########")

    // 消息处理
    private val mHandler = PublicInputDialog.MyHandler(this)

    private class MyHandler(activity: PublicInputDialog) : Handler() {
        private val mActivity: WeakReference<PublicInputDialog>

        init {
            mActivity = WeakReference(activity)
        }

        override fun handleMessage(msg: Message) {
            val m = mActivity.get()
            if (m != null) {
                when (msg.what) {
                    SHOW_INPUT -> m.showKeyboard(m.et_input)
                    HIDE_INPUT -> m.hideKeyboard(m.et_input)
                }
            }
        }
    }

    override fun setLayoutResID(): Int {
        return R.layout.ab_public_input
    }

    override fun initData() {
        df = DecimalFormat("#.########")
        setListener()
        bundle()
    }

    override fun setListener() {
        et_input!!.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
            override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
            override fun afterTextChanged(s: Editable) {
                tv_clear!!.visibility = if (s.toString().length > 0) View.VISIBLE else View.GONE
            }
        })

    }

    /**
     * get send Data
     */
    private fun bundle() {
        val bundle = context.intent.extras
        if (bundle != null) {
            val hintName = bundle.getString("hintName", "")
            val showInfo = bundle.getString("showInfo", "")
            val value = bundle.getString("value", "")

            tv_hintName!!.text = hintName
            tv_showInfo!!.visibility = if (showInfo.length > 0) View.VISIBLE else View.GONE
            tv_showInfo!!.text = Html.fromHtml(showInfo)
            setTexts(et_input, value)

            // 0:表示数字，0.0：表示有小数点，+0：表示全部为数字都是正整数，none:调用系统输入键盘
            inputType = bundle.getString("inputType", "0")
            if (inputType == "0" || inputType == "0.0" || inputType == "+0") {
                // 如果传过来的值为0.0这种格式，就把.0去掉
                if (value.indexOf(".") > -1) {
                    val d = parseDouble(value)
                    setTexts(et_input, if (d > 0) df.format(d) else "")
                }
                nums[10] = if (inputType == "0") "" else "." // 如果为数字就把小数点去掉
                nums[10] = if (inputType == "+0") "" else "." // 如果为数字就把小数点去掉
                nums[11] = if (inputType == "+0") "" else "-" // 如果为数字就把小数点去掉

                et_input!!.isEnabled = false
                tv_tmp!!.visibility = View.GONE

                gridNums!!.visibility = View.VISIBLE
                val adapter = MyAdapter()
                gridNums!!.adapter = adapter

                gridNums!!.onItemClickListener = OnItemClickListener { arg0, arg1, pos, arg3 ->
                    val item = nums[pos]
                    //						if(item.equals("OK")) { // 返回回去
                    //							setResults(context, getValues(et_input));
                    //							context.finish();
                    //
                    if (item == "-") { // 减号，每次点击都到第一个位置
                        val `val` = getValues(et_input)
                        setTexts(et_input, "-" + `val`.replace("-", ""))

                        val val2 = getValues(et_input)
                        if (val2 == "-0") {
                            setTexts(et_input, "")
                        }

                    } else {
                        setTexts(et_input, getValues(et_input) + item)
                    }
                }
            } else {
                // 显示输入框
                mHandler.sendEmptyMessageDelayed(SHOW_INPUT, 200)

            }
        }
    }

    @OnClick(R.id.btn_close, R.id.btn_confirm, R.id.tv_clear)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_close -> {
                hideKeyboard(et_input)
                context.finish()
            }
            R.id.btn_confirm // 确定按钮
            -> {
                hideKeyboard(et_input)
                var inputName = getValues(et_input).trim { it <= ' ' }
                if (inputType == "0" || inputType == "0.0") {
                    //                    double num = parseDouble(inputName);
                    if (inputName.length == 0) {
                        toasts("请输入数量！")
                        return
                    }
                    inputName = df.format(parseDouble(inputName))

                } else if (inputName.length == 0) {
                    toasts("请输入内容！")
                    return
                }
                setResults(context, inputName)
                context.finish()
            }
            R.id.tv_clear -> et_input!!.setText("")
        }
    }

    /**
     * 适配器
     */
    private inner class MyAdapter : BaseAdapter() {

        override fun getCount(): Int {
            return nums.size
        }

        override fun getItem(position: Int): Any {
            return nums[position]
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        override fun getView(pos: Int, v: View?, parent: ViewGroup): View {
            var v = v
            var holder: ViewHolder? = null
            if (v == null) {
                v = context.layoutInflater.inflate(R.layout.ab_public_input_item, null)
                holder = ViewHolder(v!!)

                v.tag = holder
            } else {
                holder = v.tag as ViewHolder
            }
            holder.tv_item.text = nums[pos]

            return v
        }

        internal inner class ViewHolder(v: View) {
            var tv_item: TextView

            init {
                tv_item = v.findViewById<View>(R.id.tv_item) as TextView
            }
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            closeHandler(mHandler)
            context.finish()
        }
        return false
    }

    companion object {
        private val SHOW_INPUT = 100
        private val HIDE_INPUT = 102
    }

}
