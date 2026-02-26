package habitquest.quest.domain.events;

import habitquest.quest.domain.Quest;

public record QuestCreated(Quest quest) implements QuestEvent {}
