package habitquest.quest.domain.events;

import habitquest.quest.domain.Quest;

public record QuestLeft(Quest quest, String avatarId) implements QuestEvent {}
