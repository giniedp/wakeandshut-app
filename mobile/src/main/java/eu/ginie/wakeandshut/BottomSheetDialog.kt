package eu.ginie.wakeandshut

import android.annotation.SuppressLint
import android.app.Dialog
import android.support.design.widget.BottomSheetBehavior
import android.support.design.widget.BottomSheetDialogFragment
import android.support.design.widget.CoordinatorLayout
import android.view.View


class BottomSheetDialog : BottomSheetDialogFragment() {

    companion object {
        val BTN_REBOOT = 0
        val BTN_HIBERNATE = 1
        val BTN_SHUTDOWN = 3
        val BTN_WAKE = 4
        val BTN_DELETE = 5
    }

    lateinit var rec: Item
    lateinit var cb: (rec: Item, btn: Int) -> Unit

    fun setup(rec: Item, fn: (rec: Item, btn: Int) -> Unit) {
        this.rec = rec
        this.cb = fn
    }

    private fun onBtnClicked(btn: Int) {
        dismiss()
        cb(rec, btn)
    }

    private val bottomSheetCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onStateChanged(bottomSheet: View, newState: Int) {
            if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                dismiss()
            }
        }

        override fun onSlide(bottomSheet: View, slideOffset: Float) {}
    }

    @SuppressLint("RestrictedApi")
    override fun setupDialog(dialog: Dialog, style: Int) {
        super.setupDialog(dialog, style)

        val view = View.inflate(context, R.layout.bottom_sheet, null)

        val btnReboot = view.findViewById<View>(R.id.btn_reboot)
        val btnHibernate = view.findViewById<View>(R.id.btn_hibernate)
        val btnShutdown = view.findViewById<View>(R.id.btn_shutdown)
        val btnWake = view.findViewById<View>(R.id.btn_wake)
        val btnDelete = view.findViewById<View>(R.id.btn_delete)

        btnReboot.setOnClickListener { onBtnClicked(BTN_REBOOT) }
        btnHibernate.setOnClickListener { onBtnClicked(BTN_HIBERNATE) }
        btnShutdown.setOnClickListener { onBtnClicked(BTN_SHUTDOWN) }
        btnWake.setOnClickListener { onBtnClicked(BTN_WAKE) }
        btnDelete.setOnClickListener { onBtnClicked(BTN_DELETE) }

        if (rec.alive) {
            btnReboot.visibility = View.VISIBLE
            btnHibernate.visibility = View.VISIBLE
            btnShutdown.visibility = View.VISIBLE
            btnWake.visibility = View.GONE
            btnDelete.visibility = View.VISIBLE
        } else {
            btnReboot.visibility = View.GONE
            btnHibernate.visibility = View.GONE
            btnShutdown.visibility = View.GONE
            btnWake.visibility = View.VISIBLE
            btnDelete.visibility = View.VISIBLE
        }

        dialog.setContentView(view)

        val params = (view.parent as View).layoutParams as CoordinatorLayout.LayoutParams
        val behavior = params.behavior
        if (behavior != null && behavior is BottomSheetBehavior<*>) {
            behavior.setBottomSheetCallback(bottomSheetCallback)
        }
    }
}