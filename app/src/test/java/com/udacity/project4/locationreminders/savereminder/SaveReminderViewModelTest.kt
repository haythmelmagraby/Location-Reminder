package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class SaveReminderViewModelTest {


    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @Before
    fun initViewModel() {
            fakeDataSource = FakeDataSource()
        saveReminderViewModel =
                SaveReminderViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
        stopKoin()
        modules()
    }
    private fun modules() {
        val myModules = module {
            single {
                saveReminderViewModel
            }
        }
        startKoin { modules(listOf(myModules)) }
    }
    @Test
    fun testSaveReminder(){
        GlobalScope.launch(Dispatchers.Main) {
            val reminderOne = ReminderDataItem(
            "test title",
            "test Descreption",
            "test location",
            10.0,
            10.0
        )
            val reminderTow = ReminderDataItem(
                "test title",
                "test Descreption",
                "test location",
                10.0,
                10.0
            )
            saveReminderViewModel.saveReminder(reminderOne)
            saveReminderViewModel.saveReminder(reminderTow)
            val reminders = fakeDataSource.getReminders() as Result.Success
            assertEquals(reminders.data.size,2)
        }
    }




}