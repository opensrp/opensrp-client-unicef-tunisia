package org.smartregister.path.reporting.monthly.daily

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.ConfigurationCompat
import androidx.recyclerview.widget.RecyclerView
import com.github.florent37.expansionpanel.viewgroup.ExpansionLayoutCollection
import kotlinx.android.extensions.LayoutContainer
import kotlinx.android.synthetic.main.sent_monthly_report_list_item.view.*
import kotlinx.android.synthetic.main.sent_reports_expansion_panel_item.*

import org.smartregister.path.reporting.monthly.domain.DailyTally
import org.smartregister.uniceftunisia.R
import org.smartregister.uniceftunisia.reporting.common.ReportingUtils.dateFormatter
import org.smartregister.uniceftunisia.reporting.common.translateString

class DailyTalliesRecyclerAdapter(val onClickListener: View.OnClickListener) :
        RecyclerView.Adapter<DailyTalliesRecyclerAdapter.SentReportsRecyclerHolder>() {

    var dailyTallies: List<Pair<String, List<DailyTally>>> = arrayListOf()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    private val expansionsCollection = ExpansionLayoutCollection()

    init {
        expansionsCollection.openOnlyOne(false)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SentReportsRecyclerHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sent_reports_expansion_panel_item, parent, false)
        return SentReportsRecyclerHolder(view)
    }

    override fun onBindViewHolder(holderSentReports: SentReportsRecyclerHolder, position: Int) {
        holderSentReports.bindViews(dailyTallies[position])
        expansionsCollection.add(holderSentReports.sentReportsExpansionLayout.apply { collapse(false) })
    }

    override fun getItemCount() = dailyTallies.size

    inner class SentReportsRecyclerHolder(override val containerView: View) : RecyclerView.ViewHolder(containerView), LayoutContainer {

        fun bindViews(dailyTallies: Pair<String, List<DailyTally>>) {
            val (day, tallies) = dailyTallies

            //Set year header
            val dayDate = dateFormatter("MMMM yyyy").parse(day)
            yearHeaderTextView.text = dateFormatter("MMMM yyyy", locale = ConfigurationCompat.getLocales(sentReportContainer.resources.configuration)[0]).format(dayDate!!)

            //Set tallies
            sentReportContainer.removeAllViews()
            tallies.forEach {
                val view = LayoutInflater.from(containerView.context).inflate(R.layout.sent_monthly_report_list_item,
                        sentReportContainer, false).apply {
                    tag = it
                    dateReportSentTextView.text = dateFormatter("dd MMMM yy", locale = ConfigurationCompat.getLocales(resources.configuration)[0]).format(it.day)
                    sentReportDetailsTextView.visibility = View.GONE
                    setOnClickListener(onClickListener)
                }
                sentReportContainer.addView(view)
            }
        }
    }
}