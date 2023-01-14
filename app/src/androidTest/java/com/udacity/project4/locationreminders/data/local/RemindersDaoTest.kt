package com.udacity.project4.locationreminders.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import androidx.test.platform.app.InstrumentationRegistry
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import org.junit.runner.RunWith;
import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var reminderDB: RemindersDatabase

    @Before
    fun createDB() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        reminderDB = Room.inMemoryDatabaseBuilder(
            context,
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @After
    fun closeDB(){
        reminderDB.close()
    }

    @Test
    fun testInsertWithGetReminders(){
        GlobalScope.launch(Dispatchers.Main) {
            val reminder = ReminderDTO(
                "test title",
                "test Descreption",
                "test location",
                10.0,
                10.0
            )
            reminderDB.reminderDao().saveReminder(
                reminder
            )

            val getReminders = reminderDB.reminderDao().getReminders()
            assertEquals(getReminders[0],reminder)
        }
    }

    @Test
    fun testGetReminderByID(){
        GlobalScope.launch(Dispatchers.Main) {
            val reminder = ReminderDTO(
                "test title",
                "test Descreption",
                "test location",
                10.0,
                10.0,
                "23"
            )
            reminderDB.reminderDao().saveReminder(
                reminder
            )

            val getReminder = reminderDB.reminderDao().getReminderById("23")
            assertEquals(getReminder,reminder)
        }
    }

    @Test
    fun testDeleteAllReminders(){
        GlobalScope.launch(Dispatchers.Main) {
            val reminder = ReminderDTO(
                "test title",
                "test Descreption",
                "test location",
                10.0,
                10.0
            )
            reminderDB.reminderDao().saveReminder(
                reminder
            )
            reminderDB.reminderDao().saveReminder(
                reminder
            )
                reminderDB.reminderDao().deleteAllReminders()
            val getReminders = reminderDB.reminderDao().getReminders()
            assertEquals(getReminders.isEmpty(),true)
        }
    }

}