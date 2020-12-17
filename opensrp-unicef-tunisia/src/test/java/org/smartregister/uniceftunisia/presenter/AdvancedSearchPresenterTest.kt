package org.smartregister.uniceftunisia.presenter

import android.database.MatrixCursor
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.unmockkAll
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.smartregister.child.contract.ChildAdvancedSearchContract
import org.smartregister.child.cursor.AdvancedMatrixCursor
import org.smartregister.uniceftunisia.TestUnicefTunisiaApplication
import org.smartregister.uniceftunisia.util.AppConstants.KEY.*

@RunWith(RobolectricTestRunner::class)
@Config(application = TestUnicefTunisiaApplication::class)
class AdvancedSearchPresenterTest {

    private val view: ChildAdvancedSearchContract.View = mockk()
    private val advancedSearchPresenter = spyk(objToCopy = AdvancedSearchPresenter(view, "view-config-id"))
    private val query = "some awesome query"

    @Before
    fun `Before every test`() {
        every { view.filterAndSortQuery() } returns query
    }

    @After
    fun `After every test`() {
        unmockkAll()
    }

    @Test
    fun `Should return remote cursor`() {
        val columnNames = arrayOf(
                ID_LOWER_CASE, RELATIONALID, RELATIONAL_ID, FATHER_BASE_ENTITY_ID, FIRST_NAME,
                LAST_NAME, GENDER, DOB, ZEIR_ID, MOTHER_FIRST_NAME, MOTHER_LAST_NAME, INACTIVE, LOST_TO_FOLLOW_UP)

        val localCursor = MatrixCursor(columnNames)
        //Populate localCursor
        localCursor.addRow(arrayOf("1", null, "relational-id1", "fatherbaseentity1", "Judith", "Powell", "Female", "2020-01-21", "1001", "Mama", "Mtoto", null, null))
        every { view.getRawCustomQueryForAdapter(query) } returns localCursor

        val remoteCursor = AdvancedMatrixCursor(columnNames)
        remoteCursor.addRow(arrayOf("2", null, "relational-id2", "fatherbaseentity2", "Janet", "Mary", "Female", "2019-11-25", "1003", "Mary", "Antone", null, null))
        remoteCursor.addRow(arrayOf("3", null, "relational-i3", "fatherbaseentity3", "Jared", "Ockland", "Male", "2018-06-08", "1002", "Faith", "Ruby", null, null))

        val remoteLocalMatrixCursor = advancedSearchPresenter.getRemoteLocalMatrixCursor(remoteCursor)
        assertNotNull(remoteLocalMatrixCursor)
        assertTrue(remoteLocalMatrixCursor.count > 0)
    }

    @Test
    fun `Should return the main condition`() {
        assertEquals("(ec_client.dod IS NULL AND ec_client.date_removed is null AND ec_client.is_closed IS NOT '1' AND ec_child_details.is_closed IS NOT '1')",
                advancedSearchPresenter.mainCondition)
    }
}