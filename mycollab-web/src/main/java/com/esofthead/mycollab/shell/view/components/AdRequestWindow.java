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
package com.esofthead.mycollab.shell.view.components;

import com.esofthead.mycollab.html.DivLessFormatter;
import com.esofthead.mycollab.module.user.domain.SimpleUser;
import com.esofthead.mycollab.module.user.service.UserService;
import com.esofthead.mycollab.spring.AppContextUtil;
import com.esofthead.mycollab.vaadin.AppContext;
import com.esofthead.mycollab.vaadin.ui.NotificationUtil;
import com.esofthead.mycollab.vaadin.web.ui.UIConstants;
import com.hp.gagawa.java.elements.A;
import com.hp.gagawa.java.elements.Div;
import com.hp.gagawa.java.elements.Text;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

/**
 * @author MyCollab Ltd
 * @since 5.1.3
 */
public class AdRequestWindow extends Window {
    public AdRequestWindow(final SimpleUser user) {
        super("Need help!");
        this.setModal(true);
        this.setResizable(false);
        this.setWidth("600px");

        MVerticalLayout content = new MVerticalLayout();

        Label message = new Label("Hey <b>" + AppContext.getUser().getDisplayName() + "</b>, you've been " +
                "using MyCollab for a while now. And we hope you are happy with it. We spent countless hours and money developing this free " +
                "software for you. If you like it, please write a few words on twitter, blog or our " +
                "testimonial form. It will help other " +
                "people find this useful software quickly. <b> Thank you</b>", ContentMode.HTML);

        MVerticalLayout shareControls = new MVerticalLayout();
        Label rateSourceforge = new Label(new Div().appendChild(new Text(FontAwesome.THUMBS_O_UP.getHtml()), DivLessFormatter.EMPTY_SPACE(), new A("http://sourceforge.net/projects/mycollab/reviews/new", "_blank")
                .appendText("Rate us on Sourceforge")).setStyle("color:#006dac").write(), ContentMode.HTML);

        Label tweetUs = new Label(new Div().appendChild(new Text(FontAwesome.TWITTER.getHtml()), DivLessFormatter.EMPTY_SPACE(),
                new A("https://twitter.com/intent/tweet?text=Im using MyCollab to manage all project activities, accounts and it works great @mycollabdotcom&source=webclient", "_blank")
                        .appendText("Share on Twitter")).setStyle("color:#006dac").write(), ContentMode.HTML);

        Label linkedIn = new Label(new Div().appendChild(new Text(FontAwesome.LINKEDIN_SQUARE.getHtml()), DivLessFormatter.EMPTY_SPACE(),
                new A("https://www.linkedin.com/cws/share?url=https%3A%2F%2Fwww.mycollab.com&original_referer=https%3A%2F%2Fwww.mycollab.com&token=&isFramed=false&lang=en_US", "_blank")
                        .appendText("Share on LinkedIn")).setStyle("color:#006dac").write(), ContentMode.HTML);

        Button testimonialBtn = new Button("Write a testimonial", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                AdRequestWindow.this.close();
                turnOffAdd(user);
                UI.getCurrent().addWindow(new TestimonialWindow());
            }
        });
        testimonialBtn.setStyleName(UIConstants.BUTTON_LINK);
        testimonialBtn.setIcon(FontAwesome.KEYBOARD_O);

        shareControls.with(rateSourceforge, tweetUs, linkedIn, testimonialBtn);

        MHorizontalLayout btnControls = new MHorizontalLayout();
        Button ignoreBtn = new Button("No, thanks", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                close();
                turnOffAdd(user);
            }
        });
        ignoreBtn.addStyleName(UIConstants.BUTTON_OPTION);

        Button loveBtn = new Button("I did", new Button.ClickListener() {
            @Override
            public void buttonClick(Button.ClickEvent clickEvent) {
                AdRequestWindow.this.close();
                NotificationUtil.showNotification("We appreciate your kindness action", "Thank you for your time");
                turnOffAdd(user);
            }
        });
        loveBtn.addStyleName(UIConstants.BUTTON_ACTION);
        loveBtn.setIcon(FontAwesome.HEART);

        btnControls.with(ignoreBtn, loveBtn);

        content.with(message, shareControls, btnControls).withAlign(btnControls, Alignment.MIDDLE_RIGHT);
        this.setContent(content);
    }

    private void turnOffAdd(SimpleUser user) {
        user.setRequestad(false);
        UserService userService = AppContextUtil.getSpringBean(UserService.class);
        userService.updateSelectiveWithSession(user, AppContext.getUsername());
    }
}
