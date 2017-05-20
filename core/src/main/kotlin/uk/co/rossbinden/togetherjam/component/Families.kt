package uk.co.rossbinden.togetherjam.component

import com.badlogic.ashley.core.Family

val CONTROLLABLES = Family.all(ControllableComponent::class.java).get()
val RENDERABLES = Family.all(SpriteComponent::class.java).get()
val MOVABLES = Family.all(PositionComponent::class.java, VelocityComponent::class.java).get()