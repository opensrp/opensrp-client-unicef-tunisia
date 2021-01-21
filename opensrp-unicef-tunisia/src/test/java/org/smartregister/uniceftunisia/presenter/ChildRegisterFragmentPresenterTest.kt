package org.smartregister.uniceftunisia.presenter

import io.mockk.mockk
import io.mockk.spyk
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.smartregister.child.contract.ChildRegisterFragmentContract
import org.smartregister.uniceftunisia.TestUnicefTunisiaApplication
import org.smartregister.uniceftunisia.util.DBQueryHelper

@RunWith(RobolectricTestRunner::class)
@Config(application = TestUnicefTunisiaApplication::class)
class ChildRegisterFragmentPresenterTest {
    private val view: ChildRegisterFragmentContract.View = mockk()
    private val model: ChildRegisterFragmentContract.Model = spyk()
    private val registerFragmentPresenter = spyk(objToCopy = ChildRegisterFragmentPresenter(view, model, "view-config-id"))

    @Test
    fun `Should return main condition`() {
        assertEquals(
                "(ec_client.dod IS NULL AND ec_client.date_removed is null AND ec_client.is_closed IS NOT '1' AND ec_child_details.is_closed IS NOT '1')",
                registerFragmentPresenter.mainCondition)
    }

    @Test
    fun `Should return the default sort query`() {
        assertEquals(DBQueryHelper.getSortQuery(), registerFragmentPresenter.defaultSortQuery)
    }
}