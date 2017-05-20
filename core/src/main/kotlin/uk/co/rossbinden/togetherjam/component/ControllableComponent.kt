package uk.co.rossbinden.togetherjam.component

import com.badlogic.ashley.core.Component
import com.badlogic.ashley.core.Entity

class ControllableComponent(
        val keyPressMapping: Map<Int, (E: Entity) -> Unit>,
        val keyReleaseMapping: Map<Int, (E: Entity) -> Unit>
): Component