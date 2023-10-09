package ru.newlevel.hordemap.presentatin

import android.graphics.Color
import android.os.Bundle
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.FirebaseApp
import ru.newlevel.hordemap.R
import ru.newlevel.hordemap.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        FirebaseApp.initializeApp(applicationContext);

        windowSettings()

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory(this))[LoginViewModel::class.java]


        //TODO удалить ресет (тест первого запуска)
        loginViewModel.reset()
        loginViewModel.checkLogin()

        loginViewModel.loginResult.observe(this) {
            if (it.name.isNotEmpty()) {
                Toast.makeText(this,"Привет ${it.name}", Toast.LENGTH_LONG).show()
                val mapFragment = MapFragment(it)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, mapFragment)
                    .commit()
            } else {
                val loginFragment = LoginFragment(loginViewModel)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, loginFragment)
                    .commit()
            }
        }
    }

    private fun windowSettings() {
        window.statusBarColor = Color.TRANSPARENT // Прозрачный цвет строки состояния
        supportActionBar?.hide()                  // Скрыть акшн бар
    }

}