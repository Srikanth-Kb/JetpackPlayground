package com.playground.jetpackplayground.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import com.google.android.material.appbar.AppBarLayout
import com.playground.jetpackplayground.R
import com.playground.jetpackplayground.ui.BaseActivity
import com.playground.jetpackplayground.ui.auth.state.AuthStateEvent
import com.playground.jetpackplayground.ui.main.MainActivity
import com.playground.jetpackplayground.viewmodels.ViewModelProviderFactory
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

class AuthActivity : BaseActivity(),
        NavController.OnDestinationChangedListener
{
    @Inject
    lateinit var providerFactory: ViewModelProviderFactory
    lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        viewModel = ViewModelProvider(this, providerFactory).get(AuthViewModel::class.java)
        findNavController(R.id.auth_nav_host_fragment).addOnDestinationChangedListener(this)
        subscribeObservers()
        checkPreviousAuthUser()
    }

    override fun expandAppbar() {
        // Do nothing
    }

    fun subscribeObservers() {

        viewModel.dataState.observe(this, Observer { dataState ->
            // handle progress bar, error dialogs, success dialogs
            onDataStateChange(dataState)

            dataState.data?.let { data ->
                data.data?.let { event ->
                    event.getContentIfNotHandled()?.let {
                        it.authToken?.let {
                            Log.d(TAG, "AuthActivity, Datastate : ${it} ")
                            viewModel.setAuthToken(it)
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(this, Observer {
            it.authToken?.let {
                sessionManager.login(it)
            }
        })
        sessionManager.cachedToken.observe(this, Observer {authToken->
            Log.d(TAG, "AuthActivity: subscribeObservers: AuthToken : ${authToken}")

            if (authToken != null && authToken.account_pk != -1 && authToken.token != null) {
                navMainActivity()
                finish()
            }
        })
    }

    fun checkPreviousAuthUser() {
        viewModel.setStateEvent(AuthStateEvent.CheckPreviousAuthEvent())
    }
    private fun navMainActivity() {
        Log.d(TAG, "navMainActivity: Calling main activity")
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        viewModel.cancelActiveJobs()
    }

    override fun displayProgressBar(boolean: Boolean) {
        if(boolean) {
            progress_bar.visibility = View.VISIBLE
        } else {
            progress_bar.visibility = View.GONE
        }
    }
}