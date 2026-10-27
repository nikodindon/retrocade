package com.nikodindon.retrocade.games.asteroids

import com.nikodindon.retrocade.games.api.Game
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AsteroidsModule {
    @Provides
    @Singleton
    @IntoSet
    fun provideAsteroidsGame(game: AsteroidsGame): Game = game
}
