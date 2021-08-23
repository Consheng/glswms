package ykk.xc.com.glswms.basics

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import butterknife.OnClick
import kotlinx.android.synthetic.main.ab_public_input2.*
import ykk.xc.com.glswms.R
import ykk.xc.com.glswms.comm.BaseDialogActivity

class PublicInputDialog2 : BaseDialogActivity() {

    private val context = this

    override fun setLayoutResID(): Int {
        return R.layout.ab_public_input2
    }

    override fun initData() {
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
        et_input2!!.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
            override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
            override fun afterTextChanged(s: Editable) {
                tv_clear2!!.visibility = if (s.toString().length > 0) View.VISIBLE else View.GONE
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
            val itemName = bundle.getString("itemName", "")
            val itemName2 = bundle.getString("itemName2", "")
            val value = bundle.getString("value", "")
            val value2 = bundle.getString("value2", "")

            tv_hintName!!.text = hintName
            tv_itemName!!.text = itemName
            tv_itemName2!!.text = itemName2
            setTexts(et_input, value)
            setTexts(et_input2, value2)
        }
    }

    @OnClick(R.id.btn_close, R.id.btn_confirm, R.id.tv_clear, R.id.tv_clear2)
    fun onViewClicked(view: View) {
        when (view.id) {
            R.id.btn_close -> context.finish()
            R.id.btn_confirm -> {
                val inputName = getValues(et_input).trim { it <= ' ' }
                if (inputName.length == 0) {
                    toasts("请输入" + getValues(tv_itemName) + "！")
                    return
                }
                val inputName2 = getValues(et_input2).trim { it <= ' ' }
                if (inputName2.length == 0) {
                    toasts("请输入" + getValues(tv_itemName2) + "！")
                    return
                }
                val bundle = Bundle()
                bundle.putString("resultVal1", inputName)
                bundle.putString("resultVal2", inputName2)
                setResults(context, bundle)
                context.finish()
            }
            R.id.tv_clear -> et_input!!.setText("")
            R.id.tv_clear2 -> et_input2!!.setText("")
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent): Boolean {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //            closeHandler(mHandler);
            context.finish()
        }
        return false
    }

}
