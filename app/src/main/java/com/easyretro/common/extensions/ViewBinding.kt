package com.easyretro.common.extensions

import android.view.LayoutInflater
import android.view.View
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

inline fun <T : ViewBinding> ComponentActivity.viewBinding(
    crossinline bindingInflater: (LayoutInflater) -> T
) = lazy(LazyThreadSafetyMode.NONE) {
    bindingInflater.invoke(layoutInflater)
}

class FragmentViewBindingDelegate<T : ViewBinding>(
    val fragment: Fragment,
    val binds: (View) -> T
) : ReadOnlyProperty<Fragment, T> {

    private var _binding: T? = null

    private val viewLifecycleObserver = object : DefaultLifecycleObserver {
        override fun onDestroy(owner: LifecycleOwner) {
            _binding = null
        }
    }

    init {
        fragment.viewLifecycleOwnerLiveData.observe(fragment) { viewLifecycleOwner ->
            viewLifecycleOwner.lifecycle.addObserver(viewLifecycleObserver)
        }
    }

    override fun getValue(thisRef: Fragment, property: KProperty<*>): T {
        val binding = _binding
        if (binding != null) return binding

        if (!fragment.viewLifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.INITIALIZED)) {
            throw IllegalStateException("Should not attempt to get bindings when Fragment views are destroyed.")
        }

        return binds(thisRef.requireView()).also { _binding = it }
    }
}

fun <T : ViewBinding> Fragment.viewBinding(binds: (View) -> T): FragmentViewBindingDelegate<T> {
    return FragmentViewBindingDelegate(this, binds)
}
