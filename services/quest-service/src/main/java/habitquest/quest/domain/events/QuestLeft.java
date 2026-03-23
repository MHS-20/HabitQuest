package habitquest.quest.domain.events;

import common.ddd.Id;
import habitquest.quest.domain.Avatar;
import habitquest.quest.domain.Quest;

public record QuestLeft(Quest quest, Id<Avatar> avatarId) implements QuestEvent {}
