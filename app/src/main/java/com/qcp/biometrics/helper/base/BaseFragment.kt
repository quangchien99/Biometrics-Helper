package com.qcp.biometrics.helper.base

import androidx.fragment.app.Fragment
/**
 * @author chienpham
 * @since 06/05/2024
 */
abstract class BaseFragment : Fragment() {
    abstract fun getName(): String
}