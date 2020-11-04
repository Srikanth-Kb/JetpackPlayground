package com.playground.jetpackplayground.ui.auth

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.map
import com.playground.jetpackplayground.R
import com.playground.jetpackplayground.models.AuthToken
import com.playground.jetpackplayground.ui.auth.state.AuthStateEvent
import com.playground.jetpackplayground.ui.auth.state.LoginFields
import com.playground.jetpackplayground.util.GenericApiResponse
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.input_email
import kotlinx.android.synthetic.main.fragment_login.input_password
import kotlinx.android.synthetic.main.fragment_register.*

class LoginFragment : BaseAuthFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "onViewCreated: LoginFragment : ${viewModel.hashCode()}")

        subsribeObservers()

        login_button.setOnClickListener{
            login()
        }

    }

    fun subsribeObservers() {
        viewModel.viewState.observe(viewLifecycleOwner, Observer {
            it.login_fields?.let {loginFields ->
                loginFields.login_email?.let {input_email.setText(it)}
                loginFields.login_password?.let {input_password.setText(it)}
            }
        })
    }

    fun login() {
        viewModel.setStateEvent(
            AuthStateEvent.LoginAttemptEvent(
                input_email.text.toString(),
                input_password.text.toString()
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.setLoginFields(
            LoginFields(
                input_email.text.toString(),
                input_password.text.toString()
            )
        )
    }
}