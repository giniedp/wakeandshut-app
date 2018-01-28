package eu.ginie.wakeandshut

import android.app.AlertDialog
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView


class MainActivity : AppCompatActivity() {

    private var nsdHelper: NSDHelper? = null

    private val list = ArrayList<Item>()
    private val adapter = ItemListAdapter()
    private lateinit var recyclerView: RecyclerView
    private lateinit var refreshLayout: SwipeRefreshLayout
    private lateinit var settings: Settings

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        settings = Settings(this)
        list.addAll(settings.allHosts().map { Item(it) })
    }

    override fun onStart() {
        super.onStart()

        refreshLayout = findViewById(R.id.refresh_layout)
        refreshLayout.setOnRefreshListener { onSwipeRefresh() }

        recyclerView = findViewById(R.id.recycler_view)
        recyclerView.adapter = adapter

        adapter.setData(list)
        adapter.setClickListener(::onItemClicked)
    }

    override fun onResume() {
        super.onResume()
        startDiscovery()
    }

    override fun onPause() {
        super.onPause()
        stopDiscovery()
    }

    private fun onSwipeRefresh() {
        startDiscovery()
        refreshLayout.isRefreshing = false
        list.forEach { rec ->
            rec.sendPing("", {
                adapter.notifyItemChanged(list.indexOf(rec))
            })
            adapter.notifyItemChanged(list.indexOf(rec))
        }
    }

    private fun startDiscovery() {
        stopDiscovery()
        nsdHelper = NSDHelper(this, ::onItemFound)
        nsdHelper!!.startDiscovery()
    }

    private fun stopDiscovery() {
        if (nsdHelper != null) {
            nsdHelper!!.stopDiscovery()
            nsdHelper = null
        }
    }

    private fun onItemFound(item: Item) {
        settings.hostData(item.host, item.json)
        item.alive = true // item was just discovered, so it is alive
        val found = list.find { it.host == item.host }
        if (found != null) {
            val index = list.indexOf(found)
            list[index] = item
            adapter.notifyItemChanged(index)
        } else {
            list.add(item)
            adapter.notifyItemInserted(list.indexOf(item))
        }
    }

    private fun onItemClicked(item: Item) {
        val dialog = BottomSheetDialog()
        dialog.setup(item, ::onDialogResult)
        dialog.show(supportFragmentManager, dialog.tag)
    }

    private fun onDialogResult(item: Item, btn: Int) {
        when (btn) {
            BottomSheetDialog.BTN_DELETE -> actionDelete(item)
            BottomSheetDialog.BTN_HIBERNATE -> actionHibernate(item)
            BottomSheetDialog.BTN_REBOOT -> actionReboot(item)
            BottomSheetDialog.BTN_SHUTDOWN -> actionShutdown(item)
            BottomSheetDialog.BTN_WAKE -> actionWake(item)
        }
    }

    private fun actionDelete(item: Item) {
        askForConfirmation(item.name, "Delete this entry/", item, false, {
            val index = list.indexOf(item)
            list.removeAt(index)
            settings.hostData(item.host, null)
            adapter.notifyItemRemoved(index)
            showSuccessMessage("Item deleted")
        })
    }

    private fun actionHibernate(item: Item) {
        askForConfirmation(item.name, "Send hibernate request?", item, true, { pass ->
            item.sendHibernate(pass, { status ->
                if (status == 200) {
                    showSuccessMessage("Hibernate initiated")
                } else {
                    showFailureMessage("Failed to send Message: $status")
                }
            })
        })
    }

    private fun actionReboot(item: Item) {
        askForConfirmation(item.name, "Send reboot request?", item, true, { pass ->
            item.sendReboot(pass, { status ->
                if (status == 200) {
                    showSuccessMessage("Reboot initiated")
                } else {
                    showFailureMessage("Failed to send Message: $status")
                }
            })
        })
    }

    private fun actionShutdown(item: Item) {
        askForConfirmation(item.name, "Send shutdown request?", item, true, { pass ->
            item.sendShutdown(pass, { status ->
                if (status == 200) {
                    showSuccessMessage("Shutdown initiated")
                } else {
                    showFailureMessage("Failed to send Message: $status")
                }
            })
        })
    }

    private fun actionWake(item: Item) {
        askForConfirmation(item.name, "Send a WOL request?", item, false, {
            item.sendWakeup({ ok ->
                if (ok) {
                    showSuccessMessage("Wake on lan packet sent")
                } else {
                    showFailureMessage("Failed to send Wake on lan packet")
                }
            })
        })
    }

    private fun askForConfirmation(
            title: String,
            message: String,
            item: Item,
            pass: Boolean,
            cb: (pass: String) -> Unit) {
        AlertDialog
                .Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK", { d, _ ->
                    d.dismiss()
                    cb("")
                })
                .show()
    }

    private fun showSuccessMessage(message: String) {
        val bar = Snackbar.make(findViewById<CoordinatorLayout>(R.id.coordinator_layout), message, Snackbar.LENGTH_LONG)
        bar.view.setBackgroundColor(ContextCompat.getColor(this, R.color.colorSuccess))
        bar.show()
    }

    private fun showFailureMessage(message: String) {
        val bar = Snackbar.make(findViewById<CoordinatorLayout>(R.id.coordinator_layout), message, Snackbar.LENGTH_LONG)
        bar.view.setBackgroundColor(ContextCompat.getColor(this, R.color.colorDanger))
        bar.show()
    }
}
