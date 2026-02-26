package habitquest.quest.domain.events;

import habitquest.quest.domain.Quest;

public record QuestJoined(Quest quest, String avatarId) implements QuestEvent {}
