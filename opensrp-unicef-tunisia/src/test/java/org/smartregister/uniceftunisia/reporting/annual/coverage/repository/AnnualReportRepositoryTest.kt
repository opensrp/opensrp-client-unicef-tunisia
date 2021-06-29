package org.smartregister.uniceftunisia.reporting.annual.coverage.repository

import io.mockk.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.smartregister.uniceftunisia.TestUnicefTunisiaApplication
import org.smartregister.uniceftunisia.application.UnicefTunisiaApplication
import org.smartregister.uniceftunisia.reporting.ReportsDao
import org.smartregister.uniceftunisia.reporting.annual.coverage.domain.CoverageTarget
import org.smartregister.uniceftunisia.reporting.annual.coverage.domain.CoverageTargetType
import org.smartregister.uniceftunisia.reporting.annual.coverage.domain.VaccineCount

@RunWith(RobolectricTestRunner::class)
@Config(application = TestUnicefTunisiaApplication::class)
class AnnualReportRepositoryTest {

    private val annualReportRepository = spyk(objToCopy = AnnualReportRepository.getInstance(), recordPrivateCalls = true)

    @Before
    fun `Before every test`() {
        mockkObject(ReportsDao)
    }

    @After
    fun `After every test`() {
        unmockkAll()
    }

    @Test
    fun `Should return the right vaccine coverages`() {
        UnicefTunisiaApplication.getInstance().currentActivity = mockk(relaxed = true)
        every { ReportsDao.getCoverageTarget(2020) } returns
                listOf(
                        CoverageTarget(CoverageTargetType.UNDER_ONE_TARGET, 2020, 20),
                        CoverageTarget(CoverageTargetType.ONE_TWO_YEAR_TARGET, 2020, 50)
                )
        val vaccines = setOf("bcg", "hep_b_0", "penta_1", "penta_2", "penta_3", "ipv_1", "ipv_2",
                "opv_1", "pcv_1", "pcv_2", "pcv_3", "mr_1", "mr_2", "dtp_4", "opv_2")
        every { ReportsDao.getTargetVaccineCounts(2020) } returns vaccines.map { VaccineCount(it, 10) }
        val vaccineCoverage = annualReportRepository.getVaccineCoverage(2020)
        assertTrue(vaccineCoverage.isNotEmpty())
        assertEquals(15, vaccineCoverage.size)

        val bcg = vaccineCoverage[0]
        assertEquals( "bcg", bcg.name )
        assertEquals( "50%", bcg.coverage )
        assertEquals( "2020", bcg.year )
        assertEquals( "10", bcg.vaccinated )

        val opv2 = vaccineCoverage[14]
        assertEquals( "opv_2", opv2.name )
        assertEquals( "20%", opv2.coverage )
        assertEquals( "2020", opv2.year )
        assertEquals( "10", opv2.vaccinated )
        UnicefTunisiaApplication.getInstance().currentActivity = null
    }

    @Test
    fun `Should return annual report years`() {
        every { ReportsDao.getDistinctReportMonths() } returns listOf("January 2020", "October 2019", "September 2019", "December 2019", "November 2019")
        val reportYears = annualReportRepository.getReportYears()
        assertEquals(2, reportYears.size)
        assertEquals(reportYears[0], "2019")
        assertEquals(reportYears[1], "2020")
    }
}