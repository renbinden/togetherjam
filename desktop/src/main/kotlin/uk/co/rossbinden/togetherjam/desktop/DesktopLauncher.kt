package uk.co.rossbinden.togetherjam.desktop

import com.badlogic.gdx.backends.lwjgl.LwjglApplication
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration
import uk.co.rossbinden.togetherjam.TogetherJam


fun main(args: Array<String>) {
    val config = LwjglApplicationConfiguration()
//    val graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
//    config.width = graphicsDevice.displayMode.width
//    config.height = graphicsDevice.displayMode.height
//    config.fullscreen = true
    config.width = 800
    config.height = 600
    LwjglApplication(TogetherJam(), config)
}
