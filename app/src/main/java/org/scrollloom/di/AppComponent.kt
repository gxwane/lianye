package org.scrollloom.di

import org.scrollloom.domain.repository.LoomRepository

class AppComponent {
    val loomRepository: LoomRepository by lazy { LoomRepository() }
}
