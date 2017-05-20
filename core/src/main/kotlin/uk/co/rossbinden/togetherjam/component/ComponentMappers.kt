package uk.co.rossbinden.togetherjam.component

import com.badlogic.ashley.core.ComponentMapper

val BODY: ComponentMapper<BodyComponent> = ComponentMapper.getFor(BodyComponent::class.java)
val CONTROLLABLE: ComponentMapper<ControllableComponent> = ComponentMapper.getFor(ControllableComponent::class.java)
val FOOTSTEP_SOUNDS: ComponentMapper<FootstepSoundsComponent> = ComponentMapper.getFor(FootstepSoundsComponent::class.java)
val POSITION: ComponentMapper<PositionComponent> = ComponentMapper.getFor(PositionComponent::class.java)
val SPRITE: ComponentMapper<SpriteComponent> = ComponentMapper.getFor(SpriteComponent::class.java)
val VELOCITY: ComponentMapper<VelocityComponent> = ComponentMapper.getFor(VelocityComponent::class.java)