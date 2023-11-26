package ru.newlevel.hordemap.presentation.sign_in

import android.animation.ObjectAnimator
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.IntentSender
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import by.kirich1409.viewbindingdelegate.viewBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.app.ResizeAnimation
import ru.newlevel.hordemap.app.TAG
import ru.newlevel.hordemap.databinding.FragmentSingInBinding
import ru.newlevel.hordemap.presentation.DisplayLocationUi
import ru.newlevel.hordemap.presentation.MyResult
import kotlin.math.roundToInt


class SingInFragment : Fragment(R.layout.fragment_sing_in) {

    private val signInViewModel by viewModel<SignInViewModel>()
    private val binding: FragmentSingInBinding by viewBinding()
    private var activityListener: DisplayLocationUi? = null
    private val handler = Handler(Looper.getMainLooper())

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
        val launcher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                lifecycleScope.launch {
                    Log.e(TAG, result.data.toString())
                    val signInResult = signInViewModel.signInFromIntent(
                        intent = result.data ?: return@launch
                    )
                    signInViewModel.onSingInResult(signInResult)
                }
            }
        }
        lifecycleScope.launch {
            signInViewModel.state.collect { state ->
                state.signInError?.let { showLoginFailed(it) }
                if (state.isSingSuccess) {
                    signInViewModel.getSignedInUser()?.userName?.let { updateUiWithUser(it) }
                    signInViewModel.saveUser(signInViewModel.getSignedInUser(), binding.editName.text.toString().trim(), requireContext())
                    hideBtns()
                    handler.postDelayed({
                        activityListener?.displayLocationUI()
                    }, 300)
                } else {
                    setButtonsEnabled()
                }
            }
        }
        binding.editName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (s.toString().trim().length < 3 && binding.loginLinearAnonymous.isVisible && binding.loginLinearAnonymous.rotationX.toInt() == 0) {
                    hideBtns()
                }
            }

            override fun afterTextChanged(s: Editable) {
                binding.editName.setOnEditorActionListener { _, actionId, event ->
                    if (actionId == EditorInfo.IME_ACTION_DONE || (event?.action == KeyEvent.ACTION_DOWN && event.keyCode == KeyEvent.KEYCODE_ENTER)) {
                        val imm =
                            requireContext().applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(binding.editName.windowToken, 0)
                        binding.editName.clearFocus()
                        val inputText = s.toString()
                        if (inputText.length > 2) {
                            showBtns()
                        } else {
                            Toast.makeText(requireContext(), R.string.name_must_be_3, Toast.LENGTH_SHORT).show()
                        }
                    }
                    true
                }
            }
        })

        binding.btnSignInAsAnonymous.setOnClickListener {
            setButtonsNoEnabled()
            lifecycleScope.launch {
                loadingProgressBar.visibility = View.VISIBLE
                val signInResult = signInViewModel.signInAnonymously()
                loadingProgressBar.visibility = View.GONE
                signInViewModel.onSingInResult(signInResult)
            }
        }
        binding.btnSignInGoogle.setOnClickListener {
            setButtonsNoEnabled()
            lifecycleScope.launch {
                loadingProgressBar.visibility = View.VISIBLE
                val singInResult = signInViewModel.signIn()
                loadingProgressBar.visibility = View.GONE
                when (singInResult) {
                    is MyResult.Success -> {
                        launcher.launch(
                            IntentSenderRequest.Builder(
                                singInResult.data as? IntentSender ?: return@launch
                            ).build()
                        )
                    }
                    is MyResult.Error -> {
                        showLoginFailed(singInResult.exception.message)
                        setButtonsEnabled()
                    }
                }
            }
        }
    }

    private fun setButtonsEnabled() {
        binding.cardView.alpha = 1f
        binding.editName.isEnabled = true
        binding.btnSignInAsAnonymous.isEnabled = true
        binding.btnSignInGoogle.isEnabled = true
    }

    private fun setButtonsNoEnabled() {
        binding.cardView.alpha = 0.6f
        binding.editName.isEnabled = false
        binding.btnSignInAsAnonymous.isEnabled = false
        binding.btnSignInGoogle.isEnabled = false
    }

    private fun hideBtns() {
        val cardView = binding.cardView
        val resizeAnimation = ResizeAnimation(cardView, cardView.height - convertDpToPx(126))
        resizeAnimation.duration = 600
        cardView.startAnimation(resizeAnimation)
        val inputLayout = binding.loginLinearAnonymous
        val inputLayout2 = binding.loginLinearGoogle
        inputLayout.rotationX = 0f
        inputLayout2.rotationX = 0f
        val animator = ObjectAnimator.ofFloat(inputLayout, "rotationX", 90f)
        animator.duration = 500
        animator.start()
        val animator2 = ObjectAnimator.ofFloat(inputLayout2, "rotationX", 90f)
        animator2.duration = 500
        animator2.start()
        handler.postDelayed({
            binding.loginLinearAnonymous.visibility = View.GONE
            binding.loginLinearGoogle.visibility = View.GONE
        }, 300)
    }

    private fun showBtns() {
        val cardView = binding.cardView
        val inputLayout = binding.loginLinearAnonymous
        val inputLayout2 = binding.loginLinearGoogle
        inputLayout.rotationX = 90f
        inputLayout2.rotationX = 90f
        val resizeAnimation = ResizeAnimation(cardView, cardView.height + convertDpToPx(126))
        resizeAnimation.duration = 600
        cardView.startAnimation(resizeAnimation)
        handler.postDelayed({
            binding.loginLinearAnonymous.visibility = View.VISIBLE
            binding.loginLinearGoogle.visibility = View.VISIBLE
            val animator = ObjectAnimator.ofFloat(inputLayout, "rotationX", 0f)
            animator.interpolator = AccelerateDecelerateInterpolator()
            animator.duration = 500
            animator.start()
            val animator2 = ObjectAnimator.ofFloat(inputLayout2, "rotationX", 0f)
            animator2.interpolator = AccelerateDecelerateInterpolator()
            animator2.duration = 500
            animator2.start()
        }, 400)
    }

    private fun updateUiWithUser(name: String) {
        val welcome = getString(R.string.hello) + " " + name
        val appContext = context?.applicationContext ?: return
        Toast.makeText(appContext, welcome, Toast.LENGTH_LONG).show()
    }

    private fun showLoginFailed(errorString: String?) {
        Toast.makeText(requireContext(), errorString, Toast.LENGTH_LONG).show()
    }

    private fun convertDpToPx(dp: Int): Int {
        val density: Float = resources.displayMetrics.density
        return (dp.toFloat() * density).roundToInt()
    }
}

