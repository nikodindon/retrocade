package com.nikodindon.retrocade.core.billing

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope

@Module
@InstallIn(SingletonComponent::class)
abstract class BillingBindingsModule {
    @Binds
    @Singleton
    abstract fun bindBillingGateway(gateway: PlayBillingGateway): BillingGateway

    @Binds
    @Singleton
    abstract fun bindBillingRepository(repository: PlayBillingRepository): BillingRepository
}

@Module
@InstallIn(SingletonComponent::class)
object BillingScopeModule {
    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope =
        CoroutineScope(SupervisorJob() + Dispatchers.IO)
}
