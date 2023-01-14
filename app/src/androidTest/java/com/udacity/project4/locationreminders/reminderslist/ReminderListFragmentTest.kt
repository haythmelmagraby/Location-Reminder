package com.udacity.project4.locationreminders.reminderslist


import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.util.DataBindingIdlingResource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {


    private lateinit var reminderDataSource: FakeDataSource
    private lateinit var remindersListViewModel: RemindersListViewModel
    private var binding = DataBindingIdlingResource()

    @Before
    fun createDBAndRepo(){
        reminderDataSource = FakeDataSource()
        remindersListViewModel = RemindersListViewModel(getApplicationContext(),reminderDataSource)
        stopKoin()
        modules()
    }

    private fun modules() {
        val myModules = module {
            single {
                remindersListViewModel
            }
        }
        startKoin { modules(listOf(myModules)) }
    }
    @Test
    fun testFragmentNavigation(){
        GlobalScope.launch(Dispatchers.IO) {

            val fragment = launchFragmentInContainer<ReminderListFragment>(Bundle.EMPTY)
            val navController = mock(NavController::class.java)

            fragment.onFragment {
                Navigation.setViewNavController(it.view!!, navController)
            }
            onView(withId(R.id.addReminderFAB)).perform(click())
            verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
        }
    }

    @Test
    fun testDataDisplay(){
        GlobalScope.launch(Dispatchers.IO) {
            val reminder = ReminderDTO(
                "test title",
                "test Descreption",
                "test location",
                10.0,
                10.0
            )
            reminderDataSource.saveReminder(
                reminder
            )
            val fragment = launchFragmentInContainer<ReminderListFragment>(Bundle.EMPTY)
            onView(withId(R.id.noDataTextView)).check(matches(CoreMatchers.not(isDisplayed())))
            onView(withText(reminder.title)).check(matches(isDisplayed()))
            onView(withText(reminder.description)).check(matches(isDisplayed()))
            onView(withText(reminder.location)).check(matches(isDisplayed()))

        }
    }

}