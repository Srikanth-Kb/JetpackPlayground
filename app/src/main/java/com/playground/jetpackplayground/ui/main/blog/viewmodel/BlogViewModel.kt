package com.playground.jetpackplayground.ui.main.blog.viewmodel

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.bumptech.glide.RequestManager
import com.playground.jetpackplayground.models.BlogPost
import com.playground.jetpackplayground.repository.main.BlogRepository
import com.playground.jetpackplayground.session.SessionManager
import com.playground.jetpackplayground.ui.BaseViewModel
import com.playground.jetpackplayground.ui.DataState
import com.playground.jetpackplayground.ui.Loading
import com.playground.jetpackplayground.ui.main.blog.state.BlogStateEvent
import com.playground.jetpackplayground.ui.main.blog.state.BlogStateEvent.*
import com.playground.jetpackplayground.ui.main.blog.state.BlogViewState
import com.playground.jetpackplayground.util.AbsentLiveData
import javax.inject.Inject

class BlogViewModel
@Inject
constructor(
    private val sessionManager: SessionManager,
    private val blogRepository: BlogRepository,
    private val sharedPreferences: SharedPreferences,
    private val requestManager: RequestManager
): BaseViewModel<BlogStateEvent, BlogViewState> () {

    override fun handleStateEvent(stateEvent: BlogStateEvent): LiveData<DataState<BlogViewState>> {
        when(stateEvent) {
            is BlogSearchEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    blogRepository.searchBlogPosts(
                        authToken,
                        getSearchQuery(),
                        getPage()
                    )
                }?: AbsentLiveData.create()
            }
            is CheckAuthorOfBlogPost -> {
                return AbsentLiveData.create()
            }
            is None -> {
                return object : LiveData<DataState<BlogViewState>>() {
                    override fun onActive() {
                        super.onActive()
                        value = DataState(
                            null,
                            Loading(false),
                            null
                        )
                    }
                }
            }
        }
    }

    override fun initNewViewState(): BlogViewState {
        return BlogViewState()
    }

    fun cancelActiveJobs() {
        blogRepository.cancelActiveJobs()
        handlePendingData()
    }

    private fun handlePendingData() {
        setStateEvent(None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }
}