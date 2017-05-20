package uk.co.rossbinden.togetherjam.component

import com.badlogic.ashley.core.Component
import com.badlogic.gdx.audio.Sound

class FootstepSoundsComponent(
        val sounds: com.badlogic.gdx.utils.Array<Sound>
): Component {
    var dt = 0f
}