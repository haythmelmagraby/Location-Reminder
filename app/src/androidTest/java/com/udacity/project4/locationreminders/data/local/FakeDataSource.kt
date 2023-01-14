package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import java.lang.Error

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {
    private var shouldReturnError = false

    private val remindersList = ArrayList<ReminderDTO>()

    fun setReurnError(value:Boolean){
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Test Exception")
        }
        return Result.Success(remindersList)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Result.Error("Test Exception")
        }
        remindersList.forEach{
            if (id == it.id){
                return Result.Success(it)
            }
        }
        return Result.Error("$id is not exist")
    }

    override suspend fun deleteAllReminders() {
        remindersList.clear()
    }


}