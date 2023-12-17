package ru.newlevel.hordemap.presentation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import kotlinx.coroutines.delay
import ru.newlevel.hordemap.R

class FragmentManager(private val fragmentManager: FragmentManager) {
    private val delayToRemoveFragment = 150L
    fun addAndShowFragment(fragment: Fragment): Fragment {
        fragmentManager.beginTransaction().setCustomAnimations(
            R.anim.slide_in_bottom,
            R.anim.slide_out_bottom,
            R.anim.slide_in_bottom,
            R.anim.slide_out_bottom,
        ).add(R.id.container, fragment).addToBackStack(fragment.id.toString()).show(fragment).commit()
        return fragment
    }

    suspend fun removeFragment(fragment: Fragment) {
        fragmentManager.clearBackStack(fragment.id.toString())
        fragmentManager.beginTransaction().setCustomAnimations(
            R.anim.slide_in_bottom,
            R.anim.slide_out_bottom,
            R.anim.slide_in_bottom,
            R.anim.slide_out_bottom,
        ).hide(fragment).commit()
        delay(delayToRemoveFragment)
        fragmentManager.beginTransaction().remove(fragment).commit()
    }

    fun replaceFragment(fragment: Fragment): Fragment {
        fragmentManager.beginTransaction().setCustomAnimations(
            R.anim.slide_in_bottom,
            R.anim.slide_out_bottom,
            R.anim.slide_in_bottom,
            R.anim.slide_out_bottom,
        ).replace(R.id.container, fragment).addToBackStack(null).commit()
        return fragment
    }
}