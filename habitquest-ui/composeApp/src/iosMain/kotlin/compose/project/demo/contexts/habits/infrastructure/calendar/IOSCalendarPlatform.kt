package compose.project.demo.contexts.habits.infrastructure.calendar

import be.vandeas.kalendar.kit.CalendarEventManager
import platform.UIKit.UIViewController

object IOSCalendarPlatform {
    val manager: CalendarEventManager = CalendarEventManager()

    fun initializeCalendarEventManager(viewController: UIViewController) {
        manager.setPresentingViewController(viewController)
    }
}
