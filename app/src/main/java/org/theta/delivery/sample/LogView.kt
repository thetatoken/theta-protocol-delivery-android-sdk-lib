package org.theta.delivery.sample

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.item_log.view.*
import org.theta.deliverysdk.models.*
import java.text.SimpleDateFormat
import java.util.*

class LogView : LinearLayout {

    private var blueColor: Int
    private var greenColor: Int
    private var redColor: Int
    private var whiteColor: Int
    private var turquoiseColor: Int
    private var dateFormat: SimpleDateFormat

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    init {
        View.inflate(context, R.layout.item_log, this)
        blueColor = ContextCompat.getColor(context, R.color.blue)
        greenColor = ContextCompat.getColor(context, R.color.green)
        redColor = ContextCompat.getColor(context, R.color.red)
        turquoiseColor = ContextCompat.getColor(context, R.color.turquoise)
        whiteColor = ContextCompat.getColor(context, R.color.white)
        dateFormat = SimpleDateFormat("mm:ss:SSS", Locale.getDefault())
    }

    fun setModel(eventWithDate: ThetaEventWithDate) {
        val event = eventWithDate.event

        when (event) {
            is ThetaTrafficEvent -> when (event.name) {
                ThetaTrafficEvent.TO_PEERS -> {
                    logContentText.text = resources.getString(R.string.fragment_sent_to_peers)
                    logContentText.setTextColor(greenColor)
                }
                ThetaTrafficEvent.FROM_PEERS -> {
                    logContentText.text = resources.getString(R.string.fragment_loaded_from_peers)
                    logContentText.setTextColor(blueColor)
                }
                ThetaTrafficEvent.FROM_EDGE_CACHER -> {
                    logContentText.text = resources.getString(R.string.fragment_loaded_from_edge_cacher)
                    logContentText.setTextColor(turquoiseColor)
                }
                ThetaTrafficEvent.FROM_CDN -> {
                    logContentText.text = resources.getString(R.string.fragment_loaded_from_cdn)
                    logContentText.setTextColor(redColor)
                }
            }
            is ThetaPeersChangedEvent -> {
                logContentText.text = resources.getString(R.string.new_peers_number, event.totalPeers)
                logContentText.setTextColor(whiteColor)
            }
            is ThetaInfoEvent -> {
                logContentText.text = event.message
                logContentText.setTextColor(whiteColor)
            }
            is ThetaUserWalletEvent -> {
                logContentText.text = resources.getString(R.string.wallet_desc,
                        event.address,
                        event.thetaWei,
                        event.tfuelWei.toString())
                logContentText.setTextColor(whiteColor)
            }
            is ThetaErrorEvent -> {
                logContentText.text = event.name
                logContentText.setTextColor(redColor)
            }
        }

        val formattedDate = dateFormat.format(eventWithDate.date) + ": "
        logTimeText.text = formattedDate
    }
}
