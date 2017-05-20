package uk.co.rossbinden.togetherjam

import com.badlogic.gdx.Game
import uk.co.rossbinden.togetherjam.screen.MainScreen

class TogetherJam : Game() {

    lateinit var mainScreen: MainScreen

    override fun create() {
        mainScreen = MainScreen()
        setScreen(mainScreen)
    }

    override fun dispose() {
        mainScreen.dispose()
    }

}
