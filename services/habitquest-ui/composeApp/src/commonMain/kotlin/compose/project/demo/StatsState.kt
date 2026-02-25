package compose.project.demo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class StatsState(
    initialHp: Int = 80,
    val maxHp: Int = 100,
    initialMana: Int = 40,
    val maxMana: Int = 100,
    initialXp: Int = 230,
    initialNextLevelXp: Int = 500,
    initialLevel: Int = 1,
    initialClass: String = "Warrior"
) {
    var hp by mutableStateOf(initialHp)
    var mana by mutableStateOf(initialMana)
    var xp by mutableStateOf(initialXp)
    var nextLevelXp by mutableStateOf(initialNextLevelXp)

    // New: character level and class
    var level by mutableStateOf(initialLevel)
    var charClass by mutableStateOf(initialClass)

    fun takeDamage(amount: Int) { hp = (hp - amount).coerceAtLeast(0) }
    fun heal(amount: Int) { hp = (hp + amount).coerceAtMost(maxHp) }

    fun useMana(amount: Int) { mana = (mana - amount).coerceAtLeast(0) }
    fun restoreMana(amount: Int) { mana = (mana + amount).coerceAtMost(maxMana) }

    fun gainXp(amount: Int) {
        xp += amount
        while (xp >= nextLevelXp) {
            xp -= nextLevelXp
            // increase threshold and level
            nextLevelXp = (nextLevelXp * 1.2).toInt()
            level += 1
            // on level up restore some hp/mana
            hp = (hp + 20).coerceAtMost(maxHp)
            mana = (mana + 20).coerceAtMost(maxMana)
        }
    }

    fun resetXp() {
        xp = 0
        nextLevelXp = 500
    }

    fun setClass(newClass: String) { charClass = newClass }
}
