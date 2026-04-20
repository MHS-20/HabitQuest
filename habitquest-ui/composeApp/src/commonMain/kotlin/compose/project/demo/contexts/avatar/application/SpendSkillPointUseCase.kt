package compose.project.demo.contexts.avatar.application

import compose.project.demo.contexts.avatar.domain.contract.AvatarGateway
import compose.project.demo.contexts.avatar.domain.model.AvatarStatIncrementResult
import compose.project.demo.contexts.avatar.domain.model.AvatarStatType

class SpendSkillPointUseCase(
    private val avatarGateway: AvatarGateway,
) {
    suspend operator fun invoke(
        token: String,
        avatarId: String,
        statType: AvatarStatType,
    ): AvatarStatIncrementResult =
        when (statType) {
            AvatarStatType.STRENGTH -> avatarGateway.increaseStrength(token, avatarId)
            AvatarStatType.DEFENSE -> avatarGateway.increaseDefense(token, avatarId)
            AvatarStatType.INTELLIGENCE -> avatarGateway.increaseIntelligence(token, avatarId)
        }
}
