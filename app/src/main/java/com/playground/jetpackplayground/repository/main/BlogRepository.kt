package com.playground.jetpackplayground.repository.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.playground.jetpackplayground.api.main.OpenApiMainService
import com.playground.jetpackplayground.api.main.responses.BlogListSearchResponse
import com.playground.jetpackplayground.models.AuthToken
import com.playground.jetpackplayground.models.BlogPost
import com.playground.jetpackplayground.persistence.AuthTokenDao
import com.playground.jetpackplayground.persistence.BlogPostDao
import com.playground.jetpackplayground.repository.JobManager
import com.playground.jetpackplayground.repository.NetworkBoundResource
import com.playground.jetpackplayground.session.SessionManager
import com.playground.jetpackplayground.ui.DataState
import com.playground.jetpackplayground.ui.main.blog.state.BlogViewState
import com.playground.jetpackplayground.util.Constants.Companion.PAGINATION_PAGE_SIZE
import com.playground.jetpackplayground.util.DateUtils
import com.playground.jetpackplayground.util.GenericApiResponse
import com.playground.jetpackplayground.util.GenericApiResponse.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Exception
import javax.inject.Inject

class BlogRepository
@Inject
constructor(
    val openApiMainService: OpenApiMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
): JobManager("BlogRepository") {

    private val TAG: String = "AppDebug"

    fun searchBlogPosts(
        authToken: AuthToken,
        query: String,
        page: Int
    ): LiveData<DataState<BlogViewState>> {
        return object : NetworkBoundResource<BlogListSearchResponse, List<BlogPost>, BlogViewState> (
            sessionManager.isConnectedToTheInternet(),
            true,
            false,
            true
        ) {
            override suspend fun createCacheRequestAndReturn() {
                withContext(Main) {
                    // finish by viewing the db cache
                    result.addSource(loadFromCache()) {viewState ->
                        viewState.blogFields.isQueryInProgress = false
                        if (page * PAGINATION_PAGE_SIZE > viewState.blogFields.blogList.size)
                        onCompleteJob(DataState.data(viewState, null))
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(
                response: ApiSuccessResponse<BlogListSearchResponse>
            ) {
                val blogPostList: ArrayList<BlogPost> = ArrayList()
                for (blogPostResponse in response.body.results) {
                    blogPostList.add(
                        BlogPost(
                            pk = blogPostResponse.pk,
                            title = blogPostResponse.title,
                            slug = blogPostResponse.slug,
                            body = blogPostResponse.body,
                            image = blogPostResponse.image,
                            date_updated = DateUtils.convertServerStringDateToLong(
                                blogPostResponse.date_updated
                            ),
                            username = blogPostResponse.username
                        )
                    )
                }
                updateLocalDb(blogPostList)
                createCacheRequestAndReturn()
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogListSearchResponse>> {
                return openApiMainService.searchListBlogPosts(
                    "Token ${authToken.token!!}",
                    query = query,
                    page = page
                )
            }

            override fun loadFromCache(): LiveData<BlogViewState> {
                return blogPostDao.getAllBlogPost(
                    query = query,
                    page = page
                )
                    .switchMap {
                        object : LiveData<BlogViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = BlogViewState(
                                    BlogViewState.BlogFields(
                                        blogList = it,
                                        isQueryInProgress = true
                                    )
                                )
                            }
                        }
                    }
            }

            override suspend fun updateLocalDb(cacheObject: List<BlogPost>?) {
                if (cacheObject != null) {
                    withContext(IO) {
                        for (blogPost in cacheObject) {
                            try {
                                // launch each insert as a seperate job to be executed in parallel
                                launch {
                                    Log.d(TAG, "updateLocalDb: inserting blog: $blogPost")
                                    blogPostDao.insert(blogPost)
                                }
                            }catch (e: Exception) {
                                Log.e(TAG, "updateLocalDb: error updating cache" + "on blog post with slug: ${blogPost.slug}")
                                // Optional error handling
                            }
                        }
                    }
                }
                else{
                    Log.d(TAG, "updateLocalDb: blog post list is null")
                }
            }

            override fun setJob(job: Job) {
                addJob("searchBlogPosts", job)
            }

        }.asLiveData()
    }
}