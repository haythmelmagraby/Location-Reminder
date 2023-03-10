package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.closeSoftKeyboard
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.google.android.material.internal.ContextUtils.getActivity
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.not
import org.hamcrest.Matchers
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest :
    AutoCloseKoinTest() {// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: ReminderDataSource
    private lateinit var appContext: Application

    private val binding = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
    @Before
    fun init() {
        stopKoin()//stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(appContext) }
        }
        //declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }


    @Before
    fun registerIdelResource() {
        IdlingRegistry.getInstance().register(binding)
    }

    @After
    fun unRegisterIdelResource() {
        IdlingRegistry.getInstance().unregister(binding)
    }

    @Test
    fun testReminderScreen() {
        val activity = ActivityScenario.launch(RemindersActivity::class.java)
        binding.monitorActivity(activity)

        onView(withId(R.id.addReminderFAB)).perform(click())
        // chech snack bar if user didn't enter title
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withText(R.string.err_enter_title)).inRoot(
            withDecorView(
                Matchers.not(
                    getActivity(
                        appContext
                    )?.window?.decorView
                )
            )
        ).check(matches(isDisplayed()))
        Thread.sleep(1000)

        onView(withId(R.id.reminderTitle)).perform(typeText("Test Title"))
        closeSoftKeyboard()
        Thread.sleep(500)

        // chech snack bar if user didn't enter location
        onView(withId(R.id.saveReminder)).perform(click())
        onView(withText(R.string.err_select_location)).inRoot(
            withDecorView(
                Matchers.not(
                    getActivity(
                        appContext
                    )?.window?.decorView
                )
            )
        ).check(matches(isDisplayed()))
        Thread.sleep(1000)

        onView(withId(R.id.reminderDescription)).perform(typeText("Test Description"))
        closeSoftKeyboard()
        onView(withId(R.id.selectLocation)).perform(click())
        Thread.sleep(3000)
        onView(withId(R.id.mapId)).perform(click())
        onView(withId(R.id.btn_choose_location)).perform(click())
        onView(withId(R.id.saveReminder)).perform(click())
//        Thread.sleep(1000)
        // chech Reminder saved toast message
        onView(withText(R.string.reminder_saved)).inRoot(
            withDecorView(
                Matchers.not(
                    getActivity(
                        appContext
                    )?.window?.decorView
                )
            )
        ).check(matches(isDisplayed()))


    }

}
