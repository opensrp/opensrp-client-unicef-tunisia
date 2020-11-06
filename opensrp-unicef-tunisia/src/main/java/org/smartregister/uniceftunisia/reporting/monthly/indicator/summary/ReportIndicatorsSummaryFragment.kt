package org.smartregister.uniceftunisia.reporting.monthly.indicator.summary

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_report_indicators_summary.*
import org.smartregister.uniceftunisia.R
import org.smartregister.uniceftunisia.reporting.ReportsDao
import org.smartregister.uniceftunisia.reporting.common.ReportingUtils
import org.smartregister.uniceftunisia.reporting.monthly.MonthlyReportsRepository
import org.smartregister.uniceftunisia.reporting.monthly.domain.MonthlyTally
import org.smartregister.uniceftunisia.reporting.monthly.indicator.ReportIndicatorsViewModel

/**
 * A [Fragment] subclass used to display list of report indicators
 */
class ReportIndicatorsSummaryFragment : Fragment() {

    private val reportIndicatorsRecyclerAdapter = ReportIndicatorsRecyclerAdapter()

    private val reportIndicatorsViewModel by activityViewModels<ReportIndicatorsViewModel>()
    { ReportingUtils.createFor(ReportIndicatorsViewModel(MonthlyReportsRepository.getInstance())) }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_report_indicators_summary, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        indicatorsRecyclerView.apply {
            adapter = reportIndicatorsRecyclerAdapter
            layoutManager = LinearLayoutManager(context)
            setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
        reportIndicatorsViewModel.monthlyTalliesMap.value?.let { displayIndicators(it) }
    }

    private fun displayIndicators(monthlyTallies: Map<String, MonthlyTally>) {
        val groupedTallies: Map<String, List<MonthlyTally>> = monthlyTallies.values.groupBy { it.grouping }
        reportIndicatorsRecyclerAdapter.apply {
            if (groupedTallies.isNotEmpty()) {
                val firstMonthlyTally: MonthlyTally = groupedTallies.values.first()[0]
                val submittedBy = requireContext().getString(R.string.submitted_by_,
                        ReportsDao.dateFormatter("dd/MM/YYYY").format(firstMonthlyTally.dateSent!!), firstMonthlyTally.providerId)
                submittedByTextView.text = submittedBy
                reportIndicators = groupedTallies.toList()
            }
        }
    }
}