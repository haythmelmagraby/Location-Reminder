package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class RemindersListViewModelTest {

    private lateinit var remiderListViewModel: RemindersListViewModel
    private lateinit var fakeDataSource: FakeDataSource


    @ExperimentalCoroutinesApi
    val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()


    @ExperimentalCoroutinesApi
    @Before
    fun setupDispatcher() {
        Dispatchers.setMain(testDispatcher)
    }

    @ExperimentalCoroutinesApi
    @After
    fun tearDownDispatcher() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initViewModel() {
        fakeDataSource = FakeDataSource()
        remiderListViewModel =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
        stopKoin()
        modules()
    }

    private fun modules() {
        val myModules = module {
            single {
                remiderListViewModel
            }
        }
        startKoin { modules(listOf(myModules)) }
    }

    @After
    fun stopTheKoin() {
        stopKoin()
    }

    @Test
    fun checkLoadReminders() {
        GlobalScope.launch(Dispatchers.IO) {
            val reminderOne = ReminderDTO(
                "test title",
                "test Descreption",
                "test location",
                10.0,
                10.0
            )
            val reminderTow = ReminderDTO(
                "test title",
                "test Descreption",
                "test location",
                10.0,
                10.0
            )

            fakeDataSource.saveReminder(reminderOne)
            fakeDataSource.saveReminder(reminderTow)
            remiderListViewModel.loadReminders()

            val remiderCount = remiderListViewModel.remindersList.value?.size
            assertEquals(remiderCount, 2)
        }
    }

    @Test
    fun checkShouurnError() {
        fakeDataSource.setReurnError(true)
        remiderListViewModel.loadReminders()
        assertEquals(remiderListViewModel.error.value, true)
        assertEquals(remiderListViewModel.showSnackBar.value, "Test Exception")

    }

    @Test
    fun checkLoding() {
        testDispatcher.pauseDispatcher()
        remiderListViewModel.loadReminders()
        assertEquals(remiderListViewModel.showLoading.value, true)
        testDispatcher.resumeDispatcher()
        assertEquals(remiderListViewModel.showLoading.value, false)
    }
}


