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
package com.esofthead.mycollab.module.project.view.bug;

import com.esofthead.mycollab.common.i18n.GenericI18Enum;
import com.esofthead.mycollab.configuration.SiteConfiguration;
import com.esofthead.mycollab.core.arguments.ValuedBean;
import com.esofthead.mycollab.core.utils.BeanUtility;
import com.esofthead.mycollab.eventmanager.ApplicationEventListener;
import com.esofthead.mycollab.eventmanager.EventBusFactory;
import com.esofthead.mycollab.module.project.CurrentProjectVariables;
import com.esofthead.mycollab.module.project.ProjectRolePermissionCollections;
import com.esofthead.mycollab.module.project.ProjectTypeConstants;
import com.esofthead.mycollab.module.project.events.BugEvent;
import com.esofthead.mycollab.module.project.i18n.BugI18nEnum;
import com.esofthead.mycollab.module.project.i18n.OptionI18nEnum;
import com.esofthead.mycollab.module.project.i18n.OptionI18nEnum.BugStatus;
import com.esofthead.mycollab.module.project.i18n.ProjectCommonI18nEnum;
import com.esofthead.mycollab.module.project.ui.ProjectAssetsManager;
import com.esofthead.mycollab.module.project.ui.components.*;
import com.esofthead.mycollab.module.project.view.bug.components.LinkIssueWindow;
import com.esofthead.mycollab.module.project.view.bug.components.ToggleBugSummaryField;
import com.esofthead.mycollab.module.project.view.bug.components.ToggleBugSummaryWithDependentField;
import com.esofthead.mycollab.module.tracker.domain.SimpleBug;
import com.esofthead.mycollab.module.tracker.domain.SimpleRelatedBug;
import com.esofthead.mycollab.module.tracker.service.BugRelationService;
import com.esofthead.mycollab.module.tracker.service.BugService;
import com.esofthead.mycollab.spring.AppContextUtil;
import com.esofthead.mycollab.vaadin.AppContext;
import com.esofthead.mycollab.vaadin.events.HasPreviewFormHandlers;
import com.esofthead.mycollab.vaadin.mvp.ViewComponent;
import com.esofthead.mycollab.vaadin.mvp.ViewManager;
import com.esofthead.mycollab.vaadin.ui.ELabel;
import com.esofthead.mycollab.vaadin.ui.VerticalRemoveInlineComponentMarker;
import com.esofthead.mycollab.vaadin.web.ui.AdvancedPreviewBeanForm;
import com.esofthead.mycollab.vaadin.web.ui.ProjectPreviewFormControlsGenerator;
import com.esofthead.mycollab.vaadin.web.ui.ReadViewLayout;
import com.esofthead.mycollab.vaadin.web.ui.UIConstants;
import com.google.common.eventbus.Subscribe;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.themes.ValoTheme;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vaadin.peter.buttongroup.ButtonGroup;
import org.vaadin.viritin.button.MButton;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import java.util.List;

/**
 * @author MyCollab Ltd.
 * @since 1.0
 */
@ViewComponent
public class BugReadViewImpl extends AbstractPreviewItemComp<SimpleBug> implements BugReadView {
    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(BugReadViewImpl.class);

    private ApplicationEventListener<BugEvent.BugChanged> bugChangedHandler = new
            ApplicationEventListener<BugEvent.BugChanged>() {
                @Override
                @Subscribe
                public void handle(BugEvent.BugChanged event) {
                    Integer bugChangeId = (Integer) event.getData();
                    BugService bugService = AppContextUtil.getSpringBean(BugService.class);
                    SimpleBug bugChange = bugService.findById(bugChangeId, AppContext.getAccountId());
                    previewItem(bugChange);
                }
            };

    private TagViewComponent tagViewComponent;
    private CssLayout bugWorkflowControl;
    private ProjectFollowersComp<SimpleBug> bugFollowersList;
    private BugTimeLogSheet bugTimeLogList;
    private DateInfoComp dateInfoComp;
    private PeopleInfoComp peopleInfoComp;
    private ProjectActivityComponent activityComponent;

    public BugReadViewImpl() {
        super(AppContext.getMessage(BugI18nEnum.DETAIL),
                ProjectAssetsManager.getAsset(ProjectTypeConstants.BUG), new BugPreviewFormLayout());
    }

    @Override
    public void attach() {
        EventBusFactory.getInstance().register(bugChangedHandler);
        super.attach();
    }

