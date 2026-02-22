package habitquest.avatar_service.domain;

import common.ddd.Entity;

public class Level implements Entity<String> {
    private final Integer levelNumber;
    private final Experience experienceRequired;

    public Level(Integer levelNumber, Experience experienceRequired) {
        this.levelNumber = levelNumber;
        this.experienceRequired = experienceRequired;
    }

    public boolean canLevelUp(Experience currentExperience) {
        return currentExperience.isAtLeast(experienceRequired);
    }

    public Level levelUp(Experience nextLevelRequirement) {
        return new Level(levelNumber + 1, nextLevelRequirement);
    }

    @Override
    public String getId() {
        return "";
    }

    public Integer getLevelNumber() {
        return levelNumber;
    }

    public Experience getExperienceRequired() {
        return experienceRequired;
    }
}
