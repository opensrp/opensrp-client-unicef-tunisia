package org.smartregister.uniceftunisia.reporting.monthly.indicator

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.core.os.ConfigurationCompat
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.vijay.jsonwizard.activities.MultiLanguageActivity
import kotlinx.android.synthetic.main.activity_report_indicators.*
import kotlinx.android.synthetic.main.sent_reports_expansion_panel_item.*
import kotlinx.coroutines.launch
import org.smartregister.path.reporting.monthly.domain.DailyTally
import org.smartregister.path.reporting.monthly.domain.MonthlyTally
import org.smartregister.uniceftunisia.R
import org.smartregister.uniceftunisia.reporting.common.*
import org.smartregister.uniceftunisia.reporting.monthly.MonthlyReportsActivity
import org.smartregister.util.LangUtils

class ReportIndicatorsActivity : MultiLanguageActivity() {

    val reportIndicatorsViewModel by viewModels<ReportIndicatorsViewModel>
    { ReportingUtils.createFor(ReportIndicatorsViewModel()) }

    lateinit var navController: NavController

    private var translatedYearMonth: String? = null

    override fun attachBaseContext(base: Context?) {
        val language = LangUtils.getLanguage(base)
        val newConfiguration = LangUtils.setAppLocale(base, language)
        super.attachBaseContext(base)
        applyOverrideConfiguration(newConfiguration)
    }

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_report_indicators)
        setSupportActionBar(reportIndicatorsToolbar)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.reportIndicatorsNavController) as NavHostFragment
        navController = navHostFragment.navController

        with(intent) {
            var serializableExtra = getSerializableExtra(MONTHLY_TALLIES)
            serializableExtra?.let {
                getStringExtra(YEAR_MONTH)?.let {
                    reportIndicatorsViewModel.yearMonth.value = it
                    translatedYearMonth = it.convertToNamedMonth(hasHyphen = true).translateString(this@ReportIndicatorsActivity)
                }

                if (serializableExtra is Map<*, *>)
                    reportIndicatorsViewModel.monthlyTalliesMap.value = (serializableExtra as Map<String, MonthlyTally>).toMutableMap()

                //Navigate to ReportIndicatorsSummaryFragment when show data is true
                if (getBooleanExtra(SHOW_DATA, false)) {
                    navController.navigate(R.id.reportIndicatorsSummaryFragment)
                    saveFormButton.visibility = View.GONE
                    verticalDivider.visibility = View.GONE
                } else {
                    navController.navigate(R.id.reportIndicatorsFormFragment)
                    saveFormButton.visibility = View.VISIBLE
                    verticalDivider.visibility = View.VISIBLE
                }

                yearMonthTextView.text = if (intent.getBooleanExtra(SHOW_DATA, false))
                    getString(R.string.monthly_sent_reports_with_year, translatedYearMonth) else
                    getString(R.string.month_year_draft, translatedYearMonth)

                backButton.setOnClickListener { navigateToMonthlyReports(if (getBooleanExtra(SHOW_DATA, false)) 2 else 1) }
            } ?: run {
                serializableExtra = getSerializableExtra(DAILY_TALLIES)

                getStringExtra(DAY)?.let {
                    reportIndicatorsViewModel.day.value = it

                }

                if (serializableExtra is Map<*, *>)
                    reportIndicatorsViewModel.dailyTalliesMap.value = (serializableExtra as Map<String, DailyTally>).toMutableMap()

                val day = ReportingUtils.dateFormatter("dd MMMM yy").parse(getStringExtra(DAY)!!)
                yearMonthTextView.text = getString(R.string.daily_tallies_with_day, ReportingUtils.dateFormatter("dd MMMM yy",
                    locale = ConfigurationCompat.getLocales(resources.configuration)[0]).format(day!!))

                //Navigate to ReportIndicatorsSummaryFragment when show data is true
                if (getBooleanExtra(SHOW_DATA, false)) {
                    navController.navigate(R.id.reportIndicatorsDailySummaryFragment)
                    saveFormButton.visibility = View.GONE
                    verticalDivider.visibility = View.GONE
                }

                backButton.setOnClickListener { navigateToMonthlyReports(0) }
            }
        }

        saveFormButton.setOnClickListener { submitMonthlyDraft() }
    }

    private fun submitMonthlyDraft() {
        lifecycleScope.launch {
            when (reportIndicatorsViewModel.saveMonthlyDraft()) {
                true -> {
                    reportIndicatorsRootLayout.showSnackBar(R.string.monthly_draft_saved)
                    navigateToMonthlyReports()
                }
                else -> reportIndicatorsRootLayout.showSnackBar(R.string.error_saving_draft_reports)
            }
        }
    }

    private fun navigateToMonthlyReports(selectTab: Int = 1) {
        startActivity(Intent(this@ReportIndicatorsActivity, MonthlyReportsActivity::class.java).apply {
                putExtra(MonthlyReportsActivity.Constants.SELECT_TAB, selectTab)
        })
        finish()
    }
}