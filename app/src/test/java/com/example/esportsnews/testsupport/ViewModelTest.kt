package com.example.esportsnews.testsupport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before

@OptIn(ExperimentalCoroutinesApi::class)
open class ViewModelTest {
    protected val dispatcher = StandardTestDispatcher()
    private val store = ViewModelStore()
    @Before fun installMain() { Dispatchers.setMain(dispatcher) }
    @After fun clear() { store.clear(); dispatcher.scheduler.runCurrent(); Dispatchers.resetMain() }
    protected fun <T : ViewModel> own(model: T): T {
        store.put(model.javaClass.name, model)
        return model
    }
}
