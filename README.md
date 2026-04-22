[![Build and Package](https://github.com/MHS-20/HabitQuest/actions/workflows/build.yml/badge.svg)](https://github.com/MHS-20/HabitQuest/actions/workflows/build.yml)
[![Build and Package Multiplatform UI](https://github.com/MHS-20/HabitQuest/actions/workflows/build_ui.yml/badge.svg)](https://github.com/MHS-20/HabitQuest/actions/workflows/build_ui.yml)
[![Semantic Release](https://github.com/MHS-20/HabitQuest/actions/workflows/semantic-release.yml/badge.svg)](https://github.com/MHS-20/HabitQuest/actions/workflows/semantic-release.yml)

# HabitQuest – Gamified Habit Tracker
## Project Overview
HabitQuest is a gamified habit tracker with RPG mechanics. Users can create habits, earn XP, level up, join guilds, do quests, and fight bosses.

Living Documentation:
https://mhs-20.github.io/HabitQuest/

<p align="center">
<img src="Logo.png" alt="Logo" width="300"/>
</p>

## Features
### Habit Management
- Create, edit, and delete habits
- Configure difficulty, frequency, and tags
- Habit reminders
- XP gain for completed habits
- HP loss for missed habits

### Gamification
- Character stats: strength, constitution, intelligence, HP, mana
- Level progression
- Equipment bonuses

### Shop & Inventory
- Buy items and equipment
- Equip items to improve stats
- Use consumables (HP/mana restore)

### Guilds
- Create or join guilds
- Guild roles: Leader, Officer, Member
- Guild chat (real-time)
- Cooperative boss fights

### Quests
- Create/join quests
- Track quest progress
- Earn rewards

### Combat System
- Guild boss fights
- Attacks and magic consume resources
- Damage calculated from stats and equipment

# Deployment
## Option 1: Docker Compose
Run these commands: 
```bash
./gradlew :services:bootJar
docker compose down --remove-orphans
docker compose build
docker compose up -d
./gradlew :habitquest-ui:composeApp:run
```
NOTE: the observability stack is not deployed with docker compose, use minikube for that.

To stop, remove the containers and deleting the images:
```bash
docker compose down
docker rmi $(docker images -q habitquest-*)
```

## Option 2: Minikube
NOTE: this tools needs to be installed on the system:
- kubectl
- minikube
- helm
- kustomize

Just run these commands:
```bash
./gradlew :services:bootJar
./deploy.sh
./gradlew :habitquest-ui:composeApp:run
```

To see some metrics in Grafana, run E2E tests or load tests: 
```python
python -m venv .venv
source .venv/bin/activate
pip install httpx rich

python ./e2e/load_test.py
python ./e2e/e2e_test.py
```

To stop and delete the cluster:
```bash
./delete.sh
```