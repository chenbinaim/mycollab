/**
 * This file is part of mycollab-web.
 *
 * mycollab-web is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * mycollab-web is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with mycollab-web.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.esofthead.mycollab.module.project.view.reports

import com.esofthead.mycollab.eventmanager.EventBusFactory
import com.esofthead.mycollab.module.project.events.ReportEvent
import com.esofthead.mycollab.module.project.view.ProjectUrlResolver

/**
  * @author MyCollab Ltd
  * @since 5.3.0
  */
class TimesheetUrlResolver extends ProjectUrlResolver {

  override protected def handlePage(params: String*): Unit = {
    EventBusFactory.getInstance().post(new ReportEvent.GotoTimesheetReport(this))
  }
}
