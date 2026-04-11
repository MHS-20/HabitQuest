import org.gradle.api.Rule
tasks.addRule(object : Rule {
    override fun getDescription() = "Pattern: <taskName> - Delegates to all subprojects"
    override fun apply(taskName: String) {
        tasks.register(taskName) {
            dependsOn(subprojects.map { "${it.path}:${taskName}" })
        }
    }
})