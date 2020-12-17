package org.smartregister.uniceftunisia.presenter

import io.mockk.*
import net.sqlcipher.MatrixCursor
import net.sqlcipher.database.SQLiteDatabase
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.smartregister.child.contract.ChildRegisterContract
import org.smartregister.repository.Repository
import org.smartregister.uniceftunisia.TestUnicefTunisiaApplication
import org.smartregister.uniceftunisia.dao.AppChildDao

@RunWith(RobolectricTestRunner::class)
@Config(application = TestUnicefTunisiaApplication::class)
class AppChildRegisterPresenterTest {
    private val view: ChildRegisterContract.View = mockk()
    private val model: ChildRegisterContract.Model = spyk()
    private val registerFragmentPresenter = spyk(objToCopy = AppChildRegisterPresenter(view, model))
    private val repository: Repository = mockk(relaxed = true)
    private val database: SQLiteDatabase = mockk(relaxed = true)

    @Before
    fun `Before every test`() {
        AppChildDao.updateRepository(repository)
        every { repository.readableDatabase } returns database
    }

    @After
    fun `After every test`() {
        unmockkAll()
    }

    @Test
    fun `Should update child's card status`() {
        val openSRPId = "opensrpid"
        val baseEntityId = "sample-base-entity-id"

        val cursor = MatrixCursor(arrayOf("base_entity_id"))
        cursor.addRow(arrayOf(baseEntityId))
        every { database.rawQuery(any(), any()) } returns cursor
        registerFragmentPresenter.updateChildCardStatus(openSRPId)
        verify { database.rawQuery(any(), any()) }
    }

}