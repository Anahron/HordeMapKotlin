package ru.newlevel.hordemap.presentation.sign_in

import android.app.Activity.RESULT_OK
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import com.google.android.gms.auth.api.identity.Identity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.TAG
import ru.newlevel.hordemap.databinding.FragmentSingInBinding
import ru.newlevel.hordemap.presentation.DisplayLocationUi

class SingInFragment : Fragment(R.layout.fragment_sing_in) {

    private val signInViewModel by viewModel<SignInViewModel>()
    private val binding: FragmentSingInBinding by viewBinding()
    private var activityListener: DisplayLocationUi? = null
    private val googleAuthUiClient by lazy {
        GoogleAuthUiClient(
            context = requireContext(),
            oneTapClient = Identity.getSignInClient(requireContext())
        )
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is DisplayLocationUi) {
            activityListener = context
        } else {
            throw RuntimeException("$context must implement DisplayLocationUi.displayLocationUI")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loadingProgressBar = binding.loading
        if (googleAuthUiClient.getSignedInUser() != null)
            activityListener?.displayLocationUI()


        val launcher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                lifecycleScope.launch {
                    Log.e(TAG, result.data.toString())
                    val signInResult = googleAuthUiClient.signInFromIntent(
                        intent = result.data ?: return@launch
                    )
                    Log.e(TAG, signInResult.toString())
                    signInViewModel.onSingInResult(signInResult)
                }
            }
        }
        lifecycleScope.launch {
            signInViewModel.state.collect { state ->
                state.signInError?.let { showLoginFailed(it) }
                if (state.isSingSuccess) {
                    googleAuthUiClient.getSignedInUser()?.userName?.let { updateUiWithUser(it) }
                    signInViewModel.saveUser(googleAuthUiClient.getSignedInUser())
                    activityListener?.displayLocationUI()
                    signInViewModel.resetState()
                }
            }
        }

        binding.btnSignInGoogle.setOnClickListener {
            lifecycleScope.launch {
                val singInIntent = googleAuthUiClient.signIn()
                launcher.launch(
                    IntentSenderRequest.Builder(
                        singInIntent ?: return@launch
                    ).build()
                )
            }
        }

//        loginViewModel.loginFormState.observe(viewLifecycleOwner,
//            Observer { loginFormState ->
//                if (loginFormState == null) {
//                    return@Observer
//                }
//                loginButton.isEnabled = loginFormState.isDataValid
//                loginFormState.usernameError?.let {
//                    usernameEditText.error = getString(it)
//                }
//                loginFormState.passwordError?.let {
//                    passwordEditText.error = getString(it)
//                }
//            })
//            .observe(viewLifecycleOwner,
//                Observer { loginResult ->
//                    loginResult ?: return@Observer
//                    loadingProgressBar.visibility = View.GONE
//                    loginResult.error?.let {
//                        showLoginFailed(it)
//                    }
//                    loginResult.success?.let {
//                        updateUiWithUser(it)
//                    }
//                })

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
                // ignore
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // ignore
            }

            override fun afterTextChanged(s: Editable) {
//                loginViewModel.loginDataChanged(
//                    usernameEditText.text.toString(),
//                    passwordEditText.text.toString()
//                )
            }
        }
//       usernameEditText.addTextChangedListener(afterTextChangedListener)
//        passwordEditText.addTextChangedListener(afterTextChangedListener)
//        passwordEditText.setOnEditorActionListener { _, actionId, _ ->
//            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                loginViewModel.login(
//                    usernameEditText.text.toString(),
//                    passwordEditText.text.toString()
//                )
//            }
//            false
//        }

//        loginButton.setOnClickListener {
//            loadingProgressBar.visibility = View.VISIBLE
//            loginViewModel.login(
//                usernameEditText.text.toString(),
//                passwordEditText.text.toString()
//            )
//        }
    }

    private fun updateUiWithUser(name: String) {
        val welcome = getString(R.string.hello) + " " +name
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()
    }

    private fun showLoginFailed(errorString: String?) {
        Toast.makeText(requireContext(), errorString, Toast.LENGTH_LONG).show()
    }
}

