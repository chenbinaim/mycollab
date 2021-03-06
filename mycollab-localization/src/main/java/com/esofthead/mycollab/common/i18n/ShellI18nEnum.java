/**
 * This file is part of mycollab-localization.
 *
 * mycollab-localization is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * mycollab-localization is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with mycollab-localization.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.esofthead.mycollab.common.i18n;

import ch.qos.cal10n.BaseName;
import ch.qos.cal10n.Locale;
import ch.qos.cal10n.LocaleData;

@BaseName("localization/common-shell")
@LocaleData({@Locale("en-US"), @Locale("ja-JP")})
public enum ShellI18nEnum {
    BUTTON_IGNORE_RESET_PASSWORD,
    BUTTON_RESET_PASSWORD,
    BUTTON_LOG_IN,
    BUTTON_FORGOT_PASSWORD,
    BUTTON_BACK_TO_HOME_PAGE,

    ERROR_NO_SUB_DOMAIN,

    OPT_FORGOT_PASSWORD_VIEW_TITLE,
    OPT_EMAIL_SENDER_NOTIFICATION,
    OPT_REMEMBER_PASSWORD,

    FORM_EMAIL,
    FORM_PASSWORD,
    FORM_PASSWORD_HELP,

    WINDOW_STMP_NOT_SETUP,
    WINDOW_SMTP_CONFIRM_SETUP_FOR_ADMIN,
    WINDOW_SMTP_CONFIRM_SETUP_FOR_USER
}
