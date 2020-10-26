package com.example.booksworld.main.activities

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.example.booksworld.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
    class BookReaderTest{
       @Rule
       var activityTest = ActivityTestRule(BookReader::class.java)

    @Test
    fun CreateNewNote(){
        var nextButton = onView(withId(R.id.nextbutton))
        nextButton.perform(click())
    }
}