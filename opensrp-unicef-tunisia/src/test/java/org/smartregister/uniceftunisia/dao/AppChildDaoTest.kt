package org.smartregister.uniceftunisia.dao

import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import net.sqlcipher.MatrixCursor
import net.sqlcipher.database.SQLiteDatabase
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.smartregister.repository.Repository

class AppChildDaoTest {

    private val repository: Repository = mockk(relaxed = true)
    private val database: SQLiteDatabase = mockk(relaxed = true)
    private val baseEntityId = "sample-base-entity-id"

    @Before
    fun `Before every test`() {
        AppChildDao.updateRepository(repository)
        every { repository.readableDatabase } returns database
    }

    @After
    fun `After every test`() {
        unmockkAll()
    }

    private fun mockMatrixCursor(columnArray: Array<String>, vararg rowData: Array<Any?>): MatrixCursor {
        val matrixCursor = MatrixCursor(columnArray)
        rowData.forEach {
            matrixCursor.addRow(it)
        }
        return matrixCursor
    }

    @Test
    fun `Should return true if baby is premature`() {
        val matrixCursor = mockMatrixCursor(
                columnArray = arrayOf("count"),
                rowData = arrayOf(arrayOf("1"))
        )

        every { database.rawQuery(any(), any()) } returns matrixCursor
        val prematureBaby = AppChildDao.isPrematureBaby(baseEntityId)
        assertTrue(prematureBaby)
    }

    @Test
    fun `Should return false if baby is premature`() {
        val matrixCursor = mockMatrixCursor(
                columnArray = arrayOf("count"),
                rowData = arrayOf(arrayOf("0"))
        )
        every { database.rawQuery(any(), any()) } returns matrixCursor
        val prematureBaby = AppChildDao.isPrematureBaby(baseEntityId)
        assertFalse(prematureBaby)

        every { database.rawQuery(any(), any()) } returns null
        val nullResponse = AppChildDao.isPrematureBaby(baseEntityId)
        assertFalse(nullResponse)
    }

    @Test
    fun `Should return true for client who needs card`() {
        val matrixCursor = mockMatrixCursor(
                columnArray = arrayOf("count"),
                rowData = arrayOf(arrayOf("1"))
        )

        every { database.rawQuery(any(), any()) } returns matrixCursor
        val needsCard = AppChildDao.clientNeedsCard(baseEntityId)
        assertTrue(needsCard)
    }

    @Test
    fun `Should return list of children above 5 years otherwise emptylist if none is found`() {
        val matrixCursor = mockMatrixCursor(
                columnArray = arrayOf("base_entity_id"),
                rowData = arrayOf(
                        arrayOf("sample-id-1"),
                        arrayOf("sample-id-2"),
                        arrayOf("sample-id-3")
                )
        )

        every { database.rawQuery(any(), any()) } returns matrixCursor
        val childrenAboveFive = AppChildDao.getChildrenAboveFiveYears()
        assertEquals(3, childrenAboveFive.size)
        assertEquals("sample-id-1", childrenAboveFive[0])
        assertEquals("sample-id-2", childrenAboveFive[1])
        assertEquals("sample-id-3", childrenAboveFive[2])

        every { database.rawQuery(any(), any()) } returns null
        val noChildrenList = AppChildDao.getChildrenAboveFiveYears()
        assertTrue(noChildrenList.isEmpty())
    }

    @Test
    fun `Should return base entity id from OpenSRP ID otherwise returns null`() {
        val matrixCursor = mockMatrixCursor(
                columnArray = arrayOf("base_entity_id"),
                rowData = arrayOf(arrayOf("sample-id-1"), )
        )
        val openSRPId = "19020"
        every { database.rawQuery(any(), any()) } returns matrixCursor
        val baseEntityId = AppChildDao.getBaseEntityIdByOpenSRPId(openSRPId)
        assertEquals("sample-id-1", baseEntityId)

        every { database.rawQuery(any(), any()) } returns null
        val nullEntityId = AppChildDao.getBaseEntityIdByOpenSRPId(openSRPId)
        assertNull(nullEntityId)
    }
}