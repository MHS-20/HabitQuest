package habitquest.quest.domain.events;

import habitquest.quest.domain.Quest;

public record QuestCompleted(Quest quest, String avatarId) implements QuestEvent {}