    @Override
    public void detach() {
        EventBusFactory.getInstance().unregister(bugChangedHandler);
        super.detach();
    }

    private void displayWorkflowControl() {
        if (BugStatus.Open.name().equals(beanItem.getStatus()) || BugStatus.ReOpen.name().equals(beanItem.getStatus())) {
            bugWorkflowControl.removeAllComponents();
            ButtonGroup navButton = new ButtonGroup();

            Button resolveBtn = new Button(AppContext.getMessage(BugI18nEnum.BUTTON_RESOLVED), new Button.ClickListener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void buttonClick(final ClickEvent event) {
                    UI.getCurrent().addWindow(new ResolvedInputWindow(beanItem));
                }
            });
            resolveBtn.addStyleName(UIConstants.BUTTON_ACTION);
            navButton.addButton(resolveBtn);
            bugWorkflowControl.addComponent(navButton);
        } else if (BugStatus.Verified.name().equals(beanItem.getStatus())) {
            bugWorkflowControl.removeAllComponents();
            ButtonGroup navButton = new ButtonGroup();
            Button reopenBtn = new Button(AppContext.getMessage(GenericI18Enum.BUTTON_REOPEN), new Button.ClickListener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void buttonClick(final ClickEvent event) {
                    UI.getCurrent().addWindow(new ReOpenWindow(beanItem));
                }
            });
            reopenBtn.addStyleName(UIConstants.BUTTON_ACTION);
            navButton.addButton(reopenBtn);

