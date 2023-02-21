package net.gearmaniacs.core.extensions

import android.app.Application
import androidx.lifecycle.AndroidViewModel

inline val AndroidViewModel.app get() = getApplication<Application>()

