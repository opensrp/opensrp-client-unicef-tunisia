package org.smartregister.uniceftunisia.reporting.monthly

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_monthly_reports.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.smartregister.Context
import org.smartregister.reporting.domain.TallyStatus
import org.smartregister.reporting.event.IndicatorTallyEvent
import org.smartregister.reporting.service.IndicatorGeneratorIntentService
import org.smartregister.uniceftunisia.R
import org.smartregister.uniceftunisia.reporting.ReportGroupingModel
import org.smartregister.uniceftunisia.reporting.common.ReportingUtils
import org.smartregister.view.activity.MultiLanguageActivity

class MonthlyReportsActivity : MultiLanguageActivity() {

    object Constants {
        const val SELECT_TAB = "select_tab"
    }

    val monthlyReportsViewModel by viewModels<MonthlyReportsViewModel>
    { ReportingUtils.createFor(MonthlyReportsViewModel()) }

    private lateinit var reportsPagerAdapter: MonthlyReportsPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_monthly_reports)
        reportsPagerAdapter = MonthlyReportsPagerAdapter(this, supportFragmentManager)

        monthlyReportsViewModel.apply {
            draftedMonths.observe(this@MonthlyReportsActivity, {
                reportFragmentTabLayout.getTabAt(1)?.text =
                    getString(R.string.monthly_draft_reports, it.size)
            })
        }

        //Setup UI
        nameInitialsTextView.apply {
            setOnClickListener { onBackPressed() }
            text = getLoggedInUserInitials()
        }
        containerViewPager.apply {
            adapter = reportsPagerAdapter
            currentItem = intent.getIntExtra(Constants.SELECT_TAB, 1)
            addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                    // Do nothing
                }

                override fun onPageSelected(position: Int) {
                    if (position == 0)
                        reportSyncBtn.visibility = View.VISIBLE
                    else
                        reportSyncBtn.visibility = View.GONE
                }

                override fun onPageScrollStateChanged(state: Int) {
                    // Do nothing
                }

            })
        }

        reportFragmentTabLayout.apply {
            setupWithViewPager(containerViewPager)
            tabRippleColor = null
        }
        titleTextView.apply {
            text =
                ReportGroupingModel(this@MonthlyReportsActivity).reportGroupings.first().displayName
        }

        reportSyncBtn.apply {
            setOnClickListener {
                // Call HiA2Intent Service to generate Reporting indicators
                val intent =
                    Intent(this@MonthlyReportsActivity, IndicatorGeneratorIntentService::class.java)
                startService(intent)
                // To notify user immediately (Fix late notification display issue)
                Toast.makeText(
                    this@MonthlyReportsActivity,
                    getString(R.string.generating_daily_tallies_started),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun getLoggedInUserInitials(): String {
        val allSharedPreferences = Context.getInstance().allSharedPreferences()
        return allSharedPreferences.getANMPreferredName(allSharedPreferences.fetchRegisteredANM())
            .split(" ").take(1).map { it.firstOrNull() }.joinToString("")
    }

    override fun onResume() {
        super.onResume()
        fetchData()
    }

    private fun fetchData() {
        monthlyReportsViewModel.apply {
            fetchDraftedMonths()
            fetchUnDraftedMonths()
            fetchAllSentReportMonths()
            fetchAllDailyTalliesDays()
        }
    }

    @Suppress("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onIndicatorTalliesEvent(event: IndicatorTallyEvent) {
        when (event.status) {
            TallyStatus.STARTED -> Toast.makeText(
                this,
                getString(R.string.generating_daily_tallies_started),
                Toast.LENGTH_SHORT
            ).show()
            TallyStatus.INPROGRESS -> Toast.makeText(
                this,
                getString(R.string.generating_daily_tallies_in_progress),
                Toast.LENGTH_SHORT
            ).show()
            TallyStatus.COMPLETE -> {
                fetchData()
                Toast.makeText(
                    this,
                    getString(R.string.generating_daily_tallies_completed),
                    Toast.LENGTH_SHORT
                ).show()
            }
            else -> {
                // Do nothing
            }
        }
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}