            bugWorkflowControl.addComponent(navButton);
        } else if (BugStatus.Resolved.name().equals(beanItem.getStatus())) {
            bugWorkflowControl.removeAllComponents();
            ButtonGroup navButton = new ButtonGroup();
            Button reopenBtn = new Button(AppContext.getMessage(GenericI18Enum.BUTTON_REOPEN), new Button.ClickListener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void buttonClick(final ClickEvent event) {
                    UI.getCurrent().addWindow(new ReOpenWindow(beanItem));
                }
            });
            reopenBtn.addStyleName(UIConstants.BUTTON_ACTION);
            navButton.addButton(reopenBtn);

            Button approveNCloseBtn = new Button(AppContext.getMessage(BugI18nEnum.BUTTON_APPROVE_CLOSE), new Button.ClickListener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void buttonClick(final ClickEvent event) {
                    UI.getCurrent().addWindow(new ApproveInputWindow(beanItem));
                }
            });
            approveNCloseBtn.addStyleName(UIConstants.BUTTON_ACTION);
            navButton.addButton(approveNCloseBtn);
            bugWorkflowControl.addComponent(navButton);
        } else if (BugStatus.Resolved.name().equals(beanItem.getStatus())) {
            bugWorkflowControl.removeAllComponents();
            ButtonGroup navButton = new ButtonGroup();
            Button reopenBtn = new Button(AppContext.getMessage(GenericI18Enum.BUTTON_REOPEN), new Button.ClickListener() {
                private static final long serialVersionUID = 1L;

                @Override
                public void buttonClick(final ClickEvent event) {
                    UI.getCurrent().addWindow(new ReOpenWindow(beanItem));
                }
            });
            reopenBtn.setStyleName(UIConstants.BUTTON_ACTION);
            navButton.addButton(reopenBtn);

            bugWorkflowControl.addComponent(navButton);
        }
        bugWorkflowControl.setEnabled(CurrentProjectVariables.canWrite(ProjectRolePermissionCollections.BUGS));
    }

    @Override
    public SimpleBug getItem() {
        return beanItem;
    }

    @Override
    public void previewItem(SimpleBug item) {
        super.previewItem(item);
        displayWorkflowControl();
        ((BugPreviewFormLayout) previewLayout).displayBugHeader(beanItem);
    }

    @Override
    protected void initRelatedComponents() {
        activityComponent = new ProjectActivityComponent(ProjectTypeConstants.BUG, CurrentProjectVariables.getProjectId());
        dateInfoComp = new DateInfoComp();
        peopleInfoComp = new PeopleInfoComp();
        bugFollowersList = new ProjectFollowersComp<>(ProjectTypeConstants.BUG, ProjectRolePermissionCollections.BUGS);
        bugTimeLogList = ViewManager.getCacheComponent(BugTimeLogSheet.class);
        if (SiteConfiguration.isCommunityEdition()) {
            addToSideBar(dateInfoComp, peopleInfoComp, bugFollowersList);
        } else {
            addToSideBar(dateInfoComp, peopleInfoComp, bugTimeLogList, bugFollowersList);
        }
    }

    @Override
    protected void onPreviewItem() {
        tagViewComponent.display(ProjectTypeConstants.BUG, beanItem.getId());
        activityComponent.loadActivities("" + beanItem.getId());
        bugTimeLogList.displayTime(beanItem);
        bugFollowersList.displayFollowers(beanItem);
        dateInfoComp.displayEntryDateTime(beanItem);
        peopleInfoComp.displayEntryPeople(beanItem);
    }

    @Override
    protected String initFormTitle() {
        return "";
    }

    @Override
    protected String getType() {
        return ProjectTypeConstants.BUG;
    }

    private static class BugPreviewFormLayout extends ReadViewLayout {
        private ToggleBugSummaryField toggleBugSummaryField;

        void displayBugHeader(final SimpleBug bug) {
            MVerticalLayout header = new VerticalRemoveInlineComponentMarker().withFullWidth().withMargin(false);
            toggleBugSummaryField = new ToggleBugSummaryField(bug);
            toggleBugSummaryField.addLabelStyleName(ValoTheme.LABEL_H3);
            toggleBugSummaryField.addLabelStyleName(ValoTheme.LABEL_NO_MARGIN);
            header.with(toggleBugSummaryField);
            this.addHeader(header);

            if (bug.isCompleted()) {
                toggleBugSummaryField.addLabelStyleName(UIConstants.LINK_COMPLETED);
            } else if (bug.isOverdue()) {
                toggleBugSummaryField.addLabelStyleName(UIConstants.LABEL_OVERDUE);
            }

            BugRelationService bugRelationService = AppContextUtil.getSpringBean(BugRelationService.class);
            List<SimpleRelatedBug> relatedBugs = bugRelationService.findRelatedBugs(bug.getId());
            if (CollectionUtils.isNotEmpty(relatedBugs)) {
                for (final SimpleRelatedBug relatedBug : relatedBugs) {
                    if (relatedBug.getRelated()) {
                        ELabel relatedLink = new ELabel(AppContext.getMessage(OptionI18nEnum.BugRelation.class,
                                relatedBug.getRelatedType())).withStyleName(UIConstants.ARROW_BTN).withWidthUndefined();
                        ToggleBugSummaryWithDependentField toggleRelatedBugField = new ToggleBugSummaryWithDependentField(bug, relatedBug.getRelatedBug());
                        MHorizontalLayout bugContainer = new MHorizontalLayout(relatedLink, toggleRelatedBugField)
                                .expand(toggleRelatedBugField).withFullWidth();
                        header.with(bugContainer);
                    } else {
                        Enum relatedEnum = OptionI18nEnum.BugRelation.valueOf(relatedBug.getRelatedType()).getReverse();
                        ELabel relatedLink = new ELabel(AppContext.getMessage(relatedEnum)).withStyleName(UIConstants.ARROW_BTN)
                                .withWidthUndefined();
                        ToggleBugSummaryWithDependentField toggleRelatedBugField = new ToggleBugSummaryWithDependentField(bug, relatedBug.getRelatedBug());
                        MHorizontalLayout bugContainer = new MHorizontalLayout(relatedLink, toggleRelatedBugField)
                                .expand(toggleRelatedBugField).withFullWidth();
                        header.with(bugContainer);
                    }
                }
            }
        }

        @Override
        public void addTitleStyleName(String styleName) {
            toggleBugSummaryField.addLabelStyleName(styleName);
        }

        @Override
        public void removeTitleStyleName(String styleName) {
            toggleBugSummaryField.removeLabelStyleName(styleName);
        }
    }

    @Override
    protected AdvancedPreviewBeanForm<SimpleBug> initPreviewForm() {
        return new BugPreviewForm();
    }

    @Override
    protected ComponentContainer createButtonControls() {
        ProjectPreviewFormControlsGenerator<SimpleBug> bugPreviewFormControls = new ProjectPreviewFormControlsGenerator<>(previewForm);
        MButton linkBtn = new MButton("Dependencies", new Button.ClickListener() {
            @Override
            public void buttonClick(ClickEvent clickEvent) {
                UI.getCurrent().addWindow(new LinkIssueWindow(beanItem));
            }
        });
        linkBtn.setEnabled(CurrentProjectVariables.canWrite(ProjectRolePermissionCollections.BUGS));
        linkBtn.setIcon(FontAwesome.BOLT);
        bugPreviewFormControls.addOptionButton(linkBtn);

        HorizontalLayout topPanel = bugPreviewFormControls.createButtonControls(
                ProjectPreviewFormControlsGenerator.ADD_BTN_PRESENTED
                        | ProjectPreviewFormControlsGenerator.DELETE_BTN_PRESENTED
                        | ProjectPreviewFormControlsGenerator.EDIT_BTN_PRESENTED
                        | ProjectPreviewFormControlsGenerator.PRINT_BTN_PRESENTED
                        | ProjectPreviewFormControlsGenerator.CLONE_BTN_PRESENTED
                        | ProjectPreviewFormControlsGenerator.NAVIGATOR_BTN_PRESENTED,
                ProjectRolePermissionCollections.BUGS);

        Button assignBtn = new Button(AppContext.getMessage(GenericI18Enum.BUTTON_ASSIGN), new Button.ClickListener() {
            private static final long serialVersionUID = 1L;

            @Override
            public void buttonClick(final ClickEvent event) {
                UI.getCurrent().addWindow(new AssignBugWindow(beanItem));
            }
        });
        assignBtn.setEnabled(CurrentProjectVariables.canWrite(ProjectRolePermissionCollections.BUGS));
        assignBtn.setIcon(FontAwesome.SHARE);

        assignBtn.setStyleName(UIConstants.BUTTON_ACTION);

        bugWorkflowControl = new CssLayout();
        bugPreviewFormControls.insertToControlBlock(bugWorkflowControl);
        bugPreviewFormControls.insertToControlBlock(assignBtn);
        topPanel.setSizeUndefined();

        return topPanel;
    }

    protected ComponentContainer createExtraControls() {
        tagViewComponent = new TagViewComponent();
        return tagViewComponent;
    }

    @Override
    protected ComponentContainer createBottomPanel() {
        return activityComponent;
    }

    @Override
    public HasPreviewFormHandlers<SimpleBug> getPreviewFormHandlers() {
        return this.previewForm;
    }


    private static class PeopleInfoComp extends MVerticalLayout {
        private static final long serialVersionUID = 1L;

        private void displayEntryPeople(ValuedBean bean) {
            this.removeAllComponents();
            this.withMargin(false);

            Label peopleInfoHeader = new Label(FontAwesome.USER.getHtml() + " " +
                    AppContext.getMessage(ProjectCommonI18nEnum.SUB_INFO_PEOPLE), ContentMode.HTML);
            peopleInfoHeader.setStyleName("info-hdr");
            this.addComponent(peopleInfoHeader);

            GridLayout layout = new GridLayout(2, 2);
            layout.setSpacing(true);
            layout.setWidth("100%");
            layout.setMargin(new MarginInfo(false, false, false, true));
            try {
                Label createdLbl = new Label(AppContext.getMessage(ProjectCommonI18nEnum.ITEM_CREATED_PEOPLE));
                createdLbl.setSizeUndefined();
                layout.addComponent(createdLbl, 0, 0);

                String createdUserName = (String) PropertyUtils.getProperty(bean, "logby");
                String createdUserAvatarId = (String) PropertyUtils.getProperty(bean, "loguserAvatarId");
                String createdUserDisplayName = (String) PropertyUtils.getProperty(bean, "loguserFullName");

                ProjectMemberLink createdUserLink = new ProjectMemberLink(createdUserName, createdUserAvatarId, createdUserDisplayName);
                layout.addComponent(createdUserLink, 1, 0);
                layout.setColumnExpandRatio(1, 1.0f);

                Label assigneeLbl = new Label(AppContext.getMessage(ProjectCommonI18nEnum.ITEM_ASSIGN_PEOPLE));
                assigneeLbl.setSizeUndefined();
                layout.addComponent(assigneeLbl, 0, 1);
                String assignUserName = (String) PropertyUtils.getProperty(bean, "assignuser");
                String assignUserAvatarId = (String) PropertyUtils.getProperty(bean, "assignUserAvatarId");
                String assignUserDisplayName = (String) PropertyUtils.getProperty(bean, "assignuserFullName");

                ProjectMemberLink assignUserLink = new ProjectMemberLink(assignUserName, assignUserAvatarId, assignUserDisplayName);
                layout.addComponent(assignUserLink, 1, 1);
            } catch (Exception e) {
                LOG.error("Can not build user link {} ", BeanUtility.printBeanObj(bean));
            }

            this.addComponent(layout);

        }
    }
}
