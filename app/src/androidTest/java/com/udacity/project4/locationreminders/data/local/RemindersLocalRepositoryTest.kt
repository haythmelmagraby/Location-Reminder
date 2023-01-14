package com.udacity.project4.locationreminders.data.local

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import androidx.test.platform.app.InstrumentationRegistry
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var reminderDB: RemindersDatabase
    private lateinit var reminderLocalRepository: RemindersLocalRepository
    @Before
    fun createDBAndRepo() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        reminderDB = Room.inMemoryDatabaseBuilder(
            context,
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        reminderLocalRepository = RemindersLocalRepository(reminderDB.reminderDao())
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

            val getReminders = reminderLocalRepository.getReminders() as Result.Success
            assertEquals(getReminders.data[0], reminder)
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

            val getReminder = reminderLocalRepository.getReminder("23") as Result.Success
            assertEquals(getReminder.data, reminder)
        }
    }

    @Test
    fun testGetReminderByIDIfNull(){
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

            val getReminder = reminderLocalRepository.getReminder("600") as Result.Error
            assertEquals(getReminder.message, "Reminder not found!")
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
            reminderLocalRepository.deleteAllReminders()
            val getReminders = reminderLocalRepository.getReminders() as Result.Success
            assertEquals(getReminders.data.isEmpty(), true)
        }
    }


